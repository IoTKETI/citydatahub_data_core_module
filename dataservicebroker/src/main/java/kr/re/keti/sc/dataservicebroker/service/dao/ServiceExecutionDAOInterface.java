package kr.re.keti.sc.dataservicebroker.service.dao;

import kr.re.keti.sc.dataservicebroker.service.vo.ServiceExecutionBaseVO;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceExecutionDetailVO;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceExecutionRetrieveVO;

public interface ServiceExecutionDAOInterface {

	public Integer createServiceExecutionBase(ServiceExecutionBaseVO serviceExecutionBaseVO);

	public Integer createServiceExecutionDetail(ServiceExecutionDetailVO serviceExecutionDetailVO);

	public Integer updateServiceExecutionDetail(ServiceExecutionDetailVO serviceExecutionDetailVO);
    
	public ServiceExecutionBaseVO retrieveServiceExecution(ServiceExecutionRetrieveVO serviceExecutionRetrieveVO);

}
