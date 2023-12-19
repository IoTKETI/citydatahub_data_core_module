package kr.re.keti.sc.dataservicebroker.service.controller;

import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.dataservicebroker.common.exception.BaseException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdBadRequestException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdResourceNotFoundException;
import kr.re.keti.sc.dataservicebroker.service.service.ServiceRegistrationSVC;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceRegistrationBaseRetrieveVO;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceRegistrationBaseVO;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceRegistrationRequestVO;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceRegistrationRequestVO.Attribute;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceRegistrationRequestVO.AttributeValue;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceRegistrationRequestVO.RegistrationInfo;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceRegistrationRequestVO.Service;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceRegistrationResponseVO;
import kr.re.keti.sc.dataservicebroker.util.HttpHeadersUtil;
import kr.re.keti.sc.dataservicebroker.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class ServiceRegistrationController {

    @Autowired
    private ServiceRegistrationSVC serviceRegistrationSVC;
    @Value("${entity.retrieve.default.limit:1000}")
    private Integer defaultLimit;
    @Autowired
    private ObjectMapper objectMapper;

    
    @PostMapping("/serviceRegistry")
    public ResponseEntity<ServiceRegistrationResponseVO> createService(HttpServletRequest request, HttpServletResponse response,
    		@RequestBody ServiceRegistrationRequestVO serviceRegistrationVO) throws BaseException {

    		log.info("Create service. serviceRegistrationVO={}", serviceRegistrationVO);
    	
        	// 1. validation
    		validateServiceRegistryParam(serviceRegistrationVO);

            // id가 없는 경우 생성
            if (serviceRegistrationVO.getId() == null) {
                serviceRegistrationVO.setId(makeRandomServiceRegistrationId());
            }

            // 2. serviceRegistrationBaseVO 생성
            ServiceRegistrationBaseVO serviceRegistrationBaseVO = new ServiceRegistrationBaseVO();
        	serviceRegistrationBaseVO.setId(serviceRegistrationVO.getId());
        	serviceRegistrationBaseVO.setName(serviceRegistrationVO.getName());
        	serviceRegistrationBaseVO.setDescription(serviceRegistrationVO.getDescription());
        	try {
        		serviceRegistrationBaseVO.setInformation(objectMapper.writeValueAsString(serviceRegistrationVO.getInformation()));
    		} catch (JsonProcessingException e) {
    			throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Invalid information parameter. information=" + serviceRegistrationVO.getInformation(), e);
    		}
        	
        	try {
        		serviceRegistrationSVC.createService(serviceRegistrationBaseVO);
	        } catch (org.springframework.dao.DuplicateKeyException e) {
	            throw new NgsiLdBadRequestException(ErrorCode.ALREADY_EXISTS, "Already Exists. serviceRegistrationVOID=" + serviceRegistrationVO.getId());
	        }

            // 3. response
        	ServiceRegistrationResponseVO serviceRegistrationResponseVO = new ServiceRegistrationResponseVO();
        	serviceRegistrationResponseVO.setId(serviceRegistrationVO.getId());
            return new ResponseEntity<>(serviceRegistrationResponseVO, HttpHeadersUtil.getDefaultHeaders(), HttpStatus.OK);
    }

    @PutMapping("/serviceRegistry/{id}")
    public ResponseEntity<Void> updateService(HttpServletRequest request, HttpServletResponse response,
    		@RequestBody ServiceRegistrationRequestVO serviceRegistrationVO, @PathVariable String id) throws BaseException {

		log.info("Update Service. serviceRegistrationVO={}", serviceRegistrationVO);

		// 1. validation
		serviceRegistrationVO.setId(id);
		validateServiceRegistryParam(serviceRegistrationVO);

    	ServiceRegistrationBaseRetrieveVO retrieveVO = new ServiceRegistrationBaseRetrieveVO();
    	retrieveVO.setId(id);
    	if(serviceRegistrationSVC.retrieveService(retrieveVO) == null) {
    		throw new NgsiLdResourceNotFoundException(ErrorCode.NOT_EXIST_ID, "There is no an existing Service which id");
    	}

        // 2. serviceRegistrationBaseVO 업데이트
        ServiceRegistrationBaseVO serviceRegistrationBaseVO = new ServiceRegistrationBaseVO();
    	serviceRegistrationBaseVO.setId(serviceRegistrationVO.getId());
    	serviceRegistrationBaseVO.setName(serviceRegistrationVO.getName());
    	serviceRegistrationBaseVO.setDescription(serviceRegistrationVO.getDescription());
    	try {
			serviceRegistrationBaseVO.setInformation(objectMapper.writeValueAsString(serviceRegistrationVO.getInformation()));
		} catch (JsonProcessingException e) {
			throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Invalid information parameter. information=" + serviceRegistrationVO.getInformation(), e);
		}

    	int result = serviceRegistrationSVC.updateService(serviceRegistrationBaseVO);
    	if(result == 0) {
    		throw new NgsiLdResourceNotFoundException(ErrorCode.NOT_EXIST_ID, "There is no an existing Service which id");
    	}

        // 3. response
        return new ResponseEntity<>(HttpHeadersUtil.getDefaultHeaders(), HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/serviceRegistry/{id}")
    public ResponseEntity<Void> deleteService(HttpServletRequest request, HttpServletResponse response, @PathVariable String id) throws BaseException {

    	log.info("Delete Service. id={}", id);
    	
        // 1. validation
    	ServiceRegistrationBaseRetrieveVO retrieveVO = new ServiceRegistrationBaseRetrieveVO();
    	retrieveVO.setId(id);
    	if(serviceRegistrationSVC.retrieveService(retrieveVO) == null) {
    		throw new NgsiLdResourceNotFoundException(ErrorCode.NOT_EXIST_ID, "There is no an existing Service which id");
    	}

        // 2. csourceRegistrations 생성 요청
    	int result = serviceRegistrationSVC.deleteService(retrieveVO);
        if(result == 0) {
    		throw new NgsiLdResourceNotFoundException(ErrorCode.NOT_EXIST_ID, "There is no an existing Service which id");
    	}
        
        // 3. response
        return new ResponseEntity<>(HttpHeadersUtil.getDefaultHeaders(), HttpStatus.NO_CONTENT);
    }


    @GetMapping(value = "/serviceRegistry/{id}")
    public @ResponseBody
    ResponseEntity<ServiceRegistrationRequestVO> retrieveService(HttpServletResponse response, @RequestHeader(HttpHeaders.ACCEPT) String accept, @PathVariable String id) throws BaseException {
    	
    	log.info("Retrieve Service. id={}", id);
    	
    	ServiceRegistrationBaseRetrieveVO retrieveVO = new ServiceRegistrationBaseRetrieveVO();
    	retrieveVO.setId(id);
    	ServiceRegistrationRequestVO serviceRegistrationRequestVO = serviceRegistrationSVC.retrieveService(retrieveVO);
    	
    	if(serviceRegistrationRequestVO == null) {
    		return new ResponseEntity<>(null, HttpHeadersUtil.getDefaultHeaders(), HttpStatus.OK);
    	}
    	
        return new ResponseEntity<>(serviceRegistrationRequestVO, HttpHeadersUtil.getDefaultHeaders(), HttpStatus.OK);
    }

    @GetMapping(value = "/serviceRegistry")
    public @ResponseBody
    ResponseEntity<List<ServiceRegistrationRequestVO>> retrieveServices(HttpServletRequest request, HttpServletResponse response, @RequestHeader(HttpHeaders.ACCEPT) String accept) throws BaseException {

    	log.info("Retrieve Services.");
    	
        // 1. subscription 조회
    	ServiceRegistrationBaseRetrieveVO retrieveVO = new ServiceRegistrationBaseRetrieveVO();
        Integer totalCount = serviceRegistrationSVC.retrieveServiceCount(retrieveVO); // TODO: 응답에 추가. 규격 정의 필요
        List<ServiceRegistrationRequestVO> serviceRegistrationRequestVOs = serviceRegistrationSVC.retrieveServiceList(retrieveVO);

        return new ResponseEntity<>(serviceRegistrationRequestVOs, HttpHeadersUtil.getDefaultHeaders(), HttpStatus.OK);

    }
    
    private boolean validateServiceRegistryParam(ServiceRegistrationRequestVO serviceRegistrationVO) throws NgsiLdBadRequestException {
    	String errorMessage = checkInvalidServiceRegistryParam(serviceRegistrationVO);
    	if(ValidateUtil.isEmptyData(errorMessage)) {
    		return true;
    	} else {
    		throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, errorMessage);
    	}
    }
    
    private String checkInvalidServiceRegistryParam(ServiceRegistrationRequestVO serviceRegistrationVO) {
    	List<RegistrationInfo> information = serviceRegistrationVO.getInformation();
    	if(ValidateUtil.isEmptyData(information)) return "No exists information";
    	for(RegistrationInfo registrationInfo : information) {
    		List<Service> services = registrationInfo.getServices();
    		if(ValidateUtil.isEmptyData(services)) return "No exists information.services";
    		for(Service service : services) {
    			Attribute input = service.getInput();
        		if(input == null) return "No exists information.service.input";
        		if(ValidateUtil.isEmptyData(input.getType())) return "No exists information.service.input.type";
        		if(ValidateUtil.isEmptyData(input.getAttribs())) return "No exists information.service.input.attribs";
        		for(AttributeValue attrib : input.getAttribs()) {
        			if(ValidateUtil.isEmptyData(attrib.getAttribname())) return "No exists information.service.input.attribs.attribname";
            		if(ValidateUtil.isEmptyData(attrib.getDatatype())) return "No exists information.service.input.attribs.datatype";
        		}
    		}
    	}
    	return null;
    }

    

    /**
     * id 없을 경우, 자동 생성
     * @return
     */
    private String makeRandomServiceRegistrationId() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String id = Constants.PREFIX_SERVICE_REGISTRATION_ID + uuid.substring(0, 10);

        return id;
    }
    
}
