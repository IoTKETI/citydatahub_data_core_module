package kr.re.keti.sc.dataservicebroker.datamodel.sqlprovider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.AttributeType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.AttributeValueType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.DbColumnType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.PropertyKey;
import kr.re.keti.sc.dataservicebroker.common.exception.BadRequestException;
import kr.re.keti.sc.dataservicebroker.datamodel.DataModelManager;
import kr.re.keti.sc.dataservicebroker.datamodel.service.DataModelSVC.DbOperation;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.Attribute;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelCacheVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelDbColumnVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelStorageMetadataVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.ObjectMember;
import kr.re.keti.sc.dataservicebroker.util.ValidateUtil;

@Component
public class BigdataTableSqlProvider {

	@Autowired
	private DataModelManager dataModelManager;

	/** 줄바꿈문자 */
	private final String LINE_SEPARATOR = "\n";
	/** 로거 */
	private final Logger logger = LoggerFactory.getLogger(BigdataTableSqlProvider.class);

	private static final String COMMA_WITH_SPACE = ", ";


	/** dataModel 기반 CREATE TABLE DDL 생성
	 *  - 엔티티최종테이블, Partial이력테이블, Full이력테이블
	 * @param dataModelCacheVO dataModelCache 정보
	 * @return 생성된 DDL
	 */
	public String generateCreateTableDdl(DataModelCacheVO dataModelCacheVO) throws BadRequestException {
		DataModelVO dataModelVO = dataModelCacheVO.getDataModelVO();
		DataModelStorageMetadataVO storageMetadataVO = dataModelCacheVO.getDataModelStorageMetadataVO();

		// index명 유효성 체크
		// 1. Index Attribute 유효성 검사
		if(!isValidIndexName(dataModelVO, dataModelVO.getIndexAttributeNames())) {
			throw new BadRequestException(ErrorCode.INVALID_DATAMODEL, "Invalid IndexAttributeName. indexAttributeNames=" + dataModelVO.getIndexAttributeNames());
		}

		List<Attribute> rootAttributes = dataModelVO.getAttributes();

		StringBuilder sql = new StringBuilder();

		// 1. 최종값 테이블 관련 쿼리 생성
		String entityTableName = dataModelManager.generateHiveTableName(dataModelVO.getId());

		StringBuilder columnsBuilder = new StringBuilder();
		columnsBuilder
				.append("ID VARCHAR(256) NOT NULL ").append(LINE_SEPARATOR).append(COMMA_WITH_SPACE)
//				.append("DATASET_ID VARCHAR(256) NOT NULL ").append(LINE_SEPARATOR).append(COMMA_WITH_SPACE)
				.append("DATASET_ID VARCHAR(256) ").append(LINE_SEPARATOR).append(COMMA_WITH_SPACE)
				.append("CREATED_AT TIMESTAMP").append(LINE_SEPARATOR).append(COMMA_WITH_SPACE)
				.append("MODIFIED_AT TIMESTAMP").append(LINE_SEPARATOR);

				generateDynamicColumnSql(columnsBuilder, null, rootAttributes, storageMetadataVO, false, null);

		//columnsBuilder.append(", ").append("ID VARCHAR(256)");

		// 1-1. 기본 컬럼
		sql.append("TableName: ").append(entityTableName).append(LINE_SEPARATOR)
				.append("Columns: ").append(columnsBuilder.toString()).append(LINE_SEPARATOR)
				.append(" CREATEHIVETABLE ");

		// 1-4. 동적 INDEX
		/*
		sql.append(generateDynamicIndexSql(entityTableName, dataModelVO.getIndexAttributeNames(), rootAttributes));
		sql.append(LINE_SEPARATOR).append(LINE_SEPARATOR);
		*/

		// 2. Partial 이력 테이블 관련 쿼리 생성
		String entityHistTableName = dataModelManager.generateHiveTableName(dataModelVO.getId(), Constants.PARTIAL_HIST_TABLE_PREFIX);

		StringBuilder histColumnsBuilder = new StringBuilder();
		histColumnsBuilder
				.append("ID VARCHAR(256) NOT NULL ").append(LINE_SEPARATOR).append(COMMA_WITH_SPACE)
				.append("DATASET_ID VARCHAR(256) NOT NULL ").append(LINE_SEPARATOR).append(COMMA_WITH_SPACE)
				.append("CREATED_AT TIMESTAMP").append(LINE_SEPARATOR).append(COMMA_WITH_SPACE)
				.append("MODIFIED_AT TIMESTAMP").append(LINE_SEPARATOR);

		generateDynamicColumnSql(histColumnsBuilder, null, rootAttributes, storageMetadataVO, false, null);

		//histColumnsBuilder.append("ID VARCHAR(256)");

		// 1-1. 기본 컬럼
		sql.append("TableName: ").append(entityHistTableName).append(LINE_SEPARATOR)
				.append("Columns: ").append(histColumnsBuilder.toString()).append(LINE_SEPARATOR)
				.append(" CREATEHIVETABLE ");



		// 2-3. 고정 INDEX
		/*
		sql.append(generateDynamicIndexSql(entityHistTableName, DefaultDbColumnName.ID.getCode()));
		sql.append(generateDynamicIndexSql(entityHistTableName, DefaultDbColumnName.MODIFIED_AT.getCode()));

		// 2-4. 동적 INDEX
		sql.append(generateDynamicIndexSql(entityHistTableName, dataModelVO.getIndexAttributeNames(), rootAttributes));
		sql.append(LINE_SEPARATOR).append(LINE_SEPARATOR);
		*/

		// 3. Full 이력 테이블 관련 쿼리 생성
		String entityFullTableName = dataModelManager.generateHiveTableName(dataModelVO.getId(), Constants.FULL_HIST_TABLE_PREFIX);

		StringBuilder fullHistColumnsBuilder = new StringBuilder();
		fullHistColumnsBuilder
				.append("ID VARCHAR(256) NOT NULL ").append(LINE_SEPARATOR).append(COMMA_WITH_SPACE)
				.append("DATASET_ID VARCHAR(256) NOT NULL ").append(LINE_SEPARATOR).append(COMMA_WITH_SPACE)
				.append("CREATED_AT TIMESTAMP").append(LINE_SEPARATOR).append(COMMA_WITH_SPACE)
				.append("MODIFIED_AT TIMESTAMP").append(LINE_SEPARATOR);

		generateDynamicColumnSql(fullHistColumnsBuilder, null, rootAttributes, storageMetadataVO, false, null);

		//fullHistColumnsBuilder.append("ID VARCHAR(256)");

		// 1-1. 기본 컬럼
		sql.append("TableName: ").append(entityFullTableName).append(LINE_SEPARATOR)
				.append("Columns: ").append(histColumnsBuilder.toString()).append(LINE_SEPARATOR);


		// 3-3. 고정 INDEX
		/*
		sql.append(generateDynamicIndexSql(entityFullTableName, DefaultDbColumnName.ID.getCode()));
		sql.append(generateDynamicIndexSql(entityFullTableName, DefaultDbColumnName.MODIFIED_AT.getCode()));

		// 3-4. 동적 INDEX
		sql.append(generateDynamicIndexSql(entityFullTableName, dataModelVO.getIndexAttributeNames(), rootAttributes));
		*/

		return sql.toString();
	}

