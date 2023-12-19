package kr.re.keti.sc.dataservicebroker.entities.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.re.keti.sc.dataservicebroker.entities.vo.EntityDataModelVO;

@Repository
public class EntityDataModelDAO {

	@Autowired
    private SqlSessionTemplate sqlSession;

    public int createEntityDataModel(EntityDataModelVO entityDataModelVO) {
        return sqlSession.update("dataservicebroker.entityDataModel.createEntityDataModel", entityDataModelVO);
    }

    public int updateEntityDataModel(EntityDataModelVO entityDataModelVO) {
        return sqlSession.update("dataservicebroker.entityDataModel.updateEntityDataModel", entityDataModelVO);
    }

    public int deleteEntityDataModel(String id) {
        return sqlSession.update("dataservicebroker.entityDataModel.deleteEntityDataModel", id);
    }

    public List<EntityDataModelVO> getEntityDataModelVOList() {
        return sqlSession.selectList("dataservicebroker.entityDataModel.selectAll");
    }

    public EntityDataModelVO getEntityDataModelVOById(String id) {
        return sqlSession.selectOne("dataservicebroker.entityDataModel.selectById", id);
    }

    public void setSqlSession(SqlSessionTemplate sqlSession) {
        this.sqlSession = sqlSession;
    }
}
