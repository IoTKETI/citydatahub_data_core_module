package kr.re.keti.sc.dataservicebroker.datamodel.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.StorageType;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelBaseVO;
import kr.re.keti.sc.dataservicebroker.entities.sqlprovider.hive.HiveEntitySqlProvider;
import kr.re.keti.sc.dataservicebroker.entities.sqlprovider.rdb.RdbEntitySqlProvider;

@Repository
public class DataModelDAO {

	@Autowired
	private SqlSessionTemplate sqlSession;
	
	@Autowired(required = false)
    @Qualifier("hiveSqlSession")
    private SqlSessionTemplate hiveSqlSession;

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

	public int updateDataModelStorage(DataModelBaseVO dataModelBaseVO) {
		return sqlSession.update("dataservicebroker.datamodel.updateDataModelStorage", dataModelBaseVO);
	}

    public int deleteDataModelBaseVO(DataModelBaseVO dataModelBaseVO) {
    	return sqlSession.update("dataservicebroker.datamodel.deleteDataModelBase", dataModelBaseVO);
    }

    public void executeDdl(String ddl, StorageType storageType) {
    	if(storageType == StorageType.HIVE) {
    		if(hiveSqlSession != null) {
    			HiveEntitySqlProvider mapper = hiveSqlSession.getMapper(HiveEntitySqlProvider.class);
            	mapper.executeDdl(ddl);
    		}
    	} else {
    		RdbEntitySqlProvider mapper = sqlSession.getMapper(RdbEntitySqlProvider.class);
        	mapper.executeDdl(ddl);
    	}
    }

	public void setSqlSession(SqlSessionTemplate sqlSession) {
		this.sqlSession = sqlSession;
	}
}