	/** dataModel 기반 DROP TABLE DDL 생성
	 *  - 엔티티최종테이블, Partial이력테이블, Full이력테이블
	 * @param id 데이터모델 아이디
	 * @return 생성된 DDL
	 */
	public String generateDropTableDdl(String id) {
		StringBuilder sql = new StringBuilder();
		
		String entityTableName = dataModelManager.generateHiveTableName(id);
		sql.append("DROP TABLE IF EXISTS ").append(entityTableName).append(";")
		   .append(LINE_SEPARATOR).append(LINE_SEPARATOR);

		sql.append(" FORSPLIT ");

		String entityPartialHistTableName = dataModelManager.generateHiveTableName(id, Constants.PARTIAL_HIST_TABLE_PREFIX);
		sql.append("DROP TABLE IF EXISTS ").append(entityPartialHistTableName).append(";")
		   .append(LINE_SEPARATOR).append(LINE_SEPARATOR);

		sql.append(" FORSPLIT ");

		String entityFullHistTableName = dataModelManager.generateHiveTableName(id, Constants.FULL_HIST_TABLE_PREFIX);
		sql.append("DROP TABLE IF EXISTS ").append(entityFullHistTableName).append(";")
		   .append(LINE_SEPARATOR).append(LINE_SEPARATOR);

		return sql.toString();
	}

	/**
	 * Attribute에 해당하는 컬럼 Add DDL 또는 Drop DDL 생성
	 * @param id 데이터모델 아이디
	 * @param beforeAttribute 이전 Attribute
	 * @param afterAttribute 이후 Attribute
	 * @return 생성된 DDL
	 */
	public String generateAlterTableColumnDdl(String id, Attribute beforeAttribute, Attribute afterAttribute) {

		StringBuilder sql = new StringBuilder();

		// 1. 최종값 테이블 컬럼 추가 DDL 생성
		String entityTableName = dataModelManager.generateHiveTableName(id);
		String alterSql = generateAlterTableColumnTableDdl(entityTableName, beforeAttribute, afterAttribute);
		if(alterSql == null) {
			return null;
		}
		sql.append(alterSql);

		sql.append("---EOS---");

		// 2. partial 이력 테이블 컬럼 추가 DDL 생성
		String partialHistTableName = dataModelManager.generateHiveTableName(id, Constants.PARTIAL_HIST_TABLE_PREFIX);
		sql.append(generateAlterTableColumnTableDdl(partialHistTableName, beforeAttribute, afterAttribute));

		sql.append("---EOS---");

		// 3. full 이력 테이블 컬럼 추가 DDL 생성
		String fullHistTableName = dataModelManager.generateHiveTableName(id, Constants.FULL_HIST_TABLE_PREFIX);
		sql.append(generateAlterTableColumnTableDdl(fullHistTableName, beforeAttribute, afterAttribute));

		sql.append("---EOS---");
		
		return sql.toString();
	}

