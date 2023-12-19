package kr.re.keti.sc.dataservicebroker.entities.controller.http;


import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.re.keti.sc.dataservicebroker.common.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.dataservicebroker.common.bulk.IBulkProcessor;
import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.BigDataStorageType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.DefaultAttributeKey;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.EntityAttributeResultType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.Operation;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.OperationOption;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.UseYn;
import kr.re.keti.sc.dataservicebroker.common.exception.ErrorPayload;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdBadRequestException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdResourceNotFoundException;
import kr.re.keti.sc.dataservicebroker.common.service.security.AASSVC;
import kr.re.keti.sc.dataservicebroker.common.vo.entities.DynamicEntityDaoVO;
import kr.re.keti.sc.dataservicebroker.common.vo.entities.DynamicEntityFullVO;
import kr.re.keti.sc.dataservicebroker.datamodel.DataModelManager;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.Attribute;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelCacheVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelVO;
import kr.re.keti.sc.dataservicebroker.entities.service.EntityDataModelSVC;
import kr.re.keti.sc.dataservicebroker.entities.service.EntityRetrieveSVC;
import kr.re.keti.sc.dataservicebroker.entities.vo.AvailableAttribute;
import kr.re.keti.sc.dataservicebroker.entities.vo.AvailableAttributeDetail;
import kr.re.keti.sc.dataservicebroker.entities.vo.AvailableAttributeInformation;
import kr.re.keti.sc.dataservicebroker.entities.vo.AvailableEntityType;
import kr.re.keti.sc.dataservicebroker.entities.vo.AvailableEntityTypeDetail;
import kr.re.keti.sc.dataservicebroker.entities.vo.AvailableEntityTypeInformation;
import kr.re.keti.sc.dataservicebroker.entities.vo.EntityDataModelVO;
import kr.re.keti.sc.dataservicebroker.entities.vo.EntityRetrieveVO;
import kr.re.keti.sc.dataservicebroker.util.ErrorUtil;
import kr.re.keti.sc.dataservicebroker.util.HttpHeadersUtil;
import kr.re.keti.sc.dataservicebroker.util.LogExecutionTime;
import kr.re.keti.sc.dataservicebroker.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class EntityController {

    @Autowired
    @Qualifier("entityBulkProcessor")
    private IBulkProcessor<IngestMessageVO> bulkProcessor;
    @Autowired
    protected EntityDataModelSVC entityDataModelSVC;
    @Autowired
    protected EntityRetrieveSVC entityRetrieveSVC;
    @Autowired
    private AASSVC aasSVC;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DataModelManager dataModelManager;

    @Value("${datacore.http.binding.response.log.yn:N}")
    private String isResponseLog;
    @Value("${entity.retrieve.default.limit:1000}")
    private Integer defaultLimit;
    @Value("${security.acl.useYn:N}")
    private String securityAclUseYn;
    @Value("${entity.default.storage:rdb}")
    protected BigDataStorageType defaultStorageType;
    @Value("${entity.retrieve.primary.accept:application/json}")
    private String primaryAccept;
    @Value("${entity.default.context-uri}")
    private String defaultContextUri;

    /**
     * 최종 값 건수 조회
     *
     * @param response
     * @param accept
     * @param queryVO  사용자 요청 파라미터
     * @throws Exception
     */
    @LogExecutionTime
    @GetMapping(value = "/entitycount")
    public @ResponseBody
    void getEntityCount(HttpServletRequest request,
                        HttpServletResponse response,
                        @RequestHeader(value = HttpHeaders.ACCEPT) String accept,
                        @RequestHeader(value = HttpHeaders.LINK, required = false) String link,
                        @ModelAttribute QueryVO queryVO) throws Exception {

        log.info("entitiesCount request msg='{}'", queryVO.toString());

        // 1. 접근권한 체크
        if (securityAclUseYn.equals(DataServiceBrokerCode.UseYn.YES.getCode())) {
            aasSVC.checkRetriveAccessRule(request, queryVO);
        }

        List<String> links = HttpHeadersUtil.extractLinkUris(link);
        queryVO.setLinks(getLinkOrDefault(links));

        accept = HttpHeadersUtil.getPrimaryAccept(accept);

        // 2. entity Count 조회
        Integer totalCount = entityRetrieveSVC.getEntityCount(queryVO, request.getQueryString(), link);

        EntityCountVO entityCountVO = new EntityCountVO();
        entityCountVO.setTotalCount(totalCount);
        entityCountVO.setType(queryVO.getType());

        // 3. 설정에 property 조건에 따라 response body를 로그로 남김
        String jsonResult = objectMapper.writeValueAsString(entityCountVO);
        log.info("response body : " + jsonResult);

        response.getWriter().print(jsonResult);
    }

    /**
     * 최종 값 조회
     *
     * @param response
     * @param accept
     * @param link
     * @param queryVO  사용자 요청 파라미터
     * @throws Exception
     */
    @LogExecutionTime
    @GetMapping(value = "/entities")
    public @ResponseBody
    void getEntity(HttpServletRequest request,
                   HttpServletResponse response,
                   @RequestHeader(value = HttpHeaders.ACCEPT) String accept,
                   @RequestHeader(value = HttpHeaders.LINK, required = false) String link,
                   @ModelAttribute QueryVO queryVO) throws Exception {

        StringBuilder requestParams = new StringBuilder();
        requestParams.append("accept=").append(accept)
                .append(", link=").append(link)
                .append(", params(queryVO)=").append(queryVO.toString());

        log.info("request msg='{}'", requestParams);

        // 1. 접근권한 체크
        if (securityAclUseYn.equals(DataServiceBrokerCode.UseYn.YES.getCode())) {
            aasSVC.checkRetriveAccessRule(request, queryVO);
        }

        List<String> links = HttpHeadersUtil.extractLinkUris(link);
        queryVO.setLinks(getLinkOrDefault(links));

        accept = HttpHeadersUtil.getPrimaryAccept(accept);

        // 2. 리소스 조회
        EntityRetrieveVO entityRetrieveVO = entityRetrieveSVC.getEntity(queryVO, request.getQueryString(), accept, link);

        String jsonResult = objectMapper.writeValueAsString(entityRetrieveVO.getEntities());

        // 3. 설정에 property 조건에 따라 response body를 로그로 남김
        if (DataServiceBrokerCode.UseYn.YES.getCode().equalsIgnoreCase(isResponseLog)) {
            log.info("response body : " + jsonResult);
        }

        // 4. 응답
        BigDataStorageType dataStorageType = getDataStorageType(queryVO.getDataStorageType());
        HttpHeadersUtil.addPaginationLinkHeader(dataStorageType, request, response, accept, queryVO.getLimit(), queryVO.getOffset(), entityRetrieveVO.getTotalCount(), defaultLimit);
        HttpHeadersUtil.addContextLinkHeader(response, accept, getLinkHeaderByAccept(accept, links));
        response.getWriter().print(jsonResult);
    }


    /**
     * 최종 리소스 조회 by ID
     *
     * @param queryVO 사용자 요청 파라미터
     * @param id      요청 조회 ID
     * @return
     */
    @LogExecutionTime
    @GetMapping("/entities/{id:.+}")
    public @ResponseBody
    void getEntityById(HttpServletRequest request,
                       HttpServletResponse response,
                       @RequestHeader(HttpHeaders.ACCEPT) String accept,
                       @RequestHeader(value = HttpHeaders.LINK, required = false) String link,
                       @PathVariable String id,
                       @ModelAttribute QueryVO queryVO) throws Exception {

        StringBuilder requestParams = new StringBuilder();
        requestParams.append("accept=").append(accept).append(", params(queryVO)=").append(queryVO.toString());

        log.info("getEntityById request msg='{}'", requestParams);

        // 1. 접근권한 체크
        if (securityAclUseYn.equals(DataServiceBrokerCode.UseYn.YES.getCode())) {
            aasSVC.checkRetriveAccessRule(request, queryVO);
        }

        List<String> links = HttpHeadersUtil.extractLinkUris(link);
        queryVO.setLinks(getLinkOrDefault(links));

        accept = HttpHeadersUtil.getPrimaryAccept(accept);

        // 2. 리소스 조회
        CommonEntityVO object = entityRetrieveSVC.getEntityById(queryVO, request.getQueryString(), accept, link);

        String jsonResult = objectMapper.writeValueAsString(object);

        // 3. 설정에 property 조건에 따라 response body를 로그로 남김
        if (DataServiceBrokerCode.UseYn.YES.getCode().equalsIgnoreCase(isResponseLog)) {
            log.info("response body : " + jsonResult);
        }

        // 4. 응답
        HttpHeadersUtil.addContextLinkHeader(response, accept, getLinkHeaderByAccept(accept, links));
        response.getWriter().print(jsonResult);
    }


    /**
     * 리소스 생성 (Entity creation)
     *
     * @param response
     * @param requestBody 사용자 요청 body
     */
    @PostMapping("/entities")
    public void create( HttpServletRequest request,
                        HttpServletResponse response,
                        @RequestHeader(value = HttpHeaders.LINK, required = false) String link,
                        @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) String contentType,
                        @RequestBody String requestBody) throws Exception {

        log.info("Create Entity Reqeust link={}, contentType={}, body={}",link, contentType, requestBody);

        List<String> links = HttpHeadersUtil.extractLinkUris(link);

        // 1. entity 생성을 위한 객체 생성
        List<IngestMessageVO> requestMessageVOList = makeRequestMessageVO(request, requestBody,
                Operation.CREATE_ENTITY, request.getRequestURI(), null, links, contentType);

        // 2. 리소스 생성 요청
        ProcessResultVO processResultVO = process(requestMessageVOList);

        // 3. 요청 결과 확인
        if (processResultVO.isProcessResult()) {
            response.setStatus(HttpStatus.CREATED.value());

        } else {
            // 리소스 생성간 에러가 있는 경우, 예외 발생
            log.error("HTTP Binding ERROR : " + processResultVO.getErrorDescription());
            throw processResultVO.getException();
        }
    }

    /**
     * 리소스 appendEntityAttributes Update  업데이트 (append entity Attributes)
     *
     * @param response
     * @param id          사용자 요청 id
     * @param requestBody 사용자 요청 body
     */
    @RequestMapping(value = "/entities/{id:.+}/attrs", method = {RequestMethod.POST})
    public void appendEntityAttributes( HttpServletRequest request,
                                        HttpServletResponse response,
                                        @PathVariable String id,
                                        @RequestBody String requestBody,
                                        @RequestHeader(value = HttpHeaders.LINK, required = false) String link,
                                        @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) String contentType,
                                        @RequestParam(value = "options", required = false) String options) throws Exception {

        log.info("AppendEntityAttributes Reqeust id={}, link={}, contentType={}, options={}, body={}",
                id, link, contentType, options, requestBody);

        List<IngestMessageVO> requestMessageVOList = null;

        List<String> links = HttpHeadersUtil.extractLinkUris(link);

        // 1. entity 업데이트를 위한 객체 생성
        if (options != null && options.equals(OperationOption.NO_OVERWRITE.getCode())) {
            // "noOverwrite". Indicates that no attribute overwrite shall be performed.
            requestMessageVOList = makeRequestMessageVO(request, requestBody, Operation.APPEND_ENTITY_ATTRIBUTES, request.getRequestURI(), id
                    , Arrays.asList(OperationOption.NO_OVERWRITE), links, contentType);
        } else {
            requestMessageVOList = makeRequestMessageVO(request, requestBody, Operation.APPEND_ENTITY_ATTRIBUTES, request.getRequestURI(), id, links, contentType);
        }
        // 2. parital  업데이트 요청
        ProcessResultVO processResultVO = process(requestMessageVOList);

        // 3. 요청 결과 확인
        if (processResultVO.isProcessResult()) {
            response.setStatus(HttpStatus.NO_CONTENT.value());

        } else {
            //리소스 생성간 에러가 있는 경우, 예외 발생
            log.error("HTTP Binding ERROR : " + processResultVO.getErrorDescription());
            throw processResultVO.getException();
        }
    }

    /**
     * Update Entity Attributes
     *
     * @param response
     * @param id          사용자 요청 id
     * @param requestBody 사용자 요청 body
     */
    @RequestMapping(value = "/entities/{id:.+}/attrs", method = {RequestMethod.PATCH})
    public void updateEntityAttributes( HttpServletRequest request,
                                        HttpServletResponse response,
                                        @PathVariable String id,
                                        @RequestHeader(value = HttpHeaders.LINK, required = false) String link,
                                        @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) String contentType,
                                        @RequestBody String requestBody) throws Exception {

        log.info("UpdateEntityAttributes Reqeust id={}, link={}, contentType={}, body={}",
                id, link, contentType, requestBody);

        List<String> links = HttpHeadersUtil.extractLinkUris(link);

        // 1. entity 업데이트를 위한 객체 생성
        List<IngestMessageVO> requestMessageVOList = makeRequestMessageVO(request, requestBody,
                Operation.UPDATE_ENTITY_ATTRIBUTES, request.getRequestURI(), id, links, contentType);

        // 2. parital  업데이트 요청
        ProcessResultVO processResultVO = process(requestMessageVOList);

        // 3. 요청 결과 확인
        if (processResultVO.isProcessResult()) {
            response.setStatus(HttpStatus.NO_CONTENT.value());

        } else {
            //리소스 생성간 에러가 있는 경우, 예외 발생
            log.error("HTTP Binding ERROR : " + processResultVO.getErrorDescription());
            throw processResultVO.getException();
        }
    }

    /**
     * 리소스 partial 업데이트 (Attribute partial update)
     *
     * @param response
     * @param id          업데이트 대상 id
     * @param attrId      업데이트 대상 attribute 항목
     * @param requestBody 사용자 요청 body
     */
    @PatchMapping("/entities/{id:.+}/attrs/{attrId:.+}")
    public void partialUpdateWithAttrId(HttpServletRequest request
            , HttpServletResponse response
            , @RequestHeader(value = HttpHeaders.LINK, required = false) String link
            , @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) String contentType
            , @PathVariable("id") String id
            , @PathVariable("attrId") String attrId
            , @RequestBody String requestBody) throws Exception {

        log.info("PartialUpdateWithAttrId Reqeust id={}, attrId={}, link={}, contentType={}, body={}",
                id, attrId, link, contentType, requestBody);

        // 1. entity 업데이트를 위한 객체 생성
        List<IngestMessageVO> requestMessageVOList = makeRequestMessageVO(request, requestBody,
                Operation.PARTIAL_ATTRIBUTE_UPDATE, request.getRequestURI(), id, HttpHeadersUtil.extractLinkUris(link), contentType);

        // 2. partial 업데이트 요청
        ProcessResultVO processResultVO = process(requestMessageVOList);

        // 3. 요청 결과 확인
        if (processResultVO.isProcessResult()) {
            response.setStatus(HttpStatus.NO_CONTENT.value());

        } else {
            //리소스 생성간 에러가 있는 경우, 예외 발생
            log.error("HTTP Binding ERROR : " + processResultVO.getErrorDescription());
            throw processResultVO.getException();
        }
    }


    /**
     * 리소스 삭제     (Entity deletion by id)
     *
     * @param response
     * @param id       삭제 대상 id
     */
    @DeleteMapping("/entities/{id:.+}")
    public void delete(HttpServletRequest request, HttpServletResponse response, @PathVariable String id) throws Exception {

        log.info("Delete entity Reqeust id={}", id);

        // 1. entity 삭제를 위한 객체 생성
        List<IngestMessageVO> requestMessageVOList = makeRequestMessageVO(request, null,
                Operation.DELETE_ENTITY, request.getRequestURI(), id, null, null);

        // 2. 리소스 삭제 요청
        ProcessResultVO processResultVO = process(requestMessageVOList);

        // 3. 요청 결과 확인
        if (processResultVO.isProcessResult()) {
            response.setStatus(HttpStatus.NO_CONTENT.value());

        } else {
            //리소스 생성간 에러가 있는 경우, 예외 발생
            log.error("HTTP Binding ERROR : " + processResultVO.getErrorDescription());

            throw processResultVO.getException();
        }
    }

    /**
     * 리소스 Attr 삭제     (Entity deletion by id, attrId)
     *
     * @param response
     * @param id       삭제 대상 Entity id
     * @param attrId   삭제 대상 Attribute id
     */
    @DeleteMapping("/entities/{id:.+}/attrs/{attrId:.+}")
    public void deleteAttr( HttpServletRequest request,
                            HttpServletResponse response,
                            @RequestHeader(value = HttpHeaders.LINK, required = false) String link,
                            @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) String contentType,
                            @PathVariable String id,
                            @PathVariable String attrId) throws Exception {

        log.info("DeleteAttr Reqeust id={}, attrId={}, link={}, contentType={}",
                id, attrId, link, contentType);

        // 1. entity 삭제를 위한 객체 생성
        List<String> links = HttpHeadersUtil.extractLinkUris(link);
        List<IngestMessageVO> requestMessageVOList = makeRequestMessageVO(request, null,
                Operation.DELETE_ENTITY_ATTRIBUTES, request.getRequestURI(), id, links, contentType);

        // 2. 리소스 삭제 요청
        ProcessResultVO processResultVO = process(requestMessageVOList);

        // 3. 요청 결과 확인
        if (processResultVO.isProcessResult()) {
            response.setStatus(HttpStatus.NO_CONTENT.value());

        } else {
            //리소스 생성간 에러가 있는 경우, 예외 발생
            log.error("HTTP Binding ERROR : " + processResultVO.getErrorDescription());

            throw processResultVO.getException();
        }
    }


    /**
     * Batch 리소스 생성 (Batch Entity Creation)
     *
     * @param response
     * @param requestBody 사용자 요청 body
     */
    @PostMapping("/entityOperations/create")
    public void batchEntityCreation(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @RequestHeader(value = HttpHeaders.LINK, required = false) String link,
                                    @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) String contentType,
                                    @RequestBody String requestBody) throws Exception {

        log.info("BatchEntityCreation Request link={}, contentType={}, body={}", link, contentType, requestBody);

        List<Map<String, Object>> jsonList = objectMapper.readValue(requestBody, List.class);

        List<String> links = HttpHeadersUtil.extractLinkUris(link);

        validateContextInBachOperation(contentType, links, jsonList);

        // 1. entity 생성을 위한 객체 생성
        BatchIngestMessageVO batchIngestMessageVO = makeBatchRequestMessageVO(request, jsonList,
                Operation.CREATE_ENTITY, request.getRequestURI(),
                null, links, contentType);

        List<IngestMessageVO> ingestMessageVO = batchIngestMessageVO.getIngestMessageVO();
        List<BatchEntityErrorVO> batchEntityErrorVO = batchIngestMessageVO.getBatchEntityErrorVO();

        // 2. 리소스 생성 요청
        List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> processResultVO = bulkProcess(ingestMessageVO);

        // 3. 배치 오퍼레이션 결과 처리
        BatchOperationResponseVO batchOperationResponseVO = batchCreateOperationResponse(processResultVO, batchEntityErrorVO);

        // 4. 설정에 property 조건에 따라 response body를 로그로 남김
        if (DataServiceBrokerCode.UseYn.YES.getCode().equalsIgnoreCase(isResponseLog)) {
            log.info("BatchEntityCreation Response. StatusCode={}, body={}",
                    batchOperationResponseVO.getStatusCode().value(), batchOperationResponseVO.getResponseBody());
        }

        // 5. 처리 결과 전달
        commonBatchOpertionResponse(response, batchOperationResponseVO);
    }

    /**
     * Batch 리소스 upsert (Batch Entity Creation or Update (Upsert))
     *
     * @param response
     * @param requestBody 사용자 요청 body
     */
    @PostMapping("/entityOperations/upsert")
    public void batchEntityUpsert(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @RequestHeader(value = HttpHeaders.LINK, required = false) String link,
                                  @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) String contentType,
                                  @RequestBody String requestBody) throws Exception {

        log.info("BatchEntityUpsert Reqeust link={}, contentType={}, body={}", link, contentType, requestBody);

        List<Map<String, Object>> jsonList = objectMapper.readValue(requestBody, List.class);

        List<String> links = HttpHeadersUtil.extractLinkUris(link);

        validateContextInBachOperation(contentType, links, jsonList);

        // 1. entity 생성을 위한 객체 생성
        BatchIngestMessageVO batchIngestMessageVO = makeBatchRequestMessageVO(request, jsonList,
                Operation.CREATE_ENTITY_OR_REPLACE_ENTITY_ATTRIBUTES, request.getRequestURI(),
                null, links, contentType);
        List<IngestMessageVO> ingestMessageVOs = batchIngestMessageVO.getIngestMessageVO();
        List<BatchEntityErrorVO> batchEntityErrorVO = batchIngestMessageVO.getBatchEntityErrorVO();

        // 2. 리소스 생성 요청
        List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> processResultVO = bulkProcess(ingestMessageVOs);

        // 3. 배치 오퍼레이션 결과 처리
        BatchOperationResponseVO batchOperationResponseVO = batchUpsertOperationResponse(processResultVO, batchEntityErrorVO);

        // 4. 설정에 property 조건에 따라 response body를 로그로 남김
        if (DataServiceBrokerCode.UseYn.YES.getCode().equalsIgnoreCase(isResponseLog)) {
            log.info("BatchEntityUpsert Response. StatusCode={}, body={}",
                    batchOperationResponseVO.getStatusCode().value(), batchOperationResponseVO.getResponseBody());
        }

        // 5. 처리 결과 전달
        commonBatchOpertionResponse(response, batchOperationResponseVO);
    }

    /**
     * Batch 리소스 Update (Batch Entity Update)
     *
     * @param response
     * @param requestBody 사용자 요청 body
     */
    @PostMapping("/entityOperations/update")
    public void batchEntityUpdate(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @RequestHeader(value = HttpHeaders.LINK, required = false) String link,
                                  @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) String contentType,
                                  @RequestBody String requestBody,
                                  @RequestParam(value = "options", required = false) String options) throws IOException {

        log.info("BatchEntityUpdate Reqeust link={}, contentType={}, options={}, body={}", link, contentType, options, requestBody);

        List<Map<String, Object>> jsonList = objectMapper.readValue(requestBody, List.class);

        List<String> links = HttpHeadersUtil.extractLinkUris(link);

        validateContextInBachOperation(contentType, links, jsonList);

        // 1. entity 생성을 위한 객체 생성

        //todo
        BatchIngestMessageVO batchIngestMessageVO;
        if (options != null && options.equals(OperationOption.NO_OVERWRITE.getCode())) {
            // "noOverwrite". Indicates that no attribute overwrite shall be performed.
            batchIngestMessageVO = makeBatchRequestMessageVO(request, jsonList, Operation.APPEND_ENTITY_ATTRIBUTES,
                    request.getRequestURI(), Arrays.asList(OperationOption.NO_OVERWRITE), links, contentType);
        } else {
            batchIngestMessageVO = makeBatchRequestMessageVO(request, jsonList, Operation.APPEND_ENTITY_ATTRIBUTES,
                    request.getRequestURI(), null, links, contentType);
        }
        List<IngestMessageVO> ingestMessageVOs = batchIngestMessageVO.getIngestMessageVO();
        List<BatchEntityErrorVO> batchEntityErrorVO = batchIngestMessageVO.getBatchEntityErrorVO();

        // 2. 리소스 생성 요청
        List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> processResultVO = bulkProcess(ingestMessageVOs);

        // 3. 배치 오퍼레이션 결과 처리
        BatchOperationResponseVO batchOperationResponseVO = batchUpdateAndDeleteOperationResponse(processResultVO, batchEntityErrorVO);

        // 4. 설정에 property 조건에 따라 response body를 로그로 남김
        if (DataServiceBrokerCode.UseYn.YES.getCode().equalsIgnoreCase(isResponseLog)) {
            log.info("BatchEntityUpdate Response. StatusCode={}, body={}",
                    batchOperationResponseVO.getStatusCode().value(), batchOperationResponseVO.getResponseBody());
        }

        // 5. 처리 결과 전달
        commonBatchOpertionResponse(response, batchOperationResponseVO);

    }


    /**
     * Batch 리소스 delete (Batch Entity delete)
     *
     * @param response
     * @param ids      사용자 요청 body
     */
    @PostMapping("/entityOperations/delete")
    public void batchEntityDelete(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @RequestHeader(value = HttpHeaders.LINK, required = false) String link,
                                  @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) String contentType,
                                  @RequestBody List<String> ids) throws Exception {

        log.info("BatchEntityDelete Reqeust link={}, contentType={}, ids={}", link, contentType, ids);

        List<String> links = HttpHeadersUtil.extractLinkUris(link);

        validateContextInBachOperation(contentType, links, null);

        BatchIngestMessageVO batchIngestMessageVO = null;
        // 1. entity 생성을 위한 객체 생성
        batchIngestMessageVO = makeBatchRequestMessageVO(request, ids, Operation.DELETE_ENTITY,
                request.getRequestURI(), null, HttpHeadersUtil.extractLinkUris(link), contentType);
        List<IngestMessageVO> ingestMessageVOs = batchIngestMessageVO.getIngestMessageVO();
        List<BatchEntityErrorVO> batchEntityErrorVO = batchIngestMessageVO.getBatchEntityErrorVO();
        // 2. 리소스 삭제
        List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> processResultVO = bulkProcess(ingestMessageVOs);

        // 3. 배치 오퍼레이션 결과 처리
        BatchOperationResponseVO batchOperationResponseVO = batchUpdateAndDeleteOperationResponse(processResultVO, batchEntityErrorVO);

        // 4. 설정에 property 조건에 따라 response body를 로그로 남김
        if (DataServiceBrokerCode.UseYn.YES.getCode().equalsIgnoreCase(isResponseLog)) {
            log.info("BatchEntityDelete Response. StatusCode={}, body={}",
                    batchOperationResponseVO.getStatusCode().value(), batchOperationResponseVO.getResponseBody());
        }

        // 5. 처리 결과 전달
        commonBatchOpertionResponse(response, batchOperationResponseVO);

    }


    /**
     * Retrieve Available Entity Types or Details of Available Entity Types
     *
     * @param request
     * @param response
     * @param accept
     * @param link
     * @param details
     * @throws Exception
     */
    @GetMapping(value = "/types")
    public @ResponseBody
    void getEntityTypes(HttpServletRequest request,
                        HttpServletResponse response,
                        @RequestHeader(value = HttpHeaders.ACCEPT) String accept,
                        @RequestHeader(value = HttpHeaders.LINK, required = false) String link,
                        @RequestParam(value = "details", required = false) boolean details) throws Exception {

        StringBuilder requestParams = new StringBuilder();
        requestParams.append("accept=").append(accept)
                .append(", link=").append(link)
                .append(", params(details)=").append(details);

        log.info("query types request. accept={}, link={}, details={}", accept, link, details);

        List<String> links = HttpHeadersUtil.extractLinkUris(link);
        accept = HttpHeadersUtil.getPrimaryAccept(accept);

        // 1. 가용한 Entity types 상세 조회
        List<DataModelCacheVO> dataModelList = dataModelManager.getDataModelVOListCache();

        if (dataModelList == null) {
            throw new NgsiLdResourceNotFoundException(ErrorCode.NOT_EXIST_ENTITY, "There is no an existing Entity.");
        }

        List<DataModelCacheVO> existsEntityModelList = new ArrayList<>();
        for(DataModelCacheVO dataModelCacheVO : dataModelList) {
            QueryVO queryVO = new QueryVO();
            queryVO.setType(dataModelCacheVO.getDataModelVO().getTypeUri());
            Integer totalCount = entityRetrieveSVC.getEntityCount(queryVO, null, null);
            if(totalCount != null && totalCount > 0) {
                existsEntityModelList.add(dataModelCacheVO);
            }
        }

        String jsonResult = null;
        int resultSize = 1;

        // Retrieve Details of Available Entity Types
        if(details) {
            List<AvailableEntityTypeDetail> entityTypeDetailList = dataModelListToEntityTypeDetails(existsEntityModelList, links);
            resultSize = entityTypeDetailList.size();
            jsonResult = objectMapper.writeValueAsString(entityTypeDetailList);
        }
        // Retrieve Available Entity Types
        else {
            AvailableEntityType entityType = dataModelListToEntityType(existsEntityModelList, links);
            jsonResult = objectMapper.writeValueAsString(entityType);
        }

        // 2. 설정에 property 조건에 따라 response body를 로그로 남김
        if (UseYn.YES.getCode().equalsIgnoreCase(isResponseLog)) {
            log.info("response body : " + jsonResult);
        }

        HttpHeadersUtil.addPaginationLinkHeader(BigDataStorageType.RDB, request, response, accept, null, null, resultSize, defaultLimit);
        HttpHeadersUtil.addContextLinkHeader(response, accept, links);
        response.getWriter().print(jsonResult);
    }


    /**
     * Retrieve Available Entity Type Information
     *
     * @param request
     * @param response
     * @param accept
     * @param link
     * @param type	조회 대상 type
     * @throws Exception
     */
    @GetMapping(value = "/types/{type}")
    public @ResponseBody
    void getEntityType(HttpServletRequest request,
                       HttpServletResponse response,
                       @RequestHeader(value = HttpHeaders.ACCEPT) String accept,
                       @RequestHeader(value = HttpHeaders.LINK, required = false) String link,
                       @PathVariable String type) throws Exception {

        log.info("retrieve types request. accept={}, link={}, type={}", accept, link, type);

        List<String> links = HttpHeadersUtil.extractLinkUris(link);
        accept = HttpHeadersUtil.getPrimaryAccept(accept);

        // 1. 가용한 Entity type 정보 조회
        DataModelCacheVO dataModel = dataModelManager.getDataModelVOCacheByContext(links, type);

        if (dataModel == null) {
            throw new NgsiLdResourceNotFoundException(ErrorCode.NOT_EXIST_ENTITY, "There is no an existing Entity which type.");
        }

        AvailableEntityTypeInformation entityTypeInfo = dataModelToEntityTypeInformation(dataModel);

        String jsonResult = objectMapper.writeValueAsString(entityTypeInfo);

        // 2. 설정에 property 조건에 따라 response body를 로그로 남김
        if (UseYn.YES.getCode().equalsIgnoreCase(isResponseLog)) {
            log.info("response body : " + jsonResult);
        }

        HttpHeadersUtil.addPaginationLinkHeader(BigDataStorageType.RDB, request, response, accept, null, null, dataModel == null ? 0 : 1, defaultLimit);
        HttpHeadersUtil.addContextLinkHeader(response, accept, links);
        response.getWriter().print(jsonResult);
    }


    /**
     * Retrieve Available Attributes or Retrieve Details of Available Attributes
     *
     * @param request
     * @param response
     * @param accept
     * @param link
     * @param details
     * @throws Exception
     */
    @GetMapping(value = "/attributes")
    public @ResponseBody
    void getAttribute(HttpServletRequest request,
                      HttpServletResponse response,
                      @RequestHeader(value = HttpHeaders.ACCEPT) String accept,
                      @RequestHeader(value = HttpHeaders.LINK, required = false) String link,
                      @RequestParam(value = "details", required = false) boolean details) throws Exception {

        log.info("query attributes request. accept={}, link={}, details={}", accept, link, details);

        List<String> links = HttpHeadersUtil.extractLinkUris(link);
        accept = HttpHeadersUtil.getPrimaryAccept(accept);

        // 1. 가용한 Entity 전체 조회
        List<DataModelCacheVO> dataModelList = dataModelManager.getDataModelVOListCache();

        if (dataModelList == null) {
            throw new NgsiLdResourceNotFoundException(ErrorCode.NOT_EXIST_ENTITY, "There is no an existing Entity.");
        }

        String jsonResult = null;
        int resultSize = 1;

        // Retrieve Details of Available Entity Types
        if(details) {
            List<AvailableAttributeDetail> attributeDetailList = dataModelListToAttributeDetails(dataModelList, links);
            resultSize = attributeDetailList.size();
            jsonResult = objectMapper.writeValueAsString(attributeDetailList);
        }
        // Retrieve Available Entity Types
        else {
            AvailableAttribute attribute = dataModelListToAttribute(dataModelList, links);
            jsonResult = objectMapper.writeValueAsString(attribute);
        }

        // 2. 설정에 property 조건에 따라 response body를 로그로 남김
        if (UseYn.YES.getCode().equalsIgnoreCase(isResponseLog)) {
            log.info("response body : " + jsonResult);
        }

        HttpHeadersUtil.addPaginationLinkHeader(BigDataStorageType.RDB, request, response, accept, null, null, resultSize, defaultLimit);
        HttpHeadersUtil.addContextLinkHeader(response, accept, links);
        response.getWriter().print(jsonResult);

    }


    /**
     * Retrieve Available Attribute Information
     *
     * @param request
     * @param response
     * @param accept
     * @param link
     * @throws Exception
     */
    @GetMapping(value = "/attributes/{attrId}")
    public @ResponseBody
    void getAttributeInformation(HttpServletRequest request,
                                 HttpServletResponse response,
                                 @RequestHeader(value = HttpHeaders.ACCEPT) String accept,
                                 @RequestHeader(value = HttpHeaders.LINK, required = false) String link,
                                 @PathVariable String attrId) throws Exception {

        log.info("retrieve attributes request. accept={}, link={}, attrId={}", accept, link, attrId);

        List<String> links = HttpHeadersUtil.extractLinkUris(link);
        accept = HttpHeadersUtil.getPrimaryAccept(accept);

        // 1. 가용한 Entity 전체 조회
        List<DataModelCacheVO> dataModelList = dataModelManager.getDataModelVOListCache();

        if (dataModelList == null) {
            throw new NgsiLdResourceNotFoundException(ErrorCode.NOT_EXIST_ENTITY, "There is no an existing Entity.");
        }

        AvailableAttributeInformation attributeInformation = dataModelToAttributeInformation(dataModelList, attrId, links);

        if (attributeInformation == null) {
            throw new NgsiLdResourceNotFoundException(ErrorCode.NOT_EXIST_ENTITY, "There is no an existing Attribute which name of attribute.");
        }

        String jsonResult = objectMapper.writeValueAsString(attributeInformation);

        // 2. 설정에 property 조건에 따라 response body를 로그로 남김
        if (UseYn.YES.getCode().equalsIgnoreCase(isResponseLog)) {
            log.info("response body : " + jsonResult);
        }

        HttpHeadersUtil.addPaginationLinkHeader(BigDataStorageType.RDB, request, response, accept, null, null, attributeInformation == null ? 0 : 1, defaultLimit);
        HttpHeadersUtil.addContextLinkHeader(response, accept, links);
        response.getWriter().print(jsonResult);
    }


    /**
     * /entityOperations/create 응답데이터 생성
     * @param entityProcessVOs entityOperations 요청 및 결과 객체
     * @param parsingErrors entityOperations 파싱오류 목록
     * @return HTTP응답코드 및 body 데이터
     * @throws JsonProcessingException Body 생성 시 Json파싱에러
     */
    private BatchOperationResponseVO batchCreateOperationResponse(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> entityProcessVOs,
                                                                  List<BatchEntityErrorVO> parsingErrors) throws JsonProcessingException {

        BatchOperationResponseVO batchOperationResponseVO = new BatchOperationResponseVO();

        // 전체 성공인 경우
        if(isComplateSuccess(entityProcessVOs, parsingErrors)) {
            List<String> result = new ArrayList<>();
            for (EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO : entityProcessVOs) {
                result.add(entityProcessVO.getEntityId());
            }
            batchOperationResponseVO.setStatusCode(HttpStatus.CREATED);
            batchOperationResponseVO.setResponseBody(objectMapper.writeValueAsString(result));

            // 전체 성공이 아닌 경우
        } else {
            batchOperationResponseVO.setStatusCode(HttpStatus.MULTI_STATUS);
            batchOperationResponseVO.setResponseBody(generateMultiStatusResponse(entityProcessVOs, parsingErrors));
        }

        return batchOperationResponseVO;
    }

    /**
     * /entityOperations/update 또는 /entityOperations/delete 응답데이터 생성
     * @param entityProcessVOs entityOperations 요청 및 결과 객체
     * @param parsingErrors entityOperations 파싱오류 목록
     * @return HTTP응답코드 및 body 데이터
     * @throws JsonProcessingException Body 생성 시 Json파싱에러
     */
    private BatchOperationResponseVO batchUpdateAndDeleteOperationResponse(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> entityProcessVOs,
                                                                           List<BatchEntityErrorVO> parsingErrors) throws JsonProcessingException {

        BatchOperationResponseVO batchOperationResponseVO = new BatchOperationResponseVO();

        // 전체 성공인 경우
        if(isComplateSuccess(entityProcessVOs, parsingErrors)) {
            batchOperationResponseVO.setStatusCode(HttpStatus.NO_CONTENT);

            // 전체 성공이 아닌 경우
        } else {
            batchOperationResponseVO.setStatusCode(HttpStatus.MULTI_STATUS);
            batchOperationResponseVO.setResponseBody(generateMultiStatusResponse(entityProcessVOs, parsingErrors));
        }

        return batchOperationResponseVO;
    }


    /**
     * /entityOperations/upsert 응답데이터 생성
     * @param entityProcessVOs entityOperations 요청 및 결과 객체
     * @param parsingErrors entityOperations 파싱오류 목록
     * @return HTTP응답코드 및 body 데이터
     * @throws JsonProcessingException Body 생성 시 Json파싱에러
     */
    private BatchOperationResponseVO batchUpsertOperationResponse(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> entityProcessVOs,
                                                                  List<BatchEntityErrorVO> parsingErrors) throws JsonProcessingException {

        BatchOperationResponseVO batchOperationResponseVO = new BatchOperationResponseVO();

        // 전체 성공인 경우
        if(isComplateSuccess(entityProcessVOs, parsingErrors)) {
            boolean isIncludeCreate = false;
            List<String> result = new ArrayList<>();
            for (EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO : entityProcessVOs) {
                if(entityProcessVO.getProcessResultVO().getProcessOperation() == Operation.CREATE_ENTITY) {
                    isIncludeCreate = true;
                    result.add(entityProcessVO.getEntityId());
                }
            }
            // create 가 포함된 경우 statusCode=201과 함께 생성된 entityId를 반환
            if(isIncludeCreate) {
                batchOperationResponseVO.setStatusCode(HttpStatus.CREATED);
                batchOperationResponseVO.setResponseBody(objectMapper.writeValueAsString(result));
                // create 가 포함되지 않은 경우 body 없이 statusCode=204만 반환
            } else {
                batchOperationResponseVO.setStatusCode(HttpStatus.NO_CONTENT);
            }

            // 전체 성공이 아닌 경우
        } else {
            batchOperationResponseVO.setStatusCode(HttpStatus.MULTI_STATUS);
            batchOperationResponseVO.setResponseBody(generateMultiStatusResponse(entityProcessVOs, parsingErrors));
        }

        return batchOperationResponseVO;
    }

    /**
     * batch 요청 전체 성공 여부 판단
     * @param entityProcessVOs entityOperations 요청 및 결과 객체
     * @param parsingErrors entityOperations 파싱오류 목록
     * @return 요청 전체 성공 여부
     */
    private boolean isComplateSuccess(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> entityProcessVOs,
                                      List<BatchEntityErrorVO> parsingErrors) {

        if(entityProcessVOs == null && parsingErrors == null) {
            return false;
        }

        if(parsingErrors != null && parsingErrors.size() > 0) {
            return false;
        }

        if (entityProcessVOs != null) {
            for(EntityProcessVO entityProcessVO : entityProcessVOs) {
                if(!entityProcessVO.getProcessResultVO().isProcessResult()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * entityOperations 요청의 MultiStatus 응답데이터 생성
     * @param entityProcessVOs entityOperations 요청 및 결과 객체
     * @param parsingErrors entityOperations 파싱오류 목록
     * @return entityOperation 요청의 HTTP response body 데이터
     * @throws JsonProcessingException Body 생성 시 Json파싱에러
     */
    private String generateMultiStatusResponse(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> entityProcessVOs,
                                               List<BatchEntityErrorVO> parsingErrors) throws JsonProcessingException {

        BatchOperationResultVO batchOperationResultVO = new BatchOperationResultVO();
        List<String> entityIds = new ArrayList<>();
        List<BatchEntityErrorVO> errors = new ArrayList<>();

        if (entityProcessVOs != null) {
            for (EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO : entityProcessVOs) {
                String entityId = entityProcessVO.getEntityId();

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
        }

        if (parsingErrors != null && parsingErrors.size() > 0) {
            errors.addAll(parsingErrors);
        }

        if(entityIds != null && entityIds.size() > 0) {
            batchOperationResultVO.setSuccess(entityIds);
        }
        if(errors != null && errors.size() > 0) {
            batchOperationResultVO.setErrors(errors);
        }
        return objectMapper.writeValueAsString(batchOperationResultVO);
    }

    /**
     * HTTP Binding operation 요청 (* retrieve 제외)
     *
     * @param requestMessageVOList 요청메시지VO리스트
     * @return
     */
    private ProcessResultVO process(List<IngestMessageVO> requestMessageVOList) {

        if (log.isDebugEnabled()) {
            log.debug("requestMessageVOList='{}'", requestMessageVOList.toString());
        }

        List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> entityProcessVOList
                = (List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>>) bulkProcessor.processBulk(requestMessageVOList);

        if (entityProcessVOList != null && entityProcessVOList.size() > 0) {
            return entityProcessVOList.get(0).getProcessResultVO();
        }

        return null;
    }

    /**
     * HTTP Binding (batch) operation 요청
     *
     * @return
     */
    private List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> bulkProcess(List<IngestMessageVO> ingestMessageVOs) {

        if (log.isDebugEnabled() && ingestMessageVOs != null) {
            log.debug("ingestMessageVOs='{}'", ingestMessageVOs.toString());
        }
        List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> entityProcessVOList = null;

        if (ingestMessageVOs != null && ingestMessageVOs.size() > 0)
            entityProcessVOList = (List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>>) bulkProcessor.processBulk(ingestMessageVOs);

        return entityProcessVOList;
    }


    /**
     * operation 요청 메시지 생성
     *
     * @param requestBody
     * @param operation
     * @param id
     * @return
     * @throws JsonProcessingException
     * @throws JsonMappingException
     */
    private List<IngestMessageVO> makeRequestMessageVO( HttpServletRequest request,
                                                        String requestBody,
                                                        Operation operation,
                                                        String requestUri,
                                                        String id,
                                                        List<String> links,
                                                        String contentType) throws JsonMappingException, JsonProcessingException {
        return makeRequestMessageVO(request, requestBody, operation, requestUri, id, null, links, contentType);
    }

    /**
     * operation 요청 메시지 생성
     *
     * @param requestBody
     * @param operation
     * @param id
     * @return
     * @throws JsonProcessingException
     * @throws JsonMappingException
     */
    private List<IngestMessageVO> makeRequestMessageVO(HttpServletRequest request,
                                                       String requestBody,
                                                       Operation operation,
                                                       String requestUri,
                                                       String id,
                                                       List<OperationOption> operationOptions,
                                                       List<String> links,
                                                       String contentType) throws JsonMappingException, JsonProcessingException {


        String entityType = extractEntityTypeById(id);
        String datasetId = extractDatasetIdById(id);
        IngestMessageVO requestMessageVO = new IngestMessageVO();
        if (operation != Operation.DELETE_ENTITY) {
            /* 단건 Delete 경우 body없으므로 JacksonMapper 적용X */
            Map<String, Object> obj = objectMapper.readValue(requestBody, Map.class);
            if(StringUtils.isEmpty(id))
                id = obj.get(DefaultAttributeKey.ID.getCode()).toString();
            if(StringUtils.isEmpty(entityType))
                entityType = obj.get(DefaultAttributeKey.TYPE.getCode()).toString();
            if(StringUtils.isEmpty(datasetId))
                datasetId = obj.get(DefaultAttributeKey.DATASET_ID.getCode()).toString();
            
        } 
        
        aasSVC.checkCUDAccessRule(request, datasetId, operation);
        
        requestMessageVO.setEntityType(entityType);
        requestMessageVO.setContent(requestBody);
        requestMessageVO.setDatasetId(datasetId);
        requestMessageVO.setOperation(operation);
        requestMessageVO.setId(id);
        requestMessageVO.setTo(requestUri);
        requestMessageVO.setOperationOptions(operationOptions);
        requestMessageVO.setLinks(links);
        requestMessageVO.setContentType(contentType);
        List<IngestMessageVO> requestMessageVOList = new ArrayList<>();
        requestMessageVOList.add(requestMessageVO);

        return requestMessageVOList;
    }

    /**
     * batch operation 요청 메시지 생성
     *
     * @param jsonList
     * @param operation
     * @return
     */
    private BatchIngestMessageVO makeBatchRequestMessageVO(HttpServletRequest request,
                                                           List jsonList,
                                                           Operation operation,
                                                           String requestUri,
                                                           List<OperationOption> operationOptions,
                                                           List<String> links,
                                                           String contentType) throws JsonProcessingException {


        List<IngestMessageVO> requestMessageVOList = new ArrayList<>();
        List<BatchEntityErrorVO> errors = new ArrayList<>();

        String entityId = null;
        String entityType = null;
        String datasetId = null;
        for (Object jsonStr : jsonList) {
            try {
                IngestMessageVO requestMessageVO = new IngestMessageVO();
                if (operation == Operation.DELETE_ENTITY) {
                    // DELETE의 경우, id가 ['xxx']; 로 옴, 바로 적용가능
                    entityId = (String) jsonStr;
                    entityType = extractEntityTypeById(entityId);
                    requestMessageVO.setEntityType(entityType);
                    requestMessageVO.setId(entityId);
                    requestMessageVO.setOperation(operation);
                    requestMessageVO.setContent("{}");
                    requestMessageVO.setTo(requestUri);
                    requestMessageVO.setOperationOptions(operationOptions);
                    requestMessageVO.setLinks(links);
                    requestMessageVO.setContentType(contentType);
                } else {
                    HashMap<String, Object> obj = (HashMap<String, Object>) jsonStr;
                    entityId = obj.get(DefaultAttributeKey.ID.getCode()).toString();
                    datasetId = obj.get(DefaultAttributeKey.DATASET_ID.getCode()).toString();
                    if (operation == Operation.CREATE_ENTITY) {
                        entityType = obj.get(DefaultAttributeKey.TYPE.getCode()).toString();
                    } else if ((operation == Operation.CREATE_ENTITY_OR_REPLACE_ENTITY_ATTRIBUTES)
                            || (operation == Operation.APPEND_ENTITY_ATTRIBUTES)) {

                        entityType = extractEntityTypeById(entityId);
                        if (entityType == null && obj.get("type") != null) {
                            entityType = obj.get("type").toString();
                        } else {
                            obj.put("type", entityType);
                        }
                        requestMessageVO.setEntityType(entityType);

                        if(ValidateUtil.isEmptyData(datasetId)){
                            datasetId = extractDatasetIdById(entityId);
                        }
                        requestMessageVO.setDatasetId(datasetId);

                    }

                    aasSVC.checkCUDAccessRule(request, requestMessageVO.getDatasetId(), operation);

                    String requestBody = objectMapper.writeValueAsString(obj);
                    requestMessageVO.setEntityType(entityType);
                    requestMessageVO.setContent(requestBody);
                    requestMessageVO.setOperation(operation);
                    requestMessageVO.setId(entityId);
                    requestMessageVO.setDatasetId(datasetId);
                    requestMessageVO.setTo(requestUri);
                    requestMessageVO.setOperationOptions(operationOptions);
                    requestMessageVO.setLinks(links);
                    requestMessageVO.setContentType(contentType);
                }
                requestMessageVOList.add(requestMessageVO);
            } catch (NgsiLdResourceNotFoundException e) {
                BatchEntityErrorVO batchEntityErrorVO = new BatchEntityErrorVO();

                Exception exception = e;
                ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(exception);
                batchEntityErrorVO.setEntityId(entityId);
                batchEntityErrorVO.setError(errorPayload);
                errors.add(batchEntityErrorVO);
            }
        }

        BatchIngestMessageVO batchIngestMessageVO = new BatchIngestMessageVO();

        if (requestMessageVOList != null && requestMessageVOList.size() > 0) {
            batchIngestMessageVO.setIngestMessageVO(requestMessageVOList);
        }
        if (errors != null && errors.size() > 0) {
            batchIngestMessageVO.setBatchEntityErrorVO(errors);
        }
        return batchIngestMessageVO;
    }

    /**
     * batch operation 결과 공통 처리
     */
    private void commonBatchOpertionResponse(HttpServletResponse response, BatchOperationResponseVO batchOperationResponseVO) throws IOException {

        // 처리 결과 전달
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(Constants.CHARSET_ENCODING);
        response.setStatus(batchOperationResponseVO.getStatusCode().value());
        if(batchOperationResponseVO.getResponseBody() != null) {
            response.getWriter().print(batchOperationResponseVO.getResponseBody());
        }

//        HttpStatus statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
//        if (batchOperationResultVO != null) {
//            List<String> success = batchOperationResultVO.getSuccess();
//            List<BatchEntityErrorVO> errors = batchOperationResultVO.getErrors();
//
//            boolean includeSuccess = false;
//            boolean includeError = false;
//            if(success != null && success.size() > 0) {
//                includeSuccess = true;
//            }
//            if(errors != null && errors.size() > 0) {
//                includeError = true;
//            }
//
//            if (includeSuccess && includeError) {
//                // 성공 & 실패
//                statusCode = HttpStatus.MULTI_STATUS;
//
//            } else if (includeSuccess && !includeError) {
//                //모두 성공
//                statusCode = HttpStatus.OK;
//
//            } else if (!includeSuccess && includeError) {
//                //모두 실패
//                statusCode = HttpStatus.MULTI_STATUS;
//            } else {
//                statusCode = HttpStatus.NOT_FOUND;
//            }
//
//            if (operation == Operation.CREATE_ENTITY) {
//                if (statusCode == HttpStatus.OK) {
//                    statusCode = HttpStatus.CREATED;
//                }
//            } else if (operation == Operation.CREATE_ENTITY_OR_REPLACE_ENTITY_ATTRIBUTES) {
//                if (statusCode == HttpStatus.OK) {
//                    statusCode = HttpStatus.NO_CONTENT;
//                }
//            } else if (operation == Operation.APPEND_ENTITY_ATTRIBUTES) {
//                if (statusCode == HttpStatus.OK) {
//                    statusCode = HttpStatus.NO_CONTENT;
//                }
//            } else if (operation == Operation.DELETE_ENTITY) {
//                if (statusCode == HttpStatus.OK) {
//                    statusCode = HttpStatus.NO_CONTENT;
//                }
//            }
//        } else {
//            statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
//        }
//
//        response.setStatus(statusCode.value());
//        response.getWriter().print(objectMapper.writeValueAsString(batchOperationResultVO));
    }


    /**
     * ID로 부터 entity Type 추출
     *
     * @param entityId 요청 ID
     * @return
     */
    private String extractEntityTypeById(String entityId) throws NgsiLdBadRequestException {

        String entityType = null;
        // 1. 데이터모델 정보 조회
        EntityDataModelVO entityDataModelVO = entityDataModelSVC.getEntityDataModelVOById(entityId);
        if (entityDataModelVO == null) {
            return null;
//            throw new NgsiLdResourceNotFoundException(ErrorCode.NOT_EXIST_ID, "There is no Entity instance with the requested identifier.﻿");
        }

        if (!ValidateUtil.isEmptyData(entityDataModelVO)) {
            String dataModelId = entityDataModelVO.getDataModelId();
            DataModelCacheVO dataModelCacheVO = dataModelManager.getDataModelVOCacheById(dataModelId);
            if(dataModelCacheVO != null) {
                entityType = dataModelCacheVO.getDataModelVO().getTypeUri();
            }
        }

        return entityType;
    }

    /**
     * ID로 부터 DatasetId 추출
     *
     * @return
     */
    private String extractDatasetIdById(String entityId) throws NgsiLdBadRequestException {

        String datasetId = null;
        // 1. 데이터모델 정보 조회
        EntityDataModelVO entityDataModelVO = entityDataModelSVC.getEntityDataModelVOById(entityId);
        if (entityDataModelVO == null) {
            return null;
        }

        if (!ValidateUtil.isEmptyData(entityId)) {
            datasetId = entityDataModelVO.getDatasetId();
        }

        return datasetId;
    }

    /**
     * DataModel 로 부터 Entity type 정보 추출
     *
     * @param dataModel
     * @return
     */
    private AvailableEntityTypeInformation dataModelToEntityTypeInformation(DataModelCacheVO dataModel) {
        AvailableEntityTypeInformation entityTypeInfo = new AvailableEntityTypeInformation();
        DataModelVO dataModelVO = dataModel.getDataModelVO();

        entityTypeInfo.setId(dataModelVO.getTypeUri());
        entityTypeInfo.setType(EntityAttributeResultType.ENTITY_TYPE_INFO.getCode());
        entityTypeInfo.setTypeName(dataModelVO.getType());
        entityTypeInfo.setEntityCount(0); // TODO: entity 건수 조회 할 수 있도록

        if(dataModelVO.getAttributes() != null) {
            List<AvailableAttributeDetail> attributeDetails = new ArrayList<AvailableAttributeDetail>();

            for(Attribute attribute : dataModelVO.getAttributes()) {
                AvailableAttributeDetail attributeDetail = new AvailableAttributeDetail();
                List<String> attributeTypes = new ArrayList<String>();

                attributeDetail.setId(attribute.getAttributeUri());
                attributeDetail.setType(EntityAttributeResultType.ATTRIBUTE.getCode());
                attributeDetail.setAttributeName(attribute.getName());
                attributeTypes.add(attribute.getAttributeType().getCode());
                attributeDetail.setAttributeTypes(attributeTypes);
                attributeDetails.add(attributeDetail);
            }

            entityTypeInfo.setAttributeDetails(attributeDetails);
        }


        return entityTypeInfo;
    }

    /**
     * DataModel List 로 부터 Entity Type 추출
     *
     * @param dataModelList
     * @param links
     * @return
     */
    private AvailableEntityType dataModelListToEntityType(List<DataModelCacheVO> dataModelList, List<String> links) {
        AvailableEntityType entityType = new AvailableEntityType();
        Map<String, String> contextMap = dataModelManager.contextToFlatMap(links);

        // URI that is unique within the system scope
        entityType.setId("urn:ngsi-ld:" + EntityAttributeResultType.ENTITY_TYPE_LIST.getCode() + ":" + "37418953");	// 값 고정
        entityType.setType(EntityAttributeResultType.ENTITY_TYPE_LIST.getCode());

        List<String> typeList = new ArrayList<String>();
        for(DataModelCacheVO dataModelCacheVO : dataModelList) {
            DataModelVO dataModel = dataModelCacheVO.getDataModelVO();
            // context 에 있을 경우 short name, 없을 경우 full uri
            if(contextMap != null && contextMap.containsKey(dataModel.getType())) {
                typeList.add(dataModel.getType());
            } else {
                typeList.add(dataModel.getTypeUri());
            }
        }

        // typeList 오름차순 정렬
        Collections.sort(typeList);
        entityType.setTypeList(typeList);

        return entityType;
    }

    /**
     * DataModel List 로 부터 Entity Type 상세 추출
     *
     * @param dataModelList
     * @param links
     * @return
     */
    private List<AvailableEntityTypeDetail> dataModelListToEntityTypeDetails(List<DataModelCacheVO> dataModelList, List<String> links) {
        List<AvailableEntityTypeDetail> entityTypeDetailList = new ArrayList<AvailableEntityTypeDetail>();
        Map<String, String> contextMap = dataModelManager.contextToFlatMap(links);

        for(DataModelCacheVO dataModelCacheVO : dataModelList) {
            AvailableEntityTypeDetail entityTypeDetail = new AvailableEntityTypeDetail();
            DataModelVO dataModel = dataModelCacheVO.getDataModelVO();
            List<String> attributeNames = new ArrayList<String>();

            entityTypeDetail.setId(dataModel.getTypeUri());
            entityTypeDetail.setType(EntityAttributeResultType.ENTITY_TYPE.getCode());
            // context 에 있을 경우 short name, 없을 경우 full uri
            if(contextMap != null && contextMap.containsKey(dataModel.getType())) {
                entityTypeDetail.setTypeName(dataModel.getType());
            } else {
                entityTypeDetail.setTypeName(dataModel.getTypeUri());
            }

            for(Attribute attribute : dataModel.getAttributes()) {
                // context 에 있을 경우 short name, 없을 경우 full uri
                if(contextMap != null && contextMap.containsKey(attribute.getName())) {
                    attributeNames.add(attribute.getName());
                } else {
                    attributeNames.add(attribute.getAttributeUri());
                }
            }
            entityTypeDetail.setAttributeNames(attributeNames);

            entityTypeDetailList.add(entityTypeDetail);
        }

        return entityTypeDetailList;
    }

    /**
     * DataModel List 로 부터 Attribute 추출
     *
     * @param dataModelList
     * @param links
     * @return
     */
    private AvailableAttribute dataModelListToAttribute(List<DataModelCacheVO> dataModelList, List<String> links) {
        AvailableAttribute attribute = new AvailableAttribute();
        Map<String, String> contextMap = dataModelManager.contextToFlatMap(links);
        Map<String, String> attributeMap = new HashMap<String, String>();

        attribute.setId("urn:ngsi-ld:" + EntityAttributeResultType.ATTRIBUTE_LIST.getCode() + ":" + "58208329"); // 값 고정
        attribute.setType(EntityAttributeResultType.ATTRIBUTE_LIST.getCode());

        for(DataModelCacheVO dataModelCache : dataModelList) {
            DataModelVO dataModel = dataModelCache.getDataModelVO();
            // map 을 통한 중복 필터링
            for(Attribute attr : dataModel.getAttributes()) {
                if(contextMap != null && contextMap.containsKey(attr.getName())) {
                    attributeMap.put(attr.getName(), "");
                } else {
                    attributeMap.put(attr.getAttributeUri(), "");
                }
            }
        }

        List<String> attributeList = new ArrayList<String>();
        for(String attr : attributeMap.keySet()) {
            attributeList.add(attr);
        }

        attribute.setAttributeList(attributeList);

        return attribute;
    }

    /**
     * DataModel List 로 부터 Attribute 상세 추출
     *
     * @param dataModelList
     * @param links
     * @return
     */
    private List<AvailableAttributeDetail> dataModelListToAttributeDetails(List<DataModelCacheVO> dataModelList,
                                                                           List<String> links) {
        List<AvailableAttributeDetail> attributeDetail = new ArrayList<AvailableAttributeDetail>();
        Map<String, String> contextMap = dataModelManager.contextToFlatMap(links);
        Map<String, AvailableAttributeDetail> attributeDetailMap = new HashMap<String, AvailableAttributeDetail>();

        for(DataModelCacheVO dataModelCache : dataModelList) {
            DataModelVO dataModel = dataModelCache.getDataModelVO();
            // map 을 통한 중복 필터링
            for(Attribute attr : dataModel.getAttributes()) {
                AvailableAttributeDetail attrDetail = null;

                if(attributeDetailMap.containsKey(attr.getAttributeUri())) {
                    attrDetail = attributeDetailMap.get(attr.getAttributeUri());
                } else {
                    attrDetail = new AvailableAttributeDetail();
                    attrDetail.setId(attr.getAttributeUri());
                    attrDetail.setType(EntityAttributeResultType.ATTRIBUTE.getCode());
                    attrDetail.setAttributeName(attr.getName());
                    attrDetail.setTypeNames(new ArrayList<String>());
                }

                if(contextMap != null && contextMap.containsKey(dataModel.getType())) {
                    attrDetail.getTypeNames().add(dataModel.getType());
                } else {
                    attrDetail.getTypeNames().add(dataModel.getTypeUri());
                }

                attributeDetailMap.put(attrDetail.getId(), attrDetail);
            }
        }

        for(String key : attributeDetailMap.keySet()) {
            attributeDetail.add(attributeDetailMap.get(key));
        }

        return attributeDetail;
    }

    /**
     * DataModel List 로 부터 Attribute 정보 추출
     *
     * @param dataModelList
     * @param attributeName
     * @param links
     * @return
     */
    private AvailableAttributeInformation dataModelToAttributeInformation(List<DataModelCacheVO> dataModelList,
                                                                          String attributeName, List<String> links) {
        AvailableAttributeInformation attributeInformation = new AvailableAttributeInformation();
        Map<String, String> contextMap = dataModelManager.contextToFlatMap(links);

        for(DataModelCacheVO dataModelCache : dataModelList) {
            DataModelVO dataModel = dataModelCache.getDataModelVO();

            for(Attribute attr : dataModel.getAttributes()) {
                // short name 또는 full uri 가 일치할 경우
                if(attr.getAttributeUri().equals(attributeName) || attr.getName().equals(attributeName)) {
                    if(attributeInformation.getId() == null) {
                        attributeInformation.setId(attr.getAttributeUri());
                        attributeInformation.setType(EntityAttributeResultType.ATTRIBUTE.getCode());
                        attributeInformation.setAttributeName(attr.getName());
                        attributeInformation.setAttributeTypes(new ArrayList<String>());
                        attributeInformation.setTypeNames(new ArrayList<String>());
                        attributeInformation.getAttributeTypes().add(attr.getAttributeType().getCode());
//						attributeInformation.setAttributeCount(0); // TODO: Attribute 개수 조회하여 세팅
                    }

                    if(contextMap != null && contextMap.containsKey(dataModel.getType())) {
                        attributeInformation.getTypeNames().add(dataModel.getType());
                    } else {
                        attributeInformation.getTypeNames().add(dataModel.getTypeUri());
                    }
                }
            }
        }

        return attributeInformation;
    }

    private BigDataStorageType getDataStorageType(String dataStorageTypeStr) {
        BigDataStorageType dataStorageType = BigDataStorageType.parseType(dataStorageTypeStr);
        if (dataStorageType == null) {
            dataStorageType = defaultStorageType;
        }
        return dataStorageType;
    }

    private String getPrimaryAccept(String requestAccept) {

        if(ValidateUtil.isEmptyData(requestAccept)) {
            return requestAccept;
        }

        if (requestAccept.contains(Constants.ACCEPT_ALL)) {
            return primaryAccept;
        } else if (requestAccept.contains(primaryAccept)) {
            return primaryAccept;
        } else if (requestAccept.contains(Constants.APPLICATION_LD_JSON_VALUE)) {
            return Constants.APPLICATION_LD_JSON_VALUE;
        } else if (requestAccept.contains(Constants.APPLICATION_JSON_VALUE)) {
            return Constants.APPLICATION_JSON_VALUE;
        } else if (requestAccept.contains(Constants.APPLICATION_GEO_JSON_VALUE)) {
            return Constants.APPLICATION_GEO_JSON_VALUE;
        }

        return requestAccept;
    }


    /**
     * batch 처리 시 context 유효성 체크
     * @param contentType
     * @param links
     * @param jsonList
     */
    private void validateContextInBachOperation(String contentType, List<String> links, List<Map<String, Object>> jsonList) {

        if(ValidateUtil.isEmptyData(contentType)) {
            return;
        }

        if(contentType.contains(Constants.APPLICATION_LD_JSON_VALUE)) {
            //Content-Type header is "application/ld+json" and a JSON-LD Link header is present in the incoming HTTP request
            if(!ValidateUtil.isEmptyData(links)) {
                throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                        "Invalid Request Content. Link Header cannot be used when contentType=application/ld+json");
            }

            //Content-Type header is "application/ld+json" and the request payload body does not contain a @context term
            if(!ValidateUtil.isEmptyData(jsonList)) {
                Set<String> contextSet = new HashSet<String>();
                for(Map<String, Object> jsonMap : jsonList) {
                    List<String> context = (List<String>)jsonMap.get(DefaultAttributeKey.CONTEXT.getCode());
                    if(ValidateUtil.isEmptyData(context)) {
                        throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                                "Invalid Request Content. @context parameter cannot be empty when contentType=application/ld+json");
                    }
                    contextSet.addAll(context);
                }
                //context에 하나라도 invalid한 건이 있으면 503 에러 반환
                dataModelManager.contextToFlatMap(new ArrayList<>(contextSet));
            }

        //Content-Type header is "application/json" and the request payload body (as JSON) contains a "@context" term
        } else if(contentType.contains(Constants.APPLICATION_JSON_VALUE)) {
            if(!ValidateUtil.isEmptyData(jsonList)) {
                for(Map<String, Object> jsonMap : jsonList) {
                    if(!ValidateUtil.isEmptyData(jsonMap.get(DefaultAttributeKey.CONTEXT.getCode()))) {
                        throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                                "Invalid Request Content. @context parameter cannot be used when contentType=application/json");
                    }
                }
            }

            //context에 하나라도 invalid한 건이 있으면 503 에러 반환
            dataModelManager.contextToFlatMap(links);
        }
    }

    private List<String> getLinkHeaderByAccept(String accept, List<String> link) {

        if (Constants.APPLICATION_LD_JSON_VALUE.equals(accept)) {
            return null;
        }

        if (!ValidateUtil.isEmptyData(link)) {
            return link;
        }
        return Collections.singletonList(defaultContextUri);
    }

    private List<String> getLinkOrDefault(List<String> link) {
        if (!ValidateUtil.isEmptyData(link)) {
            return link;
        }
        return Collections.singletonList(defaultContextUri);
    }

}
