package kr.re.keti.sc.dataservicebroker.service.dao;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import kr.re.keti.sc.dataservicebroker.service.vo.ServiceExecutionBaseVO;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceExecutionDetailVO;
import kr.re.keti.sc.dataservicebroker.service.vo.ServiceExecutionRetrieveVO;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ServiceExecutionDAO implements ServiceExecutionDAOInterface {

    @Autowired
    private SqlSessionTemplate sqlSession;
    @Autowired
    @Qualifier("retrieveSqlSession")
    private SqlSessionTemplate retrieveSqlSession;
    

	@Override
	public Integer createServiceExecutionBase(ServiceExecutionBaseVO serviceExecutionBaseVO) {
		return sqlSession.update("dataservicebroker.service.execution.createServiceExecutionBase", serviceExecutionBaseVO);
	}
	@Override
	public Integer createServiceExecutionDetail(ServiceExecutionDetailVO serviceExecutionDetailVO) {
		return sqlSession.update("dataservicebroker.service.execution.createServiceExecutionDetail", serviceExecutionDetailVO);
	}
	@Override
	public Integer updateServiceExecutionDetail(ServiceExecutionDetailVO serviceExecutionDetailVO) {
		return sqlSession.update("dataservicebroker.service.execution.updateServiceExecutionDetail", serviceExecutionDetailVO);
	}
	@Override
	public ServiceExecutionBaseVO retrieveServiceExecution(ServiceExecutionRetrieveVO serviceExecutionRetrieveVO) {
		return retrieveSqlSession.selectOne("dataservicebroker.service.execution.retrieveServiceExecution", serviceExecutionRetrieveVO);
	}
}