	/**
	 * Attribute에 해당하는 컬럼 Add DDL 또는 Drop DDL 생성
	 * @param tableName 테이블명
	 * @param beforeAttribute 이전 Attribute
	 * @param afterAttribute 이후 Attribute
	 * @return 생성된 DDL
	 */
	private String generateAlterTableColumnTableDdl(String tableName, Attribute beforeAttribute, Attribute afterAttribute) {

		// 1. 업데이트 이전 attribute 정보 기반 컬럼정보 추출
		Map<String, DataModelDbColumnVO> beforeDataModelDbColumnMap = new HashMap<>();
		dataModelManager.attributesToDbColumnVO(beforeDataModelDbColumnMap, null, new ArrayList<String>(), beforeAttribute);

		// 2. 업데이트 대상 attribute 정보 기반 컬럼정보 추출
		Map<String, DataModelDbColumnVO> afterDataModelDbColumnMap = new HashMap<>();
		dataModelManager.attributesToDbColumnVO(afterDataModelDbColumnMap, null, new ArrayList<String>(), afterAttribute);

		// 3. 위에서 생성한 두 attribute를 비교하여 sql 생성
		//  - 신규 컬럼 : ADD COLUMN, 변경 컬럼 : ALTER COLUMN, 삭제 컬럼 : DROP COLUMN
		
		// 3-1. ADD, ALTER 대상 세팅
		Map<DataModelDbColumnVO, DbOperation> dbOperationMap = new HashMap<>();
		for(Map.Entry<String, DataModelDbColumnVO> entry : afterDataModelDbColumnMap.entrySet()) {
			DataModelDbColumnVO beforeColumnVO = beforeDataModelDbColumnMap.get(entry.getKey());
			if(beforeColumnVO != null) {
				// 컬럼 타입 변경 여부 체크
				boolean doAlterColumn = false;
				if(beforeColumnVO.getColumnType() != entry.getValue().getColumnType()) {
					dbOperationMap.put(entry.getValue(), DbOperation.ALTER_COLUMN_TYPE);
					doAlterColumn = true;
				}
				if(isAlterColumnLength(beforeColumnVO.getMaxLength(), entry.getValue().getMaxLength())) {
					dbOperationMap.put(entry.getValue(), DbOperation.ALTER_COLUMN_TYPE);
					doAlterColumn = true;
				}

				// DROP NOT NULL
				boolean beforeNotNull = beforeColumnVO.getIsNotNull() == null ? false : beforeColumnVO.getIsNotNull();
				boolean afterNotNull = entry.getValue().getIsNotNull() == null ? false : entry.getValue().getIsNotNull();
				if(beforeNotNull && !afterNotNull) {
					if(doAlterColumn) { // alter column 과 not null 모두 수행
						dbOperationMap.put(entry.getValue(), DbOperation.ALTER_COLUMN_TYPE_AND_DROP_NOT_NULL);
					} else { // not null 관련 쿼리만 수행))
						dbOperationMap.put(entry.getValue(), DbOperation.DROP_NOT_NULL);
					}
				}

				// ADD NOT NULL
				if(!beforeNotNull && afterNotNull) {
					if(doAlterColumn) { // alter column 과 not null 모두 수행
						dbOperationMap.put(entry.getValue(), DbOperation.ALTER_COLUMN_TYPE_AND_ADD_NOT_NULL);
					} else { // not null 관련 쿼리만 수행))
						dbOperationMap.put(entry.getValue(), DbOperation.ADD_NOT_NULL);
					}
				}

			} else {
				dbOperationMap.put(entry.getValue(), DbOperation.ADD_COLUMN);
			}
		}
		// 3-2. DROP 대상 조회
		for(Map.Entry<String, DataModelDbColumnVO> entry : beforeDataModelDbColumnMap.entrySet()) {
			if(!afterDataModelDbColumnMap.containsKey(entry.getKey())) {
				dbOperationMap.put(entry.getValue(), DbOperation.DROP_COLUMN);
			}
		}
		
		if(dbOperationMap.isEmpty()) {
			return null;
		}

		// 4. 생성된 dbOperationMap 기반 ddl 생성
		StringBuilder sql = new StringBuilder();
//		sql.append("ALTER TABLE ").append(tableName).append(LINE_SEPARATOR);
		sql.append("ALTER TABLE ").append(tableName);
		for(Map.Entry<DataModelDbColumnVO, DbOperation> entry : dbOperationMap.entrySet()) {

			DataModelDbColumnVO dataModelDbColumnVO = entry.getKey();

			// 컬럼 alter 쿼리 세팅
			if(entry.getValue() == DbOperation.ADD_COLUMN) {
				sql.append(" ADD COLUMNS (`").append(dataModelDbColumnVO.getColumnName())
				   .append("` ").append(dataModelDbColumnVO.getColumnType().getBigdataCode()).append(",");
			} else if(entry.getValue() == DbOperation.ALTER_COLUMN_TYPE
					|| entry.getValue() == DbOperation.ALTER_COLUMN_TYPE_AND_ADD_NOT_NULL
					|| entry.getValue() == DbOperation.ALTER_COLUMN_TYPE_AND_DROP_NOT_NULL) {
				if (!sql.toString().contains("-replace- columns")) {
					sql.append(" -replace- columns (");
				}

				if (dataModelDbColumnVO.getColumnType() == DbColumnType.VARCHAR) {
					dataModelDbColumnVO.setMaxLength(null);
					dataModelDbColumnVO.setColumnType(DbColumnType.STRING);
				}

				sql.append(dataModelDbColumnVO.getColumnName());
				sql.append(" ").append(dataModelDbColumnVO.getColumnType().getBigdataCode());
				if(dataModelDbColumnVO.getMaxLength() != null) {
					sql.append("(").append(dataModelDbColumnVO.getMaxLength()).append(")");
				}
				// sql.append(" USING ").append(dataModelDbColumnVO.getColumnName()).append("::")
				// sql.append(dataModelDbColumnVO.getColumnType().getBigdataCode())
				sql.append(",");
			} else if(entry.getValue() == DbOperation.DROP_COLUMN) {
				sql.append(dataModelDbColumnVO.getColumnName()).append(" ").append(dataModelDbColumnVO.getColumnType().getBigdataCode()).append(",");
			}

			// 컬럼 not null 쿼리 세팅
			if(entry.getValue() == DbOperation.ADD_NOT_NULL
					|| entry.getValue() == DbOperation.ALTER_COLUMN_TYPE_AND_ADD_NOT_NULL) {
				
				sql.append(" ALTER COLUMN ").append(dataModelDbColumnVO.getColumnName()).append(" SET NOT NULL,");
				
			} else if(entry.getValue() == DbOperation.DROP_NOT_NULL
					|| entry.getValue() == DbOperation.ALTER_COLUMN_TYPE_AND_DROP_NOT_NULL) {
				
				sql.append(" ALTER COLUMN ").append(dataModelDbColumnVO.getColumnName()).append(" DROP NOT NULL,");
			}
//			sql.append(LINE_SEPARATOR);
		}
		sql.deleteCharAt(sql.length()-2); // 가장 뒤 콤마 제거
//		sql.append(");").append(LINE_SEPARATOR).append(LINE_SEPARATOR);
		sql.append(");");
		return sql.toString();
	}

