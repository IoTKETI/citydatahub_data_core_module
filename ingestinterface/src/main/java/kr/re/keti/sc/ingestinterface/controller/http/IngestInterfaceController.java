package kr.re.keti.sc.ingestinterface.controller.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.ingestinterface.common.bulk.IBulkProcessor;
import kr.re.keti.sc.ingestinterface.common.code.Constants;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.ErrorCode;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.Operation;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.OperationOption;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.UseYn;
import kr.re.keti.sc.ingestinterface.common.exception.BadRequestException;
import kr.re.keti.sc.ingestinterface.common.exception.ErrorPayload;
import kr.re.keti.sc.ingestinterface.common.service.security.AASSVC;
import kr.re.keti.sc.ingestinterface.common.vo.BatchEntityErrorVO;
import kr.re.keti.sc.ingestinterface.common.vo.BatchOperationResultVO;
import kr.re.keti.sc.ingestinterface.common.vo.EntityProcessVO;
import kr.re.keti.sc.ingestinterface.common.vo.IngestInterfaceVO;
import kr.re.keti.sc.ingestinterface.common.vo.RequestMessageVO;
import kr.re.keti.sc.ingestinterface.common.vo.entities.DynamicEntityFullVO;
import kr.re.keti.sc.ingestinterface.externalplatformauthentication.service.ExternalPlatformAuthenticationSVC;
import kr.re.keti.sc.ingestinterface.util.ErrorUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements Ngsi-ld entityOperations API controller class
 */
@RestController
@Slf4j
public class IngestInterfaceController {

    @Autowired
    @Qualifier("ingestBulkProcessor")
    private IBulkProcessor<RequestMessageVO> bulkProcessor;

    @Autowired
    private ExternalPlatformAuthenticationSVC externalPlatformAuthenticationSVC;
    @Autowired
    private AASSVC aasSVC;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${datacore.http.binding.response.log.yn:N}")
    private String isResponseLog;
    

