package kr.re.keti.sc.dataservicebroker.datamodel.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.AttributeType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.AttributeValueType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.BigDataStorageType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.DefaultAttributeKey;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.PropertyKey;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.StorageType;
import kr.re.keti.sc.dataservicebroker.common.exception.BadRequestException;
import kr.re.keti.sc.dataservicebroker.common.exception.BaseException;
import kr.re.keti.sc.dataservicebroker.datafederation.service.DataFederationService;
import kr.re.keti.sc.dataservicebroker.datamodel.DataModelManager;
import kr.re.keti.sc.dataservicebroker.datamodel.dao.DataModelDAO;
import kr.re.keti.sc.dataservicebroker.datamodel.service.hbase.HBaseTableSVC;
import kr.re.keti.sc.dataservicebroker.datamodel.sqlprovider.BigdataTableSqlProvider;
import kr.re.keti.sc.dataservicebroker.datamodel.sqlprovider.RdbTableSqlProvider;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.Attribute;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelBaseVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelCacheVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelStorageMetadataVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.ObjectMember;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.UpdateDataModelProcessVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.UpdateDataModelProcessVO.AttributeUpdateProcessType;
import kr.re.keti.sc.dataservicebroker.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DataModelSVC {
	
    private DataModelManager dataModelManager;
	private DataModelRetrieveSVC dataModelRetrieveSVC;
    private RdbTableSqlProvider rdbDataModelSqlProvider;
	private BigdataTableSqlProvider bigDataTableSqlProvider;
	private BigdataTableSqlProvider bigdataDataModelSqlProvider;
    private DataModelDAO dataModelDAO;
    private HBaseTableSVC hBaseTableSVC;
    private ObjectMapper objectMapper;
    private DataFederationService dataFederationService;
	
    public static final String URI_PATTERN_CREATE_DATA_MODEL = "/datamodels";
    public static final Pattern URI_PATTERN_DATA_MODEL = Pattern.compile("/datamodels/(?<id>.+)");

	public DataModelSVC(
			DataModelManager dataModelManager,
			DataModelRetrieveSVC dataModelRetrieveSVC,
			RdbTableSqlProvider rdbDataModelSqlProvider,
			BigdataTableSqlProvider bigDataTableSqlProvider,
			BigdataTableSqlProvider bigdataDataModelSqlProvider,
			DataModelDAO dataModelDAO,
			HBaseTableSVC hBaseTableSVC,
			ObjectMapper objectMapper,
			DataFederationService dataFederationService
	) {
		this.dataModelManager = dataModelManager;
		this.dataModelRetrieveSVC = dataModelRetrieveSVC;
		this.rdbDataModelSqlProvider = rdbDataModelSqlProvider;
		this.bigDataTableSqlProvider = bigDataTableSqlProvider;
		this.bigdataDataModelSqlProvider = bigdataDataModelSqlProvider;
		this.dataModelDAO = dataModelDAO;
		this.hBaseTableSVC = hBaseTableSVC;
		this.objectMapper = objectMapper;
		this.dataFederationService = dataFederationService;
	}

	public enum DbOperation {
    	ADD_COLUMN,
    	DROP_COLUMN,
    	ADD_NOT_NULL,
    	DROP_NOT_NULL,
    	ALTER_COLUMN_TYPE,
    	ALTER_COLUMN_TYPE_AND_ADD_NOT_NULL,
    	ALTER_COLUMN_TYPE_AND_DROP_NOT_NULL;
    }


    /**
     * 데이터 모델 생성 Provisioning 처리
     * @param to 데이터 모델 생성 요청 url
     * @param requestBody 요청 Body
     * @param requestId Provisioning Request Id
     * @param eventTime Provisioning Request Time
     */
    public void processCreate(String to, String requestBody, String requestId, Date eventTime) {

    	if(URI_PATTERN_CREATE_DATA_MODEL.equals(to)) {
    		createDataModel(requestBody, requestId, eventTime);
    		
    	// 404
    	} else {
    		throw new BadRequestException(ErrorCode.NOT_EXIST_ID);
    	}
    }

    /**
     * 데이터 모델 생성
     * @param requestBody 요청 Body
     * @param requestId Provisioning Request Id
     * @param eventTime Provisioning Request Time
     * @throws BaseException
     */
    private void createDataModel(String requestBody, String requestId, Date eventTime) throws BaseException {
    	// 1. 수신 데이터 파싱
    	DataModelVO dataModelVO = null;
		try {
			dataModelVO = objectMapper.readValue(requestBody, DataModelVO.class);
		} catch (IOException e) {
			throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Invalid Parameter. body=" + requestBody);
		}

		// 2. 유효성 체크
		// 2-1) get @context 정보 필수 체크
		List<String> contextUriList = dataModelVO.getContext();
		if(ValidateUtil.isEmptyData(contextUriList)) {
			throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Not exists @Context.");
		}
		// 2-2) attribute명 예약어 여부 체크
		checkAttributeName(dataModelVO.getAttributes());
		// 2-3) attributeTyp 및 valueType 체크
		checkAttributeTypeAndValueType(dataModelVO.getAttributes());
		// 2-4) context 정보 조회
		Map<String, String> contextMap = dataModelManager.contextToFlatMap(contextUriList);
		// 2-5) type 정보가 @context 내에 존재하는 지 여부 체크
		boolean validType = false;
 		// type 정보가 full uri 로 온 경우
 		if(dataModelVO.getType().startsWith("http")) {
 	 		
 	 		for(Map.Entry<String, String> entry : contextMap.entrySet()) {
 	 			String shortType = entry.getKey();
 	 			String fullUriType = entry.getValue();
 	 			
 	 			if(fullUriType.equals(dataModelVO.getType())) {
 	 				dataModelVO.setTypeUri(fullUriType);
 	 				dataModelVO.setType(shortType);
 	 				validType = true;
 	 			}
 	 		}
 		// type 정보가 short name 인 경우
 		} else {
 			if(contextMap.get(dataModelVO.getType()) != null) {
 				dataModelVO.setTypeUri(contextMap.get(dataModelVO.getType()));
 				validType = true;
 			}
 		}
 		if(!validType) {
 			throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Not exists type '" + dataModelVO.getType() + "' in @context=" + dataModelVO.getContext());
 		}
		// 2-6) attribute명이 context 내에 존재하는 지 체크
		checkAttributeNameByContext(dataModelVO.getAttributes(), contextMap);

		// 3. set attribute context uri
		setAttributeContextUri(dataModelVO.getAttributes(), contextMap);

		DataModelBaseVO retrieveDataModelBaseVO = dataModelRetrieveSVC.getDataModelBaseVOById(dataModelVO.getId());

        if (retrieveDataModelBaseVO != null) {
        	
        	// 4. 이중화되어 있는 다른 DataServiceBroker 인스턴스에서 DB 입력 했는지 체크
			if(alreadyProcessByOtherInstance(requestId, eventTime, retrieveDataModelBaseVO)) {
	        	// 다른 Instance에서 DB 업데이트 이미 처리했으므로 캐쉬만 로딩하고 성공 처리
	            dataModelManager.putDataModelCache(retrieveDataModelBaseVO);
	            return;
	        } else {
        		// 이미 존재하므로 업데이트 처리
	        	updateDataModel(retrieveDataModelBaseVO.getId(), requestBody, requestId, eventTime);
	        	return;
        	}
        }

        // 5. dataModel 정보 저장
        DataModelBaseVO dataModelBaseVO = new DataModelBaseVO();
        dataModelBaseVO.setId(dataModelVO.getId());
        dataModelBaseVO.setType(dataModelVO.getType());
        dataModelBaseVO.setTypeUri(dataModelVO.getTypeUri());
        dataModelBaseVO.setName(dataModelVO.getName());
        dataModelBaseVO.setDescription(dataModelVO.getDescription());
        dataModelBaseVO.setEnabled(true);
        dataModelBaseVO.setProvisioningRequestId(requestId);
        dataModelBaseVO.setProvisioningEventTime(eventTime);
        try {
        	dataModelBaseVO.setDataModel(objectMapper.writeValueAsString(dataModelVO));
        	dataModelBaseVO.setStorageMetadata(objectMapper.writeValueAsString(dataModelManager.createDataModelStorageMetadata(dataModelVO, null, null)));
		} catch (IOException e) {
			throw new BadRequestException(ErrorCode.UNKNOWN_ERROR, "DataModel parsing error. body=" + requestBody);
		}

        dataModelDAO.createDataModelBaseVO(dataModelBaseVO);

        // 6. Cache 정보 로딩
        dataModelManager.putDataModelCache(dataModelBaseVO);
        
        // 7. Csource Upsert
        if(dataFederationService.enableFederation()) {
        	dataFederationService.registerCsource();
        }
    }

    private void checkAttributeNameByContext(List<Attribute> attributes, Map<String, String> contextMap) {
		for(Attribute attribute : attributes) {

			List<Attribute> childAttributes = attribute.getChildAttributes();
			if(childAttributes != null && childAttributes.size() > 0) {
				checkAttributeNameByContext(childAttributes, contextMap);
			} 

			if(contextMap.get(attribute.getName()) == null) {
				throw new BadRequestException(ErrorCode.INVALID_PARAMETER,
						"Invalid attribute name. Not exists '" + attribute.getName() + "' in @context.");
			}
		}
	}

    private void setAttributeContextUri(List<Attribute> attributes, Map<String, String> contextMap) {
		for(Attribute attribute : attributes) {
			String attributeContextUri = contextMap.get(attribute.getName());
			
			List<Attribute> childAttributes = attribute.getChildAttributes();
			if(childAttributes != null && childAttributes.size() > 0) {
				setAttributeContextUri(childAttributes, contextMap);
			}

			if(attributeContextUri != null) {
				attribute.setAttributeUri(attributeContextUri);
			}
		}
	}
    

    /**
     * 데이터모델 기반 DDL 실행
     * @param ddl 데이터 모델 기반 생성된 DDL
     * @param storageType Storage 유형
     */
    public void executeDdl(String ddl, StorageType storageType) {

    	if(storageType == null) {
    		storageType = StorageType.RDB;
    	}

    	log.info("DataModel executeDdl. storageType=" + storageType + ", ddl=" + ddl);

    	try {
    		dataModelDAO.executeDdl(ddl, storageType);
    	} catch(Exception e) {
    		log.error("DataModel execute DDL ERROR. SQL=" + ddl, e);
    		throw new BadRequestException(ErrorCode.CREATE_ENTITY_TABLE_ERROR, 
    				"execute DDL ERROR. ddl=" + ddl, e);
    	}
    }

    /**
     * 데이터 모델 정보 Update
     * @param dataModelBaseVO
     * @return
     */
    public int fullUpdateDataModel(DataModelBaseVO dataModelBaseVO) {
        return dataModelDAO.updateDataModelBase(dataModelBaseVO);
    }


    /**
     * 데이터 모델 수정
     * @param to 데이터 모델 생성 요청 url
     * @param requestBody 요청 Body
     * @param requestId Provisioning Request Id
     * @param eventTime Provisioning Request Time
     * @throws BaseException
     */
    public void processUpdate(String to, String requestBody, String requestId, Date eventTime) throws BaseException {
    	Matcher matcherForUpdate = URI_PATTERN_DATA_MODEL.matcher(to);

		if(matcherForUpdate.find()) {
			String id = matcherForUpdate.group("id");

			updateDataModel(id, requestBody, requestId, eventTime);
			
	    // 404
		} else {
			throw new BadRequestException(ErrorCode.NOT_EXIST_ID);
		}
    }

    /**
     * 데이터 모델 수정
     * @param dataModelId 데이터모델 아이디
     * @param requestBody 요청 Body
     * @param requestId Provisioning Request Id
     * @param eventTime Provisioning Request Time
     */
    private void updateDataModel(String dataModelId, String requestBody, String requestId, Date eventTime) {
    	// 1. 수신 데이터 파싱
    	DataModelVO requestDataModelVO = null;
		try {
			requestDataModelVO = objectMapper.readValue(requestBody, DataModelVO.class);
		} catch (IOException e) {
			throw new BadRequestException(ErrorCode.INVALID_PARAMETER,
                    "Invalid Parameter. body=" + requestBody);
		}

		// 2. 유효성 체크
		// 2-1) get @context 정보 필수 체크
		List<String> contextUriList = requestDataModelVO.getContext();
		if(ValidateUtil.isEmptyData(contextUriList)) {
			throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Not exists @Context.");
		}
		// 2-2) attribute명 예약어 여부 체크
		checkAttributeName(requestDataModelVO.getAttributes());
		// 2-3) attributeTyp 및 valueType 체크
		checkAttributeTypeAndValueType(requestDataModelVO.getAttributes());
		// 2-4) context 정보 조회
		Map<String, String> contextMap = dataModelManager.contextToFlatMap(contextUriList);
		// 2-5) type 정보가 @context 내에 존재하는 지 여부 체크
		boolean validType = false;
 		// type 정보가 full uri 로 온 경우
 		if(requestDataModelVO.getType().startsWith("http")) {
 	 		
 	 		for(Map.Entry<String, String> entry : contextMap.entrySet()) {
 	 			String shortType = entry.getKey();
 	 			String fullUriType = entry.getValue();
 	 			
 	 			if(fullUriType.equals(requestDataModelVO.getType())) {
 	 				requestDataModelVO.setTypeUri(fullUriType);
 	 				requestDataModelVO.setType(shortType);
 	 				validType = true;
 	 			}
 	 		}
 		// type 정보가 short name 인 경우
 		} else {
 			if(contextMap.get(requestDataModelVO.getType()) != null) {
 				requestDataModelVO.setTypeUri(contextMap.get(requestDataModelVO.getType()));
 				validType = true;
 			}
 		}
 		if(!validType) {
 			throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Not exists type '" + requestDataModelVO.getType() + "' in @context=" + requestDataModelVO.getContext());
 		}
		// 2-6) attribute명이 context 내에 존재하는 지 체크
		checkAttributeNameByContext(requestDataModelVO.getAttributes(), contextMap);

		// 3. set attribute context uri
		setAttributeContextUri(requestDataModelVO.getAttributes(), contextMap);

		// 4. 기 존재하지 않을 경우 CREATE (UPSERT 처리)
        DataModelBaseVO retrieveDataModelBaseVO = dataModelRetrieveSVC.getDataModelBaseVOById(dataModelId);
        if (retrieveDataModelBaseVO == null) {
        	log.info("Create(Upsert) DataModel. requestId={}, requestBody={}", requestId, requestBody);
        	createDataModel(requestBody, requestId, eventTime);
        	return;
        }

        // 5. 이중화되어 있는 다른 DataServiceBroker 인스턴스에서 DB 입력 했는지 체크
 		if(alreadyProcessByOtherInstance(requestId, eventTime, retrieveDataModelBaseVO)) {
         	// 다른 Instance에서 DB 업데이트 이미 처리했으므로 캐쉬만 로딩하고 성공 처리
             dataModelManager.putDataModelCache(retrieveDataModelBaseVO);
             return;
        }

 		// 6. 데이터모델 업데이트를 위한 업데이트 전/후 정보 생성
 		DataModelVO beforeDataModelVO = null;
 		DataModelVO afterDataModelVO = requestDataModelVO;
 		DataModelStorageMetadataVO beforeStorageMetadataVO = null;
 		DataModelStorageMetadataVO afterStorageMetadataVO = null;
        try {
        	beforeDataModelVO = objectMapper.readValue(retrieveDataModelBaseVO.getDataModel(), DataModelVO.class);
        	if(!ValidateUtil.isEmptyData(retrieveDataModelBaseVO.getStorageMetadata())) {
        		beforeStorageMetadataVO = objectMapper.readValue(retrieveDataModelBaseVO.getStorageMetadata(), DataModelStorageMetadataVO.class);
        	} else {
        		// 하위 호환을 위해 DB 에 storageMetadata 가 없는 경우 신규 생성
        		beforeStorageMetadataVO = dataModelManager.createDataModelStorageMetadata(beforeDataModelVO, null, retrieveDataModelBaseVO.getCreatedStorageTypes());
        	}

        	afterStorageMetadataVO = dataModelManager.createDataModelStorageMetadata(afterDataModelVO, beforeStorageMetadataVO, retrieveDataModelBaseVO.getCreatedStorageTypes());

        } catch (IOException e) {
			throw new BadRequestException(ErrorCode.INVALID_DATAMODEL,
	                "datamodel parsing error. datamodel=" + retrieveDataModelBaseVO.getDataModel());
		}
        
        // 7. 업데이트 요청받은 신규 dataModel 과 기존 저장되어있던 dataModel 정보 비교하여 DDL 생성 및 실행
        // BigData 및 RDB 모두 컬럼 추가/업데이트/삭제 DDL 생성 및 실행
        updateStorage(dataModelId, beforeDataModelVO, beforeStorageMetadataVO, afterDataModelVO, afterStorageMetadataVO, retrieveDataModelBaseVO.getCreatedStorageTypes());
        
        // 8. dataModel 정보 업데이트
        DataModelBaseVO dataModelBaseVO = new DataModelBaseVO();
        dataModelBaseVO.setId(dataModelId);
        dataModelBaseVO.setName(afterDataModelVO.getName());
        dataModelBaseVO.setEnabled(true);
        dataModelBaseVO.setProvisioningRequestId(requestId);
        dataModelBaseVO.setProvisioningEventTime(eventTime);
        dataModelBaseVO.setCreatedStorageTypes(retrieveDataModelBaseVO.getCreatedStorageTypes());
        try {
        	dataModelBaseVO.setDataModel(objectMapper.writeValueAsString(requestDataModelVO));
        	dataModelBaseVO.setStorageMetadata(objectMapper.writeValueAsString(afterStorageMetadataVO));
		} catch (IOException e) {
			throw new BadRequestException(ErrorCode.UNKNOWN_ERROR, "DataModel parsing error. body=" + requestBody);
		}
        dataModelDAO.updateDataModelBase(dataModelBaseVO);

        // 8. Cache 정보 리로딩
        dataModelManager.putDataModelCache(dataModelBaseVO);
        
        // 9. Csource Upsert
        if(dataFederationService.enableFederation()) {
        	dataFederationService.registerCsource();
        }
    }
    

    /**
     * 업데이터 전/후 데이터 모델 정보 기반 Table 조작 DDL 생성 및 실행
     * @param beforeDataModelVO 업데이트 전 데이터 모델
     * @param afterDataModelVO 업데이트 후 데이터 모델
     */
    private void updateStorage(String id, 
    							DataModelVO beforeDataModelVO, DataModelStorageMetadataVO beforeStorageMetadataVO, 
    							DataModelVO afterDataModelVO, DataModelStorageMetadataVO afterStorageMetadataVO, 
    							List<BigDataStorageType> createdStorageTypes) {

    	// DB DDL 생성 작업 처리를 위한 정보 담을 VO
        List<UpdateDataModelProcessVO> updateDataModelProcessVOList = new ArrayList<>();

        // 1. 업데이트 전/후 데이터모델을 비교하여 삭제된 Attribute 및 기 존재 Attribute를 추출해서 VO 생성
        for(Attribute beforeAttribute : beforeDataModelVO.getAttributes()) {
        	Attribute doUpdateAttribute = null;
        	for(Attribute afterAttribute : afterDataModelVO.getAttributes()) {
        		if(beforeAttribute.getName().equals(afterAttribute.getName())) {
        			doUpdateAttribute = afterAttribute;
        			break;
        		}
        	}

        	UpdateDataModelProcessVO updateDataModelProcessVO = new UpdateDataModelProcessVO();
        	updateDataModelProcessVO.setBeforeAttribute(beforeAttribute);
    		updateDataModelProcessVO.setAfterAttribute(doUpdateAttribute);
        	updateDataModelProcessVO.setAttributeName(beforeAttribute.getName());
        	if(doUpdateAttribute == null) {
        		updateDataModelProcessVO.setAttributeUpdateProcessType(AttributeUpdateProcessType.REMOVE_ATTRIBUTE);
        	} else {
        		updateDataModelProcessVO.setAttributeUpdateProcessType(AttributeUpdateProcessType.EXISTS_ATTRIBUTE);
        	}
        	updateDataModelProcessVOList.add(updateDataModelProcessVO);
        }
        
        // 2. 업데이트 전/후 데이터모델을 비교하여 신규 생성된 Attribute를 추출해서 VO 생성
        for(Attribute afterAttribute : afterDataModelVO.getAttributes()) {
        	boolean isExists = false;
        	for(Attribute beforeAttribute : beforeDataModelVO.getAttributes()) {
        		if(beforeAttribute.getName().equals(afterAttribute.getName())) {
        			isExists = true;
        			break;
        		}
        	}
        	if(!isExists) {
        		UpdateDataModelProcessVO updateDataModelProcessVO = new UpdateDataModelProcessVO();
        		updateDataModelProcessVO.setAttributeUpdateProcessType(AttributeUpdateProcessType.NEW_ATTRIBUTE);
        		updateDataModelProcessVO.setAfterAttribute(afterAttribute);
            	updateDataModelProcessVO.setAttributeName(afterAttribute.getName());
            	updateDataModelProcessVOList.add(updateDataModelProcessVO);
        	}
        }

        // 3. RDB와 BIGDATA 중 어느 저장소 SQL 생성 및 실행해야 하는 지 조회
        boolean useRdb = false;
        boolean useBigData = false;

		if(useBigDataStorage(createdStorageTypes)) {
			useBigData = true;
        } else if(useRdbStorage(createdStorageTypes)) {
        	useRdb = true;
        }

        // 4. BigData DDL 생성 및 실행
        if(useBigData) {
			StringBuilder ddlBuilder = new StringBuilder();
			// 5-1. 컬럼 ADD / ALTER / DROP DDL 생성
			for(UpdateDataModelProcessVO updateDataModelProcessVO : updateDataModelProcessVOList) {
				Attribute beforeAttribute = updateDataModelProcessVO.getBeforeAttribute();
				Attribute afterAttribute = updateDataModelProcessVO.getAfterAttribute();
				String attributeDdl = null;
				switch(updateDataModelProcessVO.getAttributeUpdateProcessType()) {
					case NEW_ATTRIBUTE:  {
						attributeDdl = bigDataTableSqlProvider.generateAddOrDropColumnDdl(id, afterAttribute, afterStorageMetadataVO, DbOperation.ADD_COLUMN);
						break;
					} case EXISTS_ATTRIBUTE: {
						attributeDdl = bigDataTableSqlProvider.generateAlterTableColumnDdl(id, beforeAttribute, afterAttribute);
						break;
					} case REMOVE_ATTRIBUTE: {
						throw new UnsupportedOperationException("Bigdata not supported drop column");
					} default: {
						break;
					}
				}
				if(!ValidateUtil.isEmptyData(attributeDdl)) {
					ddlBuilder.append(attributeDdl);
				}
			}
			// 5-2. alter Index DDL 생성
			String alterIndexDdl = rdbDataModelSqlProvider.generateIndexDdl(afterDataModelVO, afterStorageMetadataVO,
					beforeDataModelVO.getIndexAttributeNames(), afterDataModelVO.getIndexAttributeNames());
			if(!ValidateUtil.isEmptyData(alterIndexDdl)) {
				ddlBuilder.append(alterIndexDdl);
			}
			// 5-3. DDL 실행
			if(!ValidateUtil.isEmptyData(ddlBuilder.toString())) {
				String[] splitQuery = ddlBuilder.toString().split("---EOS---");
				for (String query : splitQuery) {
					if (StringUtils.isNotEmpty(query)) {
						executeDdl(query, StorageType.HIVE);
					}
				}
			}
		}

        // 5. RDB DDL 생성 및 실행
        if(useRdb) {

        	StringBuilder ddlBuilder = new StringBuilder();

        	// 5-1. 컬럼 ADD / ALTER / DROP DDL 생성
//        	for(UpdateDataModelProcessVO updateDataModelProcessVO : updateDataModelProcessVOList) {
//
//        		Attribute beforeAttribute = updateDataModelProcessVO.getBeforeAttribute();
//				Attribute afterAttribute = updateDataModelProcessVO.getAfterAttribute();
//
//				String attributeDdl = null;
//
//        		switch(updateDataModelProcessVO.getAttributeUpdateProcessType()) {
//        			case NEW_ATTRIBUTE:  {
//        				attributeDdl = rdbDataModelSqlProvider.generateAddOrDropColumnDdl(id, afterAttribute, beforeStorageMetadataVO, DbOperation.ADD_COLUMN);
//        				break;
//        			} case EXISTS_ATTRIBUTE: {
//        				attributeDdl = rdbDataModelSqlProvider.generateAlterTableColumnDdl(id, beforeAttribute, beforeStorageMetadataVO, afterAttribute, afterStorageMetadataVO);
//        				break;
//        			} case REMOVE_ATTRIBUTE: {
//        				attributeDdl = rdbDataModelSqlProvider.generateAddOrDropColumnDdl(id, beforeAttribute, beforeStorageMetadataVO, DbOperation.DROP_COLUMN);
//        				break;
//        			} default: {
//        				break;
//        			}
//        		}
//
//        		if(!ValidateUtil.isEmptyData(attributeDdl)) {
//        			ddlBuilder.append(attributeDdl);	
//        		}
//        	}

        	String attributeDdl = rdbDataModelSqlProvider.generateAlterTableColumnDdl(id, beforeStorageMetadataVO, afterStorageMetadataVO);
        	if(!ValidateUtil.isEmptyData(attributeDdl)) {
    			ddlBuilder.append(attributeDdl);	
    		}

        	// 5-2. alter Index DDL 생성
        	String alterIndexDdl = rdbDataModelSqlProvider.generateIndexDdl(afterDataModelVO, afterStorageMetadataVO,
    				beforeDataModelVO.getIndexAttributeNames(), afterDataModelVO.getIndexAttributeNames());
        	if(!ValidateUtil.isEmptyData(alterIndexDdl)) {
        		ddlBuilder.append(alterIndexDdl);
        	}

        	// 5-3. DDL 실행
            if(!ValidateUtil.isEmptyData(ddlBuilder.toString())) {
            	executeDdl(ddlBuilder.toString(), StorageType.RDB);
            }
        }
	}



    /**
     * 데이터 모델 삭제
     * @param to 데이터 모델 생성 요청 url
     * @param requestId Provisioning Request Id
     * @param eventTime Provisioning Request Time
     * @throws BaseException
     */
    public void processDelete(String to, String dataModels, String requestId, Date eventTime) throws BaseException {
    	Matcher matcherForDelete = URI_PATTERN_DATA_MODEL.matcher(to);
		
    	if(matcherForDelete.find()) {
			String id = matcherForDelete.group("id");

			deleteDataModel(id);

	    // 404
		} else {
			throw new BadRequestException(ErrorCode.NOT_EXIST_ID);
		}
    }

    /**
     * 데이터 모델 삭제
     * @param id 데이터모델 아이디
     */
	private void deleteDataModel(String id) {
		
		// 1. 파라미터 파싱 및 유효성 검사
		DataModelBaseVO retrieveDataModelBaseVO = dataModelRetrieveSVC.getDataModelBaseVOById(id);

		if (retrieveDataModelBaseVO != null) {
			
			List<BigDataStorageType> createdStorageTypes = retrieveDataModelBaseVO.getCreatedStorageTypes();

			if(createdStorageTypes != null) {
				
				for(BigDataStorageType bigDataStorageType : createdStorageTypes) {
					if(bigDataStorageType == BigDataStorageType.RDB) {
						// DROP TABLE DDL 생성
						String rdbDropTableDdl = rdbDataModelSqlProvider.generateDropTableDdl(id);
						// DROP TABLE DDL 실행
						try {
							executeDdl(rdbDropTableDdl, StorageType.RDB);
						} catch(Exception e) {
							log.warn("deleteDataModel error.", e);
						}

					} else {
						// DROP TABLE DDL 생성
						String bigdataDropTableDdl = bigdataDataModelSqlProvider.generateDropTableDdl(id);

						// DROP TABLE DDL 실행
						try {
							String[] sqls = bigdataDropTableDdl.split("FORSPLIT");
					
					        for (String sql : sqls) {
					        	// TODO: HBase 리팩토링 필요
//					            List<String> tokens = Arrays.asList(sql.split(" "));
//					            String tableName = tokens.get(tokens.size()-1).replace(";","").replace("\n", "");
//					            hBaseTableSVC.dropTable(tableName);
					            executeDdl(sql, StorageType.HIVE);
					        }
						} catch(Exception e) {
							log.warn("deleteDataModel error.", e);
						}
					}
				}
			}

			// 4. dataModel 정보 삭제
			DataModelBaseVO dataModelBaseVO = new DataModelBaseVO();
			dataModelBaseVO.setId(id);
			dataModelBaseVO.setType(id.substring(id.lastIndexOf("/")+1, id.length()));

			dataModelDAO.deleteDataModelBaseVO(dataModelBaseVO);
		}

		// 5. 캐시정보 삭제
		dataModelManager.removeDataModelCache(id);

		// 6. Csource Upsert
        if(dataFederationService.enableFederation()) {
        	dataFederationService.registerCsource();
        }
	}


	/**
     * Attribute name 검증
     * @param attributes
     */
    private void checkAttributeName(List<Attribute> attributes) {
        for (Attribute attribute : attributes) {
            checkAttributeName(attribute);
        }
    }

    /**
     * Attribute name 검증
     * @param attribute
     */
	private void checkAttributeName(Attribute attribute) {
		String attributeName = attribute.getName();
		if(DefaultAttributeKey.CONTEXT.getCode().equalsIgnoreCase(attributeName)
				|| DefaultAttributeKey.ID.getCode().equalsIgnoreCase(attributeName)
				|| DefaultAttributeKey.DATASET_ID.getCode().equalsIgnoreCase(attributeName)
				|| DefaultAttributeKey.CREATED_AT.getCode().equalsIgnoreCase(attributeName)
				|| DefaultAttributeKey.MODIFIED_AT.getCode().equalsIgnoreCase(attributeName)
				|| DefaultAttributeKey.OPERATION.getCode().equalsIgnoreCase(attributeName)
				|| DefaultAttributeKey.TYPE.getCode().equalsIgnoreCase(attributeName)) {
			throw new BadRequestException(ErrorCode.INVALID_PARAMETER,
					"Invalid attribute name. '" + attributeName + "' is a reserved word");
		}
		
		List<ObjectMember> objectMembers = attribute.getObjectMembers();
		if(objectMembers != null) {
			for(ObjectMember objectMember : objectMembers) {
				String objectMemberName = objectMember.getName();
				if(PropertyKey.OBSERVED_AT.getCode().equalsIgnoreCase(objectMemberName)
						|| PropertyKey.CREATED_AT.getCode().equalsIgnoreCase(objectMemberName)
						|| PropertyKey.MODIFIED_AT.getCode().equalsIgnoreCase(objectMemberName)
						|| PropertyKey.UNIT_CODE.getCode().equalsIgnoreCase(objectMemberName)) {
					throw new BadRequestException(ErrorCode.INVALID_PARAMETER,
		        			"Invalid attribute name. '" + attributeName + "." + objectMemberName +"' is a reserved word");
				}
			}
		}
	}

    /**
     * INTEGER, DOUBLE형 부등호 옵션 체크
     * @param attribute
     */
    private void checkAttributeInequality(Attribute attribute) {

        AttributeValueType attributeValueType = attribute.getValueType();

        if (attributeValueType.equals(AttributeValueType.INTEGER)
                || attributeValueType.equals(AttributeValueType.LONG)
                || attributeValueType.equals(AttributeValueType.DOUBLE)
                || attributeValueType.equals(AttributeValueType.ARRAY_INTEGER)
                || attributeValueType.equals(AttributeValueType.ARRAY_LONG)
                || attributeValueType.equals(AttributeValueType.ARRAY_DOUBLE)) {

            if (attribute.getGreaterThan() != null && attribute.getGreaterThanOrEqualTo() != null) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "You should have only greaterThan or greaterThanOrEqualTo");
            }

            if (attribute.getLessThan() != null && attribute.getLessThanOrEqualTo() != null) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "You should have only lessThan or lessThanOrEqualTo");
            }

        } else {

            if (attribute.getGreaterThan() != null
                    || attribute.getGreaterThanOrEqualTo() != null
                    || attribute.getGreaterThan() != null
                    || attribute.getGreaterThanOrEqualTo() != null) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "You should have only lessThan or lessThanOrEqualTo");
            }
        }
    }

    /**
     * AttributeType과 valueType 검증
     * @param attributes
     */
    private void checkAttributeTypeAndValueType(List<Attribute> attributes) {

        for (Attribute attribute : attributes) {

            String name = attribute.getName();
            AttributeValueType attributeValueType = attribute.getValueType();
            AttributeType attributeType = attribute.getAttributeType();

            if (attributeType.equals(AttributeType.PROPERTY)) {

                if (attributeValueType != null) {

                    if (attributeValueType.equals(AttributeValueType.STRING)
                            || attributeValueType.equals(AttributeValueType.INTEGER)
                            || attributeValueType.equals(AttributeValueType.LONG)
                            || attributeValueType.equals(AttributeValueType.DOUBLE)
                            || attributeValueType.equals(AttributeValueType.OBJECT)
                            || attributeValueType.equals(AttributeValueType.DATE)
                            || attributeValueType.equals(AttributeValueType.BOOLEAN)
                            || attributeValueType.equals(AttributeValueType.ARRAY_STRING)
                            || attributeValueType.equals(AttributeValueType.ARRAY_INTEGER)
                            || attributeValueType.equals(AttributeValueType.ARRAY_LONG)
                            || attributeValueType.equals(AttributeValueType.ARRAY_DOUBLE)
                            || attributeValueType.equals(AttributeValueType.ARRAY_BOOLEAN)
                            || attributeValueType.equals(AttributeValueType.ARRAY_OBJECT)) {
                    	
                        checkAttributeInequality(attribute);
                    }
                    
                    // ObjectMember 빈값 체크
                    if(attributeValueType.equals(AttributeValueType.ARRAY_OBJECT)
                            || attributeValueType.equals(AttributeValueType.OBJECT)) {
                        if(ValidateUtil.isEmptyData(attribute.getObjectMembers())) {
                            throw new BadRequestException(ErrorCode.INVALID_PARAMETER,
                                    "Not exists ObjectMember. " + "name=" + name + ", attributeType=" + attributeType + ", attributeValueType=" + attributeValueType);
                        }

                    } else {
                        if(attribute.getObjectMembers() != null) {
                            throw new BadRequestException(ErrorCode.INVALID_PARAMETER,
                                    "'" + attributeValueType.getCode() + "' type cannot have ObjectMember. " + "name=" + name);
                            
                        }
                    }
                    
                    // arrayObject 인 경우 하위 objectMember로 array 형태의 valueType이 올 수 없음
                    // rdb 저장 이슈로 예외처리
                    if(attributeValueType.equals(AttributeValueType.ARRAY_OBJECT)) {
                    	checkArrayAttribute(attribute.getObjectMembers());
                    }
                    continue;
                }

            } else if (attributeType.equals(AttributeType.RELATIONSHIP)) {

                if (attributeValueType != null
						&& (attributeValueType.equals(AttributeValueType.STRING)
							|| attributeValueType.equals(AttributeValueType.ARRAY_STRING))) {
                    continue;
                }
            } else if (attributeValueType != null && attributeType.equals(AttributeType.GEO_PROPERTY)) {
                if (attributeValueType.equals(AttributeValueType.GEO_JSON)) {
                    continue;
                }
            }
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER,
                    "Mismatch attributeType and valueType. " + "name=" + name + ", attributeType=" + attributeType + ", attributeValueType=" + attributeValueType);
        }
    }

    /**
     * ArrayObject 내부에 Array형태의 valueType은 지원하지 않음
     * @param objectMemberList ArrayObject 하위 objectMember
     */
    private void checkArrayAttribute(List<ObjectMember> objectMemberList) {

    	if(objectMemberList == null || objectMemberList.size() == 0) {
    		return;
    	}

    	for(ObjectMember objectMember : objectMemberList) {
    		if(objectMember.getValueType() == AttributeValueType.ARRAY_BOOLEAN
    				|| objectMember.getValueType() == AttributeValueType.ARRAY_DOUBLE
    				|| objectMember.getValueType() == AttributeValueType.ARRAY_INTEGER
    				|| objectMember.getValueType() == AttributeValueType.ARRAY_LONG
    				|| objectMember.getValueType() == AttributeValueType.ARRAY_OBJECT
    				|| objectMember.getValueType() == AttributeValueType.ARRAY_STRING) {
    			throw new BadRequestException(ErrorCode.INVALID_PARAMETER, 
    					"Not supported ArrayObject in array attributeValueType. name=" + objectMember.getName());
    		}
    	}
    }

    /**
     * 빅데이터 저장 여부 확인
     * @param bigDataStorageTypeList
     * @return
     */
    private boolean useBigDataStorage(List<BigDataStorageType> bigDataStorageTypeList) {
    	if(bigDataStorageTypeList == null || bigDataStorageTypeList.size() == 0) {
    		return false;
    	}

    	for(BigDataStorageType bigDataStorageType : bigDataStorageTypeList) {
    		if(bigDataStorageType == BigDataStorageType.HIVE
    				|| bigDataStorageType == BigDataStorageType.HBASE) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * RDB 저장 여부 확인
     * @param bigDataStorageTypeList
     * @return
     */
    private boolean useRdbStorage(List<BigDataStorageType> bigDataStorageTypeList) {
    	if(bigDataStorageTypeList == null || bigDataStorageTypeList.size() == 0) {
    		return false;
    	}

    	for(BigDataStorageType bigDataStorageType : bigDataStorageTypeList) {
    		if(bigDataStorageType == BigDataStorageType.RDB) {
    			return true;
    		}
    	}
    	return false;
    }

    /**
     * HTTP 기반 Provisioning 처리를 다른 Instance에서 이미 처리 했는 지 여부 체크
     * @param requestId Provisioning Request Id
     * @param eventTime Provisioning Request Time
     * @param retrieveDataModelBaseVO DB조회 데이터 모델
     * @return
     */
    private boolean alreadyProcessByOtherInstance(String requestId, Date eventTime, DataModelBaseVO retrieveDataModelBaseVO) {
		// 이중화되어 있는 다른 DataServiceBroker 인스턴스에서 DB 입력 했는지 체크
    	if(retrieveDataModelBaseVO == null) {
    		return false;
    	}

    	if(requestId.equals(retrieveDataModelBaseVO.getProvisioningRequestId())
    			&& eventTime.getTime() >= retrieveDataModelBaseVO.getProvisioningEventTime().getTime()) {
    		return true;
    	}
    	return false;
	}

    public int updateDataModelStorage(DataModelBaseVO dataModelBaseVO) {
        return dataModelDAO.updateDataModelStorage(dataModelBaseVO);
    }

	public void setDataModelDAO(DataModelDAO dataModelDAO) {
		this.dataModelDAO = dataModelDAO;
	}

}