	private boolean hasMaxLengthColumnType(DbColumnType dbColumnType) {
		if(dbColumnType == DbColumnType.VARCHAR
				|| dbColumnType == DbColumnType.FLOAT
				|| dbColumnType == DbColumnType.ARRAY_VARCHAR
				|| dbColumnType == DbColumnType.ARRAY_FLOAT) {
			return true;
		}
		return false;
	}

	private boolean isAlterColumnLength(String beforeMaxLength, String afterMaxLength) {
		if(beforeMaxLength != null) {
			return !beforeMaxLength.equals(afterMaxLength);
		} else if(afterMaxLength != null) {
			return !afterMaxLength.equals(beforeMaxLength);
		} else {
			return false;
		}
	}
	
	/**
	 * Attribute에 해당하는 컬럼 Add DDL 또는 Drop DDL 생성
	 * @param id 데이터모델 아이디
	 * @param attribute 대상 Attribute
	 * @param dbOperation 동작유형 (CREATE or DELETE)
	 * @return 생성된 DDL
	 */
	public String generateAddOrDropColumnDdl(String id, Attribute attribute,
			DataModelStorageMetadataVO storageMetadataVO, DbOperation dbOperation) {

		StringBuilder sql = new StringBuilder();

		// 1. 최종값 테이블 컬럼 추가 DDL 생성
		String entityTableName = dataModelManager.generateHiveTableName(id);
		sql.append(generateAddOrDropColumnTableDdl(entityTableName, attribute, storageMetadataVO, dbOperation));

		sql.append("---EOS---");

		// 2. partial 이력 테이블 컬럼 추가 DDL 생성
		String partialHistTableName = dataModelManager.generateHiveTableName(id, Constants.PARTIAL_HIST_TABLE_PREFIX);
		sql.append(generateAddOrDropColumnTableDdl(partialHistTableName, attribute, storageMetadataVO, dbOperation));

		sql.append("---EOS---");

		// 3. full 이력 테이블 컬럼 추가 DDL 생성
		String fullHistTableName = dataModelManager.generateHiveTableName(id, Constants.FULL_HIST_TABLE_PREFIX);
		sql.append(generateAddOrDropColumnTableDdl(fullHistTableName, attribute, storageMetadataVO, dbOperation));

		sql.append("---EOS---");
		
		return sql.toString();
	}

	//@todo: Current Drop Column is not working. The method would be implemented later if necessary
	public String generateDropColumnDdl(String id, List<Attribute> attributes,
			DataModelStorageMetadataVO storageMetadataVO, DbOperation dbOperation) {
		StringBuilder sql = new StringBuilder();
		StringBuilder tempSql = new StringBuilder();

		// 1. 최종값 테이블 컬럼 추가 DDL 생성
		String entityTableName = dataModelManager.generateHiveTableName(id);
		tempSql.append(generateDropColumnTableDdl(entityTableName, attributes, storageMetadataVO, dbOperation));
		tempSql.append(" HIVEDROPCOLUMN ");
		sql.append(tempSql.toString().replaceAll(LINE_SEPARATOR, ""));
		tempSql = new StringBuilder();

		// 2. partial 이력 테이블 컬럼 추가 DDL 생성
		String partialHistTableName = dataModelManager.generateHiveTableName(id, Constants.PARTIAL_HIST_TABLE_PREFIX);
		tempSql.append(generateDropColumnTableDdl(partialHistTableName, attributes, storageMetadataVO, dbOperation));
		tempSql.append(" HIVEDROPCOLUMN ");
		sql.append(tempSql.toString().replaceAll(LINE_SEPARATOR, ""));
		tempSql = new StringBuilder();

		// 3. full 이력 테이블 컬럼 추가 DDL 생성
		String fullHistTableName = dataModelManager.generateHiveTableName(id, Constants.FULL_HIST_TABLE_PREFIX);
		tempSql.append(generateDropColumnTableDdl(fullHistTableName, attributes, storageMetadataVO, dbOperation));
		tempSql.append(" HIVEDROPCOLUMN ");
		sql.append(tempSql.toString().replaceAll(LINE_SEPARATOR, ""));

		return sql.toString();
	}

	/**
	 * Attribute에 해당하는 컬럼 Add DDL 또는 Drop DDL 생성
	 * @param tableName 테이블명
	 * @param attribute 대상 Attribute
	 * @param dbOperation DB동작유형 (ADD, DROP)
	 * @return 생성된 DDL
	 */
	//@todo: Current Drop Column is not working. The method would be implemented later if necessary
	private String generateAddOrDropColumnTableDdl(String tableName, Attribute attribute,
			DataModelStorageMetadataVO storageMetadataVO, DbOperation dbOperation) {
		StringBuilder sql = new StringBuilder();
//		sql.append("ALTER TABLE ").append(tableName).append(LINE_SEPARATOR);
		sql.append("ALTER TABLE ").append(tableName);
		
		StringBuilder addColumnSql = new StringBuilder();
		generateDynamicColumnSql(addColumnSql, null, attribute, storageMetadataVO, false, dbOperation);
		addColumnSql.deleteCharAt(0); // sql 가장 앞 콤마 제거 
		sql.append(addColumnSql);
		sql.append(")");
//		sql.append(";").append(LINE_SEPARATOR).append(LINE_SEPARATOR);
		sql.append(";");
		
		return sql.toString();
	}

