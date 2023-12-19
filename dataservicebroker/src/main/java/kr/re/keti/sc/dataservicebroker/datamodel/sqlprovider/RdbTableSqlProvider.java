package kr.re.keti.sc.dataservicebroker.datamodel.sqlprovider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.AttributeType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.AttributeValueType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.DbColumnType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.DefaultDbColumnName;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.PropertyKey;
import kr.re.keti.sc.dataservicebroker.common.exception.BadRequestException;
import kr.re.keti.sc.dataservicebroker.datamodel.DataModelManager;
import kr.re.keti.sc.dataservicebroker.datamodel.service.DataModelSVC.DbOperation;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.Attribute;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelDbColumnVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelStorageMetadataVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelCacheVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.ObjectMember;
import kr.re.keti.sc.dataservicebroker.util.StringUtil;
import kr.re.keti.sc.dataservicebroker.util.ValidateUtil;

@Component
public class RdbTableSqlProvider {

	@Autowired
	private DataModelManager dataModelManager;

	/** 줄바꿈문자 */
	private final String LINE_SEPARATOR = "\n";
	

	/** dataModel 기반 CREATE TABLE DDL 생성
	 *  - 엔티티최종테이블, Partial이력테이블, Full이력테이블
	 * @param dataModelVO dataModel 정보
	 * @return 생성된 DDL
	 */
	public String generateCreateTableDdl(DataModelCacheVO dataModelCacheVO) throws BadRequestException {

		DataModelVO dataModelVO = dataModelCacheVO.getDataModelVO();
		DataModelStorageMetadataVO storageMetadataVO = dataModelCacheVO.getDataModelStorageMetadataVO();

		// 1. Index Attribute 유효성 검사
		if(!isValidIndexName(dataModelCacheVO.getDataModelVO(), dataModelVO.getIndexAttributeNames(), storageMetadataVO)) {
			throw new BadRequestException(ErrorCode.INVALID_DATAMODEL, "Invalid IndexAttributeName. indexAttributeNames=" + dataModelVO.getIndexAttributeNames());
		}

		List<Attribute> rootAttributes = dataModelVO.getAttributes();

		StringBuilder sql = new StringBuilder();

		// 1. 최종값 테이블 관련 쿼리 생성
		String entityTableName = dataModelManager.generateRdbTableName(dataModelVO.getId());
		// 1-1. 기본 컬럼
		sql.append("CREATE TABLE IF NOT EXISTS ").append(Constants.SCHEMA_NAME).append(".\"").append(entityTableName).append("\"").append(LINE_SEPARATOR)
		   .append("(").append(LINE_SEPARATOR)
		   .append("ID VARCHAR(256) NOT NULL ").append(LINE_SEPARATOR)
		   .append(", DATASET_ID VARCHAR(256) ").append(LINE_SEPARATOR)
		   .append(", CREATED_AT TIMESTAMP WITH TIME ZONE NOT NULL ").append(LINE_SEPARATOR)
		   .append(", MODIFIED_AT TIMESTAMP WITH TIME ZONE NOT NULL ").append(LINE_SEPARATOR);

		// 1-2. 동적 컬럼
		generateDynamicColumnSql(sql, null, rootAttributes, storageMetadataVO, false, null);

		// 1-3. 고정 PK
		sql.append(" , CONSTRAINT \"").append(entityTableName).append("_PK\" PRIMARY KEY(ID)").append(LINE_SEPARATOR)
		   .append(");").append(LINE_SEPARATOR).append(LINE_SEPARATOR);

		// 1-4. 동적 INDEX
		sql.append(generateDynamicIndexSql(entityTableName, dataModelVO.getIndexAttributeNames(), storageMetadataVO));
		sql.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

		// 2. Partial 이력 테이블 관련 쿼리 생성
		String entityHistTableName = dataModelManager.generateRdbTableName(dataModelVO.getId(), Constants.PARTIAL_HIST_TABLE_PREFIX);

		// 2-1. 기본 컬럼
		sql.append("CREATE TABLE IF NOT EXISTS ").append(Constants.SCHEMA_NAME).append(".\"").append(entityHistTableName).append("\"").append(LINE_SEPARATOR)
		   .append("(").append(LINE_SEPARATOR)
		   .append("ID VARCHAR(256) NOT NULL ").append(LINE_SEPARATOR)
		   .append(", DATASET_ID VARCHAR(256) ").append(LINE_SEPARATOR)
		   .append(", MODIFIED_AT TIMESTAMP WITH TIME ZONE NOT NULL ").append(LINE_SEPARATOR)
		   .append(", OPERATION VARCHAR(40) NOT NULL ").append(LINE_SEPARATOR);

		// 2-2. 동적 컬럼
		generateDynamicColumnSql(sql, null, rootAttributes, storageMetadataVO, true, null);
		sql.append(");").append(LINE_SEPARATOR).append(LINE_SEPARATOR);

		// 2-3. 고정 INDEX
		sql.append(generateDynamicIndexSql(entityHistTableName, DefaultDbColumnName.ID.getCode(), null));
		sql.append(generateDynamicIndexSql(entityHistTableName, DefaultDbColumnName.MODIFIED_AT.getCode(), null));

		// 2-4. 동적 INDEX
		sql.append(generateDynamicIndexSql(entityHistTableName, dataModelVO.getIndexAttributeNames(), storageMetadataVO));
		sql.append(LINE_SEPARATOR).append(LINE_SEPARATOR);


		// 3. Full 이력 테이블 관련 쿼리 생성
		String entityFullTableName = dataModelManager.generateRdbTableName(dataModelVO.getId(), Constants.FULL_HIST_TABLE_PREFIX);

		// 3-1. 기본 컬럼
		sql.append("CREATE TABLE IF NOT EXISTS ").append(Constants.SCHEMA_NAME).append(".\"").append(entityFullTableName).append("\"").append(LINE_SEPARATOR)
		   .append("(").append(LINE_SEPARATOR)
		   .append("ID VARCHAR(256) NOT NULL ").append(LINE_SEPARATOR)
		   .append(", DATASET_ID VARCHAR(256) ").append(LINE_SEPARATOR)
		   .append(", MODIFIED_AT TIMESTAMP WITH TIME ZONE NOT NULL ").append(LINE_SEPARATOR)
		   .append(", OPERATION VARCHAR(40) NOT NULL ").append(LINE_SEPARATOR);

		// 3-2. 동적 컬럼
		generateDynamicColumnSql(sql, null, rootAttributes, storageMetadataVO, true, null);
		sql.append(");").append(LINE_SEPARATOR).append(LINE_SEPARATOR);

		// 3-3. 고정 INDEX
		sql.append(generateDynamicIndexSql(entityFullTableName, DefaultDbColumnName.ID.getCode(), null));
		sql.append(generateDynamicIndexSql(entityFullTableName, DefaultDbColumnName.MODIFIED_AT.getCode(), null));

		// 3-4. 동적 INDEX
		sql.append(generateDynamicIndexSql(entityFullTableName, dataModelVO.getIndexAttributeNames(), storageMetadataVO));

		return sql.toString();
	}

