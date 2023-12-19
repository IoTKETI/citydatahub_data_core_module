package kr.re.keti.sc.dataservicebroker.service.dao;

import java.util.List;

import kr.re.keti.sc.dataservicebroker.service.vo.ServiceRegistrationBaseRetrieveVO;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceRegistrationBaseVO;

public interface ServiceRegistrationDAOInterface {

	public Integer createService(ServiceRegistrationBaseVO serviceRegistrationBaseVO);
	
	public Integer updateService(ServiceRegistrationBaseVO serviceRegistrationBaseVO);
    
    public Integer deleteService(ServiceRegistrationBaseVO serviceRegistrationBaseVO);
    
	public ServiceRegistrationBaseVO retrieveService(ServiceRegistrationBaseRetrieveVO serviceRegistrationBaseRetrieveVO);

    public Integer retrieveServiceCount(ServiceRegistrationBaseRetrieveVO serviceRegistrationBaseRetrieveVO);
    
    public List<ServiceRegistrationBaseVO> retrieveServiceList(ServiceRegistrationBaseRetrieveVO serviceRegistrationBaseRetrieveVO);
    
}
