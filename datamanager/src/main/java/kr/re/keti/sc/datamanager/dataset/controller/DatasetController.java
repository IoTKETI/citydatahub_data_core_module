package kr.re.keti.sc.datamanager.dataset.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.datamanager.common.code.Constants;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ErrorCode;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ProvisionEventType;
import kr.re.keti.sc.datamanager.common.controller.kafka.api.KafkaRestManager;
import kr.re.keti.sc.datamanager.common.exception.BadRequestException;
import kr.re.keti.sc.datamanager.common.exception.ResourceNotFoundException;
import kr.re.keti.sc.datamanager.common.service.security.AASSVC;
import kr.re.keti.sc.datamanager.datamodel.service.DataModelSVC;
import kr.re.keti.sc.datamanager.datamodel.vo.DataModelBaseVO;
import kr.re.keti.sc.datamanager.dataset.service.DatasetSVC;
import kr.re.keti.sc.datamanager.dataset.vo.DatasetBaseVO;
import kr.re.keti.sc.datamanager.datasetflow.service.DatasetFlowSVC;
import kr.re.keti.sc.datamanager.datasetflow.vo.DatasetFlowBaseVO;
import kr.re.keti.sc.datamanager.provisioning.vo.ProvisionNotiVO;
import kr.re.keti.sc.datamanager.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * <pre>
 * 데이터 셋 관리 HTTP Controller 클래스
 *  - 데이터 셋 정보를 생성/수정/삭제하고 기 등록된 Provisioning 대상 서버로 생성/수정/삭제 이벤트를 Provisioning 한다
 *  - 데이터 셋 정보 조회 기능을 제공한다
 * </pre>
 */
@RestController
@Slf4j
public class DatasetController {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DatasetSVC datasetSVC;
    @Autowired
    private DatasetFlowSVC datasetFlowSVC;
    @Autowired
    private DataModelSVC dataModelSVC;
    @Autowired
    private KafkaRestManager kafkaRestManager;
    @Autowired
    private AASSVC aasSVC;

    @Value("${security.acl.useYn:N}")
    private String securityAclUseYn;

    /**
     * Create Dataset
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param datasetBaseVO create dataset object
     * @throws Exception create error
     */
    @PostMapping("/datasets")
    @Transactional
    public void create(HttpServletRequest request,
                       HttpServletResponse response,
                       @RequestBody DatasetBaseVO datasetBaseVO) throws Exception {

        log.info("create dataset request msg='{}'", datasetBaseVO);
        validateParam(datasetBaseVO);

        // 1. check validate
        DatasetBaseVO retrieveDatasetBaseVO = datasetSVC.getDatasetVOById(datasetBaseVO.getId());
        if (retrieveDatasetBaseVO != null) {
            throw new BadRequestException(DataManagerCode.ErrorCode.ALREADY_EXISTS,
                    "Already Exists. datasetId=" + retrieveDatasetBaseVO.getId());
        }

        String dataModelId = datasetBaseVO.getDataModelId();
        
        if(datasetBaseVO.getQualityCheckEnabled() != null && datasetBaseVO.getQualityCheckEnabled()) {
        	if(ValidateUtil.isEmptyData(dataModelId)) {
        		throw new BadRequestException(ErrorCode.NOT_EXISTS_DATAMODEL,
        				"Not Exists dataModel. dataModelId=" + dataModelId);
        	}
        }

        if(!ValidateUtil.isEmptyData(dataModelId)) {
        	DataModelBaseVO retrieveDataModelBaseVO = dataModelSVC.getDataModelBaseVOById(dataModelId);
            if (retrieveDataModelBaseVO == null) {
                throw new BadRequestException(ErrorCode.NOT_EXISTS_DATAMODEL,
                        "Not Exists dataModel. dataModelId=" + dataModelId);
            }
        }

        // 2. create topic (topic name is datasetId)
        kafkaRestManager.createTopic(datasetBaseVO.getId(), datasetBaseVO.getTopicRetention());

        // 3. provisioning create event
        ProvisionNotiVO provisionNotiVO = datasetSVC.provisionDataset(datasetBaseVO, ProvisionEventType.CREATED, request.getRequestURI());

        // 4. create dataset
        datasetBaseVO.setProvisioningRequestId(provisionNotiVO.getRequestId());
        datasetBaseVO.setProvisioningEventTime(provisionNotiVO.getEventTime());
        datasetSVC.createDatasetBaseVO(datasetBaseVO);

        response.setStatus(HttpStatus.CREATED.value());
    }

