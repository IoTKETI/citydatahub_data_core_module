package kr.re.keti.sc.dataservicebroker.service.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdBadRequestException;
import kr.re.keti.sc.dataservicebroker.service.dao.ServiceRegistrationDAO;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceExecutionBaseVO;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceExecutionRequestVO;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceRegistrationBaseRetrieveVO;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceRegistrationBaseVO;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceRegistrationRequestVO;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceRegistrationRequestVO.RegistrationInfo;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServiceRegistrationSVC {

    @Autowired
    protected ServiceRegistrationDAO serviceRegistrationDAO;
    @Autowired
    private ObjectMapper objectMapper;

	public Integer createService(ServiceRegistrationBaseVO serviceRegistrationBaseVO) {
		return serviceRegistrationDAO.createService(serviceRegistrationBaseVO);
	}
	public Integer updateService(ServiceRegistrationBaseVO serviceRegistrationBaseVO) {
		return serviceRegistrationDAO.updateService(serviceRegistrationBaseVO);
	}
	public Integer deleteService(ServiceRegistrationBaseVO serviceRegistrationBaseVO) {
		return serviceRegistrationDAO.deleteService(serviceRegistrationBaseVO);
	}
	public ServiceRegistrationRequestVO retrieveService(ServiceRegistrationBaseRetrieveVO serviceRegistrationBaseRetrieveVO) {
		ServiceRegistrationBaseVO serviceRegistrationBaseVO = serviceRegistrationDAO.retrieveService(serviceRegistrationBaseRetrieveVO);
		if(serviceRegistrationBaseVO == null) {
			return null;
		}
		return daoVOtoApiVO(serviceRegistrationBaseVO);
	}

	public Integer retrieveServiceCount(ServiceRegistrationBaseRetrieveVO serviceRegistrationBaseRetrieveVO) {
		return serviceRegistrationDAO.retrieveServiceCount(serviceRegistrationBaseRetrieveVO);
	}

	public List<ServiceRegistrationRequestVO> retrieveServiceList(ServiceRegistrationBaseRetrieveVO serviceRegistrationBaseRetrieveVO) {
		List<ServiceRegistrationBaseVO> serviceRegistrationBaseVOs = serviceRegistrationDAO.retrieveServiceList(serviceRegistrationBaseRetrieveVO);

		List<ServiceRegistrationRequestVO> serviceRegistrationVOs = null;
        if(serviceRegistrationBaseVOs != null && serviceRegistrationBaseVOs.size() > 0) {
        	serviceRegistrationVOs = new ArrayList<>();
        	for(ServiceRegistrationBaseVO serviceRegistrationBaseVO : serviceRegistrationBaseVOs) {
        		ServiceRegistrationRequestVO serviceRegistrationVO = daoVOtoApiVO(serviceRegistrationBaseVO);
        		serviceRegistrationVOs.add(serviceRegistrationVO);
        	}
        }
        return serviceRegistrationVOs;
	}
	
	private ServiceRegistrationRequestVO daoVOtoApiVO(ServiceRegistrationBaseVO serviceRegistrationBaseVO) throws NgsiLdBadRequestException {

    	ServiceRegistrationRequestVO serviceRegistrationVO = new ServiceRegistrationRequestVO();
    	serviceRegistrationVO.setId(serviceRegistrationBaseVO.getId());
    	serviceRegistrationVO.setName(serviceRegistrationBaseVO.getName());
    	serviceRegistrationVO.setDescription(serviceRegistrationBaseVO.getDescription());
    	try {
    		serviceRegistrationVO.setInformation(objectMapper.readValue(serviceRegistrationBaseVO.getInformation(), new TypeReference<ArrayList<RegistrationInfo>>() {}));
		} catch (JsonProcessingException e) {
			throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Invalid information parameter. information=" + serviceRegistrationVO.getInformation(), e);
		}
    	return serviceRegistrationVO;
    }

//	private ServiceRegistrationBaseVO apiVOToDaoVO(ServiceRegistrationRequestVO serviceRegistrationRequestVO) throws NgsiLdBadRequestException {
//
//		ServiceRegistrationBaseVO serviceRegistrationBaseVO = new ServiceRegistrationBaseVO();
//		serviceRegistrationBaseVO.setId(serviceRegistrationRequestVO.getId());
//		serviceRegistrationBaseVO.setName(serviceRegistrationRequestVO.getName());
//		serviceRegistrationBaseVO.setDescription(serviceRegistrationRequestVO.getDescription());
//		try {
//			serviceRegistrationBaseVO.setInformation(objectMapper.writeValueAsString(serviceRegistrationRequestVO.getInformation()));
//		} catch (JsonProcessingException e) {
//			throw new NgsiLdBadRequestException(ErrorCode.UNKNOWN_ERROR, "Internal parsing error. information=" + serviceRegistrationRequestVO.getInformation(), e);
//		}
//		return serviceRegistrationBaseVO;
//    }
}