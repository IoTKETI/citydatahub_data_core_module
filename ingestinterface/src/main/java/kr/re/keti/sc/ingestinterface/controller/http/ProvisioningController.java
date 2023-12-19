package kr.re.keti.sc.ingestinterface.controller.http;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.ingestinterface.acl.rule.service.AclRuleSVC;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.ProvisionEventType;
import kr.re.keti.sc.ingestinterface.datamodel.service.DataModelSVC;
import kr.re.keti.sc.ingestinterface.dataset.service.DatasetSVC;
import kr.re.keti.sc.ingestinterface.provisioning.vo.ProvisionNotiVO;
import lombok.extern.slf4j.Slf4j;

/**
 * <pre>
 * Provisionig Controller
 *  - dataModel, dataset, aclRule provisioning
 * </pre>
 */
@RestController
@Slf4j
public class ProvisioningController {

    @Autowired
    private DataModelSVC dataModelSVC;
    @Autowired
    private DatasetSVC datasetSVC;
    @Autowired
    private AclRuleSVC aclRuleSVC;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 데이터모델 프로비저닝 수신 처리 (create, update, delete)
     * @param response HttpServletResponse
     * @param requestBody provisioning data
     * @throws Exception provisioning error
     */
    @PostMapping("/provision/datamodels")
    public void provisionDataModels(HttpServletResponse response, @RequestBody String requestBody) throws Exception {

        log.info("Provision Datamodels request msg='{}'", requestBody);

        // 1. 파라미터 파싱 및 유효성 검사
        ProvisionNotiVO provisionNotiVO = objectMapper.readValue(requestBody, ProvisionNotiVO.class);

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
     * 데이터 셋 프로비저닝 수신 처리 (create, update, delete)
     * @param response HttpServletResponse
     * @param requestBody provisioning data
     * @throws Exception provisioning error
     */
    @PostMapping("/provision/datasets")
    public void provisionDatasets(HttpServletResponse response, @RequestBody String requestBody) throws Exception {

        log.info("Provision Datasets request msg='{}'", requestBody);

        // 1. 파라미터 파싱 및 유효성 검사
        ProvisionNotiVO provisionNotiVO = objectMapper.readValue(requestBody, ProvisionNotiVO.class);

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
     * 접근제어(데이터셋) 프로비저닝 수신 처리 (create, update, delete)
     * @param response HttpServletResponse
     * @param requestBody provisioning data
     * @throws Exception provisioning error
     */
    @PostMapping("/provision/acl/rules")
    public void provisionAclRule(HttpServletResponse response, @RequestBody String requestBody) throws Exception {

        log.info("Provision provision acl-rules request msg='{}'", requestBody);

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