	/** dataModel 기반 DROP TABLE DDL 생성
	 *  - 엔티티최종테이블, Partial이력테이블, Full이력테이블
	 * @param dataModelVO dataModel 정보
	 * @return 생성된 DDL
	 */
	public String generateDropTableDdl(String id) {
		StringBuilder sql = new StringBuilder();
		
		String entityTableName = dataModelManager.generateRdbTableName(id);
		sql.append("DROP TABLE IF EXISTS ").append(Constants.SCHEMA_NAME).append(".\"").append(entityTableName).append("\";")
		   .append(LINE_SEPARATOR).append(LINE_SEPARATOR);

		String entityPartialHistTableName = dataModelManager.generateRdbTableName(id, Constants.PARTIAL_HIST_TABLE_PREFIX);
		sql.append("DROP TABLE IF EXISTS ").append(Constants.SCHEMA_NAME).append(".\"").append(entityPartialHistTableName).append("\";")
		   .append(LINE_SEPARATOR).append(LINE_SEPARATOR);

		String entityFullHistTableName = dataModelManager.generateRdbTableName(id, Constants.FULL_HIST_TABLE_PREFIX);
		sql.append("DROP TABLE IF EXISTS ").append(Constants.SCHEMA_NAME).append(".\"").append(entityFullHistTableName).append("\";")
		   .append(LINE_SEPARATOR).append(LINE_SEPARATOR);

		return sql.toString();
	}

