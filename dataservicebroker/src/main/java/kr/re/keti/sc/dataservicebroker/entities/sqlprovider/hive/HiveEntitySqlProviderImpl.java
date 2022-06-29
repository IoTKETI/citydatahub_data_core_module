package kr.re.keti.sc.dataservicebroker.entities.sqlprovider.hive;

import java.util.*;

import kr.re.keti.sc.dataservicebroker.common.datamapperhandler.*;
import org.apache.ibatis.jdbc.SQL;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.DbColumnType;
import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityDaoVO;
import kr.re.keti.sc.dataservicebroker.common.vo.DbConditionVO;
import kr.re.keti.sc.dataservicebroker.common.vo.entities.DynamicEntityDaoVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelDbColumnVO;
import kr.re.keti.sc.dataservicebroker.util.DateUtil;
import kr.re.keti.sc.dataservicebroker.util.StringUtil;

public class HiveEntitySqlProviderImpl {

	private static final String SPACE = " ";
	private static final String COMMA_WITH_SPACE = ", ";

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

	public String bulkCreate(List<DynamicEntityDaoVO> entityDaoVOList) {
		CommonEntityDaoVO commonEntityDaoVO = entityDaoVOList.get(0);

		List<String> tableColumns = commonEntityDaoVO.getTableColumns();

		StringBuilder sql = new StringBuilder();
		StringBuilder insertBuilder = new StringBuilder();
		StringBuilder selectBuilder = new StringBuilder();
		StringBuilder asBuilder = new StringBuilder();
		StringBuilder valueBuilder = new StringBuilder();

		List<String> compareColumnList = new ArrayList<>(tableColumns);

		insertBuilder.append("INSERT INTO TABLE").append(SPACE);
		insertBuilder.append(commonEntityDaoVO.getDbTableName()).append(SPACE);

		selectBuilder.append("select").append(SPACE);
		asBuilder.append(" as (ID, DATASET_ID, CREATED_AT, MODIFIED_AT");

		valueBuilder.append("VALUES ");

		for (int i = 0; i<entityDaoVOList.size(); i++) {
			CommonEntityDaoVO entityDaoVO = entityDaoVOList.get(i);

			Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = entityDaoVO.getDbColumnInfoVOMap();
			valueBuilder.append("(");
			if (dbColumnInfoVOMap != null) {
				// 2. Default Column 설정
				valueBuilder.append("'").append(entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.ID.getCode())).append("'")
						.append(COMMA_WITH_SPACE);
				valueBuilder.append("'").append(entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()))
						.append("'").append(COMMA_WITH_SPACE);
				valueBuilder.append("from_utc_timestamp('")
						.append(DateUtil.dateToDbFormatString((Date) entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.CREATED_AT.getCode()))).append("', 'UTC')")
						.append(COMMA_WITH_SPACE);
				valueBuilder.append("from_utc_timestamp('")
						.append(DateUtil.dateToDbFormatString((Date) entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode()))).append("', 'UTC')")
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

					if (i < 1) asBuilder.append(COMMA_WITH_SPACE).append(columnName);

					compareColumnList.removeIf(e -> e.toUpperCase().equalsIgnoreCase(columnName.toUpperCase()));

					if (dbColumnType == DbColumnType.ARRAY_VARCHAR) {
						ArrayList<String> parameterList = (ArrayList) entityDaoVO.get(daoAttributeId);

						StringBuilder str = new StringBuilder();
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

						valueBuilder.append("split('").append(str.toString()).append("', ',')")
								.append(COMMA_WITH_SPACE);
					} else if (dbColumnType == DbColumnType.ARRAY_INTEGER) {
						ArrayList<Integer> parameterList = (ArrayList) entityDaoVO.get(daoAttributeId);

						StringBuilder str = new StringBuilder();
						boolean isNotNull = false;

						for (Integer parameter : parameterList) {
							if (parameter != null) {
								isNotNull = true;
								str.append(parameter.intValue()).append(",");
							}
						}

						if (isNotNull) str.deleteCharAt(str.length() - 1);

						valueBuilder.append("split('").append(str.toString()).append("', ',')")
								.append(COMMA_WITH_SPACE);
						tableColumns.replaceAll(e -> (e.equals(columnName)) ? "ST_ARRAYINT(" + columnName + ")" : e);
					} else if (dbColumnType == DbColumnType.ARRAY_FLOAT) {
						ArrayList<Double> parameterList = (ArrayList) entityDaoVO.get(daoAttributeId);

						StringBuilder str = new StringBuilder();
						boolean isNotNull = false;

						for (int idx = 0; idx<parameterList.size(); idx++) {
							Double parameter = parameterList.get(idx);

							if (parameter != null) {
								isNotNull = true;
								str.append(parameter.doubleValue()).append(",");
							}
						}

						if (isNotNull) {
							str.deleteCharAt(str.length() - 1);
						}

						valueBuilder.append("split('").append(str.toString()).append("', ',')")
								.append(COMMA_WITH_SPACE);
						tableColumns.replaceAll(e -> (e.equals(columnName)) ? "ST_ARRAYDOUBLE(" + columnName + ")" : e);
					} else if (dbColumnType == DbColumnType.ARRAY_TIMESTAMP) {
						ArrayList<Date> parameterList = (ArrayList) entityDaoVO.get(daoAttributeId);

						StringBuilder str = new StringBuilder();
						boolean isNotNull = false;

						for (Date parameter : parameterList) {
							if (parameter != null) {
								isNotNull = true;
								str.append(DateUtil.dateToDbFormatString(parameter)).append(",");
							}
						}

						if (isNotNull) str.deleteCharAt(str.length() - 1);

						valueBuilder.append("split('").append(str.toString()).append("', ',')")
								.append(COMMA_WITH_SPACE);
						tableColumns
								.replaceAll(e -> (e.equals(columnName)) ? "ST_ARRAYTIMESTAMP(" + columnName + ")" : e);
					} else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
						ArrayList<Boolean> parameterList = (ArrayList) entityDaoVO.get(daoAttributeId);

						StringBuilder str = new StringBuilder();
						boolean isNotNull = false;

						for (Boolean parameter : parameterList) {
							if (parameter != null) {
								isNotNull = true;
								str.append(parameter).append(",");
							}
						}

						if (isNotNull) str.deleteCharAt(str.length() - 1);

						valueBuilder.append("split('").append(str.toString()).append("', ',')")
								.append(COMMA_WITH_SPACE);
						tableColumns
								.replaceAll(e -> (e.equals(columnName)) ? "ST_ARRAYBOOLEAN(" + columnName + ")" : e);
					} else if (dbColumnType == DbColumnType.TIMESTAMP) {
						// valueBuilder.append("from_utc_timestamp('").append(entityDaoVO.get(daoAttributeId)).append("','UTC')")
						// .append(COMMA_WITH_SPACE);
						// 일반 테이블에서 컬럼_createdat, 컬럼_modifiedat 컬럼의 경우 시간이 기록되지 않는 문제 때문에
						// create나 createFullHist에 있는 코드로 변경
						valueBuilder.append("from_utc_timestamp(#{").append(daoAttributeId).append("},'UTC')")
								.append(COMMA_WITH_SPACE);
					} else if (dbColumnType == DbColumnType.GEOMETRY_4326) {
						tableColumns.replaceAll(e -> (e.equalsIgnoreCase(columnName))
								? "ST_AsGeoJson(ST_GeomFromGeoJSON(" + columnName + "))"
								: (e.equalsIgnoreCase(columnName + "_idx"))
										? "ST_DISKINDEX(ST_asText(ST_GeomFromGeoJSON(" + columnName + ")))"
										: e);

						compareColumnList.remove(columnName + "_idx");

						valueBuilder.append("'").append(entityDaoVO.get(daoAttributeId)).append("'").append(COMMA_WITH_SPACE);
					} else if (dbColumnType == DbColumnType.GEOMETRY_3857) {
						tableColumns.replaceAll(e -> (e.equalsIgnoreCase(columnName))
								? "ST_AsGeoJson(ST_Transform(ST_FlipCoordinates(ST_GeomFromGeoJSON(" + columnName
										+ ")), 'epsg:4326','epsg:3857'))"
								: e);
						valueBuilder.append("'").append(entityDaoVO.get(daoAttributeId)).append("'").append(COMMA_WITH_SPACE);
					} else if (dbColumnType == DbColumnType.BOOLEAN) {
						valueBuilder.append("'").append(entityDaoVO.get(daoAttributeId)).append("'")
								.append(COMMA_WITH_SPACE);
					} else {
						valueBuilder.append("'").append(entityDaoVO.get(daoAttributeId)).append("'")
								.append(COMMA_WITH_SPACE);
					}
				}

				if (i < 1) {
					if (!compareColumnList.isEmpty()) {
						for (String column : compareColumnList) {
							if (column.equalsIgnoreCase("key")) {
								asBuilder.append(COMMA_WITH_SPACE).append(column);
								valueBuilder.append("'").append(entityDaoVO.get(DataServiceBrokerCode.DefaultAttributeKey.ID.getCode()))
										.append("'").append(COMMA_WITH_SPACE);
							} else {
								asBuilder.append(COMMA_WITH_SPACE).append(column);
								valueBuilder.append("null").append(COMMA_WITH_SPACE);
							}
						}
					}

					String fields = String.join(", ", tableColumns);
					selectBuilder.append(fields).append(SPACE).append("FROM").append(SPACE);
				}
				valueBuilder.deleteCharAt(valueBuilder.length() - 2).append("), ");
			}
		}
		valueBuilder.deleteCharAt(valueBuilder.length() - 2);

		sql.append(insertBuilder.toString()).append(selectBuilder.toString()).append(valueBuilder.toString())
				.append(asBuilder.toString()).append(")");

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
                    		.append(HiveBooleanArrayListTypeHandler.class.getName()).append("}, ',')").append(COMMA_WITH_SPACE);
                    tableColumns.replaceAll(e -> (e.equals(columnName)) ? "ST_ARRAYBOOLEAN(" + columnName + ")" : e );
				} else if (dbColumnType == DbColumnType.TIMESTAMP) {
					valueBuilder.append("from_utc_timestamp(#{").append(daoAttributeId).append("},'UTC')")
							.append(COMMA_WITH_SPACE);
				} else if (dbColumnType == DbColumnType.GEOMETRY_4326) {
					tableColumns.replaceAll(
							e -> (e.equalsIgnoreCase(columnName)) ? "ST_AsGeoJson(ST_GeomFromGeoJSON(" + columnName + "))"
									: (e.equalsIgnoreCase(columnName + "_idx"))
									? "ST_DISKINDEX(ST_asText(ST_GeomFromGeoJSON(" + columnName + ")))"
									: e);

					compareColumnList.remove(columnName + "_idx");

					valueBuilder.append("#{").append(daoAttributeId).append("}").append(COMMA_WITH_SPACE);
				} else if (dbColumnType == DbColumnType.GEOMETRY_3857) {
					tableColumns.replaceAll(e -> (e.equalsIgnoreCase(columnName))
							? "ST_AsGeoJson(ST_Transform(ST_FlipCoordinates(ST_GeomFromGeoJSON(" + columnName + ")), 'epsg:4326','epsg:3857'))"
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

			valueBuilder.deleteCharAt(valueBuilder.length()-2).append(")");
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
		sql.append("USING (select").append(SPACE);
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
			Set<String> updateQueryCols = entityDaoVO.keySet();
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
				} else if (dbColumnType == DbColumnType.GEOMETRY_4326) {
					select.append("ST_AsGeoJson(ST_GeomFromGeoJSON(#{" + daoAttributeId + "})) as ").append(columnName)
							.append(COMMA_WITH_SPACE);
					select.append("ST_DISKINDEX(ST_asText(ST_GeomFromGeoJSON(#{" + daoAttributeId + "}))) as ")
							.append(columnName).append("_idx").append(COMMA_WITH_SPACE);
				} else if (dbColumnType == DbColumnType.GEOMETRY_3857) {
					select.append("ST_AsGeoJson(ST_Transform(ST_FlipCoordinates(ST_GeomFromGeoJSON(#{" + daoAttributeId
							+ "})), 'epsg:4326','epsg:3857')) as ").append(columnName).append(COMMA_WITH_SPACE);
				} else {
					select.append("#{" + daoAttributeId + "} as ").append(columnName).append(COMMA_WITH_SPACE);
				}

				// yj <-- update 되는 값에 대해서만 set 쿼리를 구성하도록 필터링 추가
				if (updateQueryCols.contains(columnName)) {
					update.append(columnName).append(" = ").append("source.").append(columnName).append(COMMA_WITH_SPACE);

					if (dbColumnType == DbColumnType.GEOMETRY_4326) {
						update.append(columnName).append("_idx").append(" = ").append("source.").append(columnName)
								.append("_idx").append(COMMA_WITH_SPACE);
					}
				}

				insertColumns.append(columnName).append(COMMA_WITH_SPACE);

				if (dbColumnType == DbColumnType.GEOMETRY_4326) {
					insertColumns.append(columnName).append("_idx").append(COMMA_WITH_SPACE);
				}

				insertValues.append("source.").append(columnName).append(COMMA_WITH_SPACE);

				if (dbColumnType == DbColumnType.GEOMETRY_4326) {
					insertValues.append("source.").append(columnName).append("_idx").append(COMMA_WITH_SPACE);
				}
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

	public String replaceAttrHBase(CommonEntityDaoVO entityDaoVO) {
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
						? "ST_AsGeoJson(ST_GeomFromGeoJSON(" +  "NEW_" + dbColumnInfoVO.getColumnName() + "))"
						: e.contains("_3857")
								? "ST_AsGeoJson(ST_Transform(ST_FlipCoordinates(ST_FlipCoordinates(ST_GeomFromGeoJSON(" + "NEW_" + dbColumnInfoVO.getColumnName()
										+ ")), 'epsg:4326','epsg:3857'))"
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
//                else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
//                    tableColumns.replaceAll(e ->
//                    (e.equals(columnName)) ? "ST_ARRAYBOOLEAN(" + columnName + ")" : e);
//                }
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
//                } else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
//                    sql.append("split(").append("#{").append(daoAttributeId).append(", typeHandler=").append(HiveBooleanArrayListTypeHandler.class.getName()).append(", jdbcType=ARRAY}").append(", ',')");
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

	/**
	 * Append Entity Attributes 시 사용될 sql을 동적 생성
	 *
	 * @param entityDaoVO entitySchema 기반으로 파싱되어 생성된 eneityDaoVO
	 * @return 생성된 sql문
	 */
	public String appendAttr(CommonEntityDaoVO entityDaoVO) {

		List<String> tableColumns = entityDaoVO.getTableColumns();

		StringBuilder sql = new StringBuilder();

		List<String> updateColumns = new ArrayList<>();

		// 테이블 설정
		sql.append("INSERT OVERWRITE TABLE").append(SPACE);
		sql.append(entityDaoVO.getDbTableName()).append(SPACE);
		sql.append("PARTITION (ID)").append(SPACE);

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
						? "ST_AsGeoJson(ST_GeomFromGeoJSON(" +  "NEW_" + dbColumnInfoVO.getColumnName() + "))"
						: e.contains("_3857")
								? "ST_AsGeoJson(ST_Transform(ST_FlipCoordinates(ST_GeomFromGeoJSON(" + "NEW_" + dbColumnInfoVO.getColumnName()
										+ ")), 'epsg:4326','epsg:3857'))"
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
//                else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
//                    tableColumns.replaceAll(e ->
//                    (e.equals(columnName)) ? "ST_ARRAYBOOLEAN(" + columnName + ")" : e);
//                }
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
                } else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
                    sql.append("array(").append("#{").append(daoAttributeId).append(", typeHandler=").append(HiveBooleanArrayListTypeHandler.class.getName()).append(", jdbcType=ARRAY}").append(")");
				} else if (dbColumnType == DbColumnType.TIMESTAMP) {
					sql.append("from_utc_timestamp(#{").append(daoAttributeId).append("},'UTC')");
				} else if (dbColumnType == DbColumnType.GEOMETRY_4326) { // 컬럼에 4326을 포함하는 경우, Geometry로 간주하여 처리
					sql.append("#{").append(daoAttributeId).append("}");
				} else if (dbColumnType == DbColumnType.GEOMETRY_3857) { // 컬럼에 3857을 포함하는 경우, Geometry로 간주하여 처리
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
			sql.append(" AS ").append("(").append(String.join(",", updateColumns)).append(");");
		}

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
		sql.append("PARTITION (ID)").append(SPACE);

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
								? "ST_AsGeoJson(ST_Transform(ST_FlipCoordinates(ST_FlipCoordinates(ST_GeomFromGeoJSON(" + "NEW_" + dbColumnInfoVO.getColumnName()
										+ ")), 'epsg:4326','epsg:3857'))"
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
//                else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
//                    tableColumns.replaceAll(e ->
//                    (e.equals(columnName)) ? "ST_ARRAYBOOLEAN(" + columnName + ")" : e);
//                }
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
//                } else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
//                    sql.append("COALESCE(array(").append("#{").append(daoAttributeId).append(", typeHandler=").append(HiveBooleanArrayListTypeHandler.class.getName()).append(", jdbcType=ARRAY}").append("))");
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
//                        } else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
//                            SET(columnName + " = CASE WHEN " + columnName + " IS NOT NULL THEN #{" + daoAttributeId + ", typeHandler=" + HiveBooleanArrayListTypeHandler.class.getName() + ", jdbcType=ARRAY}::ARRAY<BOOLEAN> END");
						} else if (dbColumnType == DbColumnType.TIMESTAMP) {
							SET(columnName + " = CASE WHEN " + columnName + " IS NOT NULL THEN #{" + daoAttributeId
									+ ", jdbcType=TIMESTAMP}::TIMESTAMP WITH TIME ZONE END");
						} else if (dbColumnType == DbColumnType.GEOMETRY_4326) {
							SET(columnName + " = CASE WHEN " + columnName
									+ " IS NOT NULL THEN ST_SetSRID(ST_GeomFromGeoJSON(#{" + daoAttributeId
									+ "}), 4326) END");
						} else if (dbColumnType == DbColumnType.GEOMETRY_3857) {
							SET(columnName + " = CASE WHEN " + columnName
									+ " IS NOT NULL THEN ST_Transform(ST_FlipCoordinates(ST_SetSRID(ST_GeomFromGeoJSON(#{" + daoAttributeId
									+ "})), 4326), 3857) END");
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
		sql.append("PARTITION (ID)").append(SPACE);

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
								? "ST_AsGeoJson(ST_Transform(ST_FlipCoordinates(ST_GeomFromGeoJSON(" + "NEW_" + dbColumnInfoVO.getColumnName()
										+ ")), 'epsg:4326','epsg:3857'))"
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
//                else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
//                    tableColumns.replaceAll(e ->
//                    (e.equals(columnName)) ? "ST_ARRAYBOOLEAN(" + columnName + ")" : e);
//                }
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
//                } else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
//                    sql.append("array(").append("#{").append(daoAttributeId).append(", typeHandler=").append(HiveBooleanArrayListTypeHandler.class.getName()).append(", jdbcType=ARRAY}").append(")");
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
		StringBuilder sql = new StringBuilder();
		String tableName = StringUtil
				.removeSpecialCharAndLower(entityDaoVO.getDbTableName() + Constants.PARTIAL_HIST_TABLE_PREFIX);

		sql.append("ALTER TABLE").append(SPACE);
		sql.append(tableName).append(SPACE);

		sql.append("DROP PARTITION (ID = ");
		sql.append("#{").append(DataServiceBrokerCode.DefaultAttributeKey.ID.getCode()).append("}");
		sql.append(");");

		return sql.toString();
	}

	/**
	 * entityFull이력 delete 시 사용될 sql을 동적 생성
	 *
	 * @param entityDaoVO entitySchema 기반으로 파싱되어 생성된 eneityDaoVO
	 * @return 생성된 sql문
	 */
	public String deleteFullHist(CommonEntityDaoVO entityDaoVO) {
		StringBuilder sql = new StringBuilder();
		String tableName = StringUtil
				.removeSpecialCharAndLower(entityDaoVO.getDbTableName() + Constants.FULL_HIST_TABLE_PREFIX);

		sql.append("ALTER TABLE").append(SPACE);
		sql.append(tableName).append(SPACE);

		sql.append("DROP PARTITION (ID = ");
		sql.append("#{").append(DataServiceBrokerCode.DefaultAttributeKey.ID.getCode()).append("}");
		sql.append(");");

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
		update.append("PARTITION (ID)").append(SPACE);

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
     * 이력 저장 시 사용될 sql을 동적 생성
     *
     * @param entityDaoVO entitySchema 기반으로 파싱되어 생성된 eneityDaoVO
     * @return 생성된 sql문
     */
    public String createHist(CommonEntityDaoVO entityDaoVO) {
    	
    	List<String> tableColumns = entityDaoVO.getTableColumns();

        StringBuilder sql = new StringBuilder();
        StringBuilder insertBuilder = new StringBuilder();
        StringBuilder selectBuilder = new StringBuilder();
        StringBuilder asBuilder = new StringBuilder();
        StringBuilder valueBuilder = new StringBuilder();

        List<String> compareColumnList = new ArrayList<>(tableColumns);

        insertBuilder.append("INSERT INTO TABLE").append(SPACE);
        insertBuilder.append(StringUtil.removeSpecialCharAndLower(entityDaoVO.getDbTableName() + Constants.PARTIAL_HIST_TABLE_PREFIX)).append(SPACE);

        selectBuilder.append("select").append(SPACE);
        asBuilder.append(" as (");

        valueBuilder.append("VALUES (");

        Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = entityDaoVO.getDbColumnInfoVOMap();
        if (dbColumnInfoVOMap != null) {
            asBuilder.append("DATASET_ID, CREATED_AT, MODIFIED_AT");
            // 2. Default Column 설정
            valueBuilder.append("#{").append(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()).append("}").append(COMMA_WITH_SPACE);
            valueBuilder.append("from_utc_timestamp(#{").append(DataServiceBrokerCode.DefaultAttributeKey.CREATED_AT.getCode()).append("}, 'UTC')").append(COMMA_WITH_SPACE);
            valueBuilder.append("from_utc_timestamp(#{").append(DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode()).append("}, 'UTC')").append(COMMA_WITH_SPACE);

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

                compareColumnList.removeIf(e -> e.toUpperCase().equalsIgnoreCase(columnName));

                if (dbColumnType == DbColumnType.ARRAY_VARCHAR) {
                    valueBuilder.append("split(#{").append(daoAttributeId).append(", typeHandler=").append(StringArrayListTypeHandler.class.getName()).append("}, ',')").append(COMMA_WITH_SPACE);
                } else if (dbColumnType == DbColumnType.ARRAY_INTEGER) {
                    valueBuilder.append("split(#{").append(daoAttributeId).append(", typeHandler=").append(HiveIntegerArrayListTypeHandler.class.getName()).append("}, ',')").append(COMMA_WITH_SPACE);
                    tableColumns.replaceAll(e ->
                            (e.equals(columnName)) ? "ST_ARRAYINT(" + columnName + ")" : e
                    );
                } else if (dbColumnType == DbColumnType.ARRAY_FLOAT) {
                    valueBuilder.append("split(#{").append(daoAttributeId).append(", typeHandler=").append(HiveDoubleArrayListTypeHandler.class.getName()).append("}, ',')").append(COMMA_WITH_SPACE);
                    tableColumns.replaceAll(e ->
                            (e.equals(columnName)) ? "ST_ARRAYDOUBLE(" + columnName + ")" : e
                    );
                } else if (dbColumnType == DbColumnType.ARRAY_TIMESTAMP) {
                    valueBuilder.append("split(#{").append(daoAttributeId).append(", typeHandler=").append(HiveDateArrayListTypeHandler.class.getName()).append("}, ',')").append(COMMA_WITH_SPACE);
                    tableColumns.replaceAll(e ->
                            (e.equals(columnName)) ? "ST_ARRAYTIMESTAMP(" + columnName + ")" : e
                    );
                } else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
                    valueBuilder.append("split(#{").append(daoAttributeId).append(", typeHandler=").append(HiveBooleanArrayListTypeHandler.class.getName()).append("}, ',')").append(COMMA_WITH_SPACE);
                    tableColumns.replaceAll(e ->
                            (e.equals(columnName)) ? "ST_ARRAYBOOLEAN(" + columnName + ")" : e
                    );
//                	valueBuilder.append("split(#{").append(daoAttributeId).append(", typeHandler=").append(HiveBooleanArrayListTypeHandler.class.getName()).append("}, ',')").append(COMMA_WITH_SPACE);
                } else if (dbColumnType == DbColumnType.TIMESTAMP) {
                    valueBuilder.append("from_utc_timestamp(#{").append(daoAttributeId).append("},'UTC')").append(COMMA_WITH_SPACE);
                } else if (dbColumnType == DbColumnType.GEOMETRY_4326) {
                    tableColumns.replaceAll(e ->
                            (e.equalsIgnoreCase(columnName))? "ST_asText(ST_GeomFromGeoJSON(" + columnName + "))"
                                    :(e.equalsIgnoreCase(columnName + "_idx"))? "ST_DISKINDEX(ST_asText(ST_GeomFromGeoJSON(" + columnName+ ")))" : e
                    );

                    compareColumnList.remove(columnName + "_idx");

                    valueBuilder.append("#{").append(daoAttributeId).append("}").append(COMMA_WITH_SPACE);
                } else if (dbColumnType == DbColumnType.GEOMETRY_3857) {
                    tableColumns.replaceAll(e -> (e.equalsIgnoreCase(columnName))? "ST_AsGeoJson(ST_Transform(ST_FlipCoordinates(ST_GeomFromGeoJSON(" + columnName + ")), 'epsg:4326','epsg:3857'))" : e );
                    valueBuilder.append("#{").append(daoAttributeId).append("}").append(COMMA_WITH_SPACE);
                } else {
                    valueBuilder.append("#{").append(daoAttributeId).append(", jdbcType=VARCHAR}").append(COMMA_WITH_SPACE);
                }
            }

            if (!compareColumnList.isEmpty()) {
                for (String column : compareColumnList) {
                    if (column.equalsIgnoreCase("key")) {
                        asBuilder.append(COMMA_WITH_SPACE).append(column);
                        valueBuilder.append("#{").append(DataServiceBrokerCode.DefaultAttributeKey.ID.getCode()).append("}").append(COMMA_WITH_SPACE);
                    } else {
                        asBuilder.append(COMMA_WITH_SPACE).append(column);
                        valueBuilder.append("null").append(COMMA_WITH_SPACE);
                    }
                }
            }

            String fields = String.join(", ", tableColumns);
            selectBuilder.append(fields).append(SPACE).append("FROM").append(SPACE);
            asBuilder.append(COMMA_WITH_SPACE).append("ID").append(");");
            valueBuilder.append("#{").append(DataServiceBrokerCode.DefaultAttributeKey.ID.getCode()).append("}");
            valueBuilder.append(")");
        }

	sql.append(insertBuilder.toString()).append(selectBuilder.toString()).append(valueBuilder.toString()).append(asBuilder.toString());return sql.toString();

	}

	/**
     * Full 이력 저장 시 사용될 sql을 동적 생성
     *
     * @param entityDaoVO entitySchema 기반으로 파싱되어 생성된 eneityDaoVO
     * @return 생성된 sql문
     */
    public String createFullHist(CommonEntityDaoVO entityDaoVO) {
    	
    	List<String> tableColumns = entityDaoVO.getTableColumns();
    	
        StringBuilder sql = new StringBuilder();
        StringBuilder insertBuilder = new StringBuilder();
        StringBuilder selectBuilder = new StringBuilder();
        StringBuilder asBuilder = new StringBuilder();
        StringBuilder valueBuilder = new StringBuilder();

        List<String> compareColumnList = new ArrayList<>(tableColumns);

        insertBuilder.append("INSERT INTO TABLE").append(SPACE);
        insertBuilder.append(StringUtil.removeSpecialCharAndLower(entityDaoVO.getDbTableName() + Constants.FULL_HIST_TABLE_PREFIX)).append(SPACE);

        selectBuilder.append("select").append(SPACE);
        asBuilder.append(" as (");

        valueBuilder.append("VALUES (");

        Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = entityDaoVO.getDbColumnInfoVOMap();
        if (dbColumnInfoVOMap != null) {
            asBuilder.append("DATASET_ID, CREATED_AT, MODIFIED_AT");
            // 2. Default Column 설정
            valueBuilder.append("#{").append(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode()).append("}").append(COMMA_WITH_SPACE);
            valueBuilder.append("from_utc_timestamp(#{").append(DataServiceBrokerCode.DefaultAttributeKey.CREATED_AT.getCode()).append("}, 'UTC')").append(COMMA_WITH_SPACE);
            valueBuilder.append("from_utc_timestamp(#{").append(DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode()).append("}, 'UTC')").append(COMMA_WITH_SPACE);

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
                    valueBuilder.append("split(#{").append(daoAttributeId).append(", typeHandler=").append(StringArrayListTypeHandler.class.getName()).append("}, ',')").append(COMMA_WITH_SPACE);
                } else if (dbColumnType == DbColumnType.ARRAY_INTEGER) {
                    valueBuilder.append("split(#{").append(daoAttributeId).append(", typeHandler=").append(HiveIntegerArrayListTypeHandler.class.getName()).append("}, ',')").append(COMMA_WITH_SPACE);
                    tableColumns.replaceAll(e ->
                            (e.equals(columnName)) ? "ST_ARRAYINT(" + columnName + ")" : e
                    );
                } else if (dbColumnType == DbColumnType.ARRAY_FLOAT) {
                    valueBuilder.append("split(#{").append(daoAttributeId).append(", typeHandler=").append(HiveDoubleArrayListTypeHandler.class.getName()).append("}, ',')").append(COMMA_WITH_SPACE);
                    tableColumns.replaceAll(e ->
                            (e.equals(columnName)) ? "ST_ARRAYDOUBLE(" + columnName + ")" : e
                    );
                } else if (dbColumnType == DbColumnType.ARRAY_TIMESTAMP) {
                    valueBuilder.append("split(#{").append(daoAttributeId).append(", typeHandler=").append(HiveDateArrayListTypeHandler.class.getName()).append("}, ',')").append(COMMA_WITH_SPACE);
                    tableColumns.replaceAll(e ->
                            (e.equals(columnName)) ? "ST_ARRAYTIMESTAMP(" + columnName + ")" : e
                    );
                } else if (dbColumnType == DbColumnType.ARRAY_BOOLEAN) {
					valueBuilder.append("split(#{").append(daoAttributeId).append(", typeHandler=").append(HiveBooleanArrayListTypeHandler.class.getName()).append("}, ',')").append(COMMA_WITH_SPACE);
					tableColumns.replaceAll(e ->
							(e.equals(columnName)) ? "ST_ARRAYBOOLEAN(" + columnName + ")" : e
					);

                	// valueBuilder.append("split(#{").append(daoAttributeId).append(", typeHandler=").append(HiveBooleanArrayListTypeHandler.class.getName()).append("}, ',')").append(COMMA_WITH_SPACE);
                } else if (dbColumnType == DbColumnType.TIMESTAMP) {
                    valueBuilder.append("from_utc_timestamp(#{").append(daoAttributeId).append("},'UTC')").append(COMMA_WITH_SPACE);
                } else if (dbColumnType == DbColumnType.GEOMETRY_4326) {
                    tableColumns.replaceAll(e ->
                            (e.equalsIgnoreCase(columnName))? "ST_AsGeoJson(ST_GeomFromGeoJSON(" + columnName + "))"
                                    :(e.equalsIgnoreCase(columnName + "_idx"))? "ST_DISKINDEX(ST_asText(ST_GeomFromGeoJSON(" + columnName+ ")))" : e
                    );

                    compareColumnList.remove(columnName + "_idx");

                    valueBuilder.append("#{").append(daoAttributeId).append("}").append(COMMA_WITH_SPACE);
                } else if (dbColumnType == DbColumnType.GEOMETRY_3857) {
                    tableColumns.replaceAll(e -> (e.equalsIgnoreCase(columnName))? "ST_AsGeoJson(ST_Transform(ST_FlipCoordinates(ST_GeomFromGeoJSON(" + columnName + ")), 'epsg:4326','epsg:3857'))" : e );
                    valueBuilder.append("#{").append(daoAttributeId).append("}").append(COMMA_WITH_SPACE);
                } else {
                    valueBuilder.append("#{").append(daoAttributeId).append(", jdbcType=VARCHAR}").append(COMMA_WITH_SPACE);
                }
            }

            if (!compareColumnList.isEmpty()) {
                for (String column : compareColumnList) {
                    if (column.equalsIgnoreCase("key")) {
                        asBuilder.append(COMMA_WITH_SPACE).append(column);
                        valueBuilder.append("#{").append(DataServiceBrokerCode.DefaultAttributeKey.ID.getCode()).append("}").append(COMMA_WITH_SPACE);
                    } else {
                        asBuilder.append(COMMA_WITH_SPACE).append(column);
                        valueBuilder.append("null").append(COMMA_WITH_SPACE);
                    }
                }
            }

            String fields = String.join(", ", tableColumns);
            selectBuilder.append(fields).append(SPACE).append("FROM").append(SPACE);
            asBuilder.append(COMMA_WITH_SPACE).append("ID").append(");");
            valueBuilder.append("#{").append(DataServiceBrokerCode.DefaultAttributeKey.ID.getCode()).append("}");
            valueBuilder.append(")");
        }

	sql.append(insertBuilder.toString()).append(selectBuilder.toString()).append(valueBuilder.toString()).append(asBuilder.toString());return sql.toString();

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
}