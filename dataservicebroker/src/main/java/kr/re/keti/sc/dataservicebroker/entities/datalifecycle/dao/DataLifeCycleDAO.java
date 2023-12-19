package kr.re.keti.sc.dataservicebroker.entities.datalifecycle.dao;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Repository
public class DataLifeCycleDAO {

    @Autowired
    private SqlSessionTemplate sqlSession;

    public int deleteEntity(String tableName, String datasetId, Date lifeCycleDate) {

        Map<String, Object> param = new HashMap<>();
        param.put("tableName", tableName);
        param.put("datasetId", datasetId);
        param.put("lifeCycleDate", lifeCycleDate);

        return sqlSession.update("dataservicebroker.datalifecycle.deleteEntity", param);
    }

}
