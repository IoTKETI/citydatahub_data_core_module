package kr.re.keti.sc.dataservicebroker.entities.sqlprovider.hive;

import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kr.re.keti.sc.dataservicebroker.common.datamapperhandler.*;

import org.apache.derby.impl.sql.compile.IsNullNode;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet.A;
import org.apache.ibatis.jdbc.SQL;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.DbColumnType;
import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityDaoVO;
import kr.re.keti.sc.dataservicebroker.common.vo.DbConditionVO;
import kr.re.keti.sc.dataservicebroker.common.vo.entities.DynamicEntityDaoVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelDbColumnVO;
import kr.re.keti.sc.dataservicebroker.entities.vo.EntityBulkVO;
import kr.re.keti.sc.dataservicebroker.util.DateUtil;
import kr.re.keti.sc.dataservicebroker.util.StringUtil;

public class HiveEntitySqlProviderImpl {

	private static final String SPACE = " ";
	private static final String COMMA_WITH_SPACE = ", ";

	private static final String DATE_FORMAT_PRE_STRING = "from_utc_timestamp('"; 
	private static final String DATE_FORMAT_END_STRING = "', 'UTC')"; 
	private static final String ARRAY_FORMAT_PRE_STRING = "split('"; 
	private static final String ARRAY_FORMAT_END_STRING = "', ',')"; 

	/**
	 * 테이블 생성을 위한 DDL
	 *
	 * @param ddl entitySchemaManager에서 생성한 ddl
	 * @return entitySchemaManager에서 생성한 ddl 그대로 사용
	 */
	public String executeDdl(String ddl) {
		return ddl;
	}

	public String refreshTable(CommonEntityDaoVO entityDaoVO) {
		StringBuilder sql = new StringBuilder();
		sql.append("REFRESH").append(SPACE).append(entityDaoVO.getDbTableName());

		return sql.toString();
	}

	public String refreshTableBulk(String tableName) {
		StringBuilder sql = new StringBuilder();
		StringBuilder subQuery = new StringBuilder();
		subQuery.append("REFRESH").append(SPACE).append(tableName).append(";");
		sql.append(subQuery);
		return sql.toString();
	}
	
	public String bulkCreate(String tableName, EntityBulkVO entityBulkVO) {
		StringBuilder sql = new StringBuilder();
		StringBuilder insertBuilder = new StringBuilder();
		StringBuilder selectBuilder = new StringBuilder();
		StringBuilder asBuilder = new StringBuilder();
		StringBuilder valueBuilder = new StringBuilder();

		insertBuilder.append("INSERT INTO TABLE").append(SPACE);
		if (tableName.contains("_tmp")){
			insertBuilder.append(tableName).append(SPACE);
		} else {
			insertBuilder
					.append(StringUtil
							.removeSpecialCharAndLower(tableName))
					.append(SPACE);
		}
		valueBuilder.append("VALUES").append(SPACE);
		// Build Query SELECT part, AS part
		selectBuilder.append(selectQueryBuilder(BulkMethodType.CREATE, entityBulkVO)); 
		Map<String, Object> asQueryOrderResult = asQueryBuilder(BulkMethodType.CREATE, entityBulkVO);
		ArrayList<String> asQueryOrder = (ArrayList<String>) asQueryOrderResult.get("order");
		asBuilder.append(asQueryOrderResult.get("query")); //OPERATION Column등 동적 파라미터 넣게
		for (DynamicEntityDaoVO entityDaoVO : entityBulkVO.getEntityDaoVOList()){

			StringBuilder valueEntityBuilder = new StringBuilder();
			valueEntityBuilder.append("(");
			valueEntityBuilder.append(valueEntityQueryBuilder(asQueryOrder, entityDaoVO, entityBulkVO.getTableColumns()));
			valueEntityBuilder.append("),");
			valueBuilder.append(valueEntityBuilder);
		}
		valueBuilder.deleteCharAt(valueBuilder.length() - 1);
		insertBuilder.append(selectBuilder.toString()).append(valueBuilder.toString())
					.append(asBuilder.toString()).append(";");
		sql.append(insertBuilder);
		return sql.toString();
	}

	/**
	 * entity create 시 사용될 sql을 동적 생성
	 *
	 * @param entityDaoVO entitySchema 기반으로 파싱되어 생성된 eneityDaoVO
	 * @return 생성된 sql문
	 */
	public String create(CommonEntityDaoVO entityDaoVO) {

		List<String> tableColumns = entityDaoVO.getTableColumns();

		StringBuilder sql = new StringBuilder();
		StringBuilder insertBuilder = new StringBuilder();
		StringBuilder selectBuilder = new StringBuilder();
		StringBuilder asBuilder = new StringBuilder();
		StringBuilder valueBuilder = new StringBuilder();

		List<String> compareColumnList = new ArrayList<>(tableColumns);

		insertBuilder.append("INSERT INTO TABLE").append(SPACE);
		insertBuilder.append(entityDaoVO.getDbTableName()).append(SPACE);

		selectBuilder.append("select").append(SPACE);
		asBuilder.append(" as (");

		valueBuilder.append("VALUES (");

		Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = entityDaoVO.getDbColumnInfoVOMap();
		if (dbColumnInfoVOMap != null) {
			asBuilder.append("ID, DATASET_ID, CREATED_AT, MODIFIED_AT");
			// 2. Default Column 설정
			valueBuilder.append("#{").append(DataServiceBrokerCode.DefaultAttributeKey.ID.getCode()).append("}")
					.append(COMMA_WITH_SPACE);
			valueBuilder.append("#{").append(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()).append("}")
					.append(COMMA_WITH_SPACE);
			valueBuilder.append("from_utc_timestamp(#{")
					.append(DataServiceBrokerCode.DefaultAttributeKey.CREATED_AT.getCode()).append("}, 'UTC')")
					.append(COMMA_WITH_SPACE);
			valueBuilder.append("from_utc_timestamp(#{")
					.append(DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode()).append("}, 'UTC')")
					.append(COMMA_WITH_SPACE);

			compareColumnList.removeIf(e -> e.toUpperCase().equalsIgnoreCase("DATASET_ID"));
			compareColumnList.removeIf(e -> e.toUpperCase().equalsIgnoreCase("CREATED_AT"));
			compareColumnList.removeIf(e -> e.toUpperCase().equalsIgnoreCase("MODIFIED_AT"));
			compareColumnList.removeIf(e -> e.toUpperCase().equalsIgnoreCase("ID"));

			// 3. Dynamic Entity Column 설정
			for (DataModelDbColumnVO dbColumnInfoVO : dbColumnInfoVOMap.values()) {

				if (entityDaoVO.get(dbColumnInfoVO.getDaoAttributeId()) == null) {
					continue;
				}

				String daoAttributeId = dbColumnInfoVO.getDaoAttributeId();
				String columnName = dbColumnInfoVO.getColumnName();
				DbColumnType dbColumnType = dbColumnInfoVO.getColumnType();

				asBuilder.append(COMMA_WITH_SPACE).append(columnName);

				compareColumnList.removeIf(e -> e.toUpperCase().equalsIgnoreCase(columnName.toUpperCase()));

				if (dbColumnType == DbColumnType.ARRAY_VARCHAR) {
					valueBuilder.append("split(#{").append(daoAttributeId).append(", typeHandler=")
							.append(StringArrayListTypeHandler.class.getName()).append("}, ',')")
							.append(COMMA_WITH_SPACE);
				} else if (dbColumnType == DbColumnType.ARRAY_INTEGER) {
					valueBuilder.append("split(#{").append(daoAttributeId).append(", typeHandler=")
							.append(HiveIntegerArrayListTypeHandler.class.getName()).append("}, ',')")
							.append(COMMA_WITH_SPACE);
					tableColumns.replaceAll(e -> (e.equals(columnName)) ? "ST_ARRAYINT(" + columnName + ")" : e);
				} else if (dbColumnType == DbColumnType.ARRAY_FLOAT) {
					valueBuilder.append("split(#{").append(daoAttributeId).append(", typeHandler=")
							.append(HiveDoubleArrayListTypeHandler.class.getName()).append("}, ',')")
							.append(COMMA_WITH_SPACE);
					tableColumns.replaceAll(e -> (e.equals(columnName)) ? "ST_ARRAYDOUBLE(" + columnName + ")" : e);
				} else if (dbColumnType == DbColumnType.ARRAY_TIMESTAMP) {
					valueBuilder.append("split(#{").append(daoAttributeId).append(", typeHandler=")
							.append(HiveDateArrayListTypeHandler.class.getName()).append("}, ',')")
							.append(COMMA_WITH_SPACE);
					tableColumns.replaceAll(e -> (e.equals(columnName)) ? "ST_ARRAYTIMESTAMP(" + columnName + ")" : e);
				} else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
					valueBuilder.append("split(#{").append(daoAttributeId).append(", typeHandler=")
							.append(HiveBooleanArrayListTypeHandler.class.getName()).append("}, ',')")
							.append(COMMA_WITH_SPACE);
					tableColumns.replaceAll(e -> (e.equals(columnName)) ? "ST_ARRAYBOOLEAN(" + columnName + ")" : e);
				} else if (dbColumnType == DbColumnType.TIMESTAMP) {
					valueBuilder.append("from_utc_timestamp(#{").append(daoAttributeId).append("},'UTC')")
							.append(COMMA_WITH_SPACE);
				} else if (dbColumnType == DbColumnType.GEOMETRY_4326) {
					tableColumns.replaceAll(e -> (e.equalsIgnoreCase(columnName))
							? "ST_AsGeoJson(ST_GeomFromGeoJSON(" + columnName + "))"
							: (e.equalsIgnoreCase(columnName + "_idx"))
									? "ST_DISKINDEX(ST_asText(ST_GeomFromGeoJSON(" + columnName + ")))"
									: e);

					compareColumnList.remove(columnName + "_idx");

					valueBuilder.append("#{").append(daoAttributeId).append("}").append(COMMA_WITH_SPACE);
				} else if (dbColumnType == DbColumnType.GEOMETRY_3857) {
					tableColumns.replaceAll(e -> (e.equalsIgnoreCase(columnName))
							? "ST_AsGeoJson(ST_Transform(ST_FlipCoordinates(ST_GeomFromGeoJSON(" + columnName
									+ ")), 'epsg:4326','epsg:3857'))"
							: e);
					valueBuilder.append("#{").append(daoAttributeId).append("}").append(COMMA_WITH_SPACE);
				} else if (dbColumnType == DbColumnType.BOOLEAN) {
					valueBuilder.append("#{").append(daoAttributeId).append(", jdbcType=BOOLEAN}")
							.append(COMMA_WITH_SPACE);
				} else {
					valueBuilder.append("#{").append(daoAttributeId).append(", jdbcType=VARCHAR}")
							.append(COMMA_WITH_SPACE);
				}
			}

			if (!compareColumnList.isEmpty()) {
				for (String column : compareColumnList) {
					if (column.equalsIgnoreCase("key")) {
						asBuilder.append(COMMA_WITH_SPACE).append(column);
						valueBuilder.append("#{").append(DataServiceBrokerCode.DefaultAttributeKey.ID.getCode())
								.append("}").append(COMMA_WITH_SPACE);
					} else {
						asBuilder.append(COMMA_WITH_SPACE).append(column);
						valueBuilder.append("null").append(COMMA_WITH_SPACE);
					}
				}
			}

			String fields = String.join(", ", tableColumns);
			selectBuilder.append(fields).append(SPACE).append("FROM").append(SPACE);

			valueBuilder.deleteCharAt(valueBuilder.length() - 2).append(")");
		}