	/**
	 * Attribute에 해당하는 컬럼 Add DDL 또는 Drop DDL 생성
	 * @param id 데이터모델 아이디
	 * @param attribute 대상 Attribute
	 * @param dbOperation 동작유형 (CREATE or DELETE)
	 * @return 생성된 DDL
	 */
	public String generateAlterTableColumnDdl(String id,
			DataModelStorageMetadataVO beforeStorageMetadataVO, 
			DataModelStorageMetadataVO afterStorageMetadataVO) {

		StringBuilder sql = new StringBuilder();

		// 1. 최종값 테이블 컬럼 추가 DDL 생성
		String entityTableName = dataModelManager.generateRdbTableName(id);
		String alterSql = generateAlterTableColumnTableDdl(entityTableName, beforeStorageMetadataVO, afterStorageMetadataVO);
		if(alterSql == null) {
			return null;
		}
		sql.append(alterSql);

		// 2. partial 이력 테이블 컬럼 추가 DDL 생성
		String partialHistTableName = dataModelManager.generateRdbTableName(id, Constants.PARTIAL_HIST_TABLE_PREFIX);
		sql.append(generateAlterTableColumnTableDdl(partialHistTableName, beforeStorageMetadataVO, afterStorageMetadataVO));

		// 3. full 이력 테이블 컬럼 추가 DDL 생성
		String fullHistTableName = dataModelManager.generateRdbTableName(id, Constants.FULL_HIST_TABLE_PREFIX);
		sql.append(generateAlterTableColumnTableDdl(fullHistTableName, beforeStorageMetadataVO, afterStorageMetadataVO));
		
		return sql.toString();
	}

	/**
	 * Attribute에 해당하는 컬럼 Add DDL 또는 Drop DDL 생성
	 * @param namespace 데이터모델 네임스페이스
	 * @param type 데이터모델 유형
	 * @param version 데이터모델 버전
	 * @param attribute 대상 Attribute
	 * @param dbOperation 동작유형 (CREATE or DELETE)
	 * @return 생성된 DDL
	 */
	private String generateAlterTableColumnTableDdl(String tableName,
			DataModelStorageMetadataVO beforeStorageMetadataVO, 
			DataModelStorageMetadataVO afterStorageMetadataVO) {

		// 1. 업데이트 이전 attribute 정보 기반 컬럼정보 추출
		Map<String, DataModelDbColumnVO> beforeDataModelDbColumnMap = beforeStorageMetadataVO.getDbColumnInfoVOMap();

		// 2. 업데이트 대상 attribute 정보 기반 컬럼정보 추출
		Map<String, DataModelDbColumnVO> afterDataModelDbColumnMap = afterStorageMetadataVO.getDbColumnInfoVOMap();

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
		sql.append("ALTER TABLE IF EXISTS ").append(Constants.SCHEMA_NAME).append(".\"").append(tableName).append("\"").append(LINE_SEPARATOR);
		for(Map.Entry<DataModelDbColumnVO, DbOperation> entry : dbOperationMap.entrySet()) {

			DataModelDbColumnVO dataModelDbColumnVO = entry.getKey();

			// 컬럼 alter 쿼리 세팅
			if(entry.getValue() == DbOperation.ADD_COLUMN) {
				sql.append(" ADD COLUMN ").append(dataModelDbColumnVO.getColumnName())
				   .append(" ").append(dataModelDbColumnVO.getColumnType().getRdbCode()).append(",");
			} else if(entry.getValue() == DbOperation.ALTER_COLUMN_TYPE
					|| entry.getValue() == DbOperation.ALTER_COLUMN_TYPE_AND_ADD_NOT_NULL
					|| entry.getValue() == DbOperation.ALTER_COLUMN_TYPE_AND_DROP_NOT_NULL) {
				sql.append(" ALTER COLUMN ").append(dataModelDbColumnVO.getColumnName());
				sql.append(" TYPE ").append(dataModelDbColumnVO.getColumnType().getRdbCode());
				if(!ValidateUtil.isEmptyData(dataModelDbColumnVO.getMaxLength())) {
					sql.append("(").append(dataModelDbColumnVO.getMaxLength()).append(")");
				}
				sql.append(" USING ").append(dataModelDbColumnVO.getColumnName()).append("::")
				   .append(dataModelDbColumnVO.getColumnType().getRdbCode()).append(",");
			} else if(entry.getValue() == DbOperation.DROP_COLUMN) {
				sql.append(" DROP COLUMN IF EXISTS ").append(dataModelDbColumnVO.getColumnName()).append(",");
			}

			// 컬럼 not null 쿼리 세팅
			if(entry.getValue() == DbOperation.ADD_NOT_NULL
					|| entry.getValue() == DbOperation.ALTER_COLUMN_TYPE_AND_ADD_NOT_NULL) {

				sql.append(" ALTER COLUMN ").append(dataModelDbColumnVO.getColumnName()).append(" SET NOT NULL,");

			} else if(entry.getValue() == DbOperation.DROP_NOT_NULL
					|| entry.getValue() == DbOperation.ALTER_COLUMN_TYPE_AND_DROP_NOT_NULL) {

				sql.append(" ALTER COLUMN ").append(dataModelDbColumnVO.getColumnName()).append(" DROP NOT NULL,");
			}
			sql.append(LINE_SEPARATOR);
		}
		sql.deleteCharAt(sql.length()-2); // 가장 뒤 콤마 제거
		sql.append(";").append(LINE_SEPARATOR).append(LINE_SEPARATOR);
		return sql.toString();
	}

