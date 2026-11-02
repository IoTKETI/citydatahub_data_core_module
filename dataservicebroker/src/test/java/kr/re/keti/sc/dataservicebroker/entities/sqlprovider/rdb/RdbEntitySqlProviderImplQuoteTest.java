package kr.re.keti.sc.dataservicebroker.entities.sqlprovider.rdb;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.DbColumnType;
import kr.re.keti.sc.dataservicebroker.common.vo.entities.DynamicEntityDaoVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelDbColumnVO;

class RdbEntitySqlProviderImplQuoteTest {

    private final RdbEntitySqlProviderImpl provider = new RdbEntitySqlProviderImpl();

    private DynamicEntityDaoVO daoVO() {
        DataModelDbColumnVO col = new DataModelDbColumnVO();
        col.setHierarchyAttributeIds(Arrays.asList("5daysRainfallMax"));
        col.setDaoAttributeId("5daysrainfallmax");
        col.setColumnName("5daysrainfallmax");
        col.setColumnType(DbColumnType.FLOAT);
        Map<String, DataModelDbColumnVO> map = new LinkedHashMap<>();
        map.put("5daysrainfallmax", col);

        DynamicEntityDaoVO vo = new DynamicEntityDaoVO();
        vo.setDbTableName("test_table");
        vo.setDbColumnInfoVOMap(map);
        vo.put("5daysrainfallmax", new BigDecimal("12.5"));
        return vo;
    }

    @Test
    void insertQuotesDynamicColumnButNotFixedOnes() {
        String sql = provider.create(daoVO());
        assertTrue(sql.contains("\"5daysrainfallmax\""), sql);
        assertTrue(sql.contains("#{5daysrainfallmax}"), sql);   // 파라미터 키는 그대로
        assertFalse(sql.contains("\"ID\""), sql);               // 고정 컬럼은 따옴표 없음
    }

    @Test
    void updateQuotesDynamicColumn() {
        String sql = provider.appendAttr(daoVO());
        assertTrue(sql.contains("\"5daysrainfallmax\" = #{5daysrainfallmax"), sql);
    }

    @Test
    void fullHistQuotesIdentifierButKeepsParameterKey() {
        String sql = provider.createFullHist(daoVO());
        assertTrue(sql.contains("\"5daysrainfallmax\""), sql);
        assertTrue(sql.contains("#{5daysrainfallmax}"), sql);
        assertFalse(sql.contains("#{\"5daysrainfallmax\"}"), sql);
    }
}
