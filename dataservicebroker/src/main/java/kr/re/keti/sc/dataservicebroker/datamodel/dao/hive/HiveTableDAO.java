package kr.re.keti.sc.dataservicebroker.datamodel.dao.hive;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@ConditionalOnProperty(value="datasource.hive.use.yn", havingValue = "Y", matchIfMissing = false)
public class HiveTableDAO {

    @Autowired
    @Qualifier("hiveSqlSession")
    private SqlSessionTemplate sqlSession;

    public int createTable(String ddl) {
    	return sqlSession.update("dataservicebroker.hive.createTable", ddl);
    }
    public int dropTable(String ddl) {
    	return sqlSession.update("dataservicebroker.hive.dropTable", ddl);
    }
    public int cacheTable(String ddl) {
    	return sqlSession.update("dataservicebroker.hive.cacheTable", ddl);
    }

    public int updateTableScheme() {
        return sqlSession.update("dataservicebroker.hive.updateTableScheme");
    }

    public List<String> getTableScheme(String tableName) {
        List<String> colNamesWithTableInfo = sqlSession.selectList("dataservicebroker.hive.getTableScheme", tableName);
        int splitPoint = 0;

        for (int i = 0; i < colNamesWithTableInfo.size(); i ++) {
            String colNm = colNamesWithTableInfo.get(i);

            if (colNm.isEmpty()) {
                splitPoint = i;
                break;
            }
        }

        if(splitPoint == 0) //Describe Table 명령어에 별도의 테이블에 대한 정의가 없이 컬럼 정보만 있을 때
            return colNamesWithTableInfo;
        else // 파티션 정보가 포함되어 column 리스트 추출이 필요할 때
            return colNamesWithTableInfo.subList(0, splitPoint);
    }

    public String getIndex(String columnName, String tableName, String id) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("columnName", columnName);
        paramMap.put("tableName", tableName);
        paramMap.put("id", id);
        return sqlSession.selectOne("dataservicebroker.hive.getIndex", paramMap);
    }

    public int getCount(String tableName, String id) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("tableName", tableName);
        paramMap.put("id", id);
        return sqlSession.selectOne("dataservicebroker.hive.getCount", paramMap);
    }
}