	//@todo: Current Drop Column is not working. The method would be implemented later if necessary
	private String generateDropColumnTableDdl(String tableName, List<Attribute> attributes,
			DataModelStorageMetadataVO storageMetadataVO, DbOperation dbOperation) {
		StringBuilder sql = new StringBuilder();
		sql.append("ALTER TABLE ").append(tableName);
		sql.append(" -replace- columns (");

		for (Attribute attribute : attributes) {
			attribute.setIsRequired(false);
			StringBuilder addColumnSql = new StringBuilder();

			generateDynamicColumnSql(addColumnSql, null, attribute, storageMetadataVO, false, dbOperation);

			addColumnSql.deleteCharAt(addColumnSql.indexOf(COMMA_WITH_SPACE));
			sql.append(addColumnSql.toString().replaceAll(LINE_SEPARATOR, "")).append(COMMA_WITH_SPACE);
		}

		sql.deleteCharAt(sql.lastIndexOf(COMMA_WITH_SPACE));
		sql.append(");").append(LINE_SEPARATOR).append(LINE_SEPARATOR);

		return sql.toString();
	}

	/**
	 * 데이터모델 정보 기반 동적 컬럼 생성
	 *
	 * @param sql               SQL 문자열
	 * @param parentHierarchyId 상위 계층구조 Attribute id
	 * @param rootAttributes    dataModel RootAttribute리스트
	 * @param ignoreRequired    필수값 무시 여부 (true인 경우 not null 제약조건을 걸지 않음)
	 * @param dbOperation       해당 값에 따라 DDL 생성 시 특정 구문을 추가
	 *                          - 'ADD'인 경우 'ADD COLUMN' 구문 추가
	 *                          - 'DROP' 경우 'DROP COLUMN' 구문 추가
	 */
	private void generateDynamicColumnSql(StringBuilder sql, String parentHierarchyId, List<Attribute> rootAttributes,
			DataModelStorageMetadataVO storageMetadataVO,
			Boolean ignoreRequired, DbOperation dbOperation) {

		for(Attribute rootAttribute : rootAttributes) {

			generateDynamicColumnSql(sql, parentHierarchyId, rootAttribute, storageMetadataVO, ignoreRequired,
					dbOperation);
		}
	}

	private boolean needAddColumn = true;

	/**
	 * 데이터모델 정보 기반 동적 컬럼 생성
	 * @param sql SQL 문자열
	 * @param parentHierarchyId 상위 계층구조 Attribute id
	 * @param rootAttribute dataModel RootAttribute
	 * @param ignoreRequired 필수값 무시 여부 (true인 경우 not null 제약조건을 걸지 않음)
	 * @param dbOperation 해당 값에 따라 DDL 생성 시 특정 구문을 추가
	 *  - 'ADD'인 경우 'ADD COLUMN' 구문 추가
	 *  - 'DROP' 경우 'DROP COLUMN' 구문 추가
	 */
	private void generateDynamicColumnSql(StringBuilder sql, String parentHierarchyId, Attribute rootAttribute,
			DataModelStorageMetadataVO storageMetadataVO, Boolean ignoreRequired, DbOperation dbOperation) {
		String currentHierarchyName = null;
		if(parentHierarchyId != null) {
			currentHierarchyName = parentHierarchyId + Constants.COLUMN_DELIMITER + rootAttribute.getName();
		} else {
			currentHierarchyName = rootAttribute.getName();
		}

		// 1. rootAttribute의 type이 'Property'인 경우
		if(rootAttribute.getAttributeType() == AttributeType.PROPERTY) {

			// 1-1. valueType이 Object인 경우
			if(rootAttribute.getValueType() == AttributeValueType.OBJECT) {
				objectToSql(sql, currentHierarchyName, rootAttribute.getObjectMembers(), ignoreRequired, dbOperation);
			// 1-2. valueType이 ArrayObject인 경우
			} else if(rootAttribute.getValueType() == AttributeValueType.ARRAY_OBJECT) {
				List<ObjectMember> objectMembers = rootAttribute.getObjectMembers();
				if(objectMembers != null) {
					for(ObjectMember objectMember : objectMembers) {
						String id = rootAttribute.getName() + Constants.COLUMN_DELIMITER + objectMember.getName();
						Boolean isRequired = objectMember.getIsRequired();
						if(ignoreRequired) isRequired = false;
						addColumnDdl(sql, id, objectMember.getValueType(), objectMember.getMaxLength(), isRequired, true, dbOperation);
					}
				}

			// 1-3. valueType이 String, Integer, Double, Date, ArrayString, ArrayInteger, ArrayDouble인 경우
			} else {
				Boolean isRequired = rootAttribute.getIsRequired();
				if(ignoreRequired) isRequired = false;
				addColumnDdl(sql, currentHierarchyName, rootAttribute.getValueType(), rootAttribute.getMaxLength(), isRequired, false, dbOperation);
			}

		// 2. rootAttribute의 type이 'GeoProperty'인 경우
		// attributeId에 _3857, _4326 을 붙인 2개의 geometry 컬럼을 생성
		} else if(rootAttribute.getAttributeType() == AttributeType.GEO_PROPERTY) {
			Boolean isRequired = rootAttribute.getIsRequired();
			if(ignoreRequired) isRequired = false;
			addGeoColumnDdl(sql, currentHierarchyName + Constants.GEO_PREFIX_3857, isRequired, dbOperation);
			addGeoColumnDdl(sql, currentHierarchyName + Constants.GEO_PREFIX_4326, isRequired, dbOperation);
			addGeoColumnDdl(sql, currentHierarchyName + Constants.GEO_PREFIX_4326 + "_idx", isRequired, dbOperation);

		// 3. rootAttribute의 type이 'Relationship'인 경우
		} else if(rootAttribute.getAttributeType() == AttributeType.RELATIONSHIP) {
			Boolean isRequired = rootAttribute.getIsRequired();
			if(ignoreRequired) isRequired = false;
			addColumnDdl(sql, currentHierarchyName, AttributeValueType.STRING, rootAttribute.getMaxLength(), isRequired, false, dbOperation);
		}

		// 4. observed=true 인 경우
		// observed_at 컬럼 sql 생성
		if(rootAttribute.getHasObservedAt() != null && rootAttribute.getHasObservedAt()) {
			String id = currentHierarchyName + Constants.COLUMN_DELIMITER + PropertyKey.OBSERVED_AT.getCode();
			addColumnDdl(sql, id, AttributeValueType.DATE, null, null, false, dbOperation);
		}

		// 5. has Property 혹은 has Relationship 이 존재하는 경우
		List<Attribute> hasAttributes = rootAttribute.getChildAttributes();
		if(hasAttributes != null && hasAttributes.size() > 0) {
			generateDynamicColumnSql(sql, currentHierarchyName, hasAttributes, storageMetadataVO, ignoreRequired,
					dbOperation);
		}

		//RdbTableSqlProvider에만 있던 내용 복사. hierarchyAttrNames는 일단 여기에 작성
		List<String> hierarchyAttrNames = new ArrayList<>();
		hierarchyAttrNames.add(rootAttribute.getName());

		// 6. property의 createdAt 컬럼 sql 생성
		String createdAtColumnName = getColumnNameByStorageMetadata(hierarchyAttrNames,
				PropertyKey.CREATED_AT.getCode(), storageMetadataVO);
		addColumnDdl(sql, createdAtColumnName, AttributeValueType.DATE, null, null, false, dbOperation);

		// 7. property의 modifiedAt 컬럼 sql 생성
		String modifiedAtColumnName = getColumnNameByStorageMetadata(hierarchyAttrNames,
				PropertyKey.MODIFIED_AT.getCode(), storageMetadataVO);
		addColumnDdl(sql, modifiedAtColumnName, AttributeValueType.DATE, null, null, false, dbOperation);

		needAddColumn = true;
	}

