package kr.re.keti.sc.datamanager.datamodel.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.re.keti.sc.datamanager.datamodel.vo.DataModelBaseVO;

/**
 * DataModel repository access Class
 */
@Repository
public class DataModelDAO {

	@Autowired
	private SqlSessionTemplate sqlSession;

	public List<DataModelBaseVO> getDataModelBaseVOList(DataModelBaseVO dataModelBaseVO) {
		return sqlSession.selectList("datamanager.datamodel.selectAll", dataModelBaseVO);
    }

	public Integer getDataModelBaseVOListTotalCount(DataModelBaseVO dataModelBaseVO) {
		return sqlSession.selectOne("datamanager.datamodel.selectTotalCount", dataModelBaseVO);
	}

    public DataModelBaseVO getDataModelBaseVOById(DataModelBaseVO dataModelBaseVO) {
    	return sqlSession.selectOne("datamanager.datamodel.selectById", dataModelBaseVO);
    }

    public int createDataModelBaseVO(DataModelBaseVO dataModelBaseVO) {
    	return sqlSession.update("datamanager.datamodel.createDataModelBase", dataModelBaseVO);
    }

	public int updateDataModelBase(DataModelBaseVO dataModelBaseVO) {
		return sqlSession.update("datamanager.datamodel.updateDataModelBase", dataModelBaseVO);
	}
	
	public int updateDataModelProvisioning(DataModelBaseVO dataModelBaseVO) {
		return sqlSession.update("datamanager.datamodel.updateDataModelProvisioning", dataModelBaseVO);
	}

    public int deleteDataModelBaseVO(DataModelBaseVO dataModelBaseVO) {
    	return sqlSession.update("datamanager.datamodel.deleteDataModelBase", dataModelBaseVO);
    }

	public List<String> getDeduplicatedDataModelNamespaces() {
		return sqlSession.selectList("datamanager.datamodel.selectDeduplicatedDataModelNamespaces");
	}

	public List<String> getDeduplicatedDataModelType(DataModelBaseVO dataModelBaseVO) {
		return sqlSession.selectList("datamanager.datamodel.selectDeduplicatedDataModelType", dataModelBaseVO);
	}

	public List<String> getDeduplicatedDataModelVersion(DataModelBaseVO dataModelBaseVO) {
		return sqlSession.selectList("datamanager.datamodel.selectDeduplicatedDataModelVersion", dataModelBaseVO);
	}

	public void setSqlSession(SqlSessionTemplate sqlSession) {
		this.sqlSession = sqlSession;
	}
}