	/**
	 * Length 를 가지는 컬럼 유형 여부 체크
	 * @param dbColumnType DB컬럼유형
	 * @return
	 */
	private boolean hasMaxLengthColumnType(DbColumnType dbColumnType) {
		if(dbColumnType == DbColumnType.VARCHAR
				|| dbColumnType == DbColumnType.FLOAT
				|| dbColumnType == DbColumnType.ARRAY_VARCHAR
				|| dbColumnType == DbColumnType.ARRAY_FLOAT) {
			return true;
		}
		return false;
	}

	/**
	 * 수정 요청 전/후 컬럼 길이를 비교하여 변동 여부 확인
	 * @param beforeMaxLength 수정 전 컬럼 최대 길이
	 * @param afterMaxLength 수정 후 컬럼 최대 길이
	 * @return
	 */
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
	public String generateAddOrDropColumnDdl(String id, Attribute attribute, DataModelStorageMetadataVO storageMetadataVO, DbOperation dbOperation) {

		StringBuilder sql = new StringBuilder();

		// 1. 최종값 테이블 컬럼 추가 DDL 생성
		String entityTableName = dataModelManager.generateRdbTableName(id);
		sql.append(generateAddOrDropColumnTableDdl(entityTableName, attribute, storageMetadataVO, dbOperation));

		// 2. partial 이력 테이블 컬럼 추가 DDL 생성
		String partialHistTableName = dataModelManager.generateRdbTableName(id, Constants.PARTIAL_HIST_TABLE_PREFIX);
		sql.append(generateAddOrDropColumnTableDdl(partialHistTableName, attribute, storageMetadataVO, dbOperation));

		// 3. full 이력 테이블 컬럼 추가 DDL 생성
		String fullHistTableName = dataModelManager.generateRdbTableName(id, Constants.FULL_HIST_TABLE_PREFIX);
		sql.append(generateAddOrDropColumnTableDdl(fullHistTableName, attribute, storageMetadataVO, dbOperation));

		return sql.toString();
	}

	/**
	 * Attribute에 해당하는 컬럼 Add DDL 또는 Drop DDL 생성
	 * @param tableName 테이블명
	 * @param attribute 대상 Attribute
	 * @param dbOperation DB동작유형 (ADD, DROP)
	 * @return 생성된 DDL
	 */
	private String generateAddOrDropColumnTableDdl(String tableName, Attribute attribute, DataModelStorageMetadataVO storageMetadataVO, DbOperation dbOperation) {
		StringBuilder sql = new StringBuilder();
		sql.append("ALTER TABLE IF EXISTS ").append(Constants.SCHEMA_NAME).append(".\"").append(tableName).append("\"").append(LINE_SEPARATOR);
		
		StringBuilder addColumnSql = new StringBuilder();
		generateDynamicColumnSql(addColumnSql, null, attribute, storageMetadataVO, false, dbOperation);
		addColumnSql.deleteCharAt(0); // sql 가장 앞 콤마 제거 
		sql.append(addColumnSql);
		sql.append(";").append(LINE_SEPARATOR).append(LINE_SEPARATOR);
		
		return sql.toString();
	}