    /**
     * Update Dataset
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param id datasetId
     * @param datasetBaseVO update dataset object
     * @throws Exception update error
     */
    @PutMapping("/datasets/{id:.+}")
    @Transactional
    public void update(HttpServletRequest request,
                       HttpServletResponse response,
                       @PathVariable String id,
                       @RequestBody DatasetBaseVO datasetBaseVO) throws Exception {

        log.info("Update dataset request id=" + id);
        datasetBaseVO.setId(id);

        // 1. check validate
        DatasetBaseVO retrieveDatasetBaseVO = datasetSVC.getDatasetVOById(id);
        if (retrieveDatasetBaseVO == null) {
            throw new BadRequestException(DataManagerCode.ErrorCode.NOT_EXISTS_DATASET, "Not Exists. id=" + id);
        }
        
        String dataModelId = datasetBaseVO.getDataModelId();
        if(datasetBaseVO.getQualityCheckEnabled() != null && datasetBaseVO.getQualityCheckEnabled()) {
        	if(ValidateUtil.isEmptyData(dataModelId)) {
        		throw new BadRequestException(ErrorCode.NOT_EXISTS_DATAMODEL,
        				"Not Exists dataModel. dataModelId=" + dataModelId);
        	}
        }

        if(!ValidateUtil.isEmptyData(dataModelId)) {
        	DataModelBaseVO retrieveDataModelBaseVO = dataModelSVC.getDataModelBaseVOById(dataModelId);
            if (retrieveDataModelBaseVO == null) {
                throw new BadRequestException(ErrorCode.NOT_EXISTS_DATAMODEL, "Not Exists dataModel. id=" + dataModelId);
            }
        }

        // set default value
        if (datasetBaseVO.getEnabled() == null) {
            datasetBaseVO.setEnabled(true);
        }

        // if change dataModel
        String currentDataModelId = retrieveDatasetBaseVO.getDataModelId();
        String updateDataModelId = datasetBaseVO.getDataModelId();
        if(currentDataModelId != null && updateDataModelId != null && !currentDataModelId.equals(updateDataModelId)) {
        	if(datasetFlowSVC.getDatasetFlowBaseVOById(datasetBaseVO.getId()) != null) {
            	throw new BadRequestException(ErrorCode.INVALID_PARAMETER, 
            			"Cannot change dataModel. Using in datasetFlow."
    	            			+ "datasetId=" + datasetBaseVO.getId() + ", using dataModelId=" + currentDataModelId);
            }
        }

        // 2. provisioning update event
        ProvisionNotiVO provisionNotiVO = datasetSVC.provisionDataset(datasetBaseVO, ProvisionEventType.UPDATED, request.getRequestURI());

        // 3. update dataset
        datasetBaseVO.setProvisioningRequestId(provisionNotiVO.getRequestId());
        datasetBaseVO.setProvisioningEventTime(provisionNotiVO.getEventTime());
        datasetSVC.updateDatasetBaseVO(datasetBaseVO);

        // 4. update kafka topic retention
        if (datasetBaseVO.getTopicRetention() != null) {
            kafkaRestManager.updateTopic(datasetBaseVO.getId(), datasetBaseVO.getTopicRetention());
        }

        response.setStatus(HttpStatus.NO_CONTENT.value());
    }