    /**
     * Batch 리소스 upsert (Batch Entity Creation or Update (Upsert))
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param requestBody upsert entity operation data
     * @param options http reqeust options header
     * @throws Exception upsert error
     */
    @PostMapping("/entityOperations/upsert")
    public void batchEntityUpsert(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @RequestBody String requestBody,
                                  @RequestParam(value = "options", required = false) String options) throws Exception {

        log.info("EntityOperations upsert Reqeust options={}, body={}", options, requestBody);

        IngestInterfaceVO ingestInterfaceVO = objectMapper.readValue(requestBody, IngestInterfaceVO.class);

        String datasetId = ingestInterfaceVO.getDatasetId();
        List<String> entities = ingestInterfaceVO.getEntities();

        if (datasetId == null) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "datasetId is null");
        }
        if (entities == null || entities.size() == 0) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "entities is null");
        }

        // 2. entity 생성을 위한 객체 생성
        Operation operation;
        List<RequestMessageVO> requestMessageVOList = null;
        if (options == null || options.equals(OperationOption.REPLACE.getCode())) {
            operation = Operation.CREATE_ENTITY_OR_REPLACE_ENTITY_ATTRIBUTES;
        } else if (options.equals(OperationOption.UPDATE.getCode())) {
            operation = Operation.CREATE_ENTITY_OR_APPEND_ENTITY_ATTRIBUTES;
        } else {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "invalid options : " + options);
        }

        requestMessageVOList = makeRequestMessageVO(request, entities, operation, request.getRequestURI(), null, datasetId);

        // 3. 리소스 생성 요청
        List<EntityProcessVO<DynamicEntityFullVO>> processResultVO = bulkProcess(requestMessageVOList, operation);

        // 4. 배치 오퍼레이션 결과 처리
        BatchOperationResultVO batchOperationResultVO = processBatchOperationResult(processResultVO);

        // 5. 설정에 property 조건에 따라 response body를 로그로 남김
        if (UseYn.YES.getCode().equalsIgnoreCase(isResponseLog)) {
            log.info("response body : " + objectMapper.writeValueAsString(batchOperationResultVO));
        }
        // 6. 처리 결과 전달
        commonBatchOpertionResponse(response, batchOperationResultVO);
    }


    /**
     * Batch 리소스 delete (Batch Entity delete)
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param requestBody delete entity operation data
     * @throws Exception delete error
     */
    @PostMapping("/entityOperations/delete")
    public void batchEntityDelete(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @RequestBody String requestBody) throws Exception {

        log.info("EntityOperations delete Reqeust body={}", requestBody);

        IngestInterfaceVO ingestInterfaceVO = objectMapper.readValue(requestBody, IngestInterfaceVO.class);

        // 요청 파라미터 확인
        String datasetId = ingestInterfaceVO.getDatasetId();
        List<String> entities = ingestInterfaceVO.getEntities();

        if (datasetId == null) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "datasetId is null");
        }
        if (entities == null || entities.size() == 0) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "entities is null");
        }

        // 1. entity 생성을 위한 객체 생성
        Operation operation = Operation.DELETE_ENTITY;
        List<RequestMessageVO> requestMessageVOList = makeRequestMessageVO(request ,entities, operation, request.getRequestURI(), null, datasetId);

        // 2. 리소스 생성 요청
        List<EntityProcessVO<DynamicEntityFullVO>> processResultVO = bulkProcess(requestMessageVOList, operation);

        // 3. 배치 오퍼레이션 결과 처리
        BatchOperationResultVO batchOperationResultVO = processBatchOperationResult(processResultVO);

        // 4. 설정에 property 조건에 따라 response body를 로그로 남김
        if (UseYn.YES.getCode().equalsIgnoreCase(isResponseLog)) {
            log.info("response body : " + objectMapper.writeValueAsString(batchOperationResultVO));
        }

        // 5. 처리 결과 전달
        commonBatchOpertionResponse(response, batchOperationResultVO);

    }


    /**
     * 배치 오퍼레이션 결과 후처리
     * @param entityProcessVOs
     * @return
     */
    private BatchOperationResultVO processBatchOperationResult(List<EntityProcessVO<DynamicEntityFullVO>> entityProcessVOs) throws JsonProcessingException {

        BatchOperationResultVO batchOperationResultVO = new BatchOperationResultVO();
        List<String> entityIds = new ArrayList<>();
        List<BatchEntityErrorVO> errors = new ArrayList<>();


        for (EntityProcessVO<DynamicEntityFullVO> entityProcessVO : entityProcessVOs) {
            String entityId = null;
            if (entityProcessVO.getEntityFullVO() != null) {
                entityId = entityProcessVO.getEntityFullVO().getId();
            } else {
            	DynamicEntityFullVO dynamicEntityFullVO = objectMapper.readValue(entityProcessVO.getContent(), DynamicEntityFullVO.class);
                entityId = dynamicEntityFullVO.getId();
            }

            if (entityProcessVO.getProcessResultVO().isProcessResult()) {
                entityIds.add(entityId);
            } else {

                BatchEntityErrorVO batchEntityErrorVO = new BatchEntityErrorVO();
                Exception exception = entityProcessVO.getProcessResultVO().getException();
                ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(exception);
                batchEntityErrorVO.setEntityId(entityId);
                batchEntityErrorVO.setError(errorPayload);
                errors.add(batchEntityErrorVO);
            }
        }
        
        if(entityIds != null && entityIds.size() > 0) {
        	batchOperationResultVO.setSuccess(entityIds);
        }
        if(errors != null && errors.size() > 0) {
        	batchOperationResultVO.setErrors(errors);
        }

        return batchOperationResultVO;
    }

    /**
     * HTTP Binding (batch) operation 요청
     * @param requestMessageVOList 요청메시지VO리스트
     * @param operation entity 처리 operation
     * @return
     */
    private List<EntityProcessVO<DynamicEntityFullVO>> bulkProcess(List<RequestMessageVO> requestMessageVOList, Operation operation) {

        if (log.isDebugEnabled()) {
            log.debug("requestMessageVOList='{}'", requestMessageVOList.toString());
        }
        List<EntityProcessVO<DynamicEntityFullVO>> entityProcessVOList = (List<EntityProcessVO<DynamicEntityFullVO>>) bulkProcessor.processBulk(requestMessageVOList, operation);
        return entityProcessVOList;
    }

    /**
     * batch operation 요청 메시지 생성
     * @param request HttpServletRequest
     * @param entities request entity data
     * @param operation reqeust operation
     * @param requestUri request uri
     * @param operationOptions request options
     * @param datasetId dataset id
     * @return entity operation processing VO
     * @throws JsonProcessingException json parse error
     */
    private List<RequestMessageVO> makeRequestMessageVO(HttpServletRequest request,
                                                        List<String> entities,
                                                        Operation operation,
                                                        String requestUri,
                                                        List<OperationOption> operationOptions,
                                                        String datasetId) throws JsonProcessingException {

        List<RequestMessageVO> requestMessageVOList = new ArrayList<>();
        Date ingestTime = new Date();
        
        for (String entity : entities) {

            RequestMessageVO requestMessageVO = new RequestMessageVO();
            requestMessageVO.setOperation(operation);
            requestMessageVO.setContent(entity);
            requestMessageVO.setTo(requestUri);
            requestMessageVO.setOperationOptions(operationOptions);
            aasSVC.checkCUDAccessRule(request, datasetId, operation);

            requestMessageVO.setDatasetId(datasetId);
            requestMessageVO.setIngestTime(ingestTime);

            requestMessageVOList.add(requestMessageVO);

        }

        return requestMessageVOList;
    }

    /**
     * batch operation 결과 공통 처리
     * @param response HttpServletResponse
     * @param batchOperationResultVO batch operation result vo
     * @throws IOException send response error, json parsing error
     */
    private void commonBatchOpertionResponse(HttpServletResponse response, BatchOperationResultVO batchOperationResultVO) throws IOException {

        // 처리 결과 전달
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(Constants.CHARSET_ENCODING);
        response.getWriter().print(objectMapper.writeValueAsString(batchOperationResultVO));

    }

}
