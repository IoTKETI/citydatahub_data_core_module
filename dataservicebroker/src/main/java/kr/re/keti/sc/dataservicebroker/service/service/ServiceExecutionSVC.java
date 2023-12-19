package kr.re.keti.sc.dataservicebroker.service.service;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ServiceExecutionResult;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ServiceExecutionStatus;
import kr.re.keti.sc.dataservicebroker.service.dao.ServiceExecutionDAO;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceExecutionBaseVO;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceExecutionDetailVO;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceExecutionRequestVO;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceExecutionRequestVO.ExecutionRequest;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceExecutionRetrieveVO;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceExecutorRequestVO;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceExecutorResponseVO;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServiceExecutionSVC {

    @Autowired
    private ServiceExecutionDAO serviceExecutionDAO;
    @Autowired
    private RestTemplate restTemplate;

    @Transactional(value = "dataSourceTransactionManager")
	public Integer createServiceExecution(ServiceExecutionBaseVO serviceExecutionBaseVO) {
		int result = serviceExecutionDAO.createServiceExecutionBase(serviceExecutionBaseVO);
		List<ServiceExecutionDetailVO> serviceExecutionDetailVOs = serviceExecutionBaseVO.getServiceExecutionDetailVOs();
		if(serviceExecutionDetailVOs != null && serviceExecutionDetailVOs.size() > 0) {
			for(ServiceExecutionDetailVO serviceExecutionDetailVO : serviceExecutionDetailVOs) {
				serviceExecutionDAO.createServiceExecutionDetail(serviceExecutionDetailVO);
			}
		}
		return result;
	}

    @Async("serviceExecutionExecutor")
    public void executeServiceToEndpoint(ServiceExecutionRequestVO serviceExecutionRequestVO, 
    		Map<ExecutionRequest, kr.re.keti.sc.dataservicebroker.service.vo.ServiceRegistrationRequestVO.Service> executionRequestServiceMap) {

    	for(Map.Entry<ExecutionRequest, kr.re.keti.sc.dataservicebroker.service.vo.ServiceRegistrationRequestVO.Service> entry : executionRequestServiceMap.entrySet()) {
    		ExecutionRequest executionRequest = entry.getKey();
    		kr.re.keti.sc.dataservicebroker.service.vo.ServiceRegistrationRequestVO.Service service = entry.getValue();

    		ServiceExecutorRequestVO requestVO = new ServiceExecutorRequestVO();
    		requestVO.setId(executionRequest.getId());
    		requestVO.setName(executionRequest.getName());
    		requestVO.setEntityId(executionRequest.getEntityId());
    		requestVO.setEntityType(executionRequest.getEntityType());
    		requestVO.setAttribs(executionRequest.getInput().getAttribs());

    		RequestEntity<ServiceExecutorRequestVO> requestEntity = RequestEntity.post(URI.create(service.getEndpoint()))
								    											 .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
																				 .body(requestVO);

			ServiceExecutionDetailVO serviceExecutionDetailVO = new ServiceExecutionDetailVO();
			serviceExecutionDetailVO.setId(serviceExecutionRequestVO.getId());
			serviceExecutionDetailVO.setServiceId(serviceExecutionRequestVO.getServiceRegistrationId());
			serviceExecutionDetailVO.setExecutionId(executionRequest.getId());

    		try {
    			ResponseEntity<ServiceExecutorResponseVO> responseEntity = restTemplate.exchange(requestEntity, ServiceExecutorResponseVO.class);
    			ServiceExecutorResponseVO serviceExecutorResponseVO = responseEntity.getBody();
    			
    			if(responseEntity.getStatusCode() == HttpStatus.OK) {
    				log.info("ServiceExecution SUCCESS. id={}, serviceId={}, executionRequest={}", 
    						serviceExecutionRequestVO.getId(), serviceExecutionRequestVO.getServiceRegistrationId(), executionRequest);
        			if(serviceExecutorResponseVO != null && serviceExecutorResponseVO.getStatus() == ServiceExecutionResult.SUCCESS) {
        				serviceExecutionDetailVO.setStatus(ServiceExecutionStatus.SUCCESS);
        			} else {
        				serviceExecutionDetailVO.setStatus(ServiceExecutionStatus.FAILED);
        			}
        		} else {
        			log.warn("ServiceExecution FAIL. Http responseCode={}, id={}, serviceId={}, executionRequest={}", responseEntity.getStatusCode(), 
        					serviceExecutionRequestVO.getId(), serviceExecutionRequestVO.getServiceRegistrationId(), executionRequest);
        			serviceExecutionDetailVO.setStatus(ServiceExecutionStatus.FAILED);
        		}
    		} catch (RestClientException e) {
    			String httpStatusCode = null;
    			if(e instanceof HttpStatusCodeException) {
    				httpStatusCode = ((HttpStatusCodeException)e).getStatusText();
    			}
    			log.warn("ServiceExecution FAIL. Http responseCode={}, id={}, serviceId={}, executionRequest={}", httpStatusCode, 
    					serviceExecutionRequestVO.getId(), serviceExecutionRequestVO.getServiceRegistrationId(), executionRequest, e);
    			serviceExecutionDetailVO.setStatus(ServiceExecutionStatus.FAILED);
    		} catch (Exception e) {
    			log.warn("ServiceExecution FAIL. id={}, serviceId={}, executionRequest={}", 
    					serviceExecutionRequestVO.getId(), serviceExecutionRequestVO.getServiceRegistrationId(), executionRequest, e);
    			serviceExecutionDetailVO.setStatus(ServiceExecutionStatus.FAILED);
    		} finally {
    			serviceExecutionDAO.updateServiceExecutionDetail(serviceExecutionDetailVO);
    		}
    	}
    }
    
	public Integer updateServiceExecutionDetail(ServiceExecutionDetailVO serviceExecutionDetailVO) {
		return serviceExecutionDAO.updateServiceExecutionDetail(serviceExecutionDetailVO);
	}

	public ServiceExecutionBaseVO retrieveServiceExecution(ServiceExecutionRetrieveVO serviceExecutionRetrieveVO) {
		return serviceExecutionDAO.retrieveServiceExecution(serviceExecutionRetrieveVO);
	}

}