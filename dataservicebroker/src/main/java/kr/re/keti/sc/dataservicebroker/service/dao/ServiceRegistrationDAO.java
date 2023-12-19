package kr.re.keti.sc.dataservicebroker.service.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.dataservicebroker.service.vo.ServiceRegistrationBaseRetrieveVO;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceRegistrationBaseVO;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ServiceRegistrationDAO implements ServiceRegistrationDAOInterface {

    @Autowired
    private SqlSessionTemplate sqlSession;
    @Autowired
    @Qualifier("retrieveSqlSession")
    private SqlSessionTemplate retrieveSqlSession;
    

	@Override
	public Integer createService(ServiceRegistrationBaseVO serviceRegistrationBaseVO) {
		return sqlSession.update("dataservicebroker.service.registration.createService", serviceRegistrationBaseVO);
	}
	@Override
	public Integer updateService(ServiceRegistrationBaseVO serviceRegistrationBaseVO) {
		return sqlSession.update("dataservicebroker.service.registration.updateService", serviceRegistrationBaseVO);
	}
	@Override
	public Integer deleteService(ServiceRegistrationBaseVO serviceRegistrationBaseVO) {
		return sqlSession.update("dataservicebroker.service.registration.deleteService", serviceRegistrationBaseVO);
	}
	@Override
	public ServiceRegistrationBaseVO retrieveService(ServiceRegistrationBaseRetrieveVO serviceRegistrationBaseRetrieveVO) {
		return retrieveSqlSession.selectOne("dataservicebroker.service.registration.retrieveService", serviceRegistrationBaseRetrieveVO);
	}
	@Override
	public Integer retrieveServiceCount(ServiceRegistrationBaseRetrieveVO serviceRegistrationBaseRetrieveVO) {
		return retrieveSqlSession.selectOne("dataservicebroker.service.registration.retrieveServiceCount", serviceRegistrationBaseRetrieveVO);
	}
	@Override
	public List<ServiceRegistrationBaseVO> retrieveServiceList(ServiceRegistrationBaseRetrieveVO serviceRegistrationBaseRetrieveVO) {
		return retrieveSqlSession.selectList("dataservicebroker.service.registration.retrieveServiceList", serviceRegistrationBaseRetrieveVO);
	}

}