	/**
	 * 데이터모델 정보 기반 동적 컬럼 생성
	 * @param sql SQL 문자열
	 * @param parentHierarchyId 상위 계층구조 Attribute id
	 * @param rootAttributes dataModel RootAttribute리스트
	 * @param ignoreRequired 필수값 무시 여부 (true인 경우 not null 제약조건을 걸지 않음)
	 * @param dbOperation 해당 값에 따라 DDL 생성 시 특정 구문을 추가
	 *  - 'ADD'인 경우 'ADD COLUMN' 구문 추가
	 *  - 'DROP' 경우 'DROP COLUMN' 구문 추가
	 */
	private void generateDynamicColumnSql(StringBuilder sql, List<String> hierarchyAttrNames, List<Attribute> rootAttributes,
			DataModelStorageMetadataVO storageMetadataVO,  Boolean ignoreRequired, DbOperation dbOperation) {

		for(Attribute rootAttribute : rootAttributes) {
			List<String> attrNames = null;
			if(hierarchyAttrNames != null) {
				attrNames = new ArrayList<>(hierarchyAttrNames);
			}
			generateDynamicColumnSql(sql, attrNames, rootAttribute, storageMetadataVO, ignoreRequired, dbOperation);
		}
	}

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
	private void generateDynamicColumnSql(StringBuilder sql, List<String> hierarchyAttrNames, Attribute rootAttribute, 
			DataModelStorageMetadataVO storageMetadataVO, Boolean ignoreRequired, DbOperation dbOperation)  {

		if(hierarchyAttrNames == null) {
			hierarchyAttrNames = new ArrayList<>();
		}
		hierarchyAttrNames.add(rootAttribute.getName());

		// 1. rootAttribute의 type이 'Property'인 경우
		if(rootAttribute.getAttributeType() == AttributeType.PROPERTY) {

			// 1-1. valueType이 Object인 경우
			if(rootAttribute.getValueType() == AttributeValueType.OBJECT) {
				objectToSql(sql, hierarchyAttrNames, rootAttribute.getObjectMembers(), storageMetadataVO, ignoreRequired, dbOperation);

			// 1-2. valueType이 ArrayObject인 경우
			} else if(rootAttribute.getValueType() == AttributeValueType.ARRAY_OBJECT) {
				List<ObjectMember> objectMembers = rootAttribute.getObjectMembers();
				if(objectMembers != null) {
					for(ObjectMember objectMember : objectMembers) {
						String columnName = getColumnNameByStorageMetadata(hierarchyAttrNames, objectMember.getName(), storageMetadataVO);
						Boolean isRequired = objectMember.getIsRequired();
						if(ignoreRequired) isRequired = false;
						addColumnDdl(sql, columnName, objectMember.getValueType(), objectMember.getMaxLength(), isRequired, true, dbOperation);
					}
				}

			// 1-3. valueType이 String, Integer, Double, Date, ArrayString, ArrayInteger, ArrayDouble인 경우
			} else {
				String columnName = getColumnNameByStorageMetadata(hierarchyAttrNames, null, storageMetadataVO);
				Boolean isRequired = rootAttribute.getIsRequired();
				if(ignoreRequired) isRequired = false;
				addColumnDdl(sql, columnName, rootAttribute.getValueType(), rootAttribute.getMaxLength(), isRequired, false, dbOperation);
			}

		// 2. rootAttribute의 type이 'GeoProperty'인 경우
		// attributeId에 _3857, _4326 을 붙인 2개의 geometry 컬럼을 생성
		} else if(rootAttribute.getAttributeType() == AttributeType.GEO_PROPERTY) {
			List<String> columnNames = getColumnNamesByStorageMetadata(hierarchyAttrNames, null, storageMetadataVO);
			Boolean isRequired = rootAttribute.getIsRequired();
			if(ignoreRequired) isRequired = false;
			for(String columnName : columnNames) {
//				addGeoColumnDdl(sql, columnName, isRequired, dbOperation);
				addColumnDdl(sql, columnName, AttributeValueType.GEO_JSON, null, isRequired, false, dbOperation);
			}

		// 3. rootAttribute의 type이 'Relationship'인 경우
		} else if(rootAttribute.getAttributeType() == AttributeType.RELATIONSHIP) {
			String columnName = getColumnNameByStorageMetadata(hierarchyAttrNames, null, storageMetadataVO);
			Boolean isRequired = rootAttribute.getIsRequired();
			if(ignoreRequired) isRequired = false;
			addColumnDdl(sql, columnName, rootAttribute.getValueType(), rootAttribute.getMaxLength(), isRequired, false, dbOperation);
		}

		// 4. observed=true 인 경우 observed_at 컬럼 sql 생성
		if(rootAttribute.getHasObservedAt() != null && rootAttribute.getHasObservedAt()) {
			String columnName = getColumnNameByStorageMetadata(hierarchyAttrNames, PropertyKey.OBSERVED_AT.getCode(), storageMetadataVO);
			addColumnDdl(sql, columnName, AttributeValueType.DATE, null, null, false, dbOperation);
		}

		// 5. hasUnitCode=true 인 경우 unit_code 컬럼 sql 생성
		if(rootAttribute.getHasUnitCode() != null && rootAttribute.getHasUnitCode()) {
			String columnName = getColumnNameByStorageMetadata(hierarchyAttrNames, PropertyKey.UNIT_CODE.getCode(), storageMetadataVO);
			addColumnDdl(sql, columnName, AttributeValueType.STRING, null, null, false, dbOperation);
		}

		// 6. property의 createdAt 컬럼 sql 생성
		String createdAtColumnName = getColumnNameByStorageMetadata(hierarchyAttrNames, PropertyKey.CREATED_AT.getCode(), storageMetadataVO);
		addColumnDdl(sql, createdAtColumnName, AttributeValueType.DATE, null, null, false, dbOperation);
		
		// 7. property의 modifiedAt 컬럼 sql 생성
		String modifiedAtColumnName = getColumnNameByStorageMetadata(hierarchyAttrNames, PropertyKey.MODIFIED_AT.getCode(), storageMetadataVO);
		addColumnDdl(sql, modifiedAtColumnName, AttributeValueType.DATE, null, null, false, dbOperation);

		// 8. has Property 혹은 has Relationship 이 존재하는 경우
		List<Attribute> hasAttributes = rootAttribute.getChildAttributes();
		if(hasAttributes != null && hasAttributes.size() > 0) {
			generateDynamicColumnSql(sql, hierarchyAttrNames, hasAttributes, storageMetadataVO, ignoreRequired, dbOperation);
		}
	}

