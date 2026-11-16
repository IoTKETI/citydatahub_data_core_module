package kr.re.keti.sc.dataservicebroker.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.AttributeType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.AttributeValueType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.DbColumnType;
import kr.re.keti.sc.dataservicebroker.common.vo.QueryVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.Attribute;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelCacheVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelDbColumnVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelStorageMetadataVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelVO;

class QueryUtilQuoteTest {

    private DataModelCacheVO cache() {
        Attribute attr = new Attribute();
        attr.setName("5daysRainfallMax");
        attr.setAttributeType(AttributeType.PROPERTY);
        attr.setValueType(AttributeValueType.DOUBLE);

        DataModelVO dataModelVO = new DataModelVO();
        dataModelVO.setAttributes(Collections.singletonList(attr));

        DataModelDbColumnVO col = new DataModelDbColumnVO();
        col.setHierarchyAttributeIds(Arrays.asList("5daysRainfallMax"));
        col.setDaoAttributeId("5daysrainfallmax");
        col.setColumnName("5daysrainfallmax");
        col.setColumnType(DbColumnType.FLOAT);

        DataModelStorageMetadataVO storage = new DataModelStorageMetadataVO();
        storage.getDbColumnInfoVOMap().put("5daysrainfallmax", col);

        DataModelCacheVO cacheVO = new DataModelCacheVO();
        cacheVO.setDataModelVO(dataModelVO);
        cacheVO.setDataModelStorageMetadataVO(storage);
        return cacheVO;
    }

    private String query(String q, boolean quote) {
        QueryVO queryVO = new QueryVO();
        queryVO.setQ(q);
        return QueryUtil.generateQuery(cache(), queryVO, quote).getQuery();
    }

    @Test
    void rdbQuotesColumn() {
        assertEquals("(\"5daysrainfallmax\" > 3)", query("5daysRainfallMax>3", true));
        assertEquals("(\"5daysrainfallmax\" = 3)", query("5daysRainfallMax==3", true));
        assertEquals("(\"5daysrainfallmax\" BETWEEN 1 AND 3)", query("5daysRainfallMax==1..3", true));
    }

    @Test
    void defaultOverloadKeepsOldBehaviour() {
        // Hive 경로가 쓰는 2인자 메서드는 예전 그대로 (Hive 에서 큰따옴표는 문자열 리터럴)
        QueryVO queryVO = new QueryVO();
        queryVO.setQ("5daysRainfallMax>3");
        assertEquals("(5daysrainfallmax > 3)", QueryUtil.generateQuery(cache(), queryVO).getQuery());
    }
}
