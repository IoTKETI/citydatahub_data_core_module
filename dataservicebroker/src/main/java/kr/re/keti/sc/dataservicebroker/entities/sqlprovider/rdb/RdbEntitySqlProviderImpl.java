package kr.re.keti.sc.dataservicebroker.entities.sqlprovider.rdb;

import java.util.List;
import java.util.Map;

import kr.re.keti.sc.dataservicebroker.common.datamapperhandler.*;
import org.apache.ibatis.jdbc.SQL;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.DbColumnType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.PropertyKey;
import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityDaoVO;
import kr.re.keti.sc.dataservicebroker.common.vo.DbConditionVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelDbColumnVO;

public class RdbEntitySqlProviderImpl {

    /**
     * 테이블 생성을 위한 DDL
     *
     * @param ddl entitySchemaManager에서 생성한 ddl
     * @return entitySchemaManager에서 생성한 ddl 그대로 사용
     */
    public String executeDdl(String ddl) {
        return ddl;
    }

    /**
     * entity create 시 사용될 sql을 동적 생성
     *
     * @param entityDaoVO entitySchema 기반으로 파싱되어 생성된 eneityDaoVO
     * @return 생성된 sql문
     */
    public String create(CommonEntityDaoVO entityDaoVO) {
        SQL sql = new SQL() {
            {
                // 1. 테이블명 설정
                INSERT_INTO(Constants.SCHEMA_NAME + ".\"" + entityDaoVO.getDbTableName() + "\"");
                Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = entityDaoVO.getDbColumnInfoVOMap();
                if (dbColumnInfoVOMap != null) {

                    // 2. Default Column 설정
                    VALUES("ID", "#{" + DataServiceBrokerCode.DefaultAttributeKey.ID.getCode() + "}");
                    VALUES("DATASET_ID", "#{" + DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode() + "}");
                    VALUES("CREATED_AT", "#{" + DataServiceBrokerCode.DefaultAttributeKey.CREATED_AT.getCode() + "}");
                    VALUES("MODIFIED_AT", "#{" + DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode() + "}");

                    // 3. Dynamic Entity Column 설정
                    for (DataModelDbColumnVO dbColumnInfoVO : dbColumnInfoVOMap.values()) {

                        if (entityDaoVO.get(dbColumnInfoVO.getDaoAttributeId()) == null) {
                            continue;
                        }

                        String daoAttributeId = dbColumnInfoVO.getDaoAttributeId();
                        String columnName = dbColumnInfoVO.getColumnName();
                        DbColumnType dbColumnType = dbColumnInfoVO.getColumnType();

                        if (dbColumnType == DbColumnType.ARRAY_VARCHAR) {
                            VALUES(columnName, "#{" + daoAttributeId + ", typeHandler=" + StringArrayListTypeHandler.class.getName() + "}::VARCHAR[]");
                        } else if (dbColumnType == DbColumnType.ARRAY_INTEGER) {
                            VALUES(columnName, "#{" + daoAttributeId + ", typeHandler=" + IntegerArrayListTypeHandler.class.getName() + "}::INT[]");
                        } else if (dbColumnType == DbColumnType.ARRAY_FLOAT) {
                            VALUES(columnName, "#{" + daoAttributeId + ", typeHandler=" + BigDecimalArrayListTypeHandler.class.getName() + "}::DECIMAL[]");
                        } else if (dbColumnType == DbColumnType.ARRAY_BIGINT) {
                            VALUES(columnName, "#{" + daoAttributeId + ", typeHandler=" + LongArrayListTypeHandler.class.getName() + "}::BIGINT[]");
                        } else if (dbColumnType == DbColumnType.ARRAY_TIMESTAMP) {
                            VALUES(columnName, "#{" + daoAttributeId + ", typeHandler=" + DateArrayListTypeHandler.class.getName() + "}::TIMESTAMP WITH TIME ZONE[]");
                        } else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
                            VALUES(columnName, "#{" + daoAttributeId + ", typeHandler=" + BooleanArrayListTypeHandler.class.getName() + "}::BOOLEAN[]");
                        }else if (dbColumnType == DbColumnType.TIMESTAMP) {
                            VALUES(columnName, "#{" + daoAttributeId + "}");
                        } else if (dbColumnType == DbColumnType.GEOMETRY_4326) {
                            VALUES(columnName, "ST_SetSRID(ST_GeomFromGeoJSON(#{" + daoAttributeId + "}), 4326)");
                        } else if (dbColumnType == DbColumnType.GEOMETRY_3857) {
                            VALUES(columnName, "ST_Transform(ST_SetSRID(ST_GeomFromGeoJSON(#{" + daoAttributeId + "}), 4326), 3857)");
                        } else {
                            VALUES(columnName, "#{" + daoAttributeId + "}");
                        }
                    }
                }
            }
        };
        return sql.toString();
    }

    /**
     * Replace Entity Attributes 시 사용될 sql을 동적 생성
     *
     * @param entityDaoVO entitySchema 기반으로 파싱되어 생성된 eneityDaoVO
     * @return 생성된 sql문
     */
    public String replaceAttr(CommonEntityDaoVO entityDaoVO) {
        SQL sql = new SQL() {
            {
                // 1. 테이블명 설정
                UPDATE(Constants.SCHEMA_NAME + ".\"" + entityDaoVO.getDbTableName() + "\"");
                Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = entityDaoVO.getDbColumnInfoVOMap();
                if (dbColumnInfoVOMap != null) {

                    // 2. Default Column 설정
                    SET("MODIFIED_AT = #{" + DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode() + "}");

                    if (entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()) != null) {
                        SET("DATASET_ID = #{" + DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode() + "}");
                    }

                    // 3. Dynamic Entity Column 설정
                    for (DataModelDbColumnVO dbColumnInfoVO : dbColumnInfoVOMap.values()) {

                        String daoAttributeId = dbColumnInfoVO.getDaoAttributeId();
                        String columnName = dbColumnInfoVO.getColumnName();
                        DbColumnType dbColumnType = dbColumnInfoVO.getColumnType();

                        // property에 포함된 속성인 createdAt 은 null 인 경우만 입력
                        if(dbColumnType == DbColumnType.TIMESTAMP && daoAttributeId.endsWith(Constants.COLUMN_DELIMITER + PropertyKey.CREATED_AT.getCode())) {
                            SET(columnName + " = COALESCE( " + columnName + ", #{" + DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode() + ", jdbcType=TIMESTAMP})");
                            continue;
                        }

                        // replace 요청이기 때문에 property의 modifiedAt은 모두 MODIFIED_AT 시간으로 업데이트
                        if(dbColumnType == DbColumnType.TIMESTAMP && daoAttributeId.endsWith(Constants.COLUMN_DELIMITER + PropertyKey.MODIFIED_AT.getCode())) {
                            SET(columnName + " = #{" + DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode() + "}");
                            continue;
                        }

                        if (dbColumnType == DbColumnType.VARCHAR) {
                            SET(columnName + " = #{" + daoAttributeId + ", jdbcType=VARCHAR}");
                        } else if (dbColumnType == DbColumnType.INTEGER) {
                            SET(columnName + " = #{" + daoAttributeId + ", jdbcType=INTEGER}");
                        } else if (dbColumnType == DbColumnType.FLOAT) {
                            SET(columnName + " = #{" + daoAttributeId + ", jdbcType=DECIMAL}");
                        } else if (dbColumnType == DbColumnType.BIGINT) {
                            SET(columnName + " = #{" + daoAttributeId + ", jdbcType=BIGINT}");
                        } else if (dbColumnType == DbColumnType.ARRAY_VARCHAR) {
                            SET(columnName + " = #{" + daoAttributeId + ", typeHandler=" + StringArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::VARCHAR[]");
                        } else if (dbColumnType == DbColumnType.ARRAY_INTEGER) {
                            SET(columnName + " = #{" + daoAttributeId + ", typeHandler=" + IntegerArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::INT[]");
                        } else if (dbColumnType == DbColumnType.ARRAY_FLOAT) {
                            SET(columnName + " = #{" + daoAttributeId + ", typeHandler=" + BigDecimalArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::DECIMAL[]");
                        } else if (dbColumnType == DbColumnType.ARRAY_BIGINT) {
                            SET(columnName + " = #{" + daoAttributeId + ", typeHandler=" + LongArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::BIGINT[]");
                        } else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
                            SET(columnName + " = #{" + daoAttributeId + ", typeHandler=" + BooleanArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::BOOLEAN[]");
                        } else if (dbColumnType == DbColumnType.ARRAY_TIMESTAMP) {
                            SET(columnName + " = #{" + daoAttributeId + ", typeHandler=" + DateArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::TIMESTAMP WITH TIME ZONE[]");
                        } else if (dbColumnType == DbColumnType.TIMESTAMP) {
                            SET(columnName + " = #{" + daoAttributeId + ", jdbcType=TIMESTAMP}");
                        } else if (dbColumnType == DbColumnType.GEOMETRY_4326) {
                            SET(columnName + " = ST_SetSRID(ST_GeomFromGeoJSON(#{" + daoAttributeId + "}), 4326)");
                        } else if (dbColumnType == DbColumnType.GEOMETRY_3857) {
                            SET(columnName + " = ST_Transform(ST_SetSRID(ST_GeomFromGeoJSON(#{" + daoAttributeId + "}), 4326), 3857)");
                        } else {
                            SET(columnName + " = #{" + daoAttributeId + "}");
                        }
                    }
                }
                // 4. WHERE 절 설정
                WHERE("ID = #{id}");
                WHERE("MODIFIED_AT <= #{modifiedAt}");
            }
        };
        return sql.toString();
    }

    public String replaceAttrHBase(CommonEntityDaoVO entityDaoVO) {
        throw new UnsupportedOperationException("DynamicRdbSqlProvider not supported 'replaceAttrHBase'");
    }

    /**
     * Append Entity Attributes 시 사용될 sql을 동적 생성
     *
     * @param entityDaoVO entitySchema 기반으로 파싱되어 생성된 eneityDaoVO
     * @return 생성된 sql문
     */
    public String appendAttr(CommonEntityDaoVO entityDaoVO) {
        SQL sql = new SQL() {
            {
                // 1. 테이블명 설정
                UPDATE(Constants.SCHEMA_NAME + ".\"" + entityDaoVO.getDbTableName() + "\"");
                Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = entityDaoVO.getDbColumnInfoVOMap();
                if (dbColumnInfoVOMap != null) {

                    // 2. Default Column 설정
                    SET("MODIFIED_AT = #{" + DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode() + "}");
                    if (entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()) != null) {
                        SET("DATASET_ID = #{" + DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode() + "}");
                    }

                    // 3. Dynamic Entity Column 설정
                    for (DataModelDbColumnVO dbColumnInfoVO : dbColumnInfoVOMap.values()) {

                        String daoAttributeId = dbColumnInfoVO.getDaoAttributeId();
                        String columnName = dbColumnInfoVO.getColumnName();
                        DbColumnType dbColumnType = dbColumnInfoVO.getColumnType();

                        if (entityDaoVO.get(daoAttributeId) == null) {
                            continue;
                        }

                        // property에 포함된 속성인 createdAt 은 null 인 경우만 입력
                        if(dbColumnType == DbColumnType.TIMESTAMP && daoAttributeId.endsWith(Constants.COLUMN_DELIMITER + PropertyKey.CREATED_AT.getCode())) {
                            SET(columnName + " = COALESCE( " + columnName + ", #{" + DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode() + ", jdbcType=TIMESTAMP})");
                            continue;
                        }

                        if (dbColumnType == DbColumnType.VARCHAR) {
                            SET(columnName + " = #{" + daoAttributeId + ", jdbcType=VARCHAR}");
                        } else if (dbColumnType == DbColumnType.INTEGER) {
                            SET(columnName + " = #{" + daoAttributeId + ", jdbcType=INTEGER}");
                        } else if (dbColumnType == DbColumnType.FLOAT) {
                            SET(columnName + " = #{" + daoAttributeId + ", jdbcType=DECIMAL}");
                        } else if (dbColumnType == DbColumnType.BIGINT) {
                            SET(columnName + " = #{" + daoAttributeId + ", jdbcType=BIGINT}");
                        } else if (dbColumnType == DbColumnType.ARRAY_VARCHAR) {
                            SET(columnName + " = #{" + daoAttributeId + ", typeHandler=" + StringArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::VARCHAR[]");
                        } else if (dbColumnType == DbColumnType.ARRAY_INTEGER) {
                            SET(columnName + " = #{" + daoAttributeId + ", typeHandler=" + IntegerArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::INT[]");
                        } else if (dbColumnType == DbColumnType.ARRAY_FLOAT) {
                            SET(columnName + " = #{" + daoAttributeId + ", typeHandler=" + BigDecimalArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::DECIMAL[]");
                        } else if (dbColumnType == DbColumnType.ARRAY_BIGINT) {
                            SET(columnName + " = #{" + daoAttributeId + ", typeHandler=" + LongArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::BIGINT[]");
                        } else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
                            SET(columnName + " = #{" + daoAttributeId + ", typeHandler=" + BooleanArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::BOOLEAN[]");
                        } else if (dbColumnType == DbColumnType.ARRAY_TIMESTAMP) {
                            SET(columnName + " = #{" + daoAttributeId + ", typeHandler=" + DateArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::TIMESTAMP WITH TIME ZONE[]");
                        } else if (dbColumnType == DbColumnType.TIMESTAMP) {
                            SET(columnName + " = #{" + daoAttributeId + ", jdbcType=TIMESTAMP}");
                        } else if (dbColumnType == DbColumnType.GEOMETRY_4326) {
                            SET(columnName + " = ST_SetSRID(ST_GeomFromGeoJSON(#{" + daoAttributeId + "}), 4326)");
                        } else if (dbColumnType == DbColumnType.GEOMETRY_3857) {
                            SET(columnName + " = ST_Transform(ST_SetSRID(ST_GeomFromGeoJSON(#{" + daoAttributeId + "}), 4326), 3857)");
                        } else {
                            SET(columnName + " = #{" + daoAttributeId + "}");
                        }
                    }
                }
                // 4. WHERE 절 설정
                WHERE("ID = #{id}");
                WHERE("MODIFIED_AT <= #{modifiedAt}");
            }
        };
        return sql.toString();
    }


    /**
     * Append Entity (noOverwrite) Attributes 시 사용될 sql을 동적 생성
     *
     * @param entityDaoVO entitySchema 기반으로 파싱되어 생성된 eneityDaoVO
     * @return 생성된 sql문
     */
    public String appendNoOverwriteAttr(CommonEntityDaoVO entityDaoVO) {
        SQL sql = new SQL() {
            {
                // 1. 테이블명 설정
                UPDATE(Constants.SCHEMA_NAME + ".\"" + entityDaoVO.getDbTableName() + "\"");
                Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = entityDaoVO.getDbColumnInfoVOMap();
                if (dbColumnInfoVOMap != null) {

                    // 2. Default Column 설정
                    SET("MODIFIED_AT = #{" + DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode() + "}");
                    if (entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()) != null) {
                        SET("DATASET_ID = #{" + DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode() + "}");
                    }

                    // 3. Dynamic Entity Column 설정
                    for (DataModelDbColumnVO dbColumnInfoVO : dbColumnInfoVOMap.values()) {

                        String daoAttributeId = dbColumnInfoVO.getDaoAttributeId();
                        String columnName = dbColumnInfoVO.getColumnName();
                        DbColumnType dbColumnType = dbColumnInfoVO.getColumnType();

                        if (entityDaoVO.get(daoAttributeId) == null) {
                            continue;
                        }

                        // property에 포함된 속성인 createdAt 은 null 인 경우만 입력
                        if(dbColumnType == DbColumnType.TIMESTAMP && daoAttributeId.endsWith(Constants.COLUMN_DELIMITER + PropertyKey.CREATED_AT.getCode())) {
                            SET(columnName + " = COALESCE( " + columnName + ", #{" + DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode() + ", jdbcType=TIMESTAMP})");
                            continue;
                        }

                        if (dbColumnType == DbColumnType.VARCHAR) {
                            SET(columnName + " = COALESCE( " + columnName + ", #{" + daoAttributeId + ", jdbcType=VARCHAR})");
                        } else if (dbColumnType == DbColumnType.INTEGER) {
                            SET(columnName + " = COALESCE( " + columnName + ", #{" + daoAttributeId + ", jdbcType=INTEGER})");
                        } else if (dbColumnType == DbColumnType.FLOAT) {
                            SET(columnName + " = COALESCE( " + columnName + ", #{" + daoAttributeId + ", jdbcType=DECIMAL})");
                        } else if (dbColumnType == DbColumnType.BIGINT) {
                            SET(columnName + " = COALESCE( " + columnName + ", #{" + daoAttributeId + ", jdbcType=BIGINT})");
                        } else if (dbColumnType == DbColumnType.ARRAY_VARCHAR) {
                            SET(columnName + " = COALESCE( " + columnName + ", #{" + daoAttributeId + ", typeHandler=" + StringArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::VARCHAR[])");
                        } else if (dbColumnType == DbColumnType.ARRAY_INTEGER) {
                            SET(columnName + " = COALESCE( " + columnName + ", #{" + daoAttributeId + ", typeHandler=" + IntegerArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::INT[])");
                        } else if (dbColumnType == DbColumnType.ARRAY_FLOAT) {
                            SET(columnName + " = COALESCE( " + columnName + ", #{" + daoAttributeId + ", typeHandler=" + BigDecimalArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::DECIMAL[])");
                        } else if (dbColumnType == DbColumnType.ARRAY_BIGINT) {
                            SET(columnName + " = COALESCE( " + columnName + ", #{" + daoAttributeId + ", typeHandler=" + LongArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::BIGINT[])");
                        } else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
                            SET(columnName + " = COALESCE( " + columnName + ", #{" + daoAttributeId + ", typeHandler=" + BooleanArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::BOOLEAN[])");
                        } else if (dbColumnType == DbColumnType.ARRAY_TIMESTAMP) {
                            SET(columnName + " = COALESCE( " + columnName + ", #{" + daoAttributeId + ", typeHandler=" + DateArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::TIMESTAMP WITH TIME ZONE[])");
                        } else if (dbColumnType == DbColumnType.TIMESTAMP) {
                            SET(columnName + " = COALESCE( " + columnName + ", #{" + daoAttributeId + ", jdbcType=TIMESTAMP})");
                        } else if (dbColumnType == DbColumnType.GEOMETRY_4326) {
                            SET(columnName + " = COALESCE( " + columnName + ",( ST_SetSRID(ST_GeomFromGeoJSON(#{" + daoAttributeId + "}), 4326)");
                        } else if (dbColumnType == DbColumnType.GEOMETRY_3857) {
                            SET(columnName + " = COALESCE( " + columnName + ",( ST_Transform(ST_SetSRID(ST_GeomFromGeoJSON(#{" + daoAttributeId + "}), 4326), 3857))");
                        } else {
                            SET(columnName + " = #{" + daoAttributeId + "}");
                        }
                    }
                }
                // 4. WHERE 절 설정
                WHERE("ID = #{id}");
                WHERE("MODIFIED_AT <= #{modifiedAt}");
            }
        };
        return sql.toString();
    }

    /**
     * Update Entity Attributes 시 사용될 sql을 동적 생성
     * @param entityDaoVO entitySchema 기반으로 파싱되어 생성된 eneityDaoVO
     * @return 생성된 sql문
     */
    public String updateAttr(CommonEntityDaoVO entityDaoVO) {
        SQL sql = new SQL() {
            {
                // 1. 테이블명 설정
                UPDATE(Constants.SCHEMA_NAME + ".\"" + entityDaoVO.getDbTableName() + "\"");
                Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = entityDaoVO.getDbColumnInfoVOMap();
                if (dbColumnInfoVOMap != null) {

                    // 2. Default Column 설정
                    SET("MODIFIED_AT = #{" + DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode() + "}");
                    if (entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()) != null) {
                        SET("DATASET_ID = #{" + DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode() + "}");
                    }

                    // 3. Dynamic Entity Column 설정
                    for (DataModelDbColumnVO dbColumnInfoVO : dbColumnInfoVOMap.values()) {

                        String daoAttributeId = dbColumnInfoVO.getDaoAttributeId();
                        String columnName = dbColumnInfoVO.getColumnName();
                        DbColumnType dbColumnType = dbColumnInfoVO.getColumnType();

                        if (entityDaoVO.get(daoAttributeId) == null) {
                            continue;
                        }

                        // property에 포함된 속성인 createdAt 은 null 인 경우만 입력
                        if(dbColumnType == DbColumnType.TIMESTAMP && daoAttributeId.endsWith(Constants.COLUMN_DELIMITER + PropertyKey.CREATED_AT.getCode())) {
                            SET(columnName + " = COALESCE( " + columnName + ", #{" + DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode() + ", jdbcType=TIMESTAMP})");
                            continue;
                        }

                        if (dbColumnType == DbColumnType.VARCHAR) {
                            SET(columnName + " = CASE WHEN " + columnName + " IS NOT NULL THEN #{" + daoAttributeId + ", jdbcType=VARCHAR} END");
                        } else if (dbColumnType == DbColumnType.INTEGER) {
                            SET(columnName + " = CASE WHEN " + columnName + " IS NOT NULL THEN #{" + daoAttributeId + ", jdbcType=INTEGER} END::NUMERIC");
                        } else if (dbColumnType == DbColumnType.FLOAT) {
                            SET(columnName + " = CASE WHEN " + columnName + " IS NOT NULL THEN #{" + daoAttributeId + ", jdbcType=DECIMAL} END::NUMERIC");
                        } else if (dbColumnType == DbColumnType.BIGINT) {
                            SET(columnName + " = CASE WHEN " + columnName + " IS NOT NULL THEN #{" + daoAttributeId + ", jdbcType=BIGINT} END::NUMERIC");
                        } else if (dbColumnType == DbColumnType.ARRAY_VARCHAR) {
                            SET(columnName + " = CASE WHEN " + columnName + " IS NOT NULL THEN #{" + daoAttributeId + ", typeHandler=" + StringArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::VARCHAR[] END");
                        } else if (dbColumnType == DbColumnType.ARRAY_INTEGER) {
                            SET(columnName + " = CASE WHEN " + columnName + " IS NOT NULL THEN #{" + daoAttributeId + ", typeHandler=" + IntegerArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::INT[] END");
                        } else if (dbColumnType == DbColumnType.ARRAY_FLOAT) {
                            SET(columnName + " = CASE WHEN " + columnName + " IS NOT NULL THEN #{" + daoAttributeId + ", typeHandler=" + BigDecimalArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::DECIMAL[] END");
                        } else if (dbColumnType == DbColumnType.ARRAY_BIGINT) {
                            SET(columnName + " = CASE WHEN " + columnName + " IS NOT NULL THEN #{" + daoAttributeId + ", typeHandler=" + LongArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::BIGINT[] END");
                        } else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
                            SET(columnName + " = CASE WHEN " + columnName + " IS NOT NULL THEN #{" + daoAttributeId + ", typeHandler=" + BooleanArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::BOOLEAN[] END");
                        } else if (dbColumnType == DbColumnType.ARRAY_TIMESTAMP) {
                            SET(columnName + " = CASE WHEN " + columnName + " IS NOT NULL THEN #{" + daoAttributeId + ", typeHandler=" + DateArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::TIMESTAMP WITH TIME ZONE[] END");
                        } else if (dbColumnType == DbColumnType.TIMESTAMP) {
                            SET(columnName + " = CASE WHEN " + columnName + " IS NOT NULL THEN #{" + daoAttributeId + ", jdbcType=TIMESTAMP}::TIMESTAMP WITH TIME ZONE END");
                        } else if (dbColumnType == DbColumnType.GEOMETRY_4326) {
                            SET(columnName + " = CASE WHEN " + columnName + " IS NOT NULL THEN ST_SetSRID(ST_GeomFromGeoJSON(#{" + daoAttributeId + "}), 4326) END");
                        } else if (dbColumnType == DbColumnType.GEOMETRY_3857) {
                            SET(columnName + " = CASE WHEN " + columnName + " IS NOT NULL THEN ST_Transform(ST_SetSRID(ST_GeomFromGeoJSON(#{" + daoAttributeId + "}), 4326), 3857) END");
                        } else {
                            SET(columnName + " = CASE WHEN " + columnName + " IS NOT NULL THEN #{" + daoAttributeId + "} END");
                        }
                    }
                }
                // 4. WHERE 절 설정
                WHERE("ID = #{id}");
                WHERE("MODIFIED_AT <= #{modifiedAt}");
            }
        };
        return sql.toString();
    }

    /**
     * Partial Attribute Update 시 사용될 sql을 동적 생성
     * @param entityDaoVO entitySchema 기반으로 파싱되어 생성된 eneityDaoVO
     * @return 생성된 sql문
     */
    public String partialAttrUpdate(CommonEntityDaoVO entityDaoVO) {
        SQL sql = new SQL() {
            {
                // 1. 테이블명 설정
                UPDATE(Constants.SCHEMA_NAME + ".\"" + entityDaoVO.getDbTableName() + "\"");
                Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = entityDaoVO.getDbColumnInfoVOMap();
                if (dbColumnInfoVOMap != null) {

                    // 2. Default Column 설정
                    SET("MODIFIED_AT = #{" + DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode() + "}");
                    if (entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()) != null) {
                        SET("DATASET_ID = #{" + DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode() + "}");
                    }

                    // 3. Dynamic Entity Column 설정
                    for (DataModelDbColumnVO dbColumnInfoVO : dbColumnInfoVOMap.values()) {

                        String daoAttributeId = dbColumnInfoVO.getDaoAttributeId();
                        String columnName = dbColumnInfoVO.getColumnName();
                        DbColumnType dbColumnType = dbColumnInfoVO.getColumnType();

                        if (entityDaoVO.get(daoAttributeId) == null) {
                            continue;
                        }

                        // property에 포함된 속성인 createdAt 은 null 인 경우만 입력
                        if(dbColumnType == DbColumnType.TIMESTAMP && daoAttributeId.endsWith(Constants.COLUMN_DELIMITER + PropertyKey.CREATED_AT.getCode())) {
                            SET(columnName + " = COALESCE( " + columnName + ", #{" + DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode() + ", jdbcType=TIMESTAMP})");
                            continue;
                        }

                        if (dbColumnType == DbColumnType.VARCHAR) {
                            SET(columnName + " = #{" + daoAttributeId + ", jdbcType=VARCHAR}");
                        } else if (dbColumnType == DbColumnType.INTEGER) {
                            SET(columnName + " = #{" + daoAttributeId + ", jdbcType=INTEGER}");
                        } else if (dbColumnType == DbColumnType.FLOAT) {
                            SET(columnName + " = #{" + daoAttributeId + ", jdbcType=DECIMAL}");
                        } else if (dbColumnType == DbColumnType.BIGINT) {
                            SET(columnName + " = #{" + daoAttributeId + ", jdbcType=BIGINT}");
                        } else if (dbColumnType == DbColumnType.ARRAY_VARCHAR) {
                            SET(columnName + " = #{" + daoAttributeId + ", typeHandler=" + StringArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::VARCHAR[]");
                        } else if (dbColumnType == DbColumnType.ARRAY_INTEGER) {
                            SET(columnName + " = #{" + daoAttributeId + ", typeHandler=" + IntegerArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::INT[]");
                        } else if (dbColumnType == DbColumnType.ARRAY_FLOAT) {
                            SET(columnName + " = #{" + daoAttributeId + ", typeHandler=" + BigDecimalArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::DECIMAL[]");
                        } else if (dbColumnType == DbColumnType.ARRAY_BIGINT) {
                            SET(columnName + " = #{" + daoAttributeId + ", typeHandler=" + LongArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::BIGINT[]");
                        } else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
                            SET(columnName + " = #{" + daoAttributeId + ", typeHandler=" + BooleanArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::BOOLEAN[]");
                        } else if (dbColumnType == DbColumnType.ARRAY_TIMESTAMP) {
                            SET(columnName + " = #{" + daoAttributeId + ", typeHandler=" + DateArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::TIMESTAMP WITH TIME ZONE[]");
                        } else if (dbColumnType == DbColumnType.TIMESTAMP) {
                            SET(columnName + " = #{" + daoAttributeId + ", jdbcType=TIMESTAMP}");
                        } else if (dbColumnType == DbColumnType.GEOMETRY_4326) {
                            SET(columnName + " = ST_SetSRID(ST_GeomFromGeoJSON(#{" + daoAttributeId + "}), 4326)");
                        } else if (dbColumnType == DbColumnType.GEOMETRY_3857) {
                            SET(columnName + " = ST_Transform(ST_SetSRID(ST_GeomFromGeoJSON(#{" + daoAttributeId + "}), 4326), 3857)");
                        } else {
                            SET(columnName + " = #{" + daoAttributeId + "}");
                        }
                    }
                }
                // 4. WHERE 절 설정
                WHERE("ID = #{id}");
                WHERE("MODIFIED_AT <= #{modifiedAt}");
            }
        };
        return sql.toString();
    }

    /**
     * entity delete 시 사용될 sql을 동적 생성
     *
     * @param entityDaoVO entitySchema 기반으로 파싱되어 생성된 eneityDaoVO
     * @return 생성된 sql문
     */
    public String delete(CommonEntityDaoVO entityDaoVO) {
        SQL sql = new SQL() {
            {
                DELETE_FROM(Constants.SCHEMA_NAME + ".\"" + entityDaoVO.getDbTableName() + "\"");
                WHERE("ID = #{" + DataServiceBrokerCode.DefaultAttributeKey.ID.getCode() + "}");
            }
        };
        return sql.toString();
    }

    /**
     * entity이력 delete 시 사용될 sql을 동적 생성
     *
     * @param entityDaoVO entitySchema 기반으로 파싱되어 생성된 eneityDaoVO
     * @return 생성된 sql문
     */
    public String deleteHist(CommonEntityDaoVO entityDaoVO) {
        SQL sql = new SQL() {
            {
                DELETE_FROM(Constants.SCHEMA_NAME + ".\"" + entityDaoVO.getDbTableName() + Constants.PARTIAL_HIST_TABLE_PREFIX + "\"");
                WHERE("ID = #{" + DataServiceBrokerCode.DefaultAttributeKey.ID.getCode() + "}");
            }
        };
        return sql.toString();
    }

    /**
     * entityFull이력 delete 시 사용될 sql을 동적 생성
     *
     * @param entityDaoVO entitySchema 기반으로 파싱되어 생성된 eneityDaoVO
     * @return 생성된 sql문
     */
    public String deleteFullHist(CommonEntityDaoVO entityDaoVO) {
        SQL sql = new SQL() {
            {
                DELETE_FROM(Constants.SCHEMA_NAME + ".\"" + entityDaoVO.getDbTableName() + Constants.FULL_HIST_TABLE_PREFIX + "\"");
                WHERE("ID = #{" + DataServiceBrokerCode.DefaultAttributeKey.ID.getCode() + "}");
            }
        };
        return sql.toString();
    }

    /**
     * delete attribute 시 사용될 sql을 동적 생성
     *
     * @param entityDaoVO entitySchema 기반으로 파싱되어 생성된 eneityDaoVO
     * @return 생성된 sql문
     */
    public String deleteAttr(CommonEntityDaoVO entityDaoVO) {
        SQL sql = new SQL() {
            {
                // 1. 테이블명 설정
                UPDATE(Constants.SCHEMA_NAME + ".\"" + entityDaoVO.getDbTableName() + "\"");
                // 2. default column 설정
                SET("MODIFIED_AT = #{" + DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode() + "}");

                Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = entityDaoVO.getDbColumnInfoVOMap();
                if (dbColumnInfoVOMap != null) {

                    String deleteAttrId = entityDaoVO.getAttrId();
                    // 3. Dynamic Entity Column 설정
                    for (DataModelDbColumnVO dbColumnInfoVO : dbColumnInfoVOMap.values()) {
                        if (dbColumnInfoVO.getHierarchyAttributeIds().get(0).equals(deleteAttrId)) {
                            SET(dbColumnInfoVO.getColumnName() + " = NULL");
                            SET(dbColumnInfoVO.getColumnName() + Constants.COLUMN_DELIMITER + PropertyKey.MODIFIED_AT + " = #{" + DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode() + "}");
                        }
                    }
                }
                // 4. WHERE 절 설정
                WHERE("ID = #{" + DataServiceBrokerCode.DefaultAttributeKey.ID.getCode() + "}");
            }
        };
        return sql.toString();
    }

    /**
     * 이력 저장 시 사용될 sql을 동적 생성
     *
     * @param entityDaoVO entitySchema 기반으로 파싱되어 생성된 eneityDaoVO
     * @return 생성된 sql문
     */
    public String createHist(CommonEntityDaoVO entityDaoVO) {
        SQL sql = new SQL() {
            {
                // 1. 테이블명 설정
                INSERT_INTO(Constants.SCHEMA_NAME + ".\"" + entityDaoVO.getDbTableName() + Constants.PARTIAL_HIST_TABLE_PREFIX + "\"");
                Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = entityDaoVO.getDbColumnInfoVOMap();
                if (dbColumnInfoVOMap != null) {

                    // 2. Default Column 설정
                    VALUES("ID", "#{" + DataServiceBrokerCode.DefaultAttributeKey.ID.getCode() + "}");
                    VALUES("DATASET_ID", "#{" + DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode() + "}");
                    VALUES("MODIFIED_AT", "#{" + DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode() + "}");
                    VALUES("OPERATION", "#{" + DataServiceBrokerCode.DefaultAttributeKey.OPERATION.getCode() + "}");

                    // 3. Dynamic Entity Column 설정
                    for (DataModelDbColumnVO dbColumnInfoVO : dbColumnInfoVOMap.values()) {

                        String daoAttributeId = dbColumnInfoVO.getDaoAttributeId();
                        String columnName = dbColumnInfoVO.getColumnName();
                        DbColumnType dbColumnType = dbColumnInfoVO.getColumnType();

                        if (entityDaoVO.get(daoAttributeId) == null) {
                            continue;
                        }

                        if (dbColumnType == DbColumnType.ARRAY_VARCHAR) {
                            VALUES(columnName, "#{" + daoAttributeId + ", typeHandler=" + StringArrayListTypeHandler.class.getName() + "}::VARCHAR[]");
                        } else if (dbColumnType == DbColumnType.ARRAY_INTEGER) {
                            VALUES(columnName, "#{" + daoAttributeId + ", typeHandler=" + IntegerArrayListTypeHandler.class.getName() + "}::INT[]");
                        } else if (dbColumnType == DbColumnType.ARRAY_FLOAT) {
                            VALUES(columnName, "#{" + daoAttributeId + ", typeHandler=" + BigDecimalArrayListTypeHandler.class.getName() + "}::DECIMAL[]");
                        } else if (dbColumnType == DbColumnType.ARRAY_BIGINT) {
                            VALUES(columnName, "#{" + daoAttributeId + ", typeHandler=" + LongArrayListTypeHandler.class.getName() + "}::BIGINT[]");
                        } else if (dbColumnType == DbColumnType.ARRAY_TIMESTAMP) {
                            VALUES(columnName, "#{" + daoAttributeId + ", typeHandler=" + DateArrayListTypeHandler.class.getName() + "}::TIMESTAMP WITH TIME ZONE[]");
                        } else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
                            VALUES(columnName, "#{" + daoAttributeId + ", typeHandler=" + BooleanArrayListTypeHandler.class.getName() + "}::BOOLEAN[]");
                        } else if (dbColumnType == DbColumnType.TIMESTAMP) {
                            VALUES(columnName, "#{" + daoAttributeId + "}");
                        } else if (dbColumnType == DbColumnType.GEOMETRY_4326) {
                            VALUES(columnName, "ST_SetSRID(ST_GeomFromGeoJSON(#{" + daoAttributeId + "}), 4326)");
                        } else if (dbColumnType == DbColumnType.GEOMETRY_3857) {
                            VALUES(columnName, "ST_Transform(ST_SetSRID(ST_GeomFromGeoJSON(#{" + daoAttributeId + "}), 4326), 3857)");
                        } else {
                            VALUES(columnName, "#{" + daoAttributeId + "}");
                        }
                    }
                }
            }
        };
        return sql.toString();
    }

    /**
     * Full 이력 저장 시 사용될 sql을 동적 생성
     *  - Full 이력테이블은 partial 이력과 다르게 property의 createdAt, modifiedAt를 저장함
     * @param entityDaoVO entitySchema 기반으로 파싱되어 생성된 eneityDaoVO
     * @return 생성된 sql문
     */
    public String createFullHist(CommonEntityDaoVO entityDaoVO) {
        SQL sql = new SQL() {
            {
                // 1. 테이블명 설정
                INSERT_INTO(Constants.SCHEMA_NAME + ".\"" + entityDaoVO.getDbTableName() + Constants.FULL_HIST_TABLE_PREFIX + "\"");
                Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = entityDaoVO.getDbColumnInfoVOMap();
                if (dbColumnInfoVOMap != null) {

                    // 2. Default Column 설정
                    VALUES("ID", "#{" + DataServiceBrokerCode.DefaultAttributeKey.ID.getCode() + "}");
                    VALUES("DATASET_ID", "#{" + DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode() + "}");
                    VALUES("MODIFIED_AT", "#{" + DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode() + "}");
                    VALUES("OPERATION", "#{" + DataServiceBrokerCode.DefaultAttributeKey.OPERATION.getCode() + "}");

                    // 3. Dynamic Entity Column 설정
                    for (DataModelDbColumnVO dbColumnInfoVO : dbColumnInfoVOMap.values()) {

                        // full 이력의 경우 기존 DB정보를 조회해서 입력을 한다.
                        // 조회 결과가 entityDaoVO 에 담겨 있는데, map의 모든 key가 lowerCase로 되어있다. (mybatis 조회 결과가 그러함)
                        // 따라서 lower case 처리 한다.
                        String columnName = dbColumnInfoVO.getColumnName().toLowerCase();

                        if (entityDaoVO.get(columnName) == null) {
                            continue;
                        }

                        DbColumnType dbColumnType = dbColumnInfoVO.getColumnType();

                        if (dbColumnType == DbColumnType.ARRAY_VARCHAR) {
                            VALUES(columnName, "#{" + columnName + ", typeHandler=" + StringArrayListTypeHandler.class.getName() + "}::VARCHAR[]");
                        } else if (dbColumnType == DbColumnType.ARRAY_INTEGER) {
                            VALUES(columnName, "#{" + columnName + ", typeHandler=" + IntegerArrayListTypeHandler.class.getName() + "}::INT[]");
                        } else if (dbColumnType == DbColumnType.ARRAY_FLOAT) {
                            VALUES(columnName, "#{" + columnName + ", typeHandler=" + BigDecimalArrayListTypeHandler.class.getName() + "}::DECIMAL[]");
                        } else if (dbColumnType == DbColumnType.ARRAY_BIGINT) {
                            VALUES(columnName, "#{" + columnName + ", typeHandler=" + LongArrayListTypeHandler.class.getName() + "}::BIGINT[]");
                        } else if (dbColumnType == DbColumnType.ARRAY_TIMESTAMP) {
                            VALUES(columnName, "#{" + columnName + ", typeHandler=" + DateArrayListTypeHandler.class.getName() + "}::TIMESTAMP WITH TIME ZONE[]");
                        } else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
                            VALUES(columnName, "#{" + columnName + ", typeHandler=" + BooleanArrayListTypeHandler.class.getName() + "}::BOOLEAN[]");
                        } else if (dbColumnType == DbColumnType.TIMESTAMP) {
                            VALUES(columnName, "#{" + columnName + "}");
                        } else if (dbColumnType == DbColumnType.GEOMETRY_4326) {
                            VALUES(columnName, "ST_SetSRID(ST_GeomFromGeoJSON(#{" + columnName + "}), 4326)");
                        } else if (dbColumnType == DbColumnType.GEOMETRY_3857) {
                            VALUES(columnName, "ST_Transform(ST_SetSRID(ST_GeomFromGeoJSON(#{" + columnName + "}), 4326), 3857)");
                        } else {
                            VALUES(columnName, "#{" + columnName + "}");
                        }
                    }
                }
            }
        };
        return sql.toString();
    }

    /**
     * 최종 데이터 조회
     * @param dbConditionVO
     * @return
     */
    public String selectList(DbConditionVO dbConditionVO) {

        String selectCondition = dbConditionVO.getSelectCondition();
        String tableName = dbConditionVO.getTableName();
        String geoCondition = dbConditionVO.getGeoCondition();
        String queryCondition = dbConditionVO.getQueryCondition();
        String aclDatasetCondition = dbConditionVO.getAclDatasetCondition();

        List<String> searchIdList = dbConditionVO.getSearchIdList();
        String idPattern = dbConditionVO.getIdPattern();

        Integer limit = dbConditionVO.getLimit();
        Integer offset = dbConditionVO.getOffset();


        SQL sql = new SQL() {
            { // 익명의 클래스 생성
                SELECT(selectCondition);
                FROM(tableName);
                if (idPattern != null) {
                    WHERE("id ~'" + idPattern + "'");

                }
                if (searchIdList != null) {

                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("(");

                    for (int i = 0; i < searchIdList.size(); i++) {
                        stringBuilder.append("'");
                        stringBuilder.append(searchIdList.get(i));
                        stringBuilder.append("'");

                        if (i != searchIdList.size() - 1) {

                            stringBuilder.append(",");

                        }

                    }
                    stringBuilder.append(")");

                    WHERE("id IN " + stringBuilder.toString() );

                }
                if (geoCondition != null) {

                    WHERE(geoCondition);

                }
                if (queryCondition != null) {

                    WHERE(queryCondition);

                }

                if (aclDatasetCondition != null) {

                    WHERE(aclDatasetCondition);

                }

                if (limit != null && limit > 0) {

                    LIMIT(limit);
                }

                if (offset != null) {

                    OFFSET(offset);
                }
            }
        };

        return sql.toString();
    }

    /**
     * 최종 데이터 조회 by ID
     *
     * @param dbConditionVO
     * @return
     */
    public String selectOne(DbConditionVO dbConditionVO) {

        String selectCondition = dbConditionVO.getSelectCondition();
        String tableName = dbConditionVO.getTableName();
        String geoCondition = dbConditionVO.getGeoCondition();
        String queryCondition = dbConditionVO.getQueryCondition();
        String aclDatasetCondition = dbConditionVO.getAclDatasetCondition();
        String id = dbConditionVO.getId();
        SQL sql = new SQL() {
            {

                SELECT(selectCondition);
                FROM(tableName);

                if (geoCondition != null) {

                    WHERE(geoCondition);

                }
                if (queryCondition != null) {

                    WHERE(queryCondition);

                }
                if (aclDatasetCondition != null) {
                    WHERE(aclDatasetCondition);
                }
                // by ID 전용 조건
                WHERE("ID ='" + id + "'");
                LIMIT(1);
            }
        };

        return sql.toString();
    }


    /**
     * 이력 데이터 조회
     *
     * @param dbConditionVO
     * @return
     */
    public String selectHistList(DbConditionVO dbConditionVO) {

        SQL sql = new SQL() {
            {
                String selectCondition = dbConditionVO.getSelectCondition();
                String tableName = dbConditionVO.getTableName();
                String histTableName = dbConditionVO.getHistTableName();
                String geoCondition = dbConditionVO.getGeoCondition();
                String queryCondition = dbConditionVO.getQueryCondition();
                String timerelCondition = dbConditionVO.getTimerelCondition();
                String id = dbConditionVO.getId();
                List<String> searchIdList = dbConditionVO.getSearchIdList();
                String idPattern = dbConditionVO.getIdPattern();
                String aclDatasetCondition = dbConditionVO.getAclDatasetCondition();

                Integer limit = dbConditionVO.getLimit();
                Integer offset = dbConditionVO.getOffset();

                SELECT(selectCondition);
                FROM(histTableName + " as HIST_T");

                //1. 삭제 이전 데이터는 조회 되지 않게 필터링
                INNER_JOIN("(SELECT ID AS LAST_T_ID, CREATED_AT "
                        + " FROM " + tableName + " ) AS LAST_T "
                        + " ON HIST_T.ID = LAST_T.LAST_T_ID");
                WHERE(" HIST_T.MODIFIED_AT >= LAST_T.CREATED_AT");
                WHERE("OPERATION != 'DELETE'");

                if (id != null) {
                    WHERE("ID ='" + id + "'");
                }

                if (idPattern != null) {
                    WHERE("id ~'" + idPattern + "'");
                }

                if (searchIdList != null) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("(");

                    for (int i = 0; i < searchIdList.size(); i++) {
                        stringBuilder.append("'");
                        stringBuilder.append(searchIdList.get(i));
                        stringBuilder.append("'");

                        if (i != searchIdList.size() - 1) {
                            stringBuilder.append(",");
                        }
                    }
                    stringBuilder.append(")");

                    WHERE("id IN " + stringBuilder.toString() );

                }

                if (geoCondition != null) {
                    WHERE(geoCondition);
                }

                if (queryCondition != null) {
                    WHERE(queryCondition);
                }

                if (timerelCondition != null) {
                    WHERE(timerelCondition);
                }
                if (aclDatasetCondition != null) {
                    WHERE(aclDatasetCondition);
                }

                if (limit != null && limit > 0) {
                    LIMIT(limit);
                }

                if (offset != null) {
                    OFFSET(offset);
                }

                ORDER_BY("MODIFIED_AT DESC");
            }
        };
        return sql.toString();
    }


    /**
     * 최종 데이터 건수 조회
     * @param dbConditionVO
     * @return
     */
    public String selectCount(DbConditionVO dbConditionVO) {

        String tableName = dbConditionVO.getTableName();
        String geoCondition = dbConditionVO.getGeoCondition();
        String queryCondition = dbConditionVO.getQueryCondition();
        String aclDatasetCondition = dbConditionVO.getAclDatasetCondition();

        List<String> searchIdList = dbConditionVO.getSearchIdList();
        String idPattern = dbConditionVO.getIdPattern();

        SQL sql = new SQL() {
            { // 익명의 클래스 생성
                SELECT("count(*)");
                FROM(tableName);

                if (idPattern != null) {
                    WHERE("id ~'" + idPattern + "'");
                }

                if (searchIdList != null) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("(");

                    for (int i = 0; i < searchIdList.size(); i++) {
                        stringBuilder.append("'");
                        stringBuilder.append(searchIdList.get(i));
                        stringBuilder.append("'");

                        if (i != searchIdList.size() - 1) {
                            stringBuilder.append(",");
                        }
                    }
                    stringBuilder.append(")");

                    WHERE("id IN " + stringBuilder.toString() );
                }

                if (geoCondition != null) {
                    WHERE(geoCondition);
                }

                if (queryCondition != null) {
                    WHERE(queryCondition);
                }
                if (aclDatasetCondition != null) {
                    WHERE(aclDatasetCondition);
                }
            }
        };
        return sql.toString();
    }

    /**
     * 이력 데이터 건수 조회
     * @param dbConditionVO
     * @return
     */
    public String selectHistCount(DbConditionVO dbConditionVO) {

        SQL sql = new SQL() {
            {
                String tableName = dbConditionVO.getTableName();
                String histTableName = dbConditionVO.getHistTableName();
                String geoCondition = dbConditionVO.getGeoCondition();
                String queryCondition = dbConditionVO.getQueryCondition();
                String timerelCondition = dbConditionVO.getTimerelCondition();
                String id = dbConditionVO.getId();
                List<String> searchIdList = dbConditionVO.getSearchIdList();
                String idPattern = dbConditionVO.getIdPattern();
                String aclDatasetCondition = dbConditionVO.getAclDatasetCondition();

                SELECT("count(*)");
                FROM(histTableName + " as HIST_T");

                //1. 삭제 이전 데이터는 조회 되지 않게 필터링
                INNER_JOIN("(SELECT ID AS LAST_T_ID, CREATED_AT "
                        + " FROM " + tableName + " ) AS LAST_T "
                        + " ON HIST_T.ID = LAST_T.LAST_T_ID");
                WHERE(" HIST_T.MODIFIED_AT >= LAST_T.CREATED_AT");
                WHERE("OPERATION != 'DELETE'");

                if (id != null) {
                    WHERE("ID ='" + id + "'");
                }

                if (idPattern != null) {
                    WHERE("id ~'" + idPattern + "'");
                }

                if (searchIdList != null) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("(");

                    for (int i = 0; i < searchIdList.size(); i++) {
                        stringBuilder.append("'");
                        stringBuilder.append(searchIdList.get(i));
                        stringBuilder.append("'");

                        if (i != searchIdList.size() - 1) {
                            stringBuilder.append(",");
                        }
                    }
                    stringBuilder.append(")");

                    WHERE("id IN " + stringBuilder.toString() );
                }

                if (geoCondition != null) {
                    WHERE(geoCondition);
                }

                if (queryCondition != null) {
                    WHERE(queryCondition);
                }
                if (aclDatasetCondition != null) {
                    WHERE(aclDatasetCondition);
                }

                if (timerelCondition != null) {
                    WHERE(timerelCondition);
                }
            }
        };
        return sql.toString();
    }
}