		sql.append(insertBuilder.toString()).append(selectBuilder.toString()).append(valueBuilder.toString())
				.append(asBuilder.toString()).append(")");
		return sql.toString();
	}

	/**
	 * Replace Entity Attributes 시 사용될 sql을 동적 생성
	 *
	 * @param entityDaoVO entitySchema 기반으로 파싱되어 생성된 eneityDaoVO
	 * @return 생성된 sql문
	 */
	public String replaceAttr(CommonEntityDaoVO entityDaoVO) {
		StringBuilder sql = new StringBuilder();
		StringBuilder select = new StringBuilder();
		StringBuilder update = new StringBuilder();
		StringBuilder insertColumns = new StringBuilder();
		StringBuilder insertValues = new StringBuilder();

		sql.append("MERGE INTO").append(SPACE).append(entityDaoVO.getDbTableName()).append(SPACE).append("as target")
				.append(SPACE);
		sql.append("USING (SELECT").append(SPACE);
		Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = entityDaoVO.getDbColumnInfoVOMap();
		if (dbColumnInfoVOMap != null) {

			// 2. Default Column 설정
			select.append("#{" + DataServiceBrokerCode.DefaultAttributeKey.ID.getCode() + "} as ID")
					.append(COMMA_WITH_SPACE);

			select.append("from_utc_timestamp(#{")
					.append(DataServiceBrokerCode.DefaultAttributeKey.CREATED_AT.getCode())
					.append("}, 'UTC') as CREATED_AT").append(COMMA_WITH_SPACE);

			// select.append("from_utc_timestamp(#{").append(
			// "#{" + DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode() + "},
			// 'UTC') as MODIFIED_AT")
			// .append(COMMA_WITH_SPACE);
			// #{ 가 2개여서 하나 삭제
			select.append("from_utc_timestamp(#{").append(
					DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode() + "}, 'UTC') as MODIFIED_AT")
					.append(COMMA_WITH_SPACE);

			if (entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()) != null) {
				select.append("#{" + DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode() + "} as DATASET_ID")
						.append(COMMA_WITH_SPACE);
			}

			update.append("MODIFIED_AT = source.MODIFIED_AT").append(COMMA_WITH_SPACE);

			if (entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()) != null) {
				update.append("DATASET_ID = source.DATASET_ID").append(COMMA_WITH_SPACE);
			}

			insertColumns.append("ID").append(COMMA_WITH_SPACE);
			insertColumns.append("CREATED_AT").append(COMMA_WITH_SPACE);
			insertColumns.append("MODIFIED_AT").append(COMMA_WITH_SPACE);

			if (entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()) != null) {
				insertColumns.append("DATASET_ID").append(COMMA_WITH_SPACE);
			}

			insertValues.append("source.ID").append(COMMA_WITH_SPACE);
			insertValues.append("source.CREATED_AT").append(COMMA_WITH_SPACE);
			insertValues.append("source.MODIFIED_AT").append(COMMA_WITH_SPACE);

			if (entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()) != null) {
				insertValues.append("source.DATASET_ID").append(COMMA_WITH_SPACE);
			}

			// 3. Dynamic Entity Column 설정
			//Set<String> updateQueryCols = entityDaoVO.keySet();
			for (DataModelDbColumnVO dbColumnInfoVO : dbColumnInfoVOMap.values()) {
				String daoAttributeId = dbColumnInfoVO.getDaoAttributeId();
				String columnName = dbColumnInfoVO.getColumnName();
				DbColumnType dbColumnType = dbColumnInfoVO.getColumnType();

				if (dbColumnType == DbColumnType.VARCHAR) {
					select.append("#{" + daoAttributeId + ", jdbcType=VARCHAR} as ").append(columnName)
							.append(COMMA_WITH_SPACE);
				} else if (dbColumnType == DbColumnType.INTEGER) {
					select.append("#{" + daoAttributeId + ", jdbcType=INTEGER} as ").append(columnName)
							.append(COMMA_WITH_SPACE);
				} else if (dbColumnType == DbColumnType.FLOAT) {
					select.append("#{" + daoAttributeId + ", jdbcType=FLOAT} as ").append(columnName)
							.append(COMMA_WITH_SPACE);
				} else if (dbColumnType == DbColumnType.BOOLEAN) {
					select.append("#{").append(daoAttributeId).append(", jdbcType=BOOLEAN} as ").append(columnName)
							.append(COMMA_WITH_SPACE);
				} else if (dbColumnType == DbColumnType.ARRAY_VARCHAR) {
					select.append("split(#{" + daoAttributeId + ", typeHandler="
							+ StringArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}, ',') as ")
							.append(columnName).append(COMMA_WITH_SPACE);
				} else if (dbColumnType == DbColumnType.ARRAY_INTEGER) {
					select.append("ST_ARRAYINT(split(#{" + daoAttributeId + ", typeHandler="
							+ HiveIntegerArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}, ',')) as ")
							.append(columnName).append(COMMA_WITH_SPACE);
				} else if (dbColumnType == DbColumnType.ARRAY_FLOAT) {
					select.append("ST_ARRAYDOUBLE(split(#{" + daoAttributeId + ", typeHandler="
							+ HiveDoubleArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}, ',')) as ")
							.append(columnName).append(COMMA_WITH_SPACE);
				} else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
					select.append("ST_ARRAYBOOLEAN(split(#{" + daoAttributeId + ", typeHandler="
							+ HiveBooleanArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}, ',')) as ")
							.append(columnName).append(COMMA_WITH_SPACE);
				} else if (dbColumnType == DbColumnType.ARRAY_TIMESTAMP) {
					select.append("ST_ARRAYTIMESTAMP(split(#{" + daoAttributeId + ", typeHandler="
							+ HiveDateArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}, ',')) as ")
							.append(columnName).append(COMMA_WITH_SPACE);
				} else if (dbColumnType == DbColumnType.TIMESTAMP) {
					select.append("#{" + daoAttributeId + ", jdbcType=TIMESTAMP} as ").append(columnName)
							.append(COMMA_WITH_SPACE);
				} else if (dbColumnType == DbColumnType.GEOMETRY_4326 && entityDaoVO.containsKey(daoAttributeId)) {
					select.append("ST_AsGeoJson(ST_GeomFromGeoJSON(#{" + daoAttributeId + "})) as ").append(columnName)
							.append(COMMA_WITH_SPACE);
					// select.append("ST_DISKINDEX(ST_asText(ST_GeomFromGeoJSON(#{" + daoAttributeId + "}))) as ")
					// 		.append(columnName).append("_idx").append(COMMA_WITH_SPACE);
				} else if (dbColumnType == DbColumnType.GEOMETRY_3857 && entityDaoVO.containsKey(daoAttributeId)) {
					select.append("ST_AsGeoJson(ST_Transform(ST_FlipCoordinates(ST_GeomFromGeoJSON(#{" + daoAttributeId
							+ "})), 'epsg:4326','epsg:3857')) as ").append(columnName).append(COMMA_WITH_SPACE);
				} else {
					select.append("#{" + daoAttributeId + "} as ").append(columnName).append(COMMA_WITH_SPACE);
				}

				// update 되는 값에 대해서만 set 쿼리를 구성하도록 필터링 추가
				
				//if (updateQueryCols.contains(columnName)) {
					update.append(columnName).append(" = ").append("source.").append(columnName).append(COMMA_WITH_SPACE);

					// if (dbColumnType == DbColumnType.GEOMETRY_4326) {
					// 	update.append(columnName).append("_idx").append(" = ").append("source.").append(columnName)
					// 			.append("_idx").append(COMMA_WITH_SPACE);
					// }
				//}

				insertColumns.append(columnName).append(COMMA_WITH_SPACE);

				// if (dbColumnType == DbColumnType.GEOMETRY_4326) {
				// 	insertColumns.append(columnName).append("_idx").append(COMMA_WITH_SPACE);
				// }

				insertValues.append("source.").append(columnName).append(COMMA_WITH_SPACE);

				// if (dbColumnType == DbColumnType.GEOMETRY_4326) {
				// 	insertValues.append("source.").append(columnName).append("_idx").append(COMMA_WITH_SPACE);
				// }
			}
		}

		// 마지막 콤마 제거 ex) column, ) -> column)
		select.deleteCharAt(select.length() - 2);
		sql.append(select).append(") as source").append(SPACE);

		sql.append("ON target.ID = source.ID").append(SPACE);

		sql.append("WHEN MATCHED THEN UPDATE SET").append(SPACE);

		update.deleteCharAt(update.length() - 2);
		sql.append(update).append(SPACE);

		sql.append("WHEN NOT MATCHED THEN INSERT (");

		insertColumns.deleteCharAt(insertColumns.length() - 2);
		sql.append(insertColumns).append(") VALUES (");

		insertValues.deleteCharAt(insertValues.length() - 2);
		sql.append(insertValues).append(");");

		return sql.toString();
	}
	/**
	 * Replace Entity Bulk Attributes 시 사용될 sql을 동적 생성
	 *
	 * @param entityDaoVO entitySchema 기반으로 파싱되어 생성된 eneityDaoVO
	 * @return 생성된 sql문
	 */
	public String replaceAttrBulk(List<DynamicEntityDaoVO> entityDaoVOList) {
		StringBuilder sql = new StringBuilder();
		StringBuilder update = new StringBuilder();
		StringBuilder insertColumns = new StringBuilder();
		StringBuilder insertValues = new StringBuilder();
		CommonEntityDaoVO entityDaoVO = entityDaoVOList.get(0); 
		sql.append("MERGE INTO").append(SPACE).append(entityDaoVO.getDbTableName()).append(SPACE).append("as target")
				.append(SPACE);
		sql.append("USING").append(SPACE).append(entityDaoVO.getDbTableName()+"_tmp").append(SPACE).append("as source").append(SPACE);
		Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = entityDaoVO.getDbColumnInfoVOMap();
		if (dbColumnInfoVOMap != null) {
			update.append("MODIFIED_AT = source.MODIFIED_AT").append(COMMA_WITH_SPACE);
			if (entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()) != null) {
				update.append("DATASET_ID = source.DATASET_ID").append(COMMA_WITH_SPACE);
			}
			insertColumns.append("ID").append(COMMA_WITH_SPACE);
			insertColumns.append("CREATED_AT").append(COMMA_WITH_SPACE);
			insertColumns.append("MODIFIED_AT").append(COMMA_WITH_SPACE);
			if (entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()) != null) {
				insertColumns.append("DATASET_ID").append(COMMA_WITH_SPACE);
			}
			insertValues.append("source.ID").append(COMMA_WITH_SPACE);
			insertValues.append("source.CREATED_AT").append(COMMA_WITH_SPACE);
			insertValues.append("source.MODIFIED_AT").append(COMMA_WITH_SPACE);

			if (entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()) != null) {
				insertValues.append("source.DATASET_ID").append(COMMA_WITH_SPACE);
			}

			// 3. Dynamic Entity Column 설정
			for (DataModelDbColumnVO dbColumnInfoVO : dbColumnInfoVOMap.values()) {
				String columnName = dbColumnInfoVO.getColumnName();
				update.append(columnName).append(" = ").append("source.").append(columnName).append(COMMA_WITH_SPACE);
				insertColumns.append(columnName).append(COMMA_WITH_SPACE);
				insertValues.append("source.").append(columnName).append(COMMA_WITH_SPACE);

			}
		}
		sql.append("ON target.ID = source.ID").append(SPACE);
		sql.append("WHEN MATCHED THEN UPDATE SET").append(SPACE);
		update.deleteCharAt(update.length() - 2);
		sql.append(update).append(SPACE);
		sql.append("WHEN NOT MATCHED THEN INSERT (");
		insertColumns.deleteCharAt(insertColumns.length() - 2);
		sql.append(insertColumns).append(") VALUES (");
		insertValues.deleteCharAt(insertValues.length() - 2);
		sql.append(insertValues).append(");");
		return sql.toString();
	}

	public String replaceAttrHBase_backup(CommonEntityDaoVO entityDaoVO) {
		List<String> tableColumns = entityDaoVO.getTableColumns();
		StringBuilder sql = new StringBuilder();

		List<String> updateColumns = new ArrayList<>();

		// 테이블 설정
		sql.append("INSERT OVERWRITE TABLE").append(SPACE);
		sql.append(entityDaoVO.getDbTableName()).append(SPACE);

		// Update할 컬럼 설정
		sql.append("SELECT").append(SPACE);

		Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = entityDaoVO.getDbColumnInfoVOMap();
		if (dbColumnInfoVOMap != null) {
			updateColumns.add("NEW_MODIFIED_AT");
			tableColumns.replaceAll(e -> (e.equals("MODIFIED_AT")) ? "NEW_MODIFIED_AT" : e);

			if (entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()) != null) {
				updateColumns.add("NEW_DATASET_ID");
				tableColumns.replaceAll(e -> (e.equals("DATASET_ID")) ? "NEW_DATASET_ID" : e);
			}

			for (DataModelDbColumnVO dbColumnInfoVO : dbColumnInfoVOMap.values()) {
				String columnName = dbColumnInfoVO.getColumnName();
				DbColumnType dbColumnType = dbColumnInfoVO.getColumnType();

				updateColumns.add("NEW_" + dbColumnInfoVO.getColumnName());

				tableColumns.replaceAll(e -> (e.equals(dbColumnInfoVO.getColumnName())) ? e.contains("_4326")
						? "ST_AsGeoJson(ST_GeomFromGeoJSON(" + "NEW_" + dbColumnInfoVO.getColumnName() + "))"
						: e.contains("_3857")
								? "ST_AsGeoJson(ST_Transform(ST_FlipCoordinates(ST_FlipCoordinates(ST_GeomFromGeoJSON("
										+ "NEW_" + dbColumnInfoVO.getColumnName() + ")), 'epsg:4326','epsg:3857'))"
								: "NEW_" + dbColumnInfoVO.getColumnName()
						: e);
				tableColumns.replaceAll(e -> (e.equals(dbColumnInfoVO.getColumnName() + "_idx"))
						? "ST_DISKINDEX(ST_asText(ST_GeomFromGeoJSON(" + "NEW_" + dbColumnInfoVO.getColumnName() + ")))"
						: e);

				if (dbColumnType == DbColumnType.ARRAY_INTEGER) {
					tableColumns.replaceAll(e -> (e.equals(columnName)) ? "ST_ARRAYINT(" + columnName + ")" : e);
				} else if (dbColumnType == DbColumnType.ARRAY_FLOAT) {
					tableColumns.replaceAll(e -> (e.equals(columnName)) ? "ST_ARRAYDOUBLE(" + columnName + ")" : e);
				} else if (dbColumnType == DbColumnType.ARRAY_TIMESTAMP) {
					tableColumns.replaceAll(e -> (e.equals(columnName)) ? "ST_ARRAYTIMESTAMP(" + columnName + ")" : e);
				}
				// else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
				// tableColumns.replaceAll(e ->
				// (e.equals(columnName)) ? "ST_ARRAYBOOLEAN(" + columnName + ")" : e);
				// }
			}
		}

		String columnStrings = String.join(COMMA_WITH_SPACE, tableColumns);

		sql.append(columnStrings).append(SPACE);

		sql.append("FROM").append(SPACE);

		sql.append("(select * from ").append(entityDaoVO.getDbTableName()).append(" where id=#{id})")
				.append(COMMA_WITH_SPACE);

		// Update할 Values 설정
		sql.append("VALUES (");

		if (dbColumnInfoVOMap != null) {
			sql.append("from_utc_timestamp(#{").append(DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode())
					.append("},'UTC')").append(COMMA_WITH_SPACE);
			;

			if (entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()) != null) {
				sql.append("#{").append(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()).append("}")
						.append(COMMA_WITH_SPACE);
				;
			}

			for (DataModelDbColumnVO dbColumnInfoVO : dbColumnInfoVOMap.values()) {
				String daoAttributeId = dbColumnInfoVO.getDaoAttributeId();
				DbColumnType dbColumnType = dbColumnInfoVO.getColumnType();

				// Hive나 HBase는 Geometry타입이 별도로 존재하지 않기 때문에, 좌표계 정보포함하는 컬럼은 varchar로 처리하지 않음
				if (dbColumnType == DbColumnType.VARCHAR) {
					sql.append("#{").append(daoAttributeId).append(", jdbcType=VARCHAR}");
				} else if (dbColumnType == DbColumnType.INTEGER) {
					sql.append("#{").append(daoAttributeId).append(", jdbcType=INTEGER}");
				} else if (dbColumnType == DbColumnType.FLOAT) {
					sql.append("#{").append(daoAttributeId).append(", jdbcType=DOUBLE}");
				} else if (dbColumnType == DbColumnType.ARRAY_VARCHAR) {
					sql.append("split(").append("#{").append(daoAttributeId).append(", typeHandler=")
							.append(StringArrayListTypeHandler.class.getName()).append(", jdbcType=ARRAY}")
							.append(", ',')");
				} else if (dbColumnType == DbColumnType.ARRAY_INTEGER) {
					sql.append("split(").append("#{").append(daoAttributeId).append(", typeHandler=")
							.append(HiveIntegerArrayListTypeHandler.class.getName()).append(", jdbcType=ARRAY}")
							.append(", ',')");
				} else if (dbColumnType == DbColumnType.ARRAY_FLOAT) {
					sql.append("split(").append("#{").append(daoAttributeId).append(", typeHandler=")
							.append(HiveDoubleArrayListTypeHandler.class.getName()).append(", jdbcType=ARRAY}")
							.append(", ',')");
				} else if (dbColumnType == DbColumnType.ARRAY_TIMESTAMP) {
					sql.append("split(").append("#{").append(daoAttributeId).append(", typeHandler=")
							.append(HiveDateArrayListTypeHandler.class.getName()).append(", jdbcType=ARRAY}")
							.append(", ',')");
					// } else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
					// sql.append("split(").append("#{").append(daoAttributeId).append(",
					// typeHandler=").append(HiveBooleanArrayListTypeHandler.class.getName()).append(",
					// jdbcType=ARRAY}").append(", ',')");
				} else if (dbColumnType == DbColumnType.TIMESTAMP) {
					sql.append("from_utc_timestamp(#{").append(daoAttributeId).append("},'UTC')");
				} else if (dbColumnType == DbColumnType.GEOMETRY_4326) {
					sql.append("#{").append(daoAttributeId).append("}");
				} else if (dbColumnType == DbColumnType.GEOMETRY_3857) {
					sql.append("#{").append(daoAttributeId).append("}");
				} else if (dbColumnType == DbColumnType.BOOLEAN) {
					sql.append("#{").append(daoAttributeId).append(", jdbcType=BOOLEAN}");
				} else {
					sql.append("#{").append(daoAttributeId).append(", jdbcType=VARCHAR}");
				}

				sql.append(COMMA_WITH_SPACE);
			}

			sql.deleteCharAt(sql.lastIndexOf(COMMA_WITH_SPACE));
			sql.append(")");
			sql.append(" AS ").append("(").append(String.join(COMMA_WITH_SPACE, updateColumns)).append(");");
		}

		return sql.toString();
	}

	public String replaceAttrHBase(CommonEntityDaoVO entityDaoVO) {
		List<String> tableColumns = entityDaoVO.getTableColumns();
		StringBuilder sql = new StringBuilder();

		List<String> updateColumns = new ArrayList<>();

		// 테이블 설정
		sql.append("UPDATE").append(SPACE);
		sql.append(entityDaoVO.getDbTableName()).append(SPACE);

		// Update할 컬럼 설정
		sql.append("SET").append(SPACE);

		Set<String> updateQueryCols = entityDaoVO.keySet();

		Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = entityDaoVO.getDbColumnInfoVOMap();
		if (dbColumnInfoVOMap != null) {
			if (entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()) != null) {
				updateColumns.add("DATASET_ID");
			}

			updateColumns.add("MODIFIED_AT");

			for (DataModelDbColumnVO dbColumnInfoVO : dbColumnInfoVOMap.values()) {
				String columnName = dbColumnInfoVO.getColumnName();
				DbColumnType dbColumnType = dbColumnInfoVO.getColumnType();

				if (updateQueryCols.contains(columnName)) {
					updateColumns.add(dbColumnInfoVO.getColumnName().toUpperCase());

					tableColumns.replaceAll(e -> (e.equals(dbColumnInfoVO.getColumnName())) ? e.contains("_4326")
							? "ST_AsGeoJson(ST_GeomFromGeoJSON(" + dbColumnInfoVO.getColumnName() + "))"
							: e.contains("_3857")
							? "ST_AsGeoJson(ST_Transform(ST_FlipCoordinates(ST_FlipCoordinates(ST_GeomFromGeoJSON("
							+ dbColumnInfoVO.getColumnName() + ")), 'epsg:4326','epsg:3857'))"
							: dbColumnInfoVO.getColumnName()
							: e);
					tableColumns.replaceAll(e -> (e.equals(dbColumnInfoVO.getColumnName() + "_idx"))
							? "ST_DISKINDEX(ST_asText(ST_GeomFromGeoJSON(" + dbColumnInfoVO.getColumnName() + ")))"
							: e);

					if (dbColumnType == DbColumnType.ARRAY_INTEGER) {
						tableColumns.replaceAll(e -> (e.equals(columnName)) ? "ST_ARRAYINT(" + columnName + ")" : e);
					} else if (dbColumnType == DbColumnType.ARRAY_FLOAT) {
						tableColumns.replaceAll(e -> (e.equals(columnName)) ? "ST_ARRAYDOUBLE(" + columnName + ")" : e);
					} else if (dbColumnType == DbColumnType.ARRAY_TIMESTAMP) {
						tableColumns.replaceAll(e -> (e.equals(columnName)) ? "ST_ARRAYTIMESTAMP(" + columnName + ")" : e);
					}
					// else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
					// tableColumns.replaceAll(e ->
					// (e.equals(columnName)) ? "ST_ARRAYBOOLEAN(" + columnName + ")" : e);
					// }
				}

			}
		}

		List<String> updateValues = new ArrayList<>();
		if (dbColumnInfoVOMap != null) {
			if (entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()) != null) {
				updateValues.add("#{" + DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode() + "}");
			}

			updateValues.add("from_utc_timestamp(#{" + DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode()
					+ "},'UTC')");

			for (DataModelDbColumnVO dbColumnInfoVO : dbColumnInfoVOMap.values()) {
				String daoAttributeId = dbColumnInfoVO.getDaoAttributeId();
				DbColumnType dbColumnType = dbColumnInfoVO.getColumnType();

				if (updateQueryCols.contains(dbColumnInfoVO.getColumnName())) {

					// Hive나 HBase는 Geometry타입이 별도로 존재하지 않기 때문에, 좌표계 정보포함하는 컬럼은 varchar로 처리하지 않음
					if (dbColumnType == DbColumnType.VARCHAR) {
						updateValues.add("#{" + daoAttributeId + ", jdbcType=VARCHAR}");
					} else if (dbColumnType == DbColumnType.INTEGER) {
						updateValues.add("#{" + daoAttributeId + ", jdbcType=INTEGER}");
					} else if (dbColumnType == DbColumnType.FLOAT) {
						updateValues.add("#{" + daoAttributeId + ", jdbcType=DOUBLE}");
					} else if (dbColumnType == DbColumnType.ARRAY_VARCHAR) {
						updateValues.add("split(" + "#{" + daoAttributeId + ", typeHandler="
								+ StringArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}" + ", ',')");
					} else if (dbColumnType == DbColumnType.ARRAY_INTEGER) {
						updateValues.add("split(" + "#{" + daoAttributeId + ", typeHandler="
								+ HiveIntegerArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}" + ", ',')");
					} else if (dbColumnType == DbColumnType.ARRAY_FLOAT) {
						updateValues.add("split(" + "#{" + daoAttributeId + ", typeHandler="
								+ HiveDoubleArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}" + ", ',')");
					} else if (dbColumnType == DbColumnType.ARRAY_TIMESTAMP) {
						updateValues.add("split(" + "#{" + daoAttributeId + ", typeHandler="
								+ HiveDateArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}" + ", ',')");
						// } else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
						// sql.append("split(").append("#{").append(daoAttributeId).append(",
						// typeHandler=").append(HiveBooleanArrayListTypeHandler.class.getName()).append(",
						// jdbcType=ARRAY}").append(", ',')");
					} else if (dbColumnType == DbColumnType.TIMESTAMP) {
						updateValues.add("from_utc_timestamp(#{" + daoAttributeId + "},'UTC')");
					} else if (dbColumnType == DbColumnType.GEOMETRY_4326) {
						updateValues.add("#{" + daoAttributeId + "}");
					} else if (dbColumnType == DbColumnType.GEOMETRY_3857) {
						updateValues.add("#{" + daoAttributeId + "}");
					} else if (dbColumnType == DbColumnType.BOOLEAN) {
						updateValues.add("#{" + daoAttributeId + ", jdbcType=BOOLEAN}");
					} else {
						updateValues.add("#{" + daoAttributeId + ", jdbcType=VARCHAR}");
					}
				}

			}

			List<String> originialTableColumns = entityDaoVO.getTableColumns();
			int cnt = 0;
			for (int i = 0; i < originialTableColumns.size(); i++) {
				String originalColumn = originialTableColumns.get(i);
				String convertedColumn = tableColumns.get(i);
				if (updateColumns.contains(originalColumn)) {
					String updateValue = convertedColumn.replace(originalColumn, updateValues.get(cnt));
					cnt++;
					sql.append(originalColumn + "=" + updateValue).append(COMMA_WITH_SPACE);
				}
			}

			sql.deleteCharAt(sql.lastIndexOf(COMMA_WITH_SPACE));
			sql.append(" where id=#{id}");

		}
		
		return sql.toString();
	}
	public String replaceAttrHBaseBulk(List<DynamicEntityDaoVO> entityDaoVOList) {
		DynamicEntityDaoVO entityDaoVO = entityDaoVOList.get(0);
		List<String> tableColumns = entityDaoVO.getTableColumns();
		StringBuilder sql = new StringBuilder();

		List<String> updateColumns = new ArrayList<>();

		// 테이블 설정
		sql.append("UPDATE").append(SPACE);
		sql.append(entityDaoVO.getDbTableName()).append(SPACE);

		// Update할 컬럼 설정
		sql.append("SET").append(SPACE);

		Set<String> updateQueryCols = entityDaoVO.keySet();

		Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = entityDaoVO.getDbColumnInfoVOMap();
		if (dbColumnInfoVOMap != null) {
			if (entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()) != null) {
				updateColumns.add("DATASET_ID");
			}

			updateColumns.add("MODIFIED_AT");

			for (DataModelDbColumnVO dbColumnInfoVO : dbColumnInfoVOMap.values()) {
				String columnName = dbColumnInfoVO.getColumnName();
				DbColumnType dbColumnType = dbColumnInfoVO.getColumnType();

				if (updateQueryCols.contains(columnName)) {
					updateColumns.add(dbColumnInfoVO.getColumnName().toUpperCase());

					tableColumns.replaceAll(e -> (e.equals(dbColumnInfoVO.getColumnName())) ? e.contains("_4326")
							? "ST_AsGeoJson(ST_GeomFromGeoJSON(" + dbColumnInfoVO.getColumnName() + "))"
							: e.contains("_3857")
							? "ST_AsGeoJson(ST_Transform(ST_FlipCoordinates(ST_FlipCoordinates(ST_GeomFromGeoJSON("
							+ dbColumnInfoVO.getColumnName() + ")), 'epsg:4326','epsg:3857'))"
							: dbColumnInfoVO.getColumnName()
							: e);
					tableColumns.replaceAll(e -> (e.equals(dbColumnInfoVO.getColumnName() + "_idx"))
							? "ST_DISKINDEX(ST_asText(ST_GeomFromGeoJSON(" + dbColumnInfoVO.getColumnName() + ")))"
							: e);

					if (dbColumnType == DbColumnType.ARRAY_INTEGER) {
						tableColumns.replaceAll(e -> (e.equals(columnName)) ? "ST_ARRAYINT(" + columnName + ")" : e);
					} else if (dbColumnType == DbColumnType.ARRAY_FLOAT) {
						tableColumns.replaceAll(e -> (e.equals(columnName)) ? "ST_ARRAYDOUBLE(" + columnName + ")" : e);
					} else if (dbColumnType == DbColumnType.ARRAY_TIMESTAMP) {
						tableColumns.replaceAll(e -> (e.equals(columnName)) ? "ST_ARRAYTIMESTAMP(" + columnName + ")" : e);
					}
					// else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
					// tableColumns.replaceAll(e ->
					// (e.equals(columnName)) ? "ST_ARRAYBOOLEAN(" + columnName + ")" : e);
					// }
				}

			}
		}

		List<String> updateValues = new ArrayList<>();
		if (dbColumnInfoVOMap != null) {
			if (entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()) != null) {
				updateValues.add("#{" + DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode() + "}");
			}

			updateValues.add("from_utc_timestamp(#{" + DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode()
					+ "},'UTC')");

			for (DataModelDbColumnVO dbColumnInfoVO : dbColumnInfoVOMap.values()) {
				String daoAttributeId = dbColumnInfoVO.getDaoAttributeId();
				DbColumnType dbColumnType = dbColumnInfoVO.getColumnType();

				if (updateQueryCols.contains(dbColumnInfoVO.getColumnName())) {

					// Hive나 HBase는 Geometry타입이 별도로 존재하지 않기 때문에, 좌표계 정보포함하는 컬럼은 varchar로 처리하지 않음
					if (dbColumnType == DbColumnType.VARCHAR) {
						updateValues.add("#{" + daoAttributeId + ", jdbcType=VARCHAR}");
					} else if (dbColumnType == DbColumnType.INTEGER) {
						updateValues.add("#{" + daoAttributeId + ", jdbcType=INTEGER}");
					} else if (dbColumnType == DbColumnType.FLOAT) {
						updateValues.add("#{" + daoAttributeId + ", jdbcType=DOUBLE}");
					} else if (dbColumnType == DbColumnType.ARRAY_VARCHAR) {
						updateValues.add("split(" + "#{" + daoAttributeId + ", typeHandler="
								+ StringArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}" + ", ',')");
					} else if (dbColumnType == DbColumnType.ARRAY_INTEGER) {
						updateValues.add("split(" + "#{" + daoAttributeId + ", typeHandler="
								+ HiveIntegerArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}" + ", ',')");
					} else if (dbColumnType == DbColumnType.ARRAY_FLOAT) {
						updateValues.add("split(" + "#{" + daoAttributeId + ", typeHandler="
								+ HiveDoubleArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}" + ", ',')");
					} else if (dbColumnType == DbColumnType.ARRAY_TIMESTAMP) {
						updateValues.add("split(" + "#{" + daoAttributeId + ", typeHandler="
								+ HiveDateArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}" + ", ',')");
						// } else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
						// sql.append("split(").append("#{").append(daoAttributeId).append(",
						// typeHandler=").append(HiveBooleanArrayListTypeHandler.class.getName()).append(",
						// jdbcType=ARRAY}").append(", ',')");
					} else if (dbColumnType == DbColumnType.TIMESTAMP) {
						updateValues.add("from_utc_timestamp(#{" + daoAttributeId + "},'UTC')");
					} else if (dbColumnType == DbColumnType.GEOMETRY_4326) {
						updateValues.add("#{" + daoAttributeId + "}");
					} else if (dbColumnType == DbColumnType.GEOMETRY_3857) {
						updateValues.add("#{" + daoAttributeId + "}");
					} else if (dbColumnType == DbColumnType.BOOLEAN) {
						updateValues.add("#{" + daoAttributeId + ", jdbcType=BOOLEAN}");
					} else {
						updateValues.add("#{" + daoAttributeId + ", jdbcType=VARCHAR}");
					}
				}

			}

			List<String> originialTableColumns = entityDaoVO.getTableColumns();
			int cnt = 0;
			for (int i = 0; i < originialTableColumns.size(); i++) {
				String originalColumn = originialTableColumns.get(i);
				String convertedColumn = tableColumns.get(i);
				if (updateColumns.contains(originalColumn)) {
					String updateValue = convertedColumn.replace(originalColumn, updateValues.get(cnt));
					cnt++;
					sql.append(originalColumn + "=" + updateValue).append(COMMA_WITH_SPACE);
				}
			}

			sql.deleteCharAt(sql.lastIndexOf(COMMA_WITH_SPACE));
			sql.append(" where id=#{id}");

		}

		return sql.toString();
	}



	/**
	 * Append Entity Attributes 시 사용될 sql을 동적 생성
	 *
	 * @param entityDaoVO entitySchema 기반으로 파싱되어 생성된 eneityDaoVO
	 * @return 생성된 sql문
	 */
	public String appendAttr(CommonEntityDaoVO entityDaoVO) {
		StringBuilder sql = new StringBuilder();
		StringBuilder select = new StringBuilder();
		StringBuilder update = new StringBuilder();
		StringBuilder updateColumns = new StringBuilder();
		StringBuilder updateValues = new StringBuilder();


		sql.append("MERGE INTO").append(SPACE).append(entityDaoVO.getDbTableName()).append(SPACE).append("as target")
				.append(SPACE);
		sql.append("USING (select").append(SPACE);
		Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = entityDaoVO.getDbColumnInfoVOMap();
		if (dbColumnInfoVOMap != null) {

			// 2. Default Column 설정
			select.append("#{" + DataServiceBrokerCode.DefaultAttributeKey.ID.getCode() + "} as ID")
					.append(COMMA_WITH_SPACE);

			select.append("from_utc_timestamp(#{")
					.append(DataServiceBrokerCode.DefaultAttributeKey.CREATED_AT.getCode())
					.append("}, 'UTC') as CREATED_AT").append(COMMA_WITH_SPACE);
			select.append("from_utc_timestamp(#{").append(
					DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode() + "}, 'UTC') as MODIFIED_AT")
					.append(COMMA_WITH_SPACE);

			if (entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()) != null) {
				select.append("#{" + DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode() + "} as DATASET_ID")
						.append(COMMA_WITH_SPACE);
			}

			update.append("MODIFIED_AT = source.MODIFIED_AT").append(COMMA_WITH_SPACE);


			if (entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()) != null) {
				updateValues.append("source.DATASET_ID").append(COMMA_WITH_SPACE);
			}

			// 3. Dynamic Entity Column 설정
			//Set<String> updateQueryCols = entityDaoVO.keySet();
			for (DataModelDbColumnVO dbColumnInfoVO : dbColumnInfoVOMap.values()) {
				if (entityDaoVO.get(dbColumnInfoVO.getDaoAttributeId()) == null) {
					continue;
				}
				String daoAttributeId = dbColumnInfoVO.getDaoAttributeId();
				String columnName = dbColumnInfoVO.getColumnName();
				DbColumnType dbColumnType = dbColumnInfoVO.getColumnType();

				if (dbColumnType == DbColumnType.VARCHAR) {
					select.append("#{" + daoAttributeId + ", jdbcType=VARCHAR} as ").append(columnName)
							.append(COMMA_WITH_SPACE);
				} else if (dbColumnType == DbColumnType.INTEGER) {
					select.append("#{" + daoAttributeId + ", jdbcType=INTEGER} as ").append(columnName)
							.append(COMMA_WITH_SPACE);
				} else if (dbColumnType == DbColumnType.FLOAT) {
					select.append("#{" + daoAttributeId + ", jdbcType=FLOAT} as ").append(columnName)
							.append(COMMA_WITH_SPACE);
				} else if (dbColumnType == DbColumnType.BOOLEAN) {
					select.append("#{").append(daoAttributeId).append(", jdbcType=BOOLEAN} as ").append(columnName)
							.append(COMMA_WITH_SPACE);
				} else if (dbColumnType == DbColumnType.ARRAY_VARCHAR) {
					select.append("split(#{" + daoAttributeId + ", typeHandler="
							+ StringArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}, ',') as ")
							.append(columnName).append(COMMA_WITH_SPACE);
				} else if (dbColumnType == DbColumnType.ARRAY_INTEGER) {
					select.append("ST_ARRAYINT(split(#{" + daoAttributeId + ", typeHandler="
							+ HiveIntegerArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}, ',')) as ")
							.append(columnName).append(COMMA_WITH_SPACE);
				} else if (dbColumnType == DbColumnType.ARRAY_FLOAT) {
					select.append("ST_ARRAYDOUBLE(split(#{" + daoAttributeId + ", typeHandler="
							+ HiveDoubleArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}, ',')) as ")
							.append(columnName).append(COMMA_WITH_SPACE);
				} else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
					select.append("ST_ARRAYBOOLEAN(split(#{" + daoAttributeId + ", typeHandler="
							+ HiveBooleanArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}, ',')) as ")
							.append(columnName).append(COMMA_WITH_SPACE);
				} else if (dbColumnType == DbColumnType.ARRAY_TIMESTAMP) {
					select.append("ST_ARRAYTIMESTAMP(split(#{" + daoAttributeId + ", typeHandler="
							+ HiveDateArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}, ',')) as ")
							.append(columnName).append(COMMA_WITH_SPACE);
				} else if (dbColumnType == DbColumnType.TIMESTAMP) {
					select.append("#{" + daoAttributeId + ", jdbcType=TIMESTAMP} as ").append(columnName)
							.append(COMMA_WITH_SPACE);
				} else if (dbColumnType == DbColumnType.GEOMETRY_4326 && entityDaoVO.containsKey(daoAttributeId)) {
					select.append("ST_AsGeoJson(ST_GeomFromGeoJSON(#{" + daoAttributeId + "})) as ").append(columnName)
							.append(COMMA_WITH_SPACE);
					// select.append("ST_DISKINDEX(ST_asText(ST_GeomFromGeoJSON(#{" + daoAttributeId + "}))) as ")
					// 		.append(columnName).append("_idx").append(COMMA_WITH_SPACE);
				} else if (dbColumnType == DbColumnType.GEOMETRY_3857 && entityDaoVO.containsKey(daoAttributeId)) {
					select.append("ST_AsGeoJson(ST_Transform(ST_FlipCoordinates(ST_GeomFromGeoJSON(#{" + daoAttributeId
							+ "})), 'epsg:4326','epsg:3857')) as ").append(columnName).append(COMMA_WITH_SPACE);
				} else {
					select.append("#{" + daoAttributeId + "} as ").append(columnName).append(COMMA_WITH_SPACE);
				}

				// update 되는 값에 대해서만 set 쿼리를 구성하도록 필터링 추가
				update.append(columnName).append(" = ").append("source.").append(columnName).append(COMMA_WITH_SPACE);
				updateColumns.append(columnName).append(COMMA_WITH_SPACE);

				updateValues.append("source.").append(columnName).append(COMMA_WITH_SPACE);
			}
		}

		// 마지막 콤마 제거 ex) column, ) -> column)
		select.deleteCharAt(select.length() - 2);
		sql.append(select).append(") as source").append(SPACE);

		sql.append("ON target.ID = source.ID").append(SPACE);

		sql.append("WHEN MATCHED THEN UPDATE SET").append(SPACE);

		update.deleteCharAt(update.length() - 2);
		sql.append(update).append(";");

	
		return sql.toString();

		
	}

	/**
	 * Append Entity (noOverwrite) Attributes 시 사용될 sql을 동적 생성
	 *
	 * @param entityDaoVO entitySchema 기반으로 파싱되어 생성된 eneityDaoVO
	 * @return 생성된 sql문
	 */
	public String appendNoOverwriteAttr(CommonEntityDaoVO entityDaoVO) {

		List<String> tableColumns = entityDaoVO.getTableColumns();

		StringBuilder sql = new StringBuilder();

		List<String> updateColumns = new ArrayList<>();

		// 테이블 설정
		sql.append("INSERT OVERWRITE TABLE").append(SPACE);
		sql.append(entityDaoVO.getDbTableName()).append(SPACE);
		// sql.append("PARTITION (ID)").append(SPACE);

		// Update할 컬럼 설정
		sql.append("SELECT").append(SPACE);

		Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = entityDaoVO.getDbColumnInfoVOMap();
		if (dbColumnInfoVOMap != null) {
			updateColumns.add("NEW_MODIFIED_AT");
			tableColumns.replaceAll(e -> (e.equals("MODIFIED_AT")) ? "NEW_MODIFIED_AT" : e);

			if (entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()) != null) {
				updateColumns.add("NEW_DATASET_ID");
				tableColumns.replaceAll(e -> (e.equals("DATASET_ID")) ? "NEW_DATASET_ID" : e);
			}

			for (DataModelDbColumnVO dbColumnInfoVO : dbColumnInfoVOMap.values()) {
				if (entityDaoVO.get(dbColumnInfoVO.getDaoAttributeId()) == null) {
					continue;
				}

				String columnName = dbColumnInfoVO.getColumnName();
				DbColumnType dbColumnType = dbColumnInfoVO.getColumnType();

				updateColumns.add("NEW_" + dbColumnInfoVO.getColumnName());
				tableColumns.replaceAll(e -> (e.equals(dbColumnInfoVO.getColumnName())) ? e.contains("_4326")
						? "ST_AsGeoJson(ST_GeomFromGeoJSON(" + "NEW_" + dbColumnInfoVO.getColumnName() + "))"
						: e.contains("_3857")
								? "ST_AsGeoJson(ST_Transform(ST_FlipCoordinates(ST_FlipCoordinates(ST_GeomFromGeoJSON("
										+ "NEW_" + dbColumnInfoVO.getColumnName() + ")), 'epsg:4326','epsg:3857'))"
								: "NEW_" + dbColumnInfoVO.getColumnName()
						: e);
				tableColumns.replaceAll(e -> (e.equals(dbColumnInfoVO.getColumnName() + "_idx"))
						? "ST_DISKINDEX(ST_asText(ST_GeomFromGeoJSON(" + "NEW_" + dbColumnInfoVO.getColumnName() + ")))"
						: e);

				if (dbColumnType == DbColumnType.ARRAY_INTEGER) {
					tableColumns.replaceAll(e -> (e.equals(columnName)) ? "ST_ARRAYINT(" + columnName + ")" : e);
				} else if (dbColumnType == DbColumnType.ARRAY_FLOAT) {
					tableColumns.replaceAll(e -> (e.equals(columnName)) ? "ST_ARRAYDOUBLE(" + columnName + ")" : e);
				} else if (dbColumnType == DbColumnType.ARRAY_TIMESTAMP) {
					tableColumns.replaceAll(e -> (e.equals(columnName)) ? "ST_ARRAYTIMESTAMP(" + columnName + ")" : e);
				}
				// else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
				// tableColumns.replaceAll(e ->
				// (e.equals(columnName)) ? "ST_ARRAYBOOLEAN(" + columnName + ")" : e);
				// }
			}
		}

		String columnStrings = String.join(COMMA_WITH_SPACE, tableColumns);

		sql.append(columnStrings).append(SPACE);

		sql.append("FROM").append(SPACE);

		sql.append("(select * from ").append(entityDaoVO.getDbTableName()).append(" where id=#{id})")
				.append(COMMA_WITH_SPACE);

		// Update할 Values 설정
		sql.append("VALUES (");

		if (dbColumnInfoVOMap != null) {
			sql.append("from_utc_timestamp(#{").append(DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode())
					.append("}, 'UTC')").append(COMMA_WITH_SPACE);
			;

			if (entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()) != null) {
				sql.append("#{").append(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()).append("}")
						.append(COMMA_WITH_SPACE);
				;
			}

			for (DataModelDbColumnVO dbColumnInfoVO : dbColumnInfoVOMap.values()) {

				if (entityDaoVO.get(dbColumnInfoVO.getDaoAttributeId()) == null) {
					continue;
				}

				String daoAttributeId = dbColumnInfoVO.getDaoAttributeId();
				DbColumnType dbColumnType = dbColumnInfoVO.getColumnType();

				// Hive나 HBase는 Geometry타입이 별도로 존재하지 않기 때문에, 좌표계 정보포함하는 컬럼은 varchar로 처리하지 않음
				if (dbColumnType == DbColumnType.VARCHAR) {
					sql.append("COALESCE(#{").append(daoAttributeId).append(", jdbcType=VARCHAR})");
				} else if (dbColumnType == DbColumnType.INTEGER) {
					sql.append("COALESCE(#{").append(daoAttributeId).append(", jdbcType=INTEGER})");
				} else if (dbColumnType == DbColumnType.FLOAT) {
					sql.append("COALESCE(#{").append(daoAttributeId).append(", jdbcType=DOUBLE})");
				} else if (dbColumnType == DbColumnType.ARRAY_VARCHAR) {
					sql.append("COALESCE(array(").append("#{").append(daoAttributeId).append(", typeHandler=")
							.append(StringArrayListTypeHandler.class.getName()).append(", jdbcType=ARRAY}")
							.append("))");
				} else if (dbColumnType == DbColumnType.ARRAY_INTEGER) {
					sql.append("COALESCE(array(").append("#{").append(daoAttributeId).append(", typeHandler=")
							.append(HiveIntegerArrayListTypeHandler.class.getName()).append(", jdbcType=ARRAY}")
							.append("))");
				} else if (dbColumnType == DbColumnType.ARRAY_FLOAT) {
					sql.append("COALESCE(array(").append("#{").append(daoAttributeId).append(", typeHandler=")
							.append(HiveDoubleArrayListTypeHandler.class.getName()).append(", jdbcType=ARRAY}")
							.append("))");
				} else if (dbColumnType == DbColumnType.ARRAY_TIMESTAMP) {
					sql.append("COALESCE(array(").append("#{").append(daoAttributeId).append(", typeHandler=")
							.append(HiveDateArrayListTypeHandler.class.getName()).append(", jdbcType=ARRAY}")
							.append("))");
					// } else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
					// sql.append("COALESCE(array(").append("#{").append(daoAttributeId).append(",
					// typeHandler=").append(HiveBooleanArrayListTypeHandler.class.getName()).append(",
					// jdbcType=ARRAY}").append("))");
				} else if (dbColumnType == DbColumnType.TIMESTAMP) {
					sql.append("COALESCE(from_utc_timestamp(#{").append(daoAttributeId).append("}, 'UTC'))");
				} else if (dbColumnType == DbColumnType.GEOMETRY_4326) { // 컬럼에 4326을 포함하는 경우, Geometry로 간주하여 처리
					sql.append("COALESCE(#{").append(daoAttributeId).append("})");
				} else if (dbColumnType == DbColumnType.GEOMETRY_3857) { // 컬럼에 3857을 포함하는 경우, Geometry로 간주하여 처리
					sql.append("COALESCE(#{").append(daoAttributeId).append("})");
				} else if (dbColumnType == DbColumnType.BOOLEAN) {
					sql.append("COALESCE(#{").append(daoAttributeId).append(", jdbcType=BOOLEAN})");
				} else {
					sql.append("COALESCE(#{").append(daoAttributeId).append(", jdbcType=VARCHAR})");
				}

				sql.append(COMMA_WITH_SPACE);
			}

			sql.deleteCharAt(sql.lastIndexOf(COMMA_WITH_SPACE));
			sql.append(")");
			sql.append(" AS ").append("(").append(String.join(",", updateColumns)).append(");");
		}
		return sql.toString();
	}

	/**
	 * Update Entity Attributes 시 사용될 sql을 동적 생성 Dtonic Comment: Bulk Update에서만 사용.
	 * 현재 Hive JDBC Driver는 Bulk Insert, Update를 지원하지 않아 single insert, update 사용 추후
	 * 벌크로 저장하거나 수정할 수 있게 되면, 수정하여 사용할 예정
	 *
	 * @param entityDaoVO entitySchema 기반으로 파싱되어 생성된 eneityDaoVO
	 * @return 생성된 sql문
	 */
	public String updateAttr(CommonEntityDaoVO entityDaoVO) {
		// 원본코드 SET(columnName + " = CASE WHEN " + columnName + " IS NOT NULL THEN #{" +
		// daoAttributeId + ", jdbcType=VARCHAR} END");
		// 위와같이 IF 문이 사용 HIVE에서 사용되는지 필요
		// Bulk 시에만 사용

		SQL sql = new SQL() {
			{
				// 1. 테이블명 설정
				UPDATE(entityDaoVO.getDbTableName());
				Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = entityDaoVO.getDbColumnInfoVOMap();
				if (dbColumnInfoVOMap != null) {

					// 2. Default Column 설정
					SET("MODIFIED_AT = #{" + DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode() + "}");
					if (entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()) != null) {
						SET("DATASET_ID = #{" + DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode() + "}");
					}

					// 3. Dynamic Entity Column 설정
					for (DataModelDbColumnVO dbColumnInfoVO : dbColumnInfoVOMap.values()) {

						if (entityDaoVO.get(dbColumnInfoVO.getDaoAttributeId()) == null) {
							continue;
						}

						String daoAttributeId = dbColumnInfoVO.getDaoAttributeId();
						String columnName = dbColumnInfoVO.getColumnName();
						DbColumnType dbColumnType = dbColumnInfoVO.getColumnType();

						if (dbColumnType == DbColumnType.VARCHAR) {
							SET(columnName + " = CASE WHEN " + columnName + " IS NOT NULL THEN #{" + daoAttributeId
									+ ", jdbcType=VARCHAR} END");
						} else if (dbColumnType == DbColumnType.INTEGER) {
							SET(columnName + " = CASE WHEN " + columnName + " IS NOT NULL THEN #{" + daoAttributeId
									+ ", jdbcType=INTEGER} END::DOUBLE");
						} else if (dbColumnType == DbColumnType.FLOAT) {
							SET(columnName + " = CASE WHEN " + columnName + " IS NOT NULL THEN #{" + daoAttributeId
									+ ", jdbcType=DOUBLE} END::DOUBLE");
						} else if (dbColumnType == DbColumnType.BOOLEAN) {
							SET(columnName + " = CASE WHEN " + columnName + " IS NOT NULL THEN #{" + daoAttributeId
									+ ", jdbcType=BOOLEAN} END::BOOLEAN");
						} else if (dbColumnType == DbColumnType.ARRAY_VARCHAR) {
							SET(columnName + " = CASE WHEN " + columnName + " IS NOT NULL THEN #{" + daoAttributeId
									+ ", typeHandler=" + StringArrayListTypeHandler.class.getName()
									+ ", jdbcType=ARRAY}::ARRAY<STRING> END");
						} else if (dbColumnType == DbColumnType.ARRAY_INTEGER) {
							SET(columnName + " = CASE WHEN " + columnName + " IS NOT NULL THEN #{" + daoAttributeId
									+ ", typeHandler=" + HiveIntegerArrayListTypeHandler.class.getName()
									+ ", jdbcType=ARRAY}::ARRAY<INTEGER> END");
						} else if (dbColumnType == DbColumnType.ARRAY_FLOAT) {
							SET(columnName + " = CASE WHEN " + columnName + " IS NOT NULL THEN #{" + daoAttributeId
									+ ", typeHandler=" + HiveDoubleArrayListTypeHandler.class.getName()
									+ ", jdbcType=ARRAY}::ARRAY<DOUBLE> END");
						} else if (dbColumnType == DbColumnType.ARRAY_TIMESTAMP) {
							SET(columnName + " = CASE WHEN " + columnName + " IS NOT NULL THEN #{" + daoAttributeId
									+ ", typeHandler=" + HiveDateArrayListTypeHandler.class.getName()
									+ ", jdbcType=ARRAY}::ARRAY<TIMESTAMP> END");
							// } else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
							// SET(columnName + " = CASE WHEN " + columnName + " IS NOT NULL THEN #{" +
							// daoAttributeId + ", typeHandler=" +
							// HiveBooleanArrayListTypeHandler.class.getName() + ",
							// jdbcType=ARRAY}::ARRAY<BOOLEAN> END");
						} else if (dbColumnType == DbColumnType.TIMESTAMP) {
							SET(columnName + " = CASE WHEN " + columnName + " IS NOT NULL THEN #{" + daoAttributeId
									+ ", jdbcType=TIMESTAMP}::TIMESTAMP WITH TIME ZONE END");
						} else if (dbColumnType == DbColumnType.GEOMETRY_4326) {
							SET(columnName + " = CASE WHEN " + columnName
									+ " IS NOT NULL THEN ST_SetSRID(ST_GeomFromGeoJSON(#{" + daoAttributeId
									+ "}), 4326) END");
						} else if (dbColumnType == DbColumnType.GEOMETRY_3857) {
							SET(columnName + " = CASE WHEN " + columnName
									+ " IS NOT NULL THEN ST_Transform(ST_FlipCoordinates(ST_SetSRID(ST_GeomFromGeoJSON(#{"
									+ daoAttributeId + "})), 4326), 3857) END");
						} else {
							SET(columnName + " = CASE WHEN " + columnName + " IS NOT NULL THEN #{" + daoAttributeId
									+ "} END");
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
	 * 
	 * @param entityDaoVO entitySchema 기반으로 파싱되어 생성된 eneityDaoVO
	 * @return 생성된 sql문
	 */
	public String partialAttrUpdate(CommonEntityDaoVO entityDaoVO) {

		List<String> tableColumns = entityDaoVO.getTableColumns();

		StringBuilder sql = new StringBuilder();

		List<String> updateColumns = new ArrayList<>();

		// 테이블 설정
		sql.append("INSERT OVERWRITE TABLE").append(SPACE);
		sql.append(entityDaoVO.getDbTableName()).append(SPACE);
		// sql.append("PARTITION (ID)").append(SPACE);

		// Update할 컬럼 설정
		sql.append("SELECT").append(SPACE);

		Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = entityDaoVO.getDbColumnInfoVOMap();
		if (dbColumnInfoVOMap != null) {
			updateColumns.add("NEW_MODIFIED_AT");
			tableColumns.replaceAll(e -> (e.equals("MODIFIED_AT")) ? "NEW_MODIFIED_AT" : e);

			if (entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()) != null) {
				updateColumns.add("NEW_DATASET_ID");
				tableColumns.replaceAll(e -> (e.equals("DATASET_ID")) ? "NEW_DATASET_ID" : e);
			}

			for (DataModelDbColumnVO dbColumnInfoVO : dbColumnInfoVOMap.values()) {

				if (entityDaoVO.get(dbColumnInfoVO.getDaoAttributeId()) == null) {
					continue;
				}

				String columnName = dbColumnInfoVO.getColumnName();
				DbColumnType dbColumnType = dbColumnInfoVO.getColumnType();

				updateColumns.add("NEW_" + dbColumnInfoVO.getColumnName());
				tableColumns.replaceAll(e -> (e.equals(dbColumnInfoVO.getColumnName())) ? e.contains("_4326")
						? "ST_AsGeoJson(ST_GeomFromGeoJSON(" + "NEW_" + dbColumnInfoVO.getColumnName() + "))"
						: e.contains("_3857")
								? "ST_AsGeoJson(ST_Transform(ST_FlipCoordinates(ST_GeomFromGeoJSON(" + "NEW_"
										+ dbColumnInfoVO.getColumnName() + ")), 'epsg:4326','epsg:3857'))"
								: "NEW_" + dbColumnInfoVO.getColumnName()
						: e);
				tableColumns.replaceAll(e -> (e.equals(dbColumnInfoVO.getColumnName() + "_idx"))
						? "ST_DISKINDEX(ST_asText(ST_GeomFromGeoJSON(" + "NEW_" + dbColumnInfoVO.getColumnName() + ")))"
						: e);

				if (dbColumnType == DbColumnType.ARRAY_INTEGER) {
					tableColumns.replaceAll(e -> (e.equals(columnName)) ? "ST_ARRAYINT(" + columnName + ")" : e);
				} else if (dbColumnType == DbColumnType.ARRAY_FLOAT) {
					tableColumns.replaceAll(e -> (e.equals(columnName)) ? "ST_ARRAYDOUBLE(" + columnName + ")" : e);
				} else if (dbColumnType == DbColumnType.ARRAY_TIMESTAMP) {
					tableColumns.replaceAll(e -> (e.equals(columnName)) ? "ST_ARRAYTIMESTAMP(" + columnName + ")" : e);
				}
				// else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
				// tableColumns.replaceAll(e ->
				// (e.equals(columnName)) ? "ST_ARRAYBOOLEAN(" + columnName + ")" : e);
				// }
			}
		}

		String columnStrings = String.join(",", tableColumns);

		sql.append(columnStrings).append(SPACE);

		sql.append("FROM").append(SPACE);

		sql.append("(select * from ").append(entityDaoVO.getDbTableName()).append(" where id=#{id})")
				.append(COMMA_WITH_SPACE);

		// Update할 Values 설정
		sql.append("VALUES (");

		if (dbColumnInfoVOMap != null) {
			sql.append("from_utc_timestamp(#{").append(DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode())
					.append("},'UTC')").append(COMMA_WITH_SPACE);
			;

			if (entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()) != null) {
				sql.append("#{").append(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()).append("}")
						.append(COMMA_WITH_SPACE);
				;
			}

			for (DataModelDbColumnVO dbColumnInfoVO : dbColumnInfoVOMap.values()) {

				if (entityDaoVO.get(dbColumnInfoVO.getDaoAttributeId()) == null) {
					continue;
				}

				String daoAttributeId = dbColumnInfoVO.getDaoAttributeId();
				DbColumnType dbColumnType = dbColumnInfoVO.getColumnType();

				// Hive나 HBase는 Geometry타입이 별도로 존재하지 않기 때문에, 좌표계 정보포함하는 컬럼은 varchar로 처리하지 않음
				if (dbColumnType == DbColumnType.VARCHAR) {
					sql.append("#{").append(daoAttributeId).append(", jdbcType=VARCHAR}");
				} else if (dbColumnType == DbColumnType.INTEGER) {
					sql.append("#{").append(daoAttributeId).append(", jdbcType=INTEGER}");
				} else if (dbColumnType == DbColumnType.FLOAT) {
					sql.append("#{").append(daoAttributeId).append(", jdbcType=DOUBLE}");
				} else if (dbColumnType == DbColumnType.BOOLEAN) {
					sql.append("#{").append(daoAttributeId).append(", jdbcType=BOOLEAN}");
				} else if (dbColumnType == DbColumnType.ARRAY_VARCHAR) {
					sql.append("array(").append("#{").append(daoAttributeId).append(", typeHandler=")
							.append(StringArrayListTypeHandler.class.getName()).append(", jdbcType=ARRAY}").append(")");
				} else if (dbColumnType == DbColumnType.ARRAY_INTEGER) {
					sql.append("array(").append("#{").append(daoAttributeId).append(", typeHandler=")
							.append(HiveIntegerArrayListTypeHandler.class.getName()).append(", jdbcType=ARRAY}")
							.append(")");
				} else if (dbColumnType == DbColumnType.ARRAY_FLOAT) {
					sql.append("array(").append("#{").append(daoAttributeId).append(", typeHandler=")
							.append(HiveDoubleArrayListTypeHandler.class.getName()).append(", jdbcType=ARRAY}")
							.append(")");
				} else if (dbColumnType == DbColumnType.ARRAY_TIMESTAMP) {
					sql.append("array(").append("#{").append(daoAttributeId).append(", typeHandler=")
							.append(HiveDateArrayListTypeHandler.class.getName()).append(", jdbcType=ARRAY}")
							.append(")");
					// } else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
					// sql.append("array(").append("#{").append(daoAttributeId).append(",
					// typeHandler=").append(HiveBooleanArrayListTypeHandler.class.getName()).append(",
					// jdbcType=ARRAY}").append(")");
				} else if (dbColumnType == DbColumnType.TIMESTAMP) {
					sql.append("from_utc_timestamp(#{").append(daoAttributeId).append("},'UTC')");
				} else if (dbColumnType == DbColumnType.GEOMETRY_4326) { // 컬럼에 4326을 포함하는 경우, Geometry로 간주하여 처리
					sql.append("#{").append(daoAttributeId).append("}");
				} else if (dbColumnType == DbColumnType.GEOMETRY_3857) { // 컬럼에 3857을 포함하는 경우, Geometry로 간주하여 처리
					sql.append("#{").append(daoAttributeId).append("}");
				} else {
					sql.append("#{").append(daoAttributeId).append(", jdbcType=VARCHAR}");
				}

				sql.append(COMMA_WITH_SPACE);
			}

			sql.deleteCharAt(sql.lastIndexOf(COMMA_WITH_SPACE));
			sql.append(")");
			sql.append(" AS ").append("(").append(String.join(",", updateColumns)).append(");");
		}
		
		return sql.toString();
	}

	/**
	 * entity delete 시 사용될 sql을 동적 생성
	 *
	 * @param entityDaoVO entitySchema 기반으로 파싱되어 생성cre된 eneityDaoVO
	 * @return 생성된 sql문
	 */
	public String delete(CommonEntityDaoVO entityDaoVO) {
		SQL sql = new SQL() {
			{
				DELETE_FROM(entityDaoVO.getDbTableName());
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
		String tableName = StringUtil
		 		.removeSpecialCharAndLower(entityDaoVO.getDbTableName() + Constants.PARTIAL_HIST_TABLE_PREFIX);
		SQL sql = new SQL() {
			{
				DELETE_FROM(tableName);
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
		String tableName = StringUtil
				.removeSpecialCharAndLower(entityDaoVO.getDbTableName() + Constants.FULL_HIST_TABLE_PREFIX);
		SQL sql = new SQL() {
			{
				DELETE_FROM(tableName);
				WHERE("ID = #{" + DataServiceBrokerCode.DefaultAttributeKey.ID.getCode() + "}");
			}
		};

		return sql.toString();
	}
	/**
	 * entity Bulk delete 시 사용될 sql을 동적 생성
	 *
	 * @param entityDaoVOList entitySchema 기반으로 파싱되어 리스트로 생성된 entityDaoVOList
	 * @return 생성된 sql문
	 */
	public String deleteBulk(EntityBulkVO entityBulkVO) {
		StringBuilder sql = new StringBuilder();
		StringBuilder subQuery = new StringBuilder();

		subQuery.append("DELETE FROM ").append(entityBulkVO.getTableName());
		Boolean isFirst = true;
		for (DynamicEntityDaoVO entityDaoVO : entityBulkVO.getEntityDaoVOList()){
			if (isFirst) {
				subQuery.append(" WHERE ID = ");
				subQuery.append("'");
				subQuery.append(entityDaoVO.getId());
				subQuery.append("'");
				isFirst = false;
			} else {
				subQuery.append(" OR ID = ");
				subQuery.append("'");
				subQuery.append(entityDaoVO.getId());
				subQuery.append("'");
			}
		}
		sql.append(subQuery).append(";");

		return sql.toString();
	}

	/**
	 * entity이력 Bulk delete 시 사용될 sql을 동적 생성
	 *
	 * @param entityDaoVOList entitySchema 기반으로 파싱되어 리스트로 생성된 entityDaoVOList
	 * @return 생성된 sql문
	 */
	public String deleteHistBulk(EntityBulkVO entityBulkVO) {
		StringBuilder sql = new StringBuilder();
		StringBuilder subQuery = new StringBuilder();
		subQuery.append("DELETE FROM ").append(StringUtil.removeSpecialCharAndLower(entityBulkVO.getTableName() + Constants.PARTIAL_HIST_TABLE_PREFIX));
		Boolean isFirst = true;
		for (DynamicEntityDaoVO entityDaoVO : entityBulkVO.getEntityDaoVOList()){
			if (isFirst) {
				subQuery.append(" WHERE ID = ");
				subQuery.append("'");
				subQuery.append(entityDaoVO.getId());
				subQuery.append("'");
				isFirst = false;
			} else {
				subQuery.append(" OR ID = ");
				subQuery.append("'");
				subQuery.append(entityDaoVO.getId());
				subQuery.append("'");
			}
		}
		subQuery.append(";");
		sql.append(subQuery);
		return sql.toString();
	}

	/**
	 * entityFull이력 Bulk delete 시 사용될 sql을 동적 생성
	 *
	 * @param entityDaoVOList entitySchema 기반으로 파싱되어 리스트로 생성된 entityDaoVOList
	 * @return 생성된 sql문
	 */
	public String deleteFullHistBulk(EntityBulkVO entityBulkVO) {
		StringBuilder sql = new StringBuilder();
		StringBuilder subQuery = new StringBuilder();
		subQuery.append("DELETE FROM ").append(StringUtil.removeSpecialCharAndLower(entityBulkVO.getTableName() + Constants.FULL_HIST_TABLE_PREFIX));
		Boolean isFirst = true;
		for (DynamicEntityDaoVO entityDaoVO : entityBulkVO.getEntityDaoVOList()){
			if (isFirst) {
				subQuery.append(" WHERE ID = ");
				subQuery.append("'");
				subQuery.append(entityDaoVO.getId());
				subQuery.append("'");
				isFirst = false;
			} else {
				subQuery.append(" OR ID = ");
				subQuery.append("'");
				subQuery.append(entityDaoVO.getId());
				subQuery.append("'");
			}
		}
		subQuery.append(";");
		sql.append(subQuery);
		return sql.toString();
	}

	/**
	 * delete attribute 시 사용될 sql을 동적 생성
	 *
	 * @param entityDaoVO entitySchema 기반으로 파싱되어 생성된 eneityDaoVO
	 * @return 생성된 sql문
	 */
	public String deleteAttr(CommonEntityDaoVO entityDaoVO) {
		StringBuilder sql = new StringBuilder();
		StringBuilder update = new StringBuilder();
		StringBuilder select = new StringBuilder();
		StringBuilder values = new StringBuilder();
		StringBuilder as = new StringBuilder();

		update.append("INSERT OVERWRITE TABLE").append(SPACE);
		update.append(entityDaoVO.getDbTableName()).append(SPACE);
		//update.append("PARTITION (ID)").append(SPACE);

		// Update할 컬럼 설정
		select.append("SELECT").append(SPACE);
		select.append("MODIFIED_AT").append(COMMA_WITH_SPACE);

		values.append("VALUES (");
		as.append("AS (");

		Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = entityDaoVO.getDbColumnInfoVOMap();
		if (dbColumnInfoVOMap != null) {
			String deleteAttrId = entityDaoVO.getAttrId();
			for (DataModelDbColumnVO dbColumnInfoVO : dbColumnInfoVOMap.values()) {
				if (dbColumnInfoVO.getHierarchyAttributeIds().get(0).equals(deleteAttrId)) {
					select.append(dbColumnInfoVO.getColumnName()).append(COMMA_WITH_SPACE);
					values.append("NULL").append(COMMA_WITH_SPACE);
					as.append(dbColumnInfoVO.getColumnName());
				}
			}
		}

		values.append(")");
		as.append(");");

		sql.append(update).append(select).append(values).append(as);
		return sql.toString();
	}

	/**
	 * 이력 저장 시 사용될 sql을 동적 생성 (Multi Row Insert query 개선)
	 * 
	 * @param entityDaoVO entitySchema 기반으로 파싱되어 생성된 eneityDaoVO
	 * @return 생성된 sql문
	 */
	public String createHist(EntityBulkVO entityBulkVO) {
		StringBuilder sql = new StringBuilder();
		StringBuilder insertBuilder = new StringBuilder();
		StringBuilder selectBuilder = new StringBuilder();
		StringBuilder asBuilder = new StringBuilder();
		StringBuilder valueBuilder = new StringBuilder();

		insertBuilder.append("INSERT INTO TABLE").append(SPACE);
		insertBuilder
				.append(StringUtil
						.removeSpecialCharAndLower(entityBulkVO.getTableName() + Constants.PARTIAL_HIST_TABLE_PREFIX))
				.append(SPACE);
		valueBuilder.append("VALUES").append(SPACE);
		// Build Query SELECT part, AS part
		selectBuilder.append(selectQueryBuilder(BulkMethodType.CREATE_HIST, entityBulkVO)); 
		Map<String, Object> asQueryOrderResult = asQueryBuilder(BulkMethodType.CREATE_HIST, entityBulkVO);
		ArrayList<String> asQueryOrder = (ArrayList<String>) asQueryOrderResult.get("order");
		asBuilder.append(asQueryOrderResult.get("query")); //OPERATION Column등 동적 파라미터 넣게
		for (DynamicEntityDaoVO entityDaoVO : entityBulkVO.getEntityDaoVOList()){

			StringBuilder valueEntityBuilder = new StringBuilder();
			valueEntityBuilder.append("(");
			valueEntityBuilder.append(valueEntityQueryBuilder(asQueryOrder, entityDaoVO, entityBulkVO.getTableColumns()));
			valueEntityBuilder.append("),");
			valueBuilder.append(valueEntityBuilder);
		}
		valueBuilder.deleteCharAt(valueBuilder.length() - 1);
		insertBuilder.append(selectBuilder.toString()).append(valueBuilder.toString())
					.append(asBuilder.toString()).append(";");
		sql.append(insertBuilder);

		return sql.toString();
	}

	/**
	 * Full 이력 저장 시 사용될 sql을 동적 생성
	 *
	 * @param entityDaoVO entitySchema 기반으로 파싱되어 생성된 eneityDaoVO
	 * @return 생성된 sql문
	 */
	public String createFullHist(EntityBulkVO entityBulkVO) {
		StringBuilder sql = new StringBuilder();
		StringBuilder insertBuilder = new StringBuilder();
		StringBuilder selectBuilder = new StringBuilder();
		StringBuilder asBuilder = new StringBuilder();
		StringBuilder valueBuilder = new StringBuilder();

		insertBuilder.append("INSERT INTO TABLE").append(SPACE);
		insertBuilder
				.append(StringUtil
						.removeSpecialCharAndLower(entityBulkVO.getTableName() + Constants.FULL_HIST_TABLE_PREFIX))
				.append(SPACE);
		valueBuilder.append("VALUES").append(SPACE);
		// Build Query SELECT part, AS part
		selectBuilder.append(selectQueryBuilder(BulkMethodType.CREATE_FULL_HIST, entityBulkVO)); 
		Map<String, Object> asQueryOrderResult = asQueryBuilder(BulkMethodType.CREATE_HIST, entityBulkVO);
		ArrayList<String> asQueryOrder = (ArrayList<String>) asQueryOrderResult.get("order");
		asBuilder.append(asQueryOrderResult.get("query"));
		for (DynamicEntityDaoVO entityDaoVO : entityBulkVO.getEntityDaoVOList()){
			StringBuilder valueEntityBuilder = new StringBuilder();
			valueEntityBuilder.append("(");
			valueEntityBuilder.append(valueEntityQueryBuilder(asQueryOrder, entityDaoVO, entityBulkVO.getTableColumns()));
			valueEntityBuilder.append("),");
			valueBuilder.append(valueEntityBuilder);
		}
		valueBuilder.deleteCharAt(valueBuilder.length() - 1);
		insertBuilder.append(selectBuilder.toString()).append(valueBuilder.toString())
					.append(asBuilder.toString()).append(";");
		sql.append(insertBuilder);

		return sql.toString();
	}

	public String selectList(DbConditionVO dbConditionVO) {

		String selectCondition = dbConditionVO.getSelectCondition();
		String tableName = dbConditionVO.getTableName();
		String geoCondition = dbConditionVO.getGeoCondition();
		String queryCondition = dbConditionVO.getQueryCondition();

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

					WHERE("id IN " + stringBuilder.toString());

				}
				if (geoCondition != null) {

					WHERE(geoCondition);

				}
				if (queryCondition != null) {

					WHERE(queryCondition);

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

	public String selectOne(DbConditionVO dbConditionVO) {

		String selectCondition = dbConditionVO.getSelectCondition();
		String tableName = generateHbaseTable(dbConditionVO.getTableName());
		String geoCondition = dbConditionVO.getGeoCondition();
		String queryCondition = dbConditionVO.getQueryCondition();
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
				// by ID 전용 조건
				WHERE("ID ='" + id + "'");
				LIMIT(1);
			}
		};
		return sql.toString();
	}

	private String generateHbaseTable(String tableName) {
		return StringUtil.removeSpecialCharAndLower(tableName.replace(Constants.SCHEMA_NAME, ""));
	}

	public String selectHistList(DbConditionVO dbConditionVO) {
		// Using in getEntityById By TemporalEntityController

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

				Integer limit = dbConditionVO.getLimit();
				Integer offset = dbConditionVO.getOffset();

				SELECT(selectCondition);
				FROM(histTableName + " as HIST_T");

				// 1. 삭제 이전 데이터는 조회 되지 않게 필터링
				INNER_JOIN("(SELECT ID AS LAST_T_ID, CREATED_AT " + " FROM " + tableName + " ) AS LAST_T "
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

					WHERE("id IN " + stringBuilder.toString());

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

	@SuppressWarnings("unchecked")
	private String valueEntityQueryBuilder(ArrayList<String> colOrder, DynamicEntityDaoVO entityDaoVO, List<String> tableColumns){
		StringBuilder valueEntityBuilder = new StringBuilder();
		BulkDataTypeHandler bulkDataTypeHandler = new BulkDataTypeHandler();
		Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = entityDaoVO.getDbColumnInfoVOMap();
		if (dbColumnInfoVOMap != null) {
			if (!tableColumns.contains("OPERATION")){
				tableColumns.add("OPERATION");
			}
			for (String col : colOrder){
				Boolean isNullValue = true;
				for (String tableColumn : tableColumns){
					if(col.equals(tableColumn)){
						if (tableColumn.toUpperCase().equalsIgnoreCase("ID")){
							isNullValue = false;
							valueEntityBuilder.append("'");
							valueEntityBuilder.append(entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.ID.getCode()));
							valueEntityBuilder.append("'");
							valueEntityBuilder.append(COMMA_WITH_SPACE);
							break;
						}
						else if (tableColumn.toUpperCase().equalsIgnoreCase("DATASET_ID")){
							isNullValue = false;
							valueEntityBuilder.append("'");
							valueEntityBuilder.append(entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()));
							valueEntityBuilder.append("'");
							valueEntityBuilder.append(COMMA_WITH_SPACE);
							break;
						}
						else if (tableColumn.toUpperCase().equalsIgnoreCase("CREATED_AT")){
							isNullValue = false;
							valueEntityBuilder.append(DATE_FORMAT_PRE_STRING);
							valueEntityBuilder.append(DateUtil.dateToDbFormatString((Date) entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.CREATED_AT.getCode())));
							valueEntityBuilder.append(DATE_FORMAT_END_STRING);
							valueEntityBuilder.append(COMMA_WITH_SPACE);
							break;
						}
						else if (tableColumn.toUpperCase().equalsIgnoreCase("MODIFIED_AT")){
							isNullValue = false;
							valueEntityBuilder.append(DATE_FORMAT_PRE_STRING);
							valueEntityBuilder.append(DateUtil.dateToDbFormatString((Date) entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode())));
							valueEntityBuilder.append(DATE_FORMAT_END_STRING);
							valueEntityBuilder.append(COMMA_WITH_SPACE);
							break;
						} else if (tableColumn.toUpperCase().equalsIgnoreCase("OPERATION")){
							isNullValue = false;
							valueEntityBuilder.append("'");
							valueEntityBuilder.append(entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.OPERATION.getCode()));
							valueEntityBuilder.append("'");
							valueEntityBuilder.append(COMMA_WITH_SPACE);
							break;
						} else{
							for (DataModelDbColumnVO dbColumnInfoVO : dbColumnInfoVOMap.values()){
								String columnName = dbColumnInfoVO.getColumnName();
								DbColumnType dbColumnType = dbColumnInfoVO.getColumnType();
								Object inputValue = entityDaoVO.get(dbColumnInfoVO.getDaoAttributeId());
								if (tableColumn.equals(columnName)){
									isNullValue = false;
									try{
										if (dbColumnType == DbColumnType.ARRAY_VARCHAR){
											ArrayList<String> parameterList = (ArrayList<String>) inputValue;
											String data = bulkDataTypeHandler.arrayVarcharHander(parameterList);
											valueEntityBuilder.append(ARRAY_FORMAT_PRE_STRING);
											valueEntityBuilder.append(data);
											valueEntityBuilder.append(ARRAY_FORMAT_END_STRING);
											valueEntityBuilder.append(COMMA_WITH_SPACE);
											break;
										} else if (dbColumnType == DbColumnType.ARRAY_INTEGER){
											ArrayList<Integer> parameterList = (ArrayList<Integer>) inputValue;
											String data = bulkDataTypeHandler.arrayIntegerHander(parameterList);
											valueEntityBuilder.append(ARRAY_FORMAT_PRE_STRING);
											valueEntityBuilder.append(data);
											valueEntityBuilder.append(ARRAY_FORMAT_END_STRING);
											valueEntityBuilder.append(COMMA_WITH_SPACE);
											break;
										} else if (dbColumnType == DbColumnType.ARRAY_FLOAT){
											ArrayList<Double> parameterList = (ArrayList<Double>) inputValue;
											String data = bulkDataTypeHandler.arrayDoubleHander(parameterList);
											valueEntityBuilder.append(ARRAY_FORMAT_PRE_STRING);
											valueEntityBuilder.append(data);
											valueEntityBuilder.append(ARRAY_FORMAT_END_STRING);
											valueEntityBuilder.append(COMMA_WITH_SPACE);
											break;
										} else if (dbColumnType == DbColumnType.ARRAY_TIMESTAMP){
											ArrayList<Date> parameterList = (ArrayList<Date>) inputValue;
											String data = bulkDataTypeHandler.arrayTimestampHander(parameterList);
											valueEntityBuilder.append(ARRAY_FORMAT_PRE_STRING);
											valueEntityBuilder.append(data);
											valueEntityBuilder.append(ARRAY_FORMAT_END_STRING);
											valueEntityBuilder.append(COMMA_WITH_SPACE);
											break;
										} else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN){
											ArrayList<Boolean> parameterList = (ArrayList<Boolean>) inputValue;
											String data = bulkDataTypeHandler.arrayBooleanHander(parameterList);
											valueEntityBuilder.append(ARRAY_FORMAT_PRE_STRING);
											valueEntityBuilder.append(data);
											valueEntityBuilder.append(ARRAY_FORMAT_END_STRING);
											valueEntityBuilder.append(COMMA_WITH_SPACE);
											break;
										} else if (dbColumnType == DbColumnType.TIMESTAMP) {
											String data = DateUtil.dateToDbFormatString((Date) inputValue);
											valueEntityBuilder.append(DATE_FORMAT_PRE_STRING);
											valueEntityBuilder.append(data);
											valueEntityBuilder.append(DATE_FORMAT_END_STRING);
											valueEntityBuilder.append(COMMA_WITH_SPACE);
											break;
										} else {
											if(dbColumnInfoVO.getColumnType().equals(DbColumnType.INTEGER) || dbColumnInfoVO.getColumnType().equals(DbColumnType.BOOLEAN) || dbColumnInfoVO.getColumnType().equals(DbColumnType.FLOAT)){
												valueEntityBuilder.append(inputValue);
											} else {
												if(inputValue.equals(null)){ // inputvalue가 이미 Null인데 당연히 Exception 나지..
													if(dbColumnInfoVO.getColumnType().equals(DbColumnType.GEOMETRY_4326) || dbColumnInfoVO.getColumnType().equals(DbColumnType.GEOMETRY_3857)){
														valueEntityBuilder.append("'");
														valueEntityBuilder.append(inputValue);
														valueEntityBuilder.append("'");
													} else {
													valueEntityBuilder.append(inputValue);
													}
												}else{
													valueEntityBuilder.append("'");
													valueEntityBuilder.append(inputValue);
													valueEntityBuilder.append("'");
												}
											}
											valueEntityBuilder.append(COMMA_WITH_SPACE);
											break;
										} 
									} catch (NullPointerException e){
										String data = null;
										valueEntityBuilder.append(data);
										valueEntityBuilder.append(COMMA_WITH_SPACE);
									}
								}
							}
						}
						if (isNullValue){
							String data = null;
							valueEntityBuilder.append(data);
							valueEntityBuilder.append(COMMA_WITH_SPACE);
						}
					}
				}
			}
		}
		valueEntityBuilder.deleteCharAt(valueEntityBuilder.length() - 2);
		return valueEntityBuilder.toString();
	}

	private String selectQueryBuilder(BulkMethodType type, EntityBulkVO entityBulkVO){
		StringBuilder selectBuilder = new StringBuilder();
		Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = entityBulkVO.getEntityDaoVOList().get(0).getDbColumnInfoVOMap();
		selectBuilder.append("SELECT ");
		selectBuilder.append("ID");
		selectBuilder.append(COMMA_WITH_SPACE);
		selectBuilder.append("DATASET_ID");
		selectBuilder.append(COMMA_WITH_SPACE);
		selectBuilder.append("CREATED_AT");
		selectBuilder.append(COMMA_WITH_SPACE);
		selectBuilder.append("MODIFIED_AT");
		selectBuilder.append(COMMA_WITH_SPACE);
		if (type.equals(BulkMethodType.CREATE_HIST) || type.equals(BulkMethodType.CREATE_FULL_HIST)){
			selectBuilder.append("OPERATION");
			selectBuilder.append(COMMA_WITH_SPACE);
		}
		for (String tableColumn : entityBulkVO.getTableColumns()){
			for (DataModelDbColumnVO dbColumnInfoVO : dbColumnInfoVOMap.values()){
				String columnName = dbColumnInfoVO.getColumnName();
				if (tableColumn.equals(columnName)){
					DbColumnType dbColumnType = dbColumnInfoVO.getColumnType();
					if (dbColumnType == DbColumnType.ARRAY_VARCHAR){
						// selectBuilder.append("ST_ARRAYSTRING(");
						selectBuilder.append(columnName);
						// selectBuilder.append(")");
						selectBuilder.append(COMMA_WITH_SPACE);
					} else if (dbColumnType == DbColumnType.ARRAY_INTEGER){
						// selectBuilder.append("NVL2(").append(columnName).append(", ");
						selectBuilder.append("ST_ARRAYINT(");
						selectBuilder.append(columnName);
						selectBuilder.append(")");
						// selectBuilder.append(", ").append(columnName).append(")");
						selectBuilder.append(COMMA_WITH_SPACE);
					} else if (dbColumnType == DbColumnType.ARRAY_FLOAT){
						// selectBuilder.append("NVL2(").append(columnName).append(", ");
						selectBuilder.append("ST_ARRAYDOUBLE(");
						selectBuilder.append(columnName);
						selectBuilder.append(")");
						// selectBuilder.append(", ").append(columnName).append(")");
						selectBuilder.append(COMMA_WITH_SPACE);
					} else if (dbColumnType == DbColumnType.ARRAY_TIMESTAMP){
						// selectBuilder.append("NVL2(").append(columnName).append(", ");
						selectBuilder.append("ST_ARRAYTIMESTAMP(");
						selectBuilder.append(columnName);
						selectBuilder.append(")");
						// selectBuilder.append(", ").append(columnName).append(")");
						selectBuilder.append(COMMA_WITH_SPACE);
					} else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN){
						// selectBuilder.append("NVL2(").append(columnName).append(", ");
						selectBuilder.append("ST_ARRAYBOOLEAN(");
						selectBuilder.append(columnName);
						selectBuilder.append(")");
						// selectBuilder.append(", ").append(columnName).append(")");
						selectBuilder.append(COMMA_WITH_SPACE);
					}  else if (dbColumnType == DbColumnType.GEOMETRY_4326){
						if (tableColumn.equalsIgnoreCase(columnName + "_idx")){ // 확인 필요.
							selectBuilder.append("NVL2(");
							selectBuilder.append(columnName);
							selectBuilder.append(", ST_DISKINDEX(ST_asText(ST_GeomFromGeoJSON(");
							selectBuilder.append(columnName);
							selectBuilder.append("))), ");
							selectBuilder.append(columnName);
							selectBuilder.append(")");
							selectBuilder.append(COMMA_WITH_SPACE);
						} else {
							selectBuilder.append("NVL2(");
							selectBuilder.append(columnName);
							selectBuilder.append(", ST_AsGeoJson(ST_GeomFromGeoJSON(");
							selectBuilder.append(columnName);
							selectBuilder.append(")), ");
							selectBuilder.append(columnName);
							selectBuilder.append(")");
							selectBuilder.append(COMMA_WITH_SPACE);
						}
					} else if (dbColumnType == DbColumnType.GEOMETRY_3857){
						selectBuilder.append("NVL2(");
						selectBuilder.append(columnName);
						selectBuilder.append(", ST_AsGeoJson(ST_Transform(ST_FlipCoordinates(ST_GeomFromGeoJSON(");
						selectBuilder.append(columnName);
						selectBuilder.append(")), 'epsg:4326','epsg:3857')), ");
						selectBuilder.append(columnName);
						selectBuilder.append(")");
						selectBuilder.append(COMMA_WITH_SPACE);
					} else {
						selectBuilder.append(columnName);
						selectBuilder.append(COMMA_WITH_SPACE);
					}
					
				}
			}
		}
		selectBuilder.deleteCharAt(selectBuilder.length() - 2);
		selectBuilder.append("FROM ");
		return selectBuilder.toString();
	}

	private Map<String, Object> asQueryBuilder(BulkMethodType type, EntityBulkVO entityBulkVO){
		Map<String, Object> tableInfoMap = new HashMap<>();
		ArrayList<String> tableColumns = new ArrayList<>();
		StringBuilder asBuilder = new StringBuilder();
		asBuilder.append(" AS (");
		if(type.equals(BulkMethodType.CREATE_HIST) || type.equals(BulkMethodType.CREATE_FULL_HIST)){
			if (!tableColumns.contains("OPERATION")){
				asBuilder.append("OPERATION").append(COMMA_WITH_SPACE);
				tableColumns.add("OPERATION");
			}
		}
		for (String column : entityBulkVO.getTableColumns()){
			asBuilder.append(column).append(COMMA_WITH_SPACE);
			tableColumns.add(column);
		}
		asBuilder.deleteCharAt(asBuilder.length() - 2);
		asBuilder.append(" )");
		tableInfoMap.put("order", tableColumns);
		tableInfoMap.put("query", asBuilder.toString());
		return tableInfoMap;
	}
	enum BulkMethodType {
		CREATE_HIST, CREATE_FULL_HIST, CREATE, UPSERT
	}
	class BulkDataTypeHandler {
		String arrayVarcharHander(ArrayList<String> parameterList){
			StringBuilder str = new StringBuilder();
			if (parameterList.size() > 0){
				for(String parameter : parameterList) {
					if(parameter == null) {
						str.append("null").append(",");
						continue;
					}
					if(parameter.indexOf("\"") > 0) {
						parameter = parameter.replace("\"", "\\\"");
					}
					str.append("\"").append(parameter).append("\"").append(",");
				}
				str.deleteCharAt(str.length() - 1);
			} else {
				str.append("null");
			}
			return str.toString();
		}
		String arrayIntegerHander(ArrayList<Integer> parameterList){
			StringBuilder str = new StringBuilder();
			boolean isNotNull = false;
			for (Integer parameter : parameterList) {
				if (parameter != null) {
					isNotNull = true;
					str.append(parameter.intValue()).append(",");
				}
			}
			if (isNotNull) str.deleteCharAt(str.length() - 1);
			return str.toString();
		}
		String arrayDoubleHander(ArrayList<Double> parameterList){
			StringBuilder str = new StringBuilder();
			boolean isNotNull = false;
			for (int idx = 0; idx<parameterList.size(); idx++) {
				Double parameter = Double.parseDouble(String.valueOf(parameterList.get(idx)));
				if (parameter != null) {
					isNotNull = true;
					str.append(parameter.doubleValue()).append(",");
				}
			}
			if (isNotNull) {
				str.deleteCharAt(str.length() - 1);
			}
			return str.toString();
		}
		String arrayTimestampHander(ArrayList<Date> parameterList){
			StringBuilder str = new StringBuilder();
			boolean isNotNull = false;
			for (Date parameter : parameterList) {
				if (parameter != null) {
					isNotNull = true;
					str.append(DateUtil.dateToDbFormatString(parameter)).append(",");
				}
			}
			if (isNotNull) str.deleteCharAt(str.length() - 1);
			return str.toString();
		}
		String arrayBooleanHander(ArrayList<Boolean> parameterList){
			StringBuilder str = new StringBuilder();
			boolean isNotNull = false;
			for (Boolean parameter : parameterList) {
				if (parameter != null) {
					isNotNull = true;
					str.append(parameter).append(",");
				}
			}
			if (isNotNull) str.deleteCharAt(str.length() - 1);
			return str.toString();
		}
	}
	public String selectCount(DbConditionVO dbConditionVO) {
		String selectCondition = dbConditionVO.getSelectCondition();
		String tableName = dbConditionVO.getTableName();
		String geoCondition = dbConditionVO.getGeoCondition();
		String queryCondition = dbConditionVO.getQueryCondition();

		List<String> searchIdList = dbConditionVO.getSearchIdList();
		String idPattern = dbConditionVO.getIdPattern();

		Integer limit = dbConditionVO.getLimit();
		Integer offset = dbConditionVO.getOffset();

		SQL sql = new SQL() {
			{ // 익명의 클래스 생성
				SELECT("count(id)");
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

					WHERE("id IN " + stringBuilder.toString());

				}
				if (geoCondition != null) {

					WHERE(geoCondition);

				}
				if (queryCondition != null) {

					WHERE(queryCondition);

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
}