	private String getColumnNameByStorageMetadata(List<String> hierarchyAttrNames, String attrName, DataModelStorageMetadataVO storageMetadataVO) {
		
		if(attrName == null) {
			return dataModelManager.getColumnNameByStorageMetadata(storageMetadataVO, hierarchyAttrNames);
		} else {
			List<String> attrNames = new ArrayList<>(hierarchyAttrNames);
			attrNames.add(attrName);
			return dataModelManager.getColumnNameByStorageMetadata(storageMetadataVO, attrNames);
		}
	}
	
	private List<String> getColumnNamesByStorageMetadata(List<String> hierarchyAttrNames, String attrName, DataModelStorageMetadataVO storageMetadataVO) {
		
		if(attrName == null) {
			return dataModelManager.getColumnNamesByStorageMetadata(storageMetadataVO, hierarchyAttrNames);
		} else {
			List<String> attrNames = new ArrayList<>(hierarchyAttrNames);
			attrNames.add(attrName);
			return dataModelManager.getColumnNamesByStorageMetadata(storageMetadataVO, attrNames);
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
	private void objectToSql(StringBuilder sql, 
							 List<String> hierarchyAttrNames,
							 List<ObjectMember> objectMembers,
							 DataModelStorageMetadataVO storageMetadataVO,
							 boolean ignoreRequired,
							 DbOperation dbOperation) {

		if(objectMembers != null) {
			for(ObjectMember objectMember : objectMembers) {

				// valueType이 OBJECT 여서 하위 필드가 또 존재하는 경우
				if(objectMember.getValueType() == AttributeValueType.OBJECT && objectMember.getObjectMembers() != null) {

					List<String> currentHierarchyAttrNames = new ArrayList<>(hierarchyAttrNames);
					currentHierarchyAttrNames.add(objectMember.getName());
					objectToSql(sql, currentHierarchyAttrNames, objectMember.getObjectMembers(), storageMetadataVO, ignoreRequired, dbOperation);

				} else {

					// SQL문 생성
					Boolean isRequired = objectMember.getIsRequired();
					if(ignoreRequired) isRequired = false;

					String columnName = getColumnNameByStorageMetadata(hierarchyAttrNames, objectMember.getName(), storageMetadataVO);
					addColumnDdl(sql, columnName, objectMember.getValueType(), objectMember.getMaxLength(), isRequired, false, dbOperation);
				}
			}
		}
	}

	/**
	 * 동적 Index 생성 sql을 만들어 반환
	 * @param dbTableName 테이블명
	 * @param indexAttributeIdList 인덱스 대상 attibuteId 리스트
	 * @param rootAttributes dataModel의 RootAttrubute
	 * @return 생성된 sql문
	 */
	private String generateDynamicIndexSql(String dbTableName, List<String> indexAttributeIdList, DataModelStorageMetadataVO storageMetadataVO) {
		StringBuilder sql = new StringBuilder();
		if(indexAttributeIdList != null) {
			for(String indexAttributeId : indexAttributeIdList) {
				sql.append(generateDynamicIndexSql(dbTableName, indexAttributeId, storageMetadataVO));
			}
		}
		return sql.toString();
	}


	/**
	 * geo가 아닌 일반 타입의 컬럼에 대한 동적 인덱스 sql문 생성하여 반환 
	 * @param dbTableName 테이블명
	 * @param indexAttributeId 인덱스 대상 attributeId
	 * @return 생성된 sql문
	 */
	private String generateDynamicIndexSql(String dbTableName, String indexAttributeId, DataModelStorageMetadataVO storageMetadataVO) {
		indexAttributeId = indexAttributeId.replace(Constants.INDEX_ATTRIBUTE_NAME_DELIMITER, Constants.COLUMN_DELIMITER);
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE INDEX IF NOT EXISTS \"").append(dbTableName)
		   .append(Constants.TABLE_NAME_DELIMITER_UNDER).append(indexAttributeId).append("\"")
		   .append(" ON ").append(Constants.SCHEMA_NAME).append(".\"").append(dbTableName).append("\"")
		   .append("(");
		String[] indexAttributeArr = indexAttributeId.split(",");
		for(String attrId : indexAttributeArr) {
			if(storageMetadataVO != null && storageMetadataVO.getDbColumnInfoVOMap() != null) {
				for(DataModelDbColumnVO dataModelDbColumnVO : storageMetadataVO.getDbColumnInfoVOMap().values()) {
					if(dataModelDbColumnVO.getDaoAttributeId().equals(attrId)) {
						if(dataModelDbColumnVO.getColumnType() == DbColumnType.GEOMETRY_4326) {
							sql.append(attrId).append(Constants.GEO_PREFIX_4326).append(",");
						} else if(dataModelDbColumnVO.getColumnType() == DbColumnType.GEOMETRY_3857) {

						} else {
							sql.append(attrId).append(",");
						}
					}
				}
			} else {
				sql.append(attrId).append(",");
			}
		}
		sql.deleteCharAt(sql.length()-1);
		sql.append(");").append(LINE_SEPARATOR).append(LINE_SEPARATOR);
		return sql.toString();
	}

	/**
	 * 기존 인덱스정보와 신규 인덱스 정보를 비교하여 인덱스를 ADD or DROP 인덱스 DDL 생성
	 * @param namespace 데이터모델 네임스페이스
	 * @param type 데이터모델 유형
	 * @param version 데이터모델 버전
	 * @param afterindexAttributeNames 신규 indexAttributeNames
	 * @param beforeIndexAttributeNames 기존 indexAttributeNames
	 * @return index ADD or DROP DDL
	 */
	public String generateIndexDdl(DataModelVO dataModelVO, DataModelStorageMetadataVO storageMetadataVO,
			List<String> beforeIndexAttributeNames, List<String> afterindexAttributeNames) {

		Map<String, DbOperation> indexAttributeMap = new HashMap<>();
		if(!ValidateUtil.isEmptyData(afterindexAttributeNames)) {

			// 2. Index Attribute 유효성 검사
			if(!isValidIndexName(dataModelVO, afterindexAttributeNames, storageMetadataVO)) {
				throw new BadRequestException(ErrorCode.INVALID_DATAMODEL, "Invalid IndexAttributeName. indexAttributeNames=" + afterindexAttributeNames);
			}
		
			// 3. Index 수정 여부 체크
			// 신규로 입력받은 indexAttribteName은 일단 모두 CREATE 로 세팅
			for(String attributeName : afterindexAttributeNames) {
				indexAttributeMap.put(attributeName, DbOperation.ADD_COLUMN);
			}
		}

		if(beforeIndexAttributeNames != null) {
			for(String beforeAttributeName : beforeIndexAttributeNames) {
				// 이전에 존재했던 인덱스들은 제외
				if(indexAttributeMap.containsKey(beforeAttributeName)) {
					indexAttributeMap.remove(beforeAttributeName);
				} else {
					// 이전에 존재했지만 신규 요청에서 사라진 인덱스 들은 삭제로 세팅
					indexAttributeMap.put(beforeAttributeName, DbOperation.DROP_COLUMN);
				}
			}
		}

		String id = dataModelVO.getId();

		String entityTableName = dataModelManager.generateRdbTableName(id);
		String partialHistTableName = dataModelManager.generateRdbTableName(id, Constants.PARTIAL_HIST_TABLE_PREFIX);
		String fullHistTableName = dataModelManager.generateRdbTableName(id, Constants.FULL_HIST_TABLE_PREFIX);

        // 4. Alter Index DDL 생성
		StringBuilder sql = new StringBuilder();
		for(Map.Entry<String, DbOperation> entry : indexAttributeMap.entrySet()) {
			switch(entry.getValue()) {
			case ADD_COLUMN:
				sql.append(generateDynamicIndexSql(entityTableName, entry.getKey(), storageMetadataVO));
				sql.append(generateDynamicIndexSql(partialHistTableName, entry.getKey(), storageMetadataVO));
				sql.append(generateDynamicIndexSql(fullHistTableName, entry.getKey(), storageMetadataVO));
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


	/**
	 * IndexAttributeName 유효성 체크
	 * @param dataModelVO 데이터 모델정보
	 * @param indexAttributeNames indexAttribute명
	 * @return
	 */
	public boolean isValidIndexName(DataModelVO dataModelVO, List<String> indexAttributeNames, DataModelStorageMetadataVO storageMetadataVO) {
		if(indexAttributeNames == null || indexAttributeNames.size() == 0) {
			return true;
		}

		// 1. Index Attribute 유효성 검사
		for(String indexAttributeName : indexAttributeNames) {
			String[] compositIndexNames = indexAttributeName.split(",");
			boolean isValid = false;
			for(String indexName : compositIndexNames) {
				String indexColumnName = indexName.replace(Constants.INDEX_ATTRIBUTE_NAME_DELIMITER, Constants.COLUMN_DELIMITER);
				for(DataModelDbColumnVO dbColumnVO : storageMetadataVO.getDbColumnInfoVOMap().values()) {
					String dbColumnName = dbColumnVO.getColumnName();
					if(dbColumnVO.getColumnType() == DbColumnType.GEOMETRY_4326) {
						dbColumnName = dbColumnName.replace(Constants.GEO_PREFIX_4326, "");
					}
					if(dbColumnName.equals(indexColumnName)) {
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
		sql.append("DROP INDEX IF EXISTS ").append(Constants.SCHEMA_NAME).append(".\"").append(tableName)
		   .append(Constants.TABLE_NAME_DELIMITER_UNDER).append(indexAttributeName).append("\";")
		   .append(LINE_SEPARATOR).append(LINE_SEPARATOR);

		return sql.toString();
	}

	/**
	 * 테이블 생성 시 컬럼부분 sql문 생성하여 반환 
	 * @param sql 테이블생성 sql문 StringBuilder
	 * @param columnName columnName
	 * @param valueType attribute value type
	 * @param maxLength 최대길이 (최대길이가 없는 경우 sql에서 길이를 지정하지 않음)
	 * @param required 필수여부 (not null 여부)
	 * @param isArrayObject array형태의 object 여부
	 * @param isAddColumn Alter table add column 여부
	 * @return 컬럼추가 sql문이 추가된 StringBuilder
	 */
	private StringBuilder addColumnDdl(StringBuilder sql, String columnName, AttributeValueType valueType, 
			String maxLength, Boolean required, Boolean isArrayObject, DbOperation dbOperation) {

		DbColumnType columnType = null;
		if(isArrayObject) {
			columnType = dataModelManager.arrayObjectValueTypeToDbColumnType(valueType);
		} else {
			columnType = dataModelManager.valueTypeToDbColumnType(valueType);
		}

		sql.append(", ");
		if(dbOperation == DbOperation.ADD_COLUMN) {
			sql.append("ADD COLUMN ").append(columnName).append(" ").append(columnType.getRdbCode());

		} else if(dbOperation == DbOperation.DROP_COLUMN) {
			sql.append("DROP COLUMN IF EXISTS ").append(columnName);
			
		} else {
			sql.append(columnName)
			   .append(" ").append(columnType.getRdbCode());
			if(!ValidateUtil.isEmptyData(maxLength) && hasMaxLengthColumnType(columnType)) {
				sql.append("(").append(maxLength).append(")");
			}
			if(required != null && required) {
				sql.append(" NOT NULL ");
			}
		}
		sql.append(LINE_SEPARATOR);
		return sql;
	}

	/**
	 * Geometry 타입의 컬럼 sql 문 생성
	 * @param sql 테이블생성 sql문 StringBuilder
	 * @param columnName columnName
	 * @param required 필수여부 (not null 여부)
	 * @return 컬럼추가 sql문이 추가된 StringBuilder
	 */
	private StringBuilder addGeoColumnDdl(StringBuilder sql, String columnName, Boolean required, DbOperation dbOperation) {

		sql.append(", ");
		if(dbOperation == DbOperation.ADD_COLUMN) {
			sql.append("ADD COLUMN ").append(columnName).append(Constants.GEO_PREFIX_3857).append(" ")
			   .append(DbColumnType.GEOMETRY_3857.getRdbCode());

		} else if(dbOperation == DbOperation.DROP_COLUMN) {
			sql.append("DROP COLUMN IF EXISTS ").append(columnName).append(Constants.GEO_PREFIX_3857);
			
		} else {
			sql.append(columnName).append(Constants.GEO_PREFIX_3857)
			   .append(" ").append(DbColumnType.GEOMETRY_3857.getRdbCode());
			if(required != null && required) {
				sql.append(" NOT NULL ");
			}
		}
		sql.append(LINE_SEPARATOR);
		
		sql.append(", ");
		if(dbOperation == DbOperation.ADD_COLUMN) {
			sql.append("ADD COLUMN ").append(columnName).append(Constants.GEO_PREFIX_4326).append(" ")
			   .append(DbColumnType.GEOMETRY_4326.getRdbCode());

		} else if(dbOperation == DbOperation.DROP_COLUMN) {
			sql.append("DROP COLUMN IF EXISTS ").append(columnName).append(Constants.GEO_PREFIX_4326);
			
		} else {
			sql.append(columnName).append(Constants.GEO_PREFIX_4326)
			   .append(" ").append(DbColumnType.GEOMETRY_4326.getRdbCode());
			if(required != null && required) {
				sql.append(" NOT NULL ");
			}
		}
		sql.append(LINE_SEPARATOR);
		return sql;
	}


}
