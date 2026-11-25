package kr.re.keti.sc.dataservicebroker.entities.sqlprovider.rdb;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.DbColumnType;
import kr.re.keti.sc.dataservicebroker.common.vo.entities.DynamicEntityDaoVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelDbColumnVO;

/**
 * 속성 삭제(deleteAttr) SQL 생성 검증
 * - 삭제 대상 속성의 하위 컬럼(값, observedAt, createdAt, modifiedAt)만 NULL 처리
 * - 존재하지 않는 컬럼(예: depth_modified_at)을 참조하지 않음
 */
class RdbEntitySqlProviderImplDeleteAttrTest {

    private final RdbEntitySqlProviderImpl provider = new RdbEntitySqlProviderImpl();

    private DataModelDbColumnVO column(String columnName, DbColumnType type, String... hierarchy) {
        DataModelDbColumnVO col = new DataModelDbColumnVO();
        col.setHierarchyAttributeIds(Arrays.asList(hierarchy));
        col.setDaoAttributeId(columnName);
        col.setColumnName(columnName);
        col.setColumnType(type);
        return col;
    }

    private DynamicEntityDaoVO daoVO(String deleteAttrId) {
        List<DataModelDbColumnVO> columns = Arrays.asList(
                column("depth", DbColumnType.FLOAT, "depth"),
                column("depth_observedat", DbColumnType.TIMESTAMP, "depth", "observedAt"),
                column("depth_createdat", DbColumnType.TIMESTAMP, "depth", "createdAt"),
                column("depth_modifiedat", DbColumnType.TIMESTAMP, "depth", "modifiedAt"),
                column("flowrate", DbColumnType.FLOAT, "flowRate"),
                column("flowrate_modifiedat", DbColumnType.TIMESTAMP, "flowRate", "modifiedAt"));
        Map<String, DataModelDbColumnVO> map = new LinkedHashMap<>();
        for (DataModelDbColumnVO col : columns) {
            map.put(col.getColumnName(), col);
        }

        DynamicEntityDaoVO vo = new DynamicEntityDaoVO();
        vo.setDbTableName("FlowRate");
        vo.setDbColumnInfoVOMap(map);
        vo.setAttrId(deleteAttrId);
        return vo;
    }

    @Test
    void nullifiesAllSubColumnsOfDeletedAttribute() {
        String sql = provider.deleteAttr(daoVO("depth"));
        assertTrue(sql.contains("\"depth\" = NULL"), sql);
        assertTrue(sql.contains("\"depth_observedat\" = NULL"), sql);
        assertTrue(sql.contains("\"depth_createdat\" = NULL"), sql);
        assertTrue(sql.contains("\"depth_modifiedat\" = NULL"), sql);
        assertTrue(sql.contains("MODIFIED_AT = #{modifiedAt}"), sql);
    }

    @Test
    void doesNotReferenceNonExistentModifiedAtColumn() {
        String sql = provider.deleteAttr(daoVO("depth"));
        assertFalse(sql.toLowerCase().contains("_modified_at"), sql);
    }

    @Test
    void leavesOtherAttributesUntouched() {
        String sql = provider.deleteAttr(daoVO("depth"));
        assertFalse(sql.contains("\"flowrate\""), sql);
        assertFalse(sql.contains("\"flowrate_modifiedat\""), sql);
    }
}