	//rdbTableSqlProvider에서 가져옴
	private String getColumnNameByStorageMetadata(List<String> hierarchyAttrNames, String attrName,
			DataModelStorageMetadataVO storageMetadataVO) {

		if (attrName == null) {
			return dataModelManager.getColumnNameByStorageMetadata(storageMetadataVO, hierarchyAttrNames);
		} else {
			List<String> attrNames = new ArrayList<>(hierarchyAttrNames);
			attrNames.add(attrName);
			return dataModelManager.getColumnNameByStorageMetadata(storageMetadataVO, attrNames);
		}
	}

	/**
	 * Object 형태의 property를 sql문 생성
	 * @param sql appendSQL문
	 * @param hierarchyAttributeId 계층구조 부모 AttributeId
	 * @param objectMembers ChildAttribute
	 * @param ignoreRequired 필수값 무시 여부 (true인 경우 not null 제약조건을 걸지 않음)
	 * @param dbOperation 해당 값에 따라 DDL 생성 시 특정 구문을 추가
	 *  - 'ADD'인 경우 'ADD COLUMN' 구문 추가
	 *  - 'DROP' 경우 'DROP COLUMN' 구문 추가
	 */
	private void objectToSql(StringBuilder sql, String hierarchyAttributeId, List<ObjectMember> objectMembers, boolean ignoreRequired, DbOperation dbOperation) {
		if(objectMembers != null) {
			for(ObjectMember objectMember : objectMembers) {

				String currentHierarchyId = hierarchyAttributeId + Constants.COLUMN_DELIMITER + objectMember.getName();

				// valueType이 OBJECT 여서 하위 필드가 또 존재하는 경우
				if(objectMember.getValueType() == AttributeValueType.OBJECT && objectMember.getObjectMembers() != null) {

					objectToSql(sql, currentHierarchyId, objectMember.getObjectMembers(), ignoreRequired, dbOperation);

				} else {

					// SQL문 생성
					Boolean isRequired = objectMember.getIsRequired();
					if(ignoreRequired) isRequired = false;

					addColumnDdl(sql, currentHierarchyId, objectMember.getValueType(), objectMember.getMaxLength(), isRequired, false, dbOperation);
				}
			}
		}
	}

	/**
	 * 동적 Index 생성 sql을 만들어 반환
	 *
	 * @param dbTableName          테이블명
	 * @param indexAttributeIdList 인덱스 대상 attibuteId 리스트
	 * @param rootAttributes       dataModel의 RootAttrubute
	 * @return 생성된 sql문
	 */
	private String generateDynamicIndexSql(String dbTableName, List<String> indexAttributeIdList, List<Attribute> rootAttributes) {
		StringBuilder sql = new StringBuilder();
		if(indexAttributeIdList != null) {
			for(int i=0; i<indexAttributeIdList.size(); i++) {

				sql.append(generateDynamicIndexSql(dbTableName, indexAttributeIdList.get(i)));

				// indexAttribute 가 GeoType인 경우
				// geoType인 경우 사전 정의된 2개의 컬럼에 대해 인덱스를 생성함 (attributeId_3857, attributeId_4326)
//				boolean isGeoType = false;
//				for(Attribute rootAttribute : rootAttributes) {
//					if(indexAttributeIdList.get(i).equals(rootAttribute.getName())
//							&& rootAttribute.getAttributeType() == AttributeType.GEO_PROPERTY) {
//						sql.append(generateDynamicIndexSql(dbTableName, indexAttributeIdList.get(i) + Constants.GEO_PREFIX_3857));
//						sql.append(generateDynamicIndexSql(dbTableName, indexAttributeIdList.get(i) + Constants.GEO_PREFIX_4326));
//						isGeoType = true;
//						break;
//					}
//				}
//
//				// geoType이 아닌 경우
//				if(!isGeoType) {
//					sql.append(generateDynamicIndexSql(dbTableName, indexAttributeIdList.get(i)));
//				}
			}
		}
		return sql.toString();
	}

