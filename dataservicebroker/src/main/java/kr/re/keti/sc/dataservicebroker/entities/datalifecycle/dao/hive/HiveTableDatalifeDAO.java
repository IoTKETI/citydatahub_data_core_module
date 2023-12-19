package kr.re.keti.sc.dataservicebroker.entities.datalifecycle.dao.hive;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Repository
@ConditionalOnProperty(value="datasource.hive.use.yn", havingValue = "Y", matchIfMissing = false)
public class HiveTableDatalifeDAO {

    @Autowired
    @Qualifier("hiveSqlSession")
    private SqlSessionTemplate sqlSession;

    public int deleteEntity(String tableName, String datasetId, Date lifeCycleDate) {

        Map<String, Object> param = new HashMap<>();
        param.put("tableName", tableName);
        param.put("datasetId", datasetId);
        param.put("lifeCycleDate", lifeCycleDate);

    	return sqlSession.delete("dataservicebroker.hive.deleteEntity", param);
    }
}