    /**
     * Delete dateset
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param id dataset id
     * @throws Exception delete error
     */
    @DeleteMapping("/datasets/{id:.+}")
    @Transactional
    public void delete(HttpServletRequest request,
                       HttpServletResponse response,
                       @PathVariable String id) throws Exception {

        log.info("Delete dataset request id=" + id);
        // 1. check validate
        DatasetBaseVO retrieveDatasetBaseVO = datasetSVC.getDatasetVOById(id);
        if (retrieveDatasetBaseVO == null) {
            throw new BadRequestException(DataManagerCode.ErrorCode.NOT_EXISTS_DATASET, "Not Exists. id=" + id);
        }

        List<DatasetFlowBaseVO> datasetFlowBaseVOs = datasetSVC.getEnabledDatasetByDatasetId(id);
        if (datasetFlowBaseVOs != null && datasetFlowBaseVOs.size() > 0) {
            StringBuilder datasetFlowNames = new StringBuilder();
            for (DatasetFlowBaseVO datasetFlowBaseVO : datasetFlowBaseVOs) {
                datasetFlowNames.append(datasetFlowBaseVO.getDatasetId()).append(",");
            }
            datasetFlowNames.deleteCharAt(datasetFlowNames.length() - 1);
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER,
                    "Using Dataset in DatasetFlow. datasetFlowId=" + datasetFlowNames);
        }

        // 2. provisioning delete event
        DatasetBaseVO datasetBaseVO = new DatasetBaseVO();
        datasetBaseVO.setId(id);
        datasetSVC.provisionDataset(datasetBaseVO, ProvisionEventType.DELETED, request.getRequestURI());

        // 3. dataset 정보 삭제
        datasetSVC.deleteDatasetBaseVO(id);