	/**
	 * geo가 아닌 일반 타입의 컬럼에 대한 동적 인덱스 sql문 생성하여 반환
	 *
	 * @param dbTableName      테이블명
	 * @param indexAttributeId 인덱스 대상 attributeId
	 * @return 생성된 sql문
	 */
	private String generateDynamicIndexSql(String dbTableName, String indexAttributeId) {
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE INDEX IF NOT EXISTS \"").append(dbTableName)
		   .append(Constants.TABLE_NAME_DELIMITER_UNDER).append(indexAttributeId).append("\"")
		   .append(" ON ").append(".\"").append(dbTableName).append("\"")
		   .append("(");
		if(indexAttributeId.contains(",")) {
			String[] indexAttributeArr = indexAttributeId.split(",");
			for(String attrId : indexAttributeArr) {
				if(!ValidateUtil.isEmptyData(attrId)) {
					sql.append(attrId).append(",");
				}
			}
			sql.deleteCharAt(sql.length()-1);
		} else {
			sql.append(indexAttributeId);
		}
		sql.append(");").append(LINE_SEPARATOR);
		return sql.toString();
	}

	/**
	 * 기존 인덱스정보와 신규 인덱스 정보를 비교하여 인덱스를 ADD or DROP 인덱스 DDL 생성
	 * @param dataModel 데이터모델
	 * @param indexAttributeNames 신규 indexAttributeNames
	 * @param beforeIndexAttributeNames 기존 indexAttributeNames
	 * @return index ADD or DROP DDL
	 */
	public String generateIndexDdl(DataModelVO dataModel,
			List<String> indexAttributeNames, List<String> beforeIndexAttributeNames) {

		if(!ValidateUtil.isEmptyData(indexAttributeNames)) {

			// 1. Index Attribute 유효성 검사
			if(!isValidIndexName(dataModel, indexAttributeNames)) {
				throw new BadRequestException(ErrorCode.INVALID_DATAMODEL, "Invalid IndexAttributeName. indexAttributeNames=" + indexAttributeNames);
			}
			
			// 2. Index 수정 여부 체크
			Map<String, DbOperation> indexAttributeMap = new HashMap<>();
			// 신규로 입력받은 indexAttribteName은 일단 모두 CREATE 로 세팅
			for(String attributeName : indexAttributeNames) {
				indexAttributeMap.put(attributeName, DbOperation.ADD_COLUMN);
			}
			for(String beforeAttributeName : beforeIndexAttributeNames) {
				// 이전에 존재했던 인덱스들은 제외
				if(indexAttributeMap.containsKey(beforeAttributeName)) {
					indexAttributeMap.remove(beforeAttributeName);
				} else {
					// 이전에 존재했지만 신규 요청에서 사라진 인덱스 들은 삭제로 세팅
					indexAttributeMap.put(beforeAttributeName, DbOperation.DROP_COLUMN);
				}
			}

			String id = dataModel.getId();
			
			String entityTableName = dataModelManager.generateHiveTableName(id);
			String partialHistTableName = dataModelManager.generateHiveTableName(id, Constants.PARTIAL_HIST_TABLE_PREFIX);
			String fullHistTableName = dataModelManager.generateHiveTableName(id, Constants.FULL_HIST_TABLE_PREFIX);

	        // 3. Alter Index DDL 생성
			StringBuilder sql = new StringBuilder();

			for(Map.Entry<String, DbOperation> entry : indexAttributeMap.entrySet()) {
				switch(entry.getValue()) {
					case ADD_COLUMN:
						sql.append(generateDynamicIndexSql(entityTableName, entry.getKey()));
						sql.append(generateDynamicIndexSql(partialHistTableName, entry.getKey()));
						sql.append(generateDynamicIndexSql(fullHistTableName, entry.getKey()));
						break;
					case DROP_COLUMN:
						sql.append(generateDropIndexDdl(entityTableName, entry.getKey()));
						sql.append(generateDropIndexDdl(partialHistTableName, entry.getKey()));
						sql.append(generateDropIndexDdl(fullHistTableName, entry.getKey()));
						break;
					default:
						break;
				}
			}
			return sql.toString();
		}
		return null;
	}

	public boolean isValidIndexName(DataModelVO dataModelVO, List<String> indexAttributeNames) {
		if(indexAttributeNames == null || indexAttributeNames.size() == 0) {
			return true;
		}

		// 1. Index Attribute 유효성 검사
		DataModelStorageMetadataVO storageMetadataVO = dataModelManager.createDataModelStorageMetadata(dataModelVO, null, null);
		Map<String, DataModelDbColumnVO> dataModelDbColumnVO = storageMetadataVO.getDbColumnInfoVOMap();

		for(String indexAttributeName : indexAttributeNames) {
			String[] compositIndexNames = indexAttributeName.split(",");
			boolean isValid = false;
			for(String indexName : compositIndexNames) {
				for(String columnName : dataModelDbColumnVO.keySet()) {
					if(columnName.equals(indexName)) {
						isValid = true;
						break;
					}
				}
			}
			if(!isValid) {
				return false;
			}
		}
		return true;
	}

	/**
	 * DROP INDEX DDL 생성
	 * @param tableName 테이블명
	 * @param indexAttributeName 인덱스이름리스트
	 * @return DROP INDEX DDL
	 */
	private String generateDropIndexDdl(String tableName, String indexAttributeName) {

		StringBuilder sql = new StringBuilder();
		sql.append("DROP INDEX IF EXISTS ").append(".\"").append(tableName)
		   .append(Constants.TABLE_NAME_DELIMITER_UNDER).append(indexAttributeName).append("\";")
		   .append(LINE_SEPARATOR).append(LINE_SEPARATOR);

		return sql.toString();
	}

	/**
	 * 테이블 생성 시 컬럼부분 sql문 생성하여 반환 
	 * @param sql 테이블생성 sql문 StringBuilder
	 * @param attributeId attribute id
	 * @param valueType attribute value type
	 * @param maxLength 최대길이 (최대길이가 없는 경우 sql에서 길이를 지정하지 않음)
	 * @param required 필수여부 (not null 여부)
	 * @param isArrayObject array형태의 object 여부
	 * @param dbOperation 동작유형 (add or drop)
	 * @return 컬럼추가 sql문이 추가된 StringBuilder
	 */
	private StringBuilder addColumnDdl(StringBuilder sql, String attributeId, AttributeValueType valueType, 
			String maxLength, Boolean required, Boolean isArrayObject, DbOperation dbOperation) {

		DbColumnType columnType = null;

		StringBuilder columnOption = new StringBuilder();
		if(isArrayObject) {
			columnType = arrayObjectValueTypeToDbColumnType(valueType);
		} else {
			columnType = valueTypeToDbColumnType(valueType);
		}

		if (columnType == DbColumnType.VARCHAR) {
			maxLength = null;
			columnType = DbColumnType.STRING;
		}

		if(maxLength != null && hasMaxLengthColumnType(columnType)) {
			columnOption.append("(").append(maxLength).append(")");
		}
		else if (!isArrayType(columnType)) {
			columnOption.append("(65535)");
		}
		/*
		if(required != null && required) {
			columnOption.append(" NOT NULL ");
		}
		 */

		sql.append(", ");
		if(dbOperation == DbOperation.ADD_COLUMN) {
			if (needAddColumn) {
				sql.append("ADD COLUMNS (");
				needAddColumn = false;
			}
			sql.append(attributeId).append(" ").append(columnType.getBigdataCode());
		} else if(dbOperation == DbOperation.DROP_COLUMN) {
			sql.append(attributeId).append(" ").append(columnType.getBigdataCode());
			if(maxLength != null && hasMaxLengthColumnType(columnType)) {
				sql.append("(").append(maxLength).append(")");
			}
		} else {
			sql.append(attributeId)
			   .append(" ").append(columnType.getBigdataCode());
		}

		sql.append(columnOption);

		sql.append(LINE_SEPARATOR);
		return sql;
	}

	public boolean isArrayType(DbColumnType dbColumnType) {
		return (dbColumnType == DbColumnType.ARRAY_TIMESTAMP
				|| dbColumnType == DbColumnType.ARRAY_INTEGER
				|| dbColumnType == DbColumnType.ARRAY_VARCHAR
				|| dbColumnType == DbColumnType.ARRAY_FLOAT
				|| dbColumnType == DbColumnType.ARRAY_BOOLEAN
				|| dbColumnType == DbColumnType.TIMESTAMP
				|| dbColumnType == DbColumnType.INTEGER
				|| dbColumnType == DbColumnType.FLOAT
				|| dbColumnType == DbColumnType.BOOLEAN
				|| dbColumnType == DbColumnType.STRING);
	}

	/**
	 * Geometry 타입의 컬럼 sql 문 생성
	 * @param sql 테이블생성 sql문 StringBuilder
	 * @param attributeId attribute id
	 * @param required 필수여부 (not null 여부)
	 * @return 컬럼추가 sql문이 추가된 StringBuilder
	 */
	private StringBuilder addGeoColumnDdl(StringBuilder sql, String attributeId, Boolean required, DbOperation dbOperation) {
		if (dbOperation == DbOperation.ADD_COLUMN) {
			if (needAddColumn) {
				sql.append(" ADD COLUMNS (");
				needAddColumn = false;
			} else {
				sql.append(", ");
			}
		} else {
			sql.append(", ");
		}
		String columnName = attributeId.toLowerCase();
		sql.append(columnName)
		   .append(" ").append("STRING");

		sql.append(LINE_SEPARATOR);
		return sql;
	}

	
	/**
	 * Java value type에 해당하는 Db column type조회
	 * @param attributeValueType attribute value type
	 * @return DbColumnType
	 */
	private DbColumnType valueTypeToDbColumnType(AttributeValueType attributeValueType) {
		switch(attributeValueType){
			case STRING:
				return DbColumnType.VARCHAR;
			case INTEGER:
				return DbColumnType.INTEGER;
			case DOUBLE:
				return DbColumnType.FLOAT;
			case DATE:
				return DbColumnType.TIMESTAMP;
			case BOOLEAN:
				return DbColumnType.BOOLEAN;
			case ARRAY_STRING:
				return DbColumnType.ARRAY_VARCHAR;
			case ARRAY_INTEGER:
				return DbColumnType.ARRAY_INTEGER;
			case ARRAY_DOUBLE:
				return DbColumnType.ARRAY_FLOAT;
			case ARRAY_BOOLEAN:
				return DbColumnType.ARRAY_BOOLEAN;
		default:
			return null;
		}
	}

	/**
	 * java value type arrayObject 에 해당하는 Db column type 조회
	 * @param attributeValueType attribute value type
	 * @return DbColumnType
	 */
	private DbColumnType arrayObjectValueTypeToDbColumnType(AttributeValueType attributeValueType) {
		switch(attributeValueType){
		case STRING:
			return DbColumnType.ARRAY_VARCHAR;
		case INTEGER:
			return DbColumnType.ARRAY_INTEGER;
		case DOUBLE:
			return DbColumnType.ARRAY_FLOAT;
		case DATE:
			return DbColumnType.ARRAY_TIMESTAMP;
		case BOOLEAN:
			return DbColumnType.ARRAY_BOOLEAN;
		default:
			return null;
		}
	}
}