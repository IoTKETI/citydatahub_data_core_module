package kr.re.keti.sc.dataservicebroker.service.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ServiceExecutionStatus;
import kr.re.keti.sc.dataservicebroker.common.exception.BaseException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdBadRequestException;
import kr.re.keti.sc.dataservicebroker.entities.service.EntityDataModelSVC;
import kr.re.keti.sc.dataservicebroker.entities.vo.EntityDataModelVO;
import kr.re.keti.sc.dataservicebroker.service.service.ServiceExecutionSVC;
import kr.re.keti.sc.dataservicebroker.service.service.ServiceRegistrationSVC;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceExecutionBaseVO;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceExecutionDetailVO;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceExecutionRequestVO;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceExecutionRequestVO.ExecutionRequest;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceExecutionStatusResponseVO.ServiceExecutionStatusVO;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceExecutionResponseVO;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceExecutionRetrieveVO;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceExecutionStatusResponseVO;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceRegistrationBaseRetrieveVO;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceRegistrationRequestVO;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceRegistrationRequestVO.Attribute;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceRegistrationRequestVO.AttributeValue;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceRegistrationRequestVO.RegistrationInfo;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceRegistrationRequestVO.Service;
import kr.re.keti.sc.dataservicebroker.util.HttpHeadersUtil;
import kr.re.keti.sc.dataservicebroker.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class ServiceExecutionController {

    @Autowired
    private ServiceExecutionSVC serviceExecutionSVC;
    @Autowired
    private ServiceRegistrationSVC serviceRegistrationSVC;
    @Autowired
    private EntityDataModelSVC entityDataModelSVC;
    @Autowired
    private ObjectMapper objectMapper;
    
    @PostMapping("/service/execute")
    public ResponseEntity<ServiceExecutionResponseVO> serviceExecution(HttpServletRequest request, HttpServletResponse response,
    		@RequestBody ServiceExecutionRequestVO serviceExecutionRequestVO) throws BaseException {

    		log.info("serviceExecute. serviceExecuteRequestVO={}", serviceExecutionRequestVO);

        	// 1. validation
    		validateServiceExecuteParam(serviceExecutionRequestVO);
    		
    		ServiceRegistrationBaseRetrieveVO serviceRegistrationBaseRetrieveVO = new ServiceRegistrationBaseRetrieveVO();
    		serviceRegistrationBaseRetrieveVO.setId(serviceExecutionRequestVO.getServiceRegistrationId());
    		ServiceRegistrationRequestVO serviceRegistrationRequestVO = serviceRegistrationSVC.retrieveService(serviceRegistrationBaseRetrieveVO);
    		
    		if(serviceRegistrationRequestVO == null) {
    			throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Not exists serviceRegistration. id=" + serviceExecutionRequestVO.getId());
    		}

    		// 2. serviceExecution에 해당하는 serviceRegistration 정보 추출하여 매핑
    		Map<ExecutionRequest, Service> executionRequestServiceMap =  matchExecutionRequestAndService(serviceExecutionRequestVO, serviceRegistrationRequestVO);

    		if(serviceExecutionRequestVO.getId() == null) {
    			serviceExecutionRequestVO.setId(makeRandomServiceExecutionId());
    		}

    		// 3. create data (@Transaction - base and detail)
    		ServiceExecutionBaseVO serviceExecutionBaseVO = apiVOToDaoVO(serviceExecutionRequestVO);
        	try {
        		serviceExecutionSVC.createServiceExecution(serviceExecutionBaseVO);
	        } catch (org.springframework.dao.DuplicateKeyException e) {
	            throw new NgsiLdBadRequestException(ErrorCode.ALREADY_EXISTS, "Already Exists. id=" + serviceExecutionRequestVO.getId());
	        }

        	// 4. execute service to endpoint (async)
        	serviceExecutionSVC.executeServiceToEndpoint(serviceExecutionRequestVO, executionRequestServiceMap);

            // 5. response
        	ServiceExecutionResponseVO serviceExecuteResponseVO = new ServiceExecutionResponseVO();
        	serviceExecuteResponseVO.setId(serviceExecutionRequestVO.getId());
            return new ResponseEntity<>(serviceExecuteResponseVO, HttpHeadersUtil.getDefaultHeaders(), HttpStatus.OK);
    }
    
	@GetMapping("/service/execute/{id}")
    public @ResponseBody
    ResponseEntity<ServiceExecutionStatusResponseVO> retrieveServiceExecutionStatus(HttpServletResponse response, @RequestHeader(HttpHeaders.ACCEPT) String accept, @PathVariable String id) throws BaseException {
    	
    	log.info("Retrieve ServiceExecutionStatus. id={}", id);

    	ServiceExecutionRetrieveVO serviceExecutionRetrieveVO = new ServiceExecutionRetrieveVO();
    	serviceExecutionRetrieveVO.setId(id);
    	ServiceExecutionBaseVO serviceExecutionBaseVO = serviceExecutionSVC.retrieveServiceExecution(serviceExecutionRetrieveVO);
    	
    	if(serviceExecutionBaseVO == null) {
    		return new ResponseEntity<>(null, HttpHeadersUtil.getDefaultHeaders(), HttpStatus.OK);
    	}

    	ServiceExecutionStatusResponseVO serviceExecutionStatusResponseVO = new ServiceExecutionStatusResponseVO();
    	serviceExecutionStatusResponseVO.setId(serviceExecutionBaseVO.getId());
    	
    	List<ServiceExecutionStatusVO> executions = new ArrayList<>();
    	for(ServiceExecutionDetailVO serviceExecutionDetailVO : serviceExecutionBaseVO.getServiceExecutionDetailVOs()) {
    		ServiceExecutionStatusVO serviceExecutionStatusVO = new ServiceExecutionStatusVO();
    		serviceExecutionStatusVO.setId(serviceExecutionDetailVO.getExecutionId());
    		serviceExecutionStatusVO.setStatus(serviceExecutionDetailVO.getStatus());
    		executions.add(serviceExecutionStatusVO);
    	}
    	serviceExecutionStatusResponseVO.setExecutions(executions);
    	
        return new ResponseEntity<>(serviceExecutionStatusResponseVO, HttpHeadersUtil.getDefaultHeaders(), HttpStatus.OK);
    }


	private void validateServiceExecuteParam(ServiceExecutionRequestVO serviceExecutionRequestVO) throws NgsiLdBadRequestException {

		// 요청 파라미터 유효성 검사
    	String errorMessage = checkInvalidServiceExecuteParam(serviceExecutionRequestVO);
    	if(!ValidateUtil.isEmptyData(errorMessage)) {
    		throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, errorMessage);
    	}
    }
	
	private Map<ExecutionRequest, Service> matchExecutionRequestAndService(ServiceExecutionRequestVO serviceExecutionRequestVO, ServiceRegistrationRequestVO serviceRegistrationRequestVO) {
		Map<ExecutionRequest, Service> executionRequestServiceMap = new HashMap<>();

    	// ServiceRegistration 시 등록한 파라미터 타입, entityId 등 체크
    	List<RegistrationInfo> information = serviceRegistrationRequestVO.getInformation();

    	List<ExecutionRequest> executions = serviceExecutionRequestVO.getExecutions();
    	for(ExecutionRequest executionRequest : executions) {
    		String entityId = executionRequest.getEntityId();
    		for(RegistrationInfo registrationInfo : information) {
        		List<Service> services = registrationInfo.getServices();
        		for(Service service : services) {
        			
        			// 1. id 매칭여부 확인
        			boolean matchEntity = false;
        			String id = service.getId();
        			String idPattern = service.getIdPattern();
        			String type = service.getType();

        			if(!ValidateUtil.isEmptyData(id)) {
        				if(id.equals(entityId)) {
        					matchEntity = true;
        				}
        			}
        			if(!matchEntity && !ValidateUtil.isEmptyData(idPattern)) {
        				if(Pattern.matches(idPattern, entityId)) {
        					matchEntity = true;
        				}
        			}
        			if(!matchEntity && !ValidateUtil.isEmptyData(type)) {
        				if(type.equals(executionRequest.getEntityType())) {
        					matchEntity = true;
        				}
        			}
        			
        			// 2. entityId 실제 존재 여부 확인
        			EntityDataModelVO entityDataModelVO = entityDataModelSVC.getEntityDataModelVOById(entityId);
        			if(entityDataModelVO == null) {
        				throw new NgsiLdBadRequestException(ErrorCode.NOT_EXIST_ENTITY, "Not exists entity. entityId=" + entityId);
        			}
        			// TODO: entityType 체크 추가

        			if(matchEntity) {
        				// 3. attbitue 매칭 여부 확인
        				Attribute executeInput = executionRequest.getInput();
        				List<AttributeValue> executeAttribs = executeInput.getAttribs();
        				for(AttributeValue executeAttributeValue : executeAttribs) {
        					boolean matchAttribute = false;
        					String executeAttributeName = executeAttributeValue.getAttribname();
        					String executeDatatype = executeAttributeValue.getDatatype();
        					Object executeValue = executeAttributeValue.getValue();

        					Attribute serviceInput = service.getInput();
        					List<AttributeValue> serviceAttribs = serviceInput.getAttribs();
            				for(AttributeValue serviceAttributeValue : serviceAttribs) {
            					String serviceAttributeName = serviceAttributeValue.getAttribname();
            					String serviceDatatype = serviceAttributeValue.getDatatype();
            					Object serviceValue = serviceAttributeValue.getValue();

            					if(executeAttributeName.equals(serviceAttributeName)) {
            						matchAttribute = true;
            						if(!executeDatatype.equals(serviceDatatype)) {
            							throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Invalid service execution datatype. "
            									+ "attributeName=" + executeAttributeName + ", datatype=" + executeDatatype);
            						}
            						// 정상 attribute
            						break;
            					}
            					
            					// TODO: datatype enum 처리. datatype에 따른 value instanceof 로 유효성 체크
            				}
            				
            				if(!matchAttribute) {
            					throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Invalid service execution attributeName. "
            							+ "Not exists in ServiceRegistry. attributeName=" + executeAttributeName);
            				}
            				executionRequestServiceMap.put(executionRequest, service);
        				}
        			} else {
        				throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Invalid service execution entityId. "
    							+ "Not match in ServiceRegistry. entityId=" + entityId);
        			}
        		}
        	}
    	}
    	return executionRequestServiceMap;
	}
    
    private String checkInvalidServiceExecuteParam(ServiceExecutionRequestVO serviceExecutionRequestVO) {
    	List<ExecutionRequest> executions = serviceExecutionRequestVO.getExecutions();
    	if(ValidateUtil.isEmptyData(executions)) return "No exists executions";
    	for(ExecutionRequest executionRequest : executions) {
    		if(ValidateUtil.isEmptyData(executionRequest.getId())) return "No exists executions.id";
    		if(ValidateUtil.isEmptyData(executionRequest.getType())) return "No exists executions.type";
    		if(ValidateUtil.isEmptyData(executionRequest.getEntityId())) return "No exists executions.entityId";
    		if(ValidateUtil.isEmptyData(executionRequest.getEntityType())) return "No exists executions.entityType";
			Attribute input = executionRequest.getInput();
    		if(input == null) return "No exists executions.input";
    		if(ValidateUtil.isEmptyData(input.getType())) return "No exists executions.input.type";
    		if(ValidateUtil.isEmptyData(input.getAttribs())) return "No exists executions.input.attribs";
    		for(AttributeValue attrib : input.getAttribs()) {
    			if(ValidateUtil.isEmptyData(attrib.getAttribname())) return "No exists executions.input.attribs.attribname";
        		if(ValidateUtil.isEmptyData(attrib.getDatatype())) return "No exists executions.input.attribs.datatype";
    		}
    	}
    	return null;
    }

    /**
     * id 없을 경우, 자동 생성
     * @return
     */
    private String makeRandomServiceExecutionId() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String id = Constants.PREFIX_SERVICE_EXECUTION_ID + uuid.substring(0, 10);

        return id;
    }
    
    private ServiceExecutionBaseVO apiVOToDaoVO(ServiceExecutionRequestVO serviceExecuteRequestVO) {
    	ServiceExecutionBaseVO serviceExecutionBaseVO = new ServiceExecutionBaseVO();
    	serviceExecutionBaseVO.setId(serviceExecuteRequestVO.getId());
    	serviceExecutionBaseVO.setServiceId(serviceExecuteRequestVO.getServiceRegistrationId());
    	serviceExecutionBaseVO.setType(serviceExecuteRequestVO.getType());
    	
    	List<ServiceExecutionDetailVO> serviceExecutionDetailVOs = new ArrayList<>();
    	
    	List<ExecutionRequest> executionRequests = serviceExecuteRequestVO.getExecutions();
    	for(ExecutionRequest executionRequest : executionRequests) {
    		ServiceExecutionDetailVO serviceExecutionDetailVO = new ServiceExecutionDetailVO();
    		serviceExecutionDetailVO.setId(serviceExecuteRequestVO.getId());
    		serviceExecutionDetailVO.setServiceId(serviceExecuteRequestVO.getServiceRegistrationId());
    		serviceExecutionDetailVO.setExecutionId(executionRequest.getId());
    		serviceExecutionDetailVO.setName(executionRequest.getName());
    		serviceExecutionDetailVO.setEntityId(executionRequest.getEntityId());
    		serviceExecutionDetailVO.setEntityType(executionRequest.getEntityType());
    		serviceExecutionDetailVO.setType(executionRequest.getType());
    		serviceExecutionDetailVO.setStatus(ServiceExecutionStatus.PENDING);
    		try {
				serviceExecutionDetailVO.setInput(objectMapper.writeValueAsString(executionRequest.getInput()));
			} catch (JsonProcessingException e) {
				throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "ServiceExecution parsing error. executionRequest.innput=" + executionRequest.getInput(), e);
			}
    		serviceExecutionDetailVOs.add(serviceExecutionDetailVO);
    	}

    	serviceExecutionBaseVO.setServiceExecutionDetailVOs(serviceExecutionDetailVOs);

		return serviceExecutionBaseVO;
	}

}
