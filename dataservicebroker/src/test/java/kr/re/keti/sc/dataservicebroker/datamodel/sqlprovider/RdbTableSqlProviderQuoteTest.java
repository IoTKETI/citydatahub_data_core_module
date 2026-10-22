package kr.re.keti.sc.dataservicebroker.datamodel.sqlprovider;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.DbColumnType;
import kr.re.keti.sc.dataservicebroker.datamodel.DataModelManager;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelDbColumnVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelStorageMetadataVO;

class RdbTableSqlProviderQuoteTest {

    private RdbTableSqlProvider provider() {
        DataModelManager manager = mock(DataModelManager.class);
        when(manager.generateRdbTableName(anyString())).thenReturn("tbl");
        when(manager.generateRdbTableName(anyString(), any())).thenReturn("tbl_hist");
        RdbTableSqlProvider provider = new RdbTableSqlProvider();
        ReflectionTestUtils.setField(provider, "dataModelManager", manager);
        return provider;
    }

    private DataModelDbColumnVO col(String name, DbColumnType type) {
        DataModelDbColumnVO col = new DataModelDbColumnVO();
        col.setHierarchyAttributeIds(Arrays.asList(name));
        col.setDaoAttributeId(name);
        col.setColumnName(name);
        col.setColumnType(type);
        return col;
    }

    @Test
    void addColumnIsQuoted() {
        DataModelStorageMetadataVO before = new DataModelStorageMetadataVO();
        before.getDbColumnInfoVOMap().put("temperature", col("temperature", DbColumnType.FLOAT));
        DataModelStorageMetadataVO after = new DataModelStorageMetadataVO();
        after.getDbColumnInfoVOMap().put("temperature", col("temperature", DbColumnType.FLOAT));
        after.getDbColumnInfoVOMap().put("5daysrainfallmax", col("5daysrainfallmax", DbColumnType.FLOAT));

        String ddl = provider().generateAlterTableColumnDdl("model", before, after);
        assertTrue(ddl.contains("ADD COLUMN \"5daysrainfallmax\" NUMERIC"), ddl);
        assertFalse(ddl.contains("ADD COLUMN 5days"), ddl);
    }

    @Test
    void alterAndDropColumnAreQuoted() {
        DataModelStorageMetadataVO before = new DataModelStorageMetadataVO();
        before.getDbColumnInfoVOMap().put("5daysrainfallmax", col("5daysrainfallmax", DbColumnType.INTEGER));
        before.getDbColumnInfoVOMap().put("7daysrainfallmax", col("7daysrainfallmax", DbColumnType.FLOAT));
        DataModelStorageMetadataVO after = new DataModelStorageMetadataVO();
        after.getDbColumnInfoVOMap().put("5daysrainfallmax", col("5daysrainfallmax", DbColumnType.FLOAT));

        String ddl = provider().generateAlterTableColumnDdl("model", before, after);
        assertTrue(ddl.contains("ALTER COLUMN \"5daysrainfallmax\" TYPE NUMERIC USING \"5daysrainfallmax\"::NUMERIC"), ddl);
        assertTrue(ddl.contains("DROP COLUMN IF EXISTS \"7daysrainfallmax\""), ddl);
    }
}