        //  4. delete kafka topic (topic name is datasetId)
        kafkaRestManager.deleteTopic(id);
    }

    /**
     * Provisioning dataset
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param id dataset id
     * @throws Exception provisioning error
     */
    @PostMapping("/datasets/{id:.+}/provisioning")
    public void provisioningDataset(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @PathVariable String id) throws Exception {

        log.info("Provisioning Dataset request id=" + id);

        // 1. check validate
        DatasetBaseVO retrieveDatasetBaseVO = datasetSVC.getDatasetVOById(id);
        if (retrieveDatasetBaseVO == null) {
            throw new BadRequestException(DataManagerCode.ErrorCode.NOT_EXISTS_DATASET, "Not Exists. id=" + id);
        }

        // 2. provisioning update event
        String requestUri = request.getRequestURI().replace("/provisioning", "");
        ProvisionNotiVO provisionNotiVO = datasetSVC.provisionDataset(retrieveDatasetBaseVO, ProvisionEventType.UPDATED, requestUri);

        // 3. update dataset
        DatasetBaseVO updateDatasetBaseVO = new DatasetBaseVO();
        updateDatasetBaseVO.setId(id);
        updateDatasetBaseVO.setProvisioningRequestId(provisionNotiVO.getRequestId());
        updateDatasetBaseVO.setProvisioningEventTime(provisionNotiVO.getEventTime());
        datasetSVC.updateDatasetProvisioning(updateDatasetBaseVO);

        response.setStatus(HttpStatus.NO_CONTENT.value());
    }

    /**
     * Retrieve dataset list
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param accept request accept header
     * @param datasetBaseVO retrieve condition
     * @throws Exception retrieve error
     */
    @GetMapping(value = "/datasets")
    public @ResponseBody
    void getDatasets(HttpServletRequest request,
                     HttpServletResponse response,
                     @RequestHeader(HttpHeaders.ACCEPT) String accept,
                     DatasetBaseVO datasetBaseVO) throws Exception {

        // 0. check access control policy
        if (securityAclUseYn.equals(DataManagerCode.UseYn.YES.getCode())) {
            aasSVC.addAclDatasetIds(request, datasetBaseVO);
        }
        // 1. get total count
        Integer totalCount = datasetSVC.getDatasetVOListTotalCount(datasetBaseVO);
        // 2. get dataset list
        List<DatasetBaseVO> datasetLisst = datasetSVC.getDatasetVOList(datasetBaseVO);
        // 3. set response header
        response.addHeader(Constants.TOTAL_COUNT, Integer.toString(totalCount));

        response.getWriter().print(objectMapper.writeValueAsString(datasetLisst));

    }


    /**
     * Retrieve dataset
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param accept request accept header
     * @param id retrieve dataset id
     * @return dataset object
     */
    @GetMapping("/datasets/{id:.+}")
    public @ResponseBody
    DatasetBaseVO getDatasetById(HttpServletRequest request,
                                 HttpServletResponse response,
                                 @RequestHeader(HttpHeaders.ACCEPT) String accept,
                                 @PathVariable String id)  {

        // 0. check access control policy
        if (securityAclUseYn.equals(DataManagerCode.UseYn.YES.getCode())) {
            aasSVC.checkPermission(request, id);
        }
        DatasetBaseVO datasetBaseVO = datasetSVC.getDatasetVOById(id);

        if (datasetBaseVO == null) {
            throw new ResourceNotFoundException(DataManagerCode.ErrorCode.NOT_EXISTS_DATASET, "There is no an dataset which id");
        }

        return datasetBaseVO;
    }


    /**
     * Retrieve dataset for UI (Admin, Dashboard)
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param accept request accept header
     * @param datasetBaseVO retrieve condition
     * @throws Exception retrieve error
     */
    @GetMapping(value = "/datasets/ui")
    public @ResponseBody
    void getDatasetsForUI(HttpServletRequest request,
                          HttpServletResponse response,
                          @RequestHeader(HttpHeaders.ACCEPT) String accept,
                          DatasetBaseVO datasetBaseVO) throws Exception {

        // 0. check access control policy
        if (securityAclUseYn.equals(DataManagerCode.UseYn.YES.getCode())) {
            aasSVC.addAclDatasetIds(request, datasetBaseVO);
        }
        // 1. get total count
        Integer totalCount = datasetSVC.getDatasetVOListTotalCountForUI(datasetBaseVO);
        // 2. get dataset list
        List<DatasetBaseVO> datasetLisst = datasetSVC.getDatasetVOListForUI(datasetBaseVO);
        // 3. set response header
        response.addHeader(Constants.TOTAL_COUNT, Integer.toString(totalCount));

        response.getWriter().print(objectMapper.writeValueAsString(datasetLisst));
    }


    /**
     * Validate dataset Parameter
     * @param datasetBaseVO dataset object
     */
    private void validateParam(DatasetBaseVO datasetBaseVO) {
        if (ValidateUtil.isEmptyData(datasetBaseVO.getId())) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Not found 'id'");
        }

        if (ValidateUtil.isEmptyData(datasetBaseVO.getUpdateInterval())) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Not found 'updateInterval'");
        }

        if (ValidateUtil.isEmptyData(datasetBaseVO.getProviderOrganization())) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Not found 'providerOrganization'");
        }

        if (ValidateUtil.isEmptyData(datasetBaseVO.getProviderSystem())) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Not found 'providerSystem'");
        }

        if (ValidateUtil.isEmptyData(datasetBaseVO.getIsProcessed())) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Not found 'IsProcessed'");
        }

        if (ValidateUtil.isEmptyData(datasetBaseVO.getLicense())) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Not found 'license'");
        }

        if (ValidateUtil.isEmptyData(datasetBaseVO.getDatasetItems())) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Not found 'datasetItems'");
        }

        if (ValidateUtil.isEmptyData(datasetBaseVO.getTargetRegions())) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Not found 'targetRegions'");
        }

        if (datasetBaseVO.getQualityCheckEnabled() == null) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Not found 'qualityCheckEnabled'");
        }
    }
}
