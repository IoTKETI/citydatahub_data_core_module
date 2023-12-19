package kr.re.keti.sc.ingestinterface.datamodel.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.re.keti.sc.ingestinterface.datamodel.vo.DataModelBaseVO;

/**
 * Data model repository access class
 */
@Repository
public class DataModelDAO {

	@Autowired
	private SqlSessionTemplate sqlSession;

	public List<DataModelBaseVO> getDataModelBaseVOList() {
		return sqlSession.selectList("dataservicebroker.datamodel.selectAll");
    }

    public DataModelBaseVO getDataModelBaseVOById(DataModelBaseVO dataModelBaseVO) {
    	return sqlSession.selectOne("dataservicebroker.datamodel.selectById", dataModelBaseVO);
    }

    public int createDataModelBaseVO(DataModelBaseVO dataModelBaseVO) {
    	return sqlSession.update("dataservicebroker.datamodel.createDataModelBase", dataModelBaseVO);
    }

	public int updateDataModelBase(DataModelBaseVO dataModelBaseVO) {
		return sqlSession.update("dataservicebroker.datamodel.updateDataModelBase", dataModelBaseVO);
	}

    public int deleteDataModelBaseVO(DataModelBaseVO dataModelBaseVO) {
    	return sqlSession.update("dataservicebroker.datamodel.deleteDataModelBase", dataModelBaseVO);
    }

	public void setSqlSession(SqlSessionTemplate sqlSession) {
		this.sqlSession = sqlSession;
	}
}
