package kr.re.keti.sc.dataservicebroker.provisioning.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.dataservicebroker.acl.rule.service.AclRuleSVC;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ProvisionEventType;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdBadRequestException;
import kr.re.keti.sc.dataservicebroker.datamodel.service.DataModelSVC;
import kr.re.keti.sc.dataservicebroker.dataset.service.DatasetSVC;
import kr.re.keti.sc.dataservicebroker.datasetflow.service.DatasetFlowSVC;
import kr.re.keti.sc.dataservicebroker.provisioning.vo.ProvisionNotiVO;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class ProvisioningController {

    @Autowired
    private DataModelSVC dataModelSVC;
    @Autowired
    private DatasetSVC datasetSVC;
    @Autowired
    private DatasetFlowSVC datasetFlowSVC;
    @Autowired
    private AclRuleSVC aclRuleSVC;
    @Autowired
    private ObjectMapper objectMapper;


    /**
     * 데이터 모델 Provisioning 수신 처리
     * @param response HttpServletResponse
     * @param requestBody requestBody
     * @throws Exception
     */
    @PostMapping("/provision/datamodels")
    public void provisionDataModels(HttpServletResponse response, @RequestBody String requestBody) throws Exception {

        log.info("Provision Datamodels request msg='{}'", requestBody);

        // 1. 파라미터 파싱 및 유효성 검사
        ProvisionNotiVO provisionNotiVO = null;
        try {
        	provisionNotiVO = objectMapper.readValue(requestBody, ProvisionNotiVO.class);
        } catch(IOException e) {
        	throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Invalid request body.", e);
        }

        // 2. Provisioning 처리
        ProvisionEventType provisionEventType = provisionNotiVO.getEventType();
        switch(provisionEventType) {
	        case CREATED:
	        	dataModelSVC.processCreate(provisionNotiVO.getTo(), provisionNotiVO.getData(), provisionNotiVO.getRequestId(), provisionNotiVO.getEventTime());
	        	response.setStatus(HttpStatus.CREATED.value());
	        	break;
	        case UPDATED:
	        	dataModelSVC.processUpdate(provisionNotiVO.getTo(), provisionNotiVO.getData(), provisionNotiVO.getRequestId(), provisionNotiVO.getEventTime());
	        	response.setStatus(HttpStatus.NO_CONTENT.value());
	        	break;
	        case DELETED:
    			dataModelSVC.processDelete(provisionNotiVO.getTo(), provisionNotiVO.getData(), provisionNotiVO.getRequestId(), provisionNotiVO.getEventTime());
    	        response.setStatus(HttpStatus.NO_CONTENT.value());
	        	break;
	        default:
	        	break;
        }
    }


    /**
     * 데이터 셋 Provisioning 처리
     * @param response HttpServletResponse
     * @param requestBody requestBody
     * @throws Exception 
     */
    @PostMapping("/provision/datasets")
    public void provisionDatasets(HttpServletResponse response, @RequestBody String requestBody) throws Exception {

        log.info("Provision Datasets request msg='{}'", requestBody);

        // 1. 파라미터 파싱 및 유효성 검사
        ProvisionNotiVO provisionNotiVO = objectMapper.readValue(requestBody, ProvisionNotiVO.class);

        // 2. Provisioning 처리
        ProvisionEventType provisionEventType = provisionNotiVO.getEventType();
        switch(provisionEventType) {
	        case CREATED:
	        	datasetSVC.createDataset(provisionNotiVO.getData(), provisionNotiVO.getRequestId(), provisionNotiVO.getEventTime());
	        	response.setStatus(HttpStatus.CREATED.value());
	        	break;
	        case UPDATED:
	        	datasetSVC.updateDataset(provisionNotiVO.getTo(), provisionNotiVO.getData(), provisionNotiVO.getRequestId(), provisionNotiVO.getEventTime());
	        	response.setStatus(HttpStatus.NO_CONTENT.value());
	        	break;
	        case DELETED:
	        	datasetSVC.deleteDataset(provisionNotiVO.getTo(), provisionNotiVO.getData());
    	        response.setStatus(HttpStatus.NO_CONTENT.value());
	        	break;
	        default:
	        	break;
        }
    }

    /**
     * 데이터 셋 흐름 Provisioning 처리
     * @param response HttpServletResponse
     * @param requestBody requestBody
     * @throws Exception
     */
    @PostMapping("/provision/datasetflows")
    public void provisionDatasetFlows(HttpServletResponse response, @RequestBody String requestBody) throws Exception {

        log.info("Provision DatasetFlows request msg='{}'", requestBody);

        // 1. 파라미터 파싱 및 유효성 검사
        ProvisionNotiVO provisionNotiVO = objectMapper.readValue(requestBody, ProvisionNotiVO.class);

        // 2. Provisioning 처리
        ProvisionEventType provisionEventType = provisionNotiVO.getEventType();
        switch(provisionEventType) {
	        case CREATED:
                datasetFlowSVC.createDatasetFlow(provisionNotiVO.getTo(), provisionNotiVO.getData(), provisionNotiVO.getRequestId(), provisionNotiVO.getEventTime());
	        	response.setStatus(HttpStatus.CREATED.value());
	        	break;
	        case UPDATED:
	        	datasetFlowSVC.updateDatasetFlow(provisionNotiVO.getTo(), provisionNotiVO.getData(), provisionNotiVO.getRequestId(), provisionNotiVO.getEventTime());
	        	response.setStatus(HttpStatus.NO_CONTENT.value());
	        	break;
	        case DELETED:
	        	datasetFlowSVC.deleteDatasetFlow(provisionNotiVO.getTo(), provisionNotiVO.getData());
    	        response.setStatus(HttpStatus.NO_CONTENT.value());
	        	break;
	        default:
	        	break;
        }
    }



    /**
     * 접근제어(데이터 셋) 흐름 Provisioning 처리
     * @param response HttpServletResponse
     * @param requestBody requestBody
     * @throws Exception
     */
    @PostMapping("/provision/acl/rules")
    public void provisionAclRule(HttpServletResponse response, @RequestBody String requestBody) throws Exception {

        log.info("Provision provision AclDataset request msg='{}'", requestBody);

        // 1. 파라미터 파싱 및 유효성 검사
        ProvisionNotiVO provisionNotiVO = objectMapper.readValue(requestBody, ProvisionNotiVO.class);

        // 2. Provisioning 처리
        ProvisionEventType provisionEventType = provisionNotiVO.getEventType();
        switch(provisionEventType) {
            case CREATED:
                aclRuleSVC.createAclRule(provisionNotiVO.getData(), provisionNotiVO.getRequestId(), provisionNotiVO.getEventTime());
                response.setStatus(HttpStatus.CREATED.value());
                break;
            case UPDATED:
                aclRuleSVC.updateAclRule(provisionNotiVO.getTo(), provisionNotiVO.getData(), provisionNotiVO.getRequestId(), provisionNotiVO.getEventTime());
                response.setStatus(HttpStatus.NO_CONTENT.value());
                break;
            case DELETED:
                aclRuleSVC.deleteAclRule(provisionNotiVO.getTo(), provisionNotiVO.getData());
                response.setStatus(HttpStatus.NO_CONTENT.value());
                break;
            default:
                break;
        }
    }
}
