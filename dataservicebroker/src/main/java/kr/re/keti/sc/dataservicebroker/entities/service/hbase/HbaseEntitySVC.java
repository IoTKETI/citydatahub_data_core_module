package kr.re.keti.sc.dataservicebroker.entities.service.hbase;

import static kr.re.keti.sc.dataservicebroker.common.code.Constants.DEFAULT_SRID;

import java.text.ParseException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.postgis.PGgeometry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.AttributeType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.AttributeValueType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.BigDataStorageType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.DefaultAttributeKey;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.DefaultDbColumnName;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.HistoryStoreType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.PropertyKey;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.UseYn;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdBadRequestException;
import kr.re.keti.sc.dataservicebroker.common.vo.AttributeVO;
import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityFullVO;
import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityVO;
import kr.re.keti.sc.dataservicebroker.common.vo.EntityProcessVO;
import kr.re.keti.sc.dataservicebroker.common.vo.GeoPropertiesVO;
import kr.re.keti.sc.dataservicebroker.common.vo.GeoPropertyVO;
import kr.re.keti.sc.dataservicebroker.common.vo.ProcessResultVO;
import kr.re.keti.sc.dataservicebroker.common.vo.PropertiesVO;
import kr.re.keti.sc.dataservicebroker.common.vo.PropertyVO;
import kr.re.keti.sc.dataservicebroker.common.vo.RelationshipVO;
import kr.re.keti.sc.dataservicebroker.common.vo.RelationshipsVO;
import kr.re.keti.sc.dataservicebroker.common.vo.entities.DynamicEntityDaoVO;
import kr.re.keti.sc.dataservicebroker.common.vo.entities.DynamicEntityFullVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.Attribute;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelCacheVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelDbColumnVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelStorageMetadataVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.ObjectMember;
import kr.re.keti.sc.dataservicebroker.entities.dao.EntityDAOInterface;
import kr.re.keti.sc.dataservicebroker.entities.service.DefaultEntitySVC;
import kr.re.keti.sc.dataservicebroker.util.DateUtil;
import kr.re.keti.sc.dataservicebroker.util.ObservedAtReverseOrder;
import kr.re.keti.sc.dataservicebroker.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HbaseEntitySVC extends DefaultEntitySVC {

	@Value("${entity.retrieve.include.datasetid:N}")
	private String retrieveIncludeDatasetid; // 조회 시 datasetId 포함여부
	@Value("${entity.default.history.store.type:full}")
	private String defaultHistoryStoreType; // 데이터 셋 정보가 없는 경우 기본 이력 저장 유형
	
    @Override
	protected String getTableName(DataModelCacheVO dataModelCacheVO) {
		return dataModelCacheVO.getDataModelStorageMetadataVO().getHiveTableName();
	}
    
    @Override
    protected BigDataStorageType getStorageType() {
    	return BigDataStorageType.HBASE;
    }

    @Override
	public void setEntityDAOInterface(EntityDAOInterface<DynamicEntityDaoVO> entityDAO) {
    	this.entityDAO = entityDAO;
	}

    /**
     * Operation 정상처리 항목 이력 저장
     *
     * @param entityProcessVOList 처리VO리스트
     */
    @Override
    public void storeEntityStatusHistory(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> entityProcessVOList) {
        // 1. 이력저장 대상 리스트 객체 생성
        List<DynamicEntityDaoVO> createPartialHistoryVOList = new ArrayList<>();
        List<DynamicEntityDaoVO> createFullHistoryTargetVOList = new ArrayList<>();

        StringBuilder logMessage = new StringBuilder();

        for (EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO : entityProcessVOList) {

            DynamicEntityDaoVO entityDaoVO = entityProcessVO.getEntityDaoVO();
            ProcessResultVO processResultVO = entityProcessVO.getProcessResultVO();

            // 2. 실패 항목은 이력저장에서 제외
            if (!processResultVO.isProcessResult()) {
                continue;
            }

            if (logMessage.length() == 0) {
                logMessage.append(entityProcessVO.getDatasetId()).append(" Process SUCCESS");
            }
            logMessage.append(System.lineSeparator()).append("\t")
                    .append("eventTime=").append(DateUtil.dateToDbFormatString(entityDaoVO.getModifiedAt()))
                    .append(", id=").append(entityDaoVO.getId())
                    .append(", processOperation=").append(processResultVO.getProcessOperation());

            // 3. 실제 수행된 Operation 값 설정
            entityDaoVO.setOperation(processResultVO.getProcessOperation());

            // 4. 이력저장 목록 추가
            HistoryStoreType historyStoreType = null;
            if(entityProcessVO.getDatasetId() != null) {
            	historyStoreType = dataModelManager.getHistoryStoreType(entityProcessVO.getDatasetId());
            } else {
            	historyStoreType = HistoryStoreType.parseType(defaultHistoryStoreType);
            }

            // PARTIAL, FULL 모두 저장
            if(historyStoreType == HistoryStoreType.ALL) {
            	createFullHistoryTargetVOList.add(entityDaoVO);
                createPartialHistoryVOList.add(entityDaoVO);
            // FULL 이력만 저장
            } else if(historyStoreType == HistoryStoreType.FULL) {
            	createFullHistoryTargetVOList.add(entityDaoVO);
            // PARTIAL 이력만 저장
            } else if(historyStoreType == HistoryStoreType.PARTIAL) {
            	createPartialHistoryVOList.add(entityDaoVO);
            // 이력 저장하지 않음
            } else if(historyStoreType == HistoryStoreType.NONE) {
            	
            } else {
            	// default 'PARTIAL'
            	createFullHistoryTargetVOList.add(entityDaoVO);
            }
        }

        if (logMessage.length() > 0) log.info(logMessage.toString());

        // 5. 이력 벌크 저장
        // 5-1 Partial 이력 저장 (요청받은 Entity 파라미터값만 저장)
        if (createPartialHistoryVOList != null && createPartialHistoryVOList.size() > 0) {
            try {
            	entityDAO.bulkCreateHist(createPartialHistoryVOList);
            } catch (Exception e) {
                log.error("Store entity PARTIAL history error", e);
            }
        }

        // 5-2. Full 이력 저장 (Entity 의 최종값 조회 후 이력에 모든 파라미터값 저장)
        /*
        if (createFullHistoryTargetVOList != null && createFullHistoryTargetVOList.size() > 0) {
            List<DynamicEntityDaoVO> createFullHistoryVOList = new ArrayList<>();
            // Full 이력저장을 위한 Entity 최종값 조회
            for (DynamicEntityDaoVO entityDaoVO : createFullHistoryTargetVOList) {

                // Delete 는 Full 이력 저장하지 않음
                if (Operation.DELETE_ENTITY == entityDaoVO.getOperation()) {
                    continue;
                }

                QueryVO queryVO = new QueryVO();
                queryVO.setId(entityDaoVO.getId());
                queryVO.setDatasetId(entityDaoVO.getDatasetId());
                queryVO.setType(entityDaoVO.getEntityType());
                DynamicEntityDaoVO entityFullDaoVO = entityDAO.selectById(queryVO, true);
                if (entityFullDaoVO == null) {
                    log.warn("Store entity FULL history error. Now exist Entity id=" + entityDaoVO.getId());
                    continue;
                }

                entityFullDaoVO.setDbTableName(StringUtil.removeSpecialCharAndLower(entityDaoVO.getDbTableName()));
                entityFullDaoVO.setDbColumnInfoVOMap(entityDaoVO.getDbColumnInfoVOMap());
                entityFullDaoVO.setDatasetId(entityDaoVO.getDatasetId());
                entityFullDaoVO.setEntityType(entityDaoVO.getEntityType());
                entityFullDaoVO.setOperation(entityDaoVO.getOperation());
                entityFullDaoVO.setCreatedAt(entityDaoVO.getCreatedAt());
                entityFullDaoVO.setModifiedAt(entityDaoVO.getModifiedAt());

                // DB입력을 위한 Array 및 Geo타입 데이터 변환 처리
                // lower case -> camel(원래 명칭)으로 변환 (TODO: 개선)
                Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = entityDaoVO.getDbColumnInfoVOMap();
                for (Map.Entry<String, DataModelDbColumnVO> entry : dbColumnInfoVOMap.entrySet()) {
                    String columnName = entry.getKey().toLowerCase();
                    DataModelDbColumnVO dbColumnInfoVO = entry.getValue();
                    Object value = entityFullDaoVO.get(columnName.toLowerCase());
                    if (value != null) {
                        // String[] -> List<String>, Integer[] -> List<Integer>, Float[] -> List<Float> 변환처리
                        if (dbColumnInfoVO.getColumnType() == DbColumnType.ARRAY_VARCHAR
                                || dbColumnInfoVO.getColumnType() == DbColumnType.ARRAY_INTEGER
                                || dbColumnInfoVO.getColumnType() == DbColumnType.ARRAY_FLOAT) {
                            Object[] valueArr = (Object[]) value;
                            List<Object> valueList = new ArrayList<>(valueArr.length);
                            for (int i = 0; i < valueArr.length; i++) {
                                valueList.add(valueArr[i]);
                            }
                            entityFullDaoVO.put(columnName, valueList);

                            // Timestamp[] 를 List<Date> 변환처리
                        } else if (dbColumnInfoVO.getColumnType() == DbColumnType.ARRAY_TIMESTAMP) {
                            Object[] dateArr = (Object[]) value;
                            List<Date> dateList = new ArrayList<>(dateArr.length);
                            for (int i = 0; i < dateArr.length; i++) {
                                dateList.add((Date) dateArr[i]);

                            }
                            entityFullDaoVO.put(columnName, dateList);

                            // PGgeometry -> GeoJson 변환처리
                        } else if (dbColumnInfoVO.getColumnType() == DbColumnType.GEOMETRY_4326) {
                            Object geom = value;
                            entityFullDaoVO.put(columnName, geom);
                            entityFullDaoVO.put(columnName + "_idx", hiveTableSVC.getIndex(columnName + "_idx", entityFullDaoVO.getDbTableName(), entityFullDaoVO.getId()));
                        }
                        continue;
                    }
                }
                createFullHistoryVOList.add(entityFullDaoVO);
            }

            try {
                entityDAO.bulkCreateFullHist(createFullHistoryVOList);
            } catch (Exception e) {
                log.error("Store entity FULL history error", e);
            }
        }
        */
        if (createFullHistoryTargetVOList != null && createFullHistoryTargetVOList.size() > 0) {
            try {
                entityDAO.bulkCreateFullHist(createFullHistoryTargetVOList);
            } catch (Exception e) {
                log.error("Store entity FULL history error", e);
            }
        }
    }
    
    @Override
    public CommonEntityVO daoVOToFullRepresentationVO(DynamicEntityDaoVO dynamicEntityDaoVO, DataModelCacheVO dataModelCacheVO, boolean includeSysAttrs, List<String> attrs) {

    	CommonEntityFullVO commonEntityFullVO = new CommonEntityFullVO();
        addDefaultFullRepresentationField(commonEntityFullVO, dynamicEntityDaoVO, dataModelCacheVO, includeSysAttrs);

        DataModelStorageMetadataVO storageMetadataVO = dataModelCacheVO.getDataModelStorageMetadataVO();
        Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = storageMetadataVO.getDbColumnInfoVOMap();
    	
        for (DataModelDbColumnVO dbColumnInfoVO : dbColumnInfoVOMap.values()) {
            String columnName = dbColumnInfoVO.getColumnName();
            Object attributeValue = dynamicEntityDaoVO.get(columnName); // rdb에서는 lowercase 를 하는데, hive에서는 하지 않음
            List<String> attributeIds = dbColumnInfoVO.getHierarchyAttributeIds();

            //부모 attributeId 가져오기
            String rootAttrId = attributeIds.get(0);
            Attribute rootAttribute = dataModelCacheVO.getRootAttribute(rootAttrId);

            //이미 한거면 SKIP
            // 1. DB조회 결과에 attribute value가 없을 경우, SKIP
            if (attributeValue == null || commonEntityFullVO.containsKey(rootAttrId)) {
                continue;
            }

            Map<String, AttributeVO> resultMap = (Map<String, AttributeVO>) converDaoToAttribute(dynamicEntityDaoVO, storageMetadataVO, rootAttribute, null, includeSysAttrs);
            
//            HashMap<String, AttributeVO> resultMap = (HashMap<String, AttributeVO>) convertDaoToAttribute(dynamicEntityDaoVO, dbColumnInfoVOMap, rootAttribute, null);
            commonEntityFullVO.putAll(resultMap);
        }
		
		// attrs filtering 조건이 있을 경우 조회된 attributes 중 attrs 항목이 포함되었는지 체크
        if (validateAttrsFiltering(attrs, commonEntityFullVO)) {
            return null;
        }

        return commonEntityFullVO;
    }

    @Override
    public CommonEntityVO daoVOToSimpleRepresentationVO(DynamicEntityDaoVO dynamicEntityDaoVO, DataModelCacheVO entitySchemaCacheVO, List<String> attrs) {
    	CommonEntityVO commonEntityVO = daoVOToFullRepresentationVO(dynamicEntityDaoVO, entitySchemaCacheVO, false, attrs);


        for (String key : commonEntityVO.keySet()) {

            Object object = commonEntityVO.get(key);
            if (object instanceof PropertyVO) {
                PropertyVO objectValue = (PropertyVO) commonEntityVO.get(key);
                commonEntityVO.replace(key, objectValue.getValue());
            } else if (object instanceof RelationshipVO) {
                RelationshipVO objectValue = (RelationshipVO) commonEntityVO.get(key);

                commonEntityVO.replace(key, objectValue.getObject());
            } else if (object instanceof GeoPropertyVO) {
                GeoPropertyVO objectValue = (GeoPropertyVO) commonEntityVO.get(key);

                commonEntityVO.replace(key, objectValue.getValue());
            } else {
                commonEntityVO.replace(key, commonEntityVO.get(key));

            }

        }

        return commonEntityVO;
    }

    @Override
    public List<CommonEntityVO> daoVOToTemporalFullRepresentationVO(List<DynamicEntityDaoVO> entityDaoVOList, DataModelCacheVO dataModelCacheVO, Integer lastN, String accept) {
    	List<CommonEntityVO> commonEntityVOS = daoVOToTemporalNormalizedHistoryRepresentationVO(entityDaoVOList, dataModelCacheVO, accept);


        Map<String, CommonEntityVO> filterdMap = new HashMap<>();
        Map<String, String> tempDatasetMap = new HashMap<>();

        for (CommonEntityVO commonEntityVO : commonEntityVOS) {

            CommonEntityVO observedAtEntityVO = (CommonEntityVO) commonEntityVO.clone();
            String id = (String) commonEntityVO.get(DefaultAttributeKey.ID.getCode());
            tempDatasetMap.put(id, (String) commonEntityVO.get(DefaultAttributeKey.DATASET_ID.getCode()));
            List<String> context = null;
            if (accept.equals(Constants.APPLICATION_LD_JSON_VALUE)) {

                context = dataModelCacheVO.getDataModelVO().getContext();
            }


            //observedAt이 아닌 항목 제거!
            for (String key : commonEntityVO.keySet()) {

                // Attribute rootAttribute = dataModelCacheVO.getRootAttribute(key);
                // if (rootAttribute == null || rootAttribute.getHasObservedAt() == null || !rootAttribute.getHasObservedAt()) {
                //     observedAtEntityVO.remove(key);
                // }
                //기본 구성요소(@context, id, createdAt ,modifiedAt ,operation ,type) 제거
                if (DefaultAttributeKey.parseType(key) != null) {
                    continue;
                }
            }


            if (filterdMap.containsKey(id)) {
                CommonEntityVO innerCommonEntityVO = filterdMap.get(id);
                for (String key : observedAtEntityVO.keySet()) {
                    
                    Attribute rootAttribute = dataModelCacheVO.getRootAttribute(key);
                    if (rootAttribute == null || rootAttribute.getHasObservedAt() == null || !rootAttribute.getHasObservedAt()) {
                        continue;
                    }

                    if (innerCommonEntityVO.get(key) instanceof List) {
                        // 기 존재하는 리스트에 값을 추가
                        AttributeVO attributeVO = (AttributeVO) observedAtEntityVO.get(key);
                        List<AttributeVO> attributeVOList = (List<AttributeVO>) innerCommonEntityVO.get(key);
                        attributeVOList.add(attributeVO);
                        observedAtEntityVO.replace(key, attributeVOList);

                    } else if (innerCommonEntityVO.get(key) instanceof AttributeVO) {

                        // 리스트를 생성하여 값 추가
                        AttributeVO attributeVO = (AttributeVO) observedAtEntityVO.get(key);
                        List<AttributeVO> attributeVOList = new ArrayList<>();
                        attributeVOList.add(attributeVO);
                        observedAtEntityVO.replace(key, attributeVOList);

                    }

                }
                filterdMap.put(id, innerCommonEntityVO);

            } else {

                for (String key : observedAtEntityVO.keySet()) {
                    if (observedAtEntityVO.get(key) instanceof AttributeVO) {
                        AttributeVO attributeVO = (AttributeVO) observedAtEntityVO.get(key);

                        List<AttributeVO> attributeVOList = new ArrayList<>();
                        attributeVOList.add(attributeVO);
                        observedAtEntityVO.replace(key, attributeVOList);

                    }

                }


                if (observedAtEntityVO != null && observedAtEntityVO.size() > 0) {

                    if (accept.equals(Constants.APPLICATION_LD_JSON_VALUE)) {
                        observedAtEntityVO.setContext(context);
                    }
                    filterdMap.put(id, observedAtEntityVO);

                }
            }
        }

        List<CommonEntityVO> filteredList = new ArrayList<>();
        for (String id : filterdMap.keySet()) {

            CommonEntityFullVO commonEntityVO = (CommonEntityFullVO) filterdMap.get(id);

            // lastN 옵션 처리
            if(lastN != null && lastN > 0 ){
                commonEntityVO = retrieveLastN(commonEntityVO, lastN);
            }
            commonEntityVO.setId(id);
            commonEntityVO.setType(dataModelCacheVO.getDataModelVO().getType());
            filteredList.add(commonEntityVO);
            if(UseYn.YES.getCode().equals(retrieveIncludeDatasetid)
            		&& tempDatasetMap.get(id) != null) {
            	commonEntityVO.setDatasetId(tempDatasetMap.get(id));
            }
        }

        return filteredList;
    }
    
    

    @Override
    public List<CommonEntityVO> daoVOToTemporalNormalizedHistoryRepresentationVO(List<DynamicEntityDaoVO> entityDaoVOList, DataModelCacheVO dataModelCacheVO, String accept) {
    	//throw new UnsupportedOperationException("HiveEntitySVC not supported 'daoVOToTemporalNormalizedHistoryRepresentationVO'");
        List<CommonEntityVO> commonEntityVOList = new ArrayList<>();

        for (DynamicEntityDaoVO dynamicEntityDaoVO : entityDaoVOList) {

            // 1. options이 없을 경우 처리, Full Representation (normalizedHistory)
            CommonEntityVO commonEntityVO = this.daoVOToFullRepresentationVO(dynamicEntityDaoVO, dataModelCacheVO, false, null);
            if(commonEntityVO != null) {
                commonEntityVOList.add(commonEntityVO);


                // 2. 요청 header의 accept가 'application/ld+json' 일 경우 @context 정보 추가
                if (accept.equals(Constants.APPLICATION_LD_JSON_VALUE)) {
                    commonEntityVO.setContext(dataModelCacheVO.getDataModelVO().getContext());
                }
            }
        }

        return commonEntityVOList;
    }

    @Override
    public List<CommonEntityVO> daoVOToTemporalTemporalValuesRepresentationVO(List<CommonEntityVO> commonEntityVOList) {
        //throw new UnsupportedOperationException("HiveEntitySVC not supported 'daoVOToTemporalTemporalValuesRepresentationVO'");
        for (CommonEntityVO commonEntityVO : commonEntityVOList) {
            CommonEntityFullVO vo = (CommonEntityFullVO) commonEntityVO;
            for (String key : vo.keySet()) {
                //기본 구성요소(@context, id, createdAt ,modifiedAt ,operation ,type) 검증 SKIP
                if (DefaultAttributeKey.parseType(key) != null) {
                    continue;
                }

                List valueList = (ArrayList) vo.get(key);
                List<Object> midList = new ArrayList<>();
                AttributeVO attributeVO = null;

                for (Object obj : valueList) {

                    attributeVO = (AttributeVO) obj;
                    List<Object> reList = new ArrayList();


                    if (attributeVO.getType().equalsIgnoreCase(AttributeType.PROPERTY.getCode())) {
                        reList.add( attributeVO.get(PropertyKey.VALUE.getCode()));
                    } else if (attributeVO.getType().equalsIgnoreCase(AttributeType.GEO_PROPERTY.getCode())) {
                        reList.add( attributeVO.get(PropertyKey.VALUE.getCode()));
                    }else if (attributeVO.getType().equalsIgnoreCase(AttributeType.RELATIONSHIP.getCode())) {

                        if (attributeVO.get(PropertyKey.OBJECTS.getCode()) != null) {
                            reList.add( attributeVO.get(PropertyKey.OBJECTS.getCode()));
                        } else {
                            reList.add( attributeVO.get(PropertyKey.OBJECT.getCode()));
                        }

                    } else {
                        throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "should include value or object");
                    }
                    if (attributeVO.get(PropertyKey.OBSERVED_AT.getCode()) != null) {
                        reList.add(attributeVO.get(PropertyKey.OBSERVED_AT.getCode()));
                    }


                    midList.add(reList);
                }

                if (attributeVO.getType().equalsIgnoreCase(AttributeType.PROPERTY.getCode())) {

                    PropertiesVO propertiesVO = new PropertiesVO();
                    propertiesVO.setValue(midList);
                    vo.replace(key, propertiesVO);

                } else if (attributeVO.getType().equalsIgnoreCase(AttributeType.GEO_PROPERTY.getCode())) {

                    GeoPropertiesVO geoPropertiesVO = new GeoPropertiesVO();
                    geoPropertiesVO.setValue(midList);
                    vo.replace(key, geoPropertiesVO);

                } else if (attributeVO.getType().equalsIgnoreCase(AttributeType.RELATIONSHIP.getCode())) {

                    if (attributeVO.get(PropertyKey.OBJECTS.getCode()) != null) {
                        RelationshipsVO relationshipsVO = new RelationshipsVO();
                        relationshipsVO.setObjects(midList);
                        vo.replace(key, relationshipsVO);
                    } else {
                        RelationshipVO relationshipVO = new RelationshipVO();
                        relationshipVO.setObject(midList);
                        vo.replace(key, relationshipVO);
                    }

                } else {
                    vo.replace(key, midList);
                }


            }
        }
        return commonEntityVOList;
    }



    /**
     * DB 조회 결과(dao) -> Map(attribute)으로 전환
     *
     * @param upperId
     * @param rootAttribute
     * @param dbColumnInfoVOMap
     * @param dynamicEntityDaoVO
     * @return
     */
//    private Map convertDaoToAttribute(DynamicEntityDaoVO dynamicEntityDaoVO, Map<String, DataModelDbColumnVO> dbColumnInfoVOMap, Attribute rootAttribute, String upperId) {
//        String id;
//        if (upperId != null) {
//            id = upperId + "_" + rootAttribute.getName().toLowerCase();
//        } else {
//            id = rootAttribute.getName().toLowerCase();
//        }
//
//        List<Attribute> hasAttributes = rootAttribute.getChildAttributes();
//        List<ObjectMember> objectMembers = rootAttribute.getObjectMembers();
//        HashMap<String, Object> convertedMap = new HashMap<>();
//
//        if (hasAttributes != null) {
//
//            HashMap<String, Object> childAttributeMap = new HashMap<>();
//            for (Attribute hasAttribute : hasAttributes) {
//                //자식 value -> AttributeVO로 변경
//                HashMap<String, Object> subChildAttributeMap = (HashMap<String, Object>) convertDaoToAttribute(dynamicEntityDaoVO, dbColumnInfoVOMap, hasAttribute, id);
//                childAttributeMap.putAll(subChildAttributeMap);
//            }
//
//            DataModelDbColumnVO dbColumnInfoVO = dbColumnInfoVOMap.get(id);
//            if(dbColumnInfoVO != null) {
//            	Object value = dynamicEntityDaoVO.get(dbColumnInfoVO.getColumnName()); // rdb에서는 lowercase를 하는데 hive 에서는 하지 않음
//
//                AttributeVO attributeVO = null;
//                if (rootAttribute.getAttributeType() == AttributeType.PROPERTY && value != null) {
//
//                    PropertyVO propertyVO = new PropertyVO();
//                    propertyVO.setValue(value);
//                    attributeVO = propertyVO;
//
//                    childAttributeMap.put(DataServiceBrokerCode.AttributeResultType.VALUE.getCode(), value);
//
//                } else if (rootAttribute.getAttributeType() == AttributeType.RELATIONSHIP && value != null) {
//                    RelationshipVO relationshipVO = new RelationshipVO();
//                    relationshipVO.setObject(value);
//                    attributeVO = relationshipVO;
//                    childAttributeMap.put(DataServiceBrokerCode.AttributeResultType.OBJECT.getCode(), value);
//                } else {
//                    attributeVO = new AttributeVO();
//                }
//
//                attributeVO.putAll(childAttributeMap);
//                convertedMap.put(id.toLowerCase(), attributeVO);
//            }
//        }
//
//        if (objectMembers != null) {
//
//            HashMap<String, Object> objectMemberMap = new HashMap<>();
//            for (ObjectMember objectMember : objectMembers) {
//
//                String objectMemberId = objectMember.getName().toLowerCase();
//                DataModelDbColumnVO vo = dbColumnInfoVOMap.get(id + "_" + objectMemberId);
//                Object value = null;
//                String[] arrayString = String.valueOf(dynamicEntityDaoVO.get(vo.getColumnName())).replaceAll("\\[", "")
//                        .replaceAll("\\]", "").split(",");
//
//                if (arrayString != null) {
//                    Object[] castedValues;
//
//                    if (isArrayType(objectMember.getValueType())) {
//                        castedValues = new Object[1];
//                        Object[] castedArrayValues = new Object[arrayString.length];
//
//                        for (int i = 0; i < arrayString.length; i++) {
//                            if (objectMember.getValueType() == AttributeValueType.ARRAY_INTEGER) {
//                                castedArrayValues[i] = Integer.parseInt(arrayString[i]);
//                            } else if (objectMember.getValueType() == AttributeValueType.ARRAY_DOUBLE) {
//                                castedArrayValues[i] = Double.parseDouble(arrayString[i]);
//                            } else if (objectMember.getValueType() == AttributeValueType.ARRAY_BOOLEAN) {
//                                castedArrayValues[i] = Boolean.parseBoolean(arrayString[i]);
//                            } else if (objectMember.getValueType() == AttributeValueType.ARRAY_STRING) {
//                                castedArrayValues[i] = arrayString[i].replace("{", "").replace("}", "").replace("\"",
//                                        "");
//                            }
//                        }
//
//                        castedValues[0] = castedArrayValues;
//                    } else {
//                        castedValues = new Object[arrayString.length];
//
//                        for (int i = 0; i < arrayString.length; i++) {
//                            if (!StringUtils.hasText(arrayString[i]) || arrayString[i].equals("null")){
//                                continue;
//                            }
//                            if (objectMember.getValueType() == AttributeValueType.INTEGER) {
//                                castedValues[i] = Integer.parseInt(arrayString[i]);
//                            } else if (objectMember.getValueType() == AttributeValueType.DOUBLE) {
//                                castedValues[i] = Double.parseDouble(arrayString[i]);
//                            } else if (objectMember.getValueType() == AttributeValueType.BOOLEAN) {
//                                castedValues[i] = Boolean.parseBoolean(arrayString[i]);
//                            } else {
//                                castedValues[i] = arrayString[i].replace("{", "").replace("}", "").replace("\"", "");
//                            }
//                        }
//                    }
//
//                    value = castedValues;
//                }
//                objectMemberMap.put(objectMemberId, value);
//            }
//
//            AttributeVO attributeVO;
//            if (rootAttribute.getValueType() == AttributeValueType.ARRAY_OBJECT) {
//                attributeVO = valueToAttributeVO(rootAttribute, objectMemberMapToArrayObject(objectMemberMap));
//            } else if (rootAttribute.getValueType() == AttributeValueType.OBJECT) {
//                attributeVO = valueToAttributeVO(rootAttribute, objectMemberMapToArrayObjectOrObject(objectMemberMap));
//            } else {
//                attributeVO = valueToAttributeVO(rootAttribute, objectMemberMap);
//            }
//            
//            if (rootAttribute.getHasObservedAt() != null && rootAttribute.getHasObservedAt()) {
//                attributeVO = addObservedAt(dynamicEntityDaoVO, dbColumnInfoVOMap, attributeVO, id);
//            }
//            convertedMap.put(id.toLowerCase(), attributeVO);
//
//        }
//
//        if (rootAttribute.getObjectMembers() == null && (rootAttribute.getChildAttributes() == null )) {
//
//            AttributeVO attributeVO = null;
//
//            if (rootAttribute.getValueType() == AttributeValueType.GEO_JSON) {
//            	DataModelDbColumnVO dbColumnInfoVO = dbColumnInfoVOMap.get(id + "_" + DEFAULT_SRID);
//
//            	if (dynamicEntityDaoVO.get(dbColumnInfoVO.getColumnName()) != null && dynamicEntityDaoVO.get(dbColumnInfoVO.getColumnName()) instanceof String) {
//            	    Object geometry = dynamicEntityDaoVO.get(dbColumnInfoVO.getColumnName());
//
//            		if (geometry != null) {
//                        Map<String, Object> convertedGeometry = null;
//            		    try {
//                            convertedGeometry = objectMapper.readValue((String) geometry, Map.class);
//            		    } catch (JsonProcessingException e) {
//                            log.error("Error while parsing geometry", e);
//                        }
//
//                        attributeVO = valueToAttributeVO(rootAttribute, convertedGeometry.get("geometry"));
//					}
//            	}
//            } else if (isArrayType(rootAttribute.getValueType())) {
//                DataModelDbColumnVO dbColumnInfoVO = dbColumnInfoVOMap.get(id);
//                
//                String arrayValue = (String) dynamicEntityDaoVO.get(dbColumnInfoVO.getColumnName()); // rdb에서는 lowercase를 하는데 hive 에서는 하지 않음
//
//                if (arrayValue != null && StringUtils.hasText(arrayValue)) {
//                    // Hive JDBC 에서는 Array 타입의 조회를 지원하지 않고, String 한줄로 리턴하기 때문에 파싱 및 캐스팅 작업이 필요
//                    String[] values = arrayValue.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
//                    List<Object> castedValues = new ArrayList<>();
//
//                    for (String value : values) {
//                        if (rootAttribute.getValueType() == AttributeValueType.ARRAY_INTEGER) {
//                            castedValues.add(Integer.parseInt(value));
//                        } else if (rootAttribute.getValueType() == AttributeValueType.ARRAY_DOUBLE) {
//                            castedValues.add(Double.parseDouble(value));
//                        } else if (rootAttribute.getValueType() == AttributeValueType.ARRAY_BOOLEAN) {
//                            castedValues.add(Boolean.parseBoolean(value));
//                        } else if (rootAttribute.getValueType() == AttributeValueType.ARRAY_STRING) {
//                            castedValues.add(value.replace("{", "").replace("}", "").replace("\"", ""));
//                        }
//                    }
//
//                    attributeVO = valueToAttributeVO(rootAttribute, castedValues);
//                }
//            } else {
//            	DataModelDbColumnVO dbColumnInfoVO = dbColumnInfoVOMap.get(id.toLowerCase());
//                Object value = dynamicEntityDaoVO.get(dbColumnInfoVO.getColumnName()); // rdb에서는 lowercase를 하는데 hive 에서는 하지 않음
//
//                if (value != null) {
//                    attributeVO = valueToAttributeVO(rootAttribute, value);
//                }
//            }
//
//            if (attributeVO != null) {
//                convertedMap.put(rootAttribute.getName().toLowerCase(), attributeVO);
//            }
//
//            if (rootAttribute.getHasObservedAt() != null && rootAttribute.getHasObservedAt()) {
//                addObservedAt(dynamicEntityDaoVO, dbColumnInfoVOMap, attributeVO, id);
//            }
//        }
//
//        return convertedMap;
//    }

    private boolean isArrayType(AttributeValueType valueType) {
        return (valueType == AttributeValueType.ARRAY_STRING
                || valueType == AttributeValueType.ARRAY_INTEGER
                || valueType == AttributeValueType.ARRAY_BOOLEAN
                || valueType == AttributeValueType.ARRAY_DOUBLE
                || valueType == AttributeValueType.ARRAY_OBJECT);
    }
    
    private AttributeVO addObservedAt(DynamicEntityDaoVO dynamicEntityDaoVO, Map<String, DataModelDbColumnVO> dbColumnInfoVOMap, AttributeVO attributeVO, String id) {
    	DataModelDbColumnVO dbColumnInfoVO = dbColumnInfoVOMap.get(id + "_" + PropertyKey.OBSERVED_AT.getCode().toLowerCase());
        Object value = dynamicEntityDaoVO.get(dbColumnInfoVO.getColumnName()); // rdb에서는 lowercase를 하는데 hive 에서는 하지 않음
        if(value != null && attributeVO != null) {
        	attributeVO.setObservedAt(new Date(((java.sql.Timestamp)value).getTime()));
        }

        return attributeVO;
    }
    
    
    /**
     * map - arr, arr 형식을  arr -> map 으로 변환 (* congestionIndexPrediction 케이스)
     *
     * @param objectMemberMap
     * @return
     */
//    private Object objectMemberMapToArrayObject(HashMap<String, Object> objectMemberMap){
//
//        List<Object> arrayObject = new ArrayList<>();
//
//        objectMemberMap.forEach((key, obj) -> {
//
//            Object[] objectArr = (Object[]) obj;
//            if(objectArr != null) {
//            	for (int idx = 0; idx < objectArr.length; idx++) {
//
//                    if (idx >= arrayObject.size()) {
//                        HashMap tmp = new HashMap();
//                        tmp.put(key, objectArr[idx]);
//
//                        arrayObject.add(tmp);
//
//                    } else {
//                        HashMap tmp = (HashMap) arrayObject.get(idx);
//                        tmp.put(key, objectArr[idx]);
//
//                        arrayObject.set(idx, tmp);
//                    }
//
//                }
//            }
//        });
//
//        return arrayObject;
//    }
//
//    private Object objectMemberMapToArrayObjectOrObject(HashMap<String, Object> objectMemberMap) {
//        List<Object> arrayObject = new ArrayList<>();
//
//        objectMemberMap.forEach((key, obj) -> {
//            Object[] objectArr = (Object[]) obj;
//
//            if (objectArr != null) {
//                for (int idx = 0; idx < objectArr.length; idx++) {
//
//                    if (idx >= arrayObject.size()) {
//                        HashMap tmp = new HashMap();
//                        tmp.put(key, objectArr[idx]);
//
//                        arrayObject.add(tmp);
//
//                    } else {
//                        HashMap tmp = (HashMap) arrayObject.get(idx);
//                        tmp.put(key, objectArr[idx]);
//
//                        arrayObject.set(idx, tmp);
//                    }
//
//                }
//            }
//        });
//
//        return arrayObject.get(0);
//    }
    
//    /**
//     * attribute 결과를  AttributeVO(*PropertyVO or RelationShipVO) 객체로 변환
//     * @param rootAttribute
//     * @param result
//     * @return
//     */
//    private AttributeVO valueToAttributeVO(Attribute rootAttribute, Object result) {
//
//        AttributeVO attributeVO = null;
//        if (rootAttribute.getAttributeType() == AttributeType.PROPERTY) {
//            PropertyVO propertyVO = new PropertyVO();
//            propertyVO.setValue(result);
//            attributeVO = propertyVO;
//        } else if (rootAttribute.getAttributeType() == AttributeType.RELATIONSHIP) {
//
//            RelationshipVO relationshipVO = new RelationshipVO();
//            relationshipVO.setObject(result);
//            attributeVO = relationshipVO;
//
//        } else if (rootAttribute.getAttributeType() == AttributeType.GEO_PROPERTY) {
//            GeoPropertyVO geoPropertyVO = new GeoPropertyVO();
//            geoPropertyVO.setValue(result);
//            attributeVO = geoPropertyVO;
//        }
//
//        return attributeVO;
//    }
//    
    /**
     * lastN 옵션 처리, 시간 내 시간 속성의 마지막 N개의 타임스템프에 해당하는 속성을 내림차순으로 리턴
     *
     * The lastN parameter refers to a number, n, of Attribute instances which shall correspond
     * to the last n timestamps (in descending ordering) of the temporal property (by default observedAt)
     * within the concerned temporal interval.
     * @param commonEntityVO
     * @param lastN
     * @return
     */
    private CommonEntityFullVO retrieveLastN(CommonEntityFullVO commonEntityVO, Integer lastN) {

        for (String key : commonEntityVO.keySet()) {

            //key가 @context의 경우, 처리하지 않음
            if (key.equalsIgnoreCase(DefaultAttributeKey.CONTEXT.getCode()))
                continue;

            List list = (List) commonEntityVO.get(key);
            if (list.size() > lastN) {
                list = list.subList(0, lastN);
            }

            Collections.sort(list, new ObservedAtReverseOrder());
            commonEntityVO.replace(key, list);

        }


        return commonEntityVO;
    }
    
    
    /**
     * FullRepresentationVO 필수 설정 (id, type, createAt, modifiedAt)
     * FullRepresentationVO 선택 설정 (datasetId)
     */
    private CommonEntityFullVO initFullRepresentationVO(DynamicEntityDaoVO dynamicEntityDaoVO, DataModelCacheVO entitySchemaCacheVO) {
        CommonEntityFullVO commonEntityFullVO = new CommonEntityFullVO();

        commonEntityFullVO.setId(dynamicEntityDaoVO.getId());
        commonEntityFullVO.setType(entitySchemaCacheVO.getDataModelVO().getType());

        if (dynamicEntityDaoVO.containsKey(DefaultDbColumnName.CREATED_AT.getCode())) {
            commonEntityFullVO.setCreatedAt((Date) dynamicEntityDaoVO.get(DefaultDbColumnName.CREATED_AT.getCode()));
        }
        if (dynamicEntityDaoVO.containsKey(DefaultDbColumnName.MODIFIED_AT.getCode())) {
            commonEntityFullVO.setModifiedAt((Date) dynamicEntityDaoVO.get(DefaultDbColumnName.MODIFIED_AT.getCode()));
        }

        if (UseYn.YES.getCode().equals(retrieveIncludeDatasetid) 
        		&& dynamicEntityDaoVO.containsKey(DefaultDbColumnName.DATASET_ID.getCode())) {
            commonEntityFullVO.setDatasetId((String) dynamicEntityDaoVO.get(DefaultDbColumnName.DATASET_ID.getCode()));
        }
        return commonEntityFullVO;
    }
    
    
    /**
     * DB 조회 결과(dao) -> Map(attribute)으로 전환
     * @param dynamicEntityDaoVO
     * @param storageMetadataVO
     * @param rootAttribute
     * @param hierarchyAttrNames
     * @param includeSysAttrs
     * @return
     */
    private Map<String, AttributeVO> converDaoToAttribute(DynamicEntityDaoVO dynamicEntityDaoVO, 
    		DataModelStorageMetadataVO storageMetadataVO, Attribute rootAttribute, List<String> hierarchyAttrNames, boolean includeSysAttrs) {
        return converDaoToAttribute(dynamicEntityDaoVO, storageMetadataVO, rootAttribute, hierarchyAttrNames, false, includeSysAttrs);
    }
    
    /**
     * DB조회 결과 DaoVO 를 entity조회 응답 형태로 변환
     * @param dynamicEntityDaoVO
     * @param storageMetadataVO
     * @param rootAttribute
     * @param hierarchyAttrNames
     * @param isChildAttribute
     * @param includeSysAttrs
     * @return
     */
    private Map<String, AttributeVO> converDaoToAttribute(DynamicEntityDaoVO dynamicEntityDaoVO, DataModelStorageMetadataVO storageMetadataVO,
    		Attribute rootAttribute, List<String> hierarchyAttrNames, boolean isChildAttribute, boolean includeSysAttrs) {

    	if(hierarchyAttrNames == null) {
    		hierarchyAttrNames = new ArrayList<>();
    	}
    	hierarchyAttrNames.add(rootAttribute.getName());

    	String currentAttrName = hierarchyAttrNames.get(hierarchyAttrNames.size()-1);

        // 1. Property 값 설정
        Map<String, AttributeVO> convertedMap = new LinkedHashMap<>();
        AttributeVO attributeVO = null;
        if (rootAttribute.getValueType() == AttributeValueType.GEO_JSON) {
        	List<String> columnNames = dataModelManager.getColumnNamesByStorageMetadata(storageMetadataVO, hierarchyAttrNames);
        	if(columnNames != null) {
        		for(String columnName : columnNames) {
        			if(columnName.endsWith(Constants.COLUMN_DELIMITER + DEFAULT_SRID)) {
//        				PGgeometry pGgeometry = (PGgeometry) dynamicEntityDaoVO.get(columnName.toLowerCase());
//                        if(pGgeometry != null) {
//                            attributeVO = valueToAttributeVO(rootAttribute, pGgeometry.getGeometry());
//                            break;
//                        }
                        
                        Object geometry = dynamicEntityDaoVO.get(columnName.toLowerCase());
                        
                    	
	            		if (geometry != null) {
	                        Map<String, Object> convertedGeometry = null;
	            		    try {
	                            convertedGeometry = objectMapper.readValue((String) geometry, Map.class);
	            		    } catch (JsonProcessingException e) {
	                            log.error("Error while parsing geometry", e);
	                        }
	                        attributeVO = valueToAttributeVO(rootAttribute, convertedGeometry.get("geometry"));
						}
        			}
        		}
        	}

        } else {
        	String columnName = dataModelManager.getColumnNameByStorageMetadata(storageMetadataVO, hierarchyAttrNames);
        	if(columnName != null) {
        		Object value = dynamicEntityDaoVO.get(columnName.toLowerCase());
        		
                if (!ObjectUtils.isEmpty(value)) {
                    
	        		if (rootAttribute.getValueType() != null && isArrayType(rootAttribute.getValueType())) {
	        			// Hive JDBC 에서는 Array 타입의 조회를 지원하지 않고, String 한줄로 리턴하기 때문에 파싱 및 캐스팅 작업이 필요
	        			String[] values = ((String) value).replaceAll("\\[", "").replaceAll("\\]", "").split(",");
//	        			String[] values = ((String) value).replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
	        		
	        			List <Object> convertedValues = new ArrayList ();
	        			
	        			for (String eachValue : values) {
	                      if (rootAttribute.getValueType() == AttributeValueType.ARRAY_INTEGER) {
	                    	  convertedValues.add(Integer.parseInt(eachValue));
	                      } else if (rootAttribute.getValueType() == AttributeValueType.ARRAY_DOUBLE) {
	                    	  convertedValues.add(Double.parseDouble(eachValue));
	                      } else if (rootAttribute.getValueType() == AttributeValueType.ARRAY_BOOLEAN) {
	                    	  convertedValues.add(Boolean.parseBoolean(eachValue));
	                      } else if (rootAttribute.getValueType() == AttributeValueType.ARRAY_STRING) {
	                    	  convertedValues.add(eachValue.replace("{", "").replace("}", "").replace("\"", ""));
	                      }
	                  }
	
	                  attributeVO = valueToAttributeVO(rootAttribute, convertedValues);
	               } else {
	            	   attributeVO = valueToAttributeVO(rootAttribute, value);   
	               }
                }
        	}
        }

        if (attributeVO != null) {
        	if(isChildAttribute) {
        		convertedMap.put(rootAttribute.getName(), attributeVO);
        	} else {
        		convertedMap.put(hierarchyAttrNames.get(hierarchyAttrNames.size()-1), attributeVO);
        	}
        }

        // 2. ObjectMember 값 설정
        List<ObjectMember> objectMembers = rootAttribute.getObjectMembers();
        if (objectMembers != null) {

        	Object objectMemberMap = objectMemberToObject(rootAttribute, dynamicEntityDaoVO, storageMetadataVO, hierarchyAttrNames);
        	if(objectMemberMap != null) {
        		if(convertedMap.get(currentAttrName) != null) {
            		convertedMap.get(currentAttrName).put(PropertyKey.TYPE.getCode(), AttributeType.PROPERTY.getCode());
            		convertedMap.get(currentAttrName).put(PropertyKey.VALUE.getCode(), objectMemberMap);
            	} else {
            		convertedMap.put(currentAttrName, valueToAttributeVO(rootAttribute, objectMemberMap));
            	}
        	}
        }

        // 3. observedAt 값 설정
        if (rootAttribute.getHasObservedAt() != null && rootAttribute.getHasObservedAt()) {
        	if(convertedMap.get(rootAttribute.getName()) != null) {
        		addObservedAt(dynamicEntityDaoVO, storageMetadataVO, convertedMap.get(rootAttribute.getName()), hierarchyAttrNames);
        	}
        }
        
        // 4. unitCode 값 설정
        if (rootAttribute.getHasUnitCode() != null && rootAttribute.getHasUnitCode()) {
        	if(convertedMap.get(rootAttribute.getName()) != null) {
        		addUnitCode(dynamicEntityDaoVO, storageMetadataVO, convertedMap.get(rootAttribute.getName()), hierarchyAttrNames);
        	}
        }
        
        // 5. sysAttrs (createdAt, modifiedAt) 값 설정
        if (includeSysAttrs) {
        	if(convertedMap.get(rootAttribute.getName()) != null) {
        		addCreatedAt(dynamicEntityDaoVO, storageMetadataVO, convertedMap.get(rootAttribute.getName()), hierarchyAttrNames);
        	}
        	if(convertedMap.get(rootAttribute.getName()) != null) {
        		addModifiedAt(dynamicEntityDaoVO, storageMetadataVO, convertedMap.get(rootAttribute.getName()), hierarchyAttrNames);
        	}
        }
        
        // 6. Child Attribute 값 설정
        List<Attribute> childAttributes = rootAttribute.getChildAttributes();
        if (childAttributes != null) {

            Map<String, AttributeVO> childAttributeMap = new LinkedHashMap<>();

            for (Attribute childAttribute : childAttributes) {
                Map<String, AttributeVO> subChildAttributeMap = (Map<String, AttributeVO>) converDaoToAttribute(dynamicEntityDaoVO, storageMetadataVO, childAttribute,
                        new ArrayList(hierarchyAttrNames), true, includeSysAttrs);
                childAttributeMap.putAll(subChildAttributeMap);
            }
            if(convertedMap.get(currentAttrName) != null) {
        		convertedMap.get(currentAttrName).putAll(childAttributeMap);
        	} else {
        		AttributeVO childAttributeVO = new AttributeVO();
        		childAttributeVO.putAll(childAttributeMap);
        		convertedMap.put(currentAttrName, childAttributeVO);
        	}
        }
        
        return convertedMap;
    }
    
    /**
     * AttributeVO에 observedAt 값 세팅
     * @param dynamicEntityDaoVO
     * @param storageMetadataVO
     * @param attributeVO
     * @param hierarchyAttrNames
     */
	private void addObservedAt(DynamicEntityDaoVO dynamicEntityDaoVO, DataModelStorageMetadataVO storageMetadataVO, AttributeVO attributeVO, List<String> hierarchyAttrNames) {
		List<String> observedAttAtrNames = new ArrayList<>(hierarchyAttrNames);
		observedAttAtrNames.add(PropertyKey.OBSERVED_AT.getCode());
    	String columnName = dataModelManager.getColumnNameByStorageMetadata(storageMetadataVO, observedAttAtrNames);
        Object value = dynamicEntityDaoVO.get(columnName.toLowerCase());
    	attributeVO.setObservedAt((Date) value);
    }
	
    /**
     * AttributeVO에 unitCode 값 세팅
     * @param dynamicEntityDaoVO
     * @param storageMetadataVO
     * @param attributeVO
     * @param hierarchyAttrNames
     */
	private void addUnitCode(DynamicEntityDaoVO dynamicEntityDaoVO, DataModelStorageMetadataVO storageMetadataVO, AttributeVO attributeVO, List<String> hierarchyAttrNames) {
		List<String> unitCodeAttrNames = new ArrayList<>(hierarchyAttrNames);
		unitCodeAttrNames.add(PropertyKey.UNIT_CODE.getCode());
		String columnName = dataModelManager.getColumnNameByStorageMetadata(storageMetadataVO, unitCodeAttrNames);
        Object value = dynamicEntityDaoVO.get(columnName.toLowerCase());
       	attributeVO.setUnitCode((String) value);
    }
	
    /**
     * AttributeVO에 createdAt 값 세팅
     * @param dynamicEntityDaoVO
     * @param storageMetadataVO
     * @param attributeVO
     * @param hierarchyAttrNames
     */
	private void addCreatedAt(DynamicEntityDaoVO dynamicEntityDaoVO, DataModelStorageMetadataVO storageMetadataVO, AttributeVO attributeVO, List<String> hierarchyAttrNames) {
		List<String> createdAtAttrNames = new ArrayList<>(hierarchyAttrNames);
		createdAtAttrNames.add(PropertyKey.CREATED_AT.getCode());
		String columnName = dataModelManager.getColumnNameByStorageMetadata(storageMetadataVO, createdAtAttrNames);
        Object value = dynamicEntityDaoVO.get(columnName.toLowerCase());
    	attributeVO.setCreatedAt((Date) value);
    }
	
    /**
     * AttributeVO에 modifedAt 값 세팅
     * @param dynamicEntityDaoVO
     * @param storageMetadataVO
     * @param attributeVO
     * @param hierarchyAttrNames
     */
	private void addModifiedAt(DynamicEntityDaoVO dynamicEntityDaoVO, DataModelStorageMetadataVO storageMetadataVO, AttributeVO attributeVO, List<String> hierarchyAttrNames) {
		List<String> modifiedAtAttrNames = new ArrayList<>(hierarchyAttrNames);
		modifiedAtAttrNames.add(PropertyKey.MODIFIED_AT.getCode());
		String columnName = dataModelManager.getColumnNameByStorageMetadata(storageMetadataVO, modifiedAtAttrNames);
        Object value = dynamicEntityDaoVO.get(columnName.toLowerCase());
    	attributeVO.setModifiedAt((Date) value);
    }
	
	/**
     * DB조회 결과 daoVO 를 entity조회 결과로 응답할 ObjectMember 형태로 변환
     * @param rootAttribute
     * @param dynamicEntityDaoVO
     * @param storageMetadataVO
     * @param hierarchyAttrNames
     * @return
     */
    private Object objectMemberToObject(Attribute rootAttribute, DynamicEntityDaoVO dynamicEntityDaoVO, DataModelStorageMetadataVO storageMetadataVO, List<String> hierarchyAttrNames) {

    	List<ObjectMember> objectMembers = rootAttribute.getObjectMembers();

    	Map<String, Object> objectMemberMap = objectMembersToMap(objectMembers, dynamicEntityDaoVO, storageMetadataVO, hierarchyAttrNames);

        if (rootAttribute.getValueType() == AttributeValueType.ARRAY_OBJECT) {
//        	return objectMemberMap;
            return objectMemberMapToArrayObject(objectMemberMap, rootAttribute);
        } else {
            return objectMemberMap;
        }
    }
    
    /**
     * DB조회 결과 daoVO 를 entity조회 결과로 응답할 ObjectMember 형태로 변환
     * @param objectMembers
     * @param dynamicEntityDaoVO
     * @param storageMetadataVO
     * @param hierarchyAttrNames
     * @return
     */
    private Map<String, Object> objectMembersToMap(List<ObjectMember> objectMembers,
    		DynamicEntityDaoVO dynamicEntityDaoVO, DataModelStorageMetadataVO storageMetadataVO, List<String> hierarchyAttrNames) {

    	Map<String, Object> objectMemberMap = null;
    	for (ObjectMember objectMember : objectMembers) {
    		String objectMemberName = objectMember.getName();
    		
    		List<String> objectMemberAttrNames = new ArrayList<>(hierarchyAttrNames);
    		objectMemberAttrNames.add(objectMember.getName());
    		
            if(objectMember.getObjectMembers() != null) {
            	Map<String, Object> innerMap = objectMembersToMap(objectMember.getObjectMembers(), dynamicEntityDaoVO, storageMetadataVO, objectMemberAttrNames);
            	if(innerMap != null && !innerMap.isEmpty()) {
            		if(objectMemberMap == null) objectMemberMap = new HashMap<>();
            		objectMemberMap.put(objectMemberName, innerMap);
            	}
            } else {
            	String columnName = dataModelManager.getColumnNameByStorageMetadata(storageMetadataVO, objectMemberAttrNames);
                Object value = dynamicEntityDaoVO.get(columnName);
                if(!ValidateUtil.isEmptyData(value)) {
                	if(objectMemberMap == null) objectMemberMap = new HashMap<>();
                	objectMemberMap.put(objectMemberName, value);
                }
            }
        }
    	return objectMemberMap;
	}
    
    /**
     * attribute 결과를  AttributeVO(*PropertyVO or RelationShipVO) 객체로 변환
     * @param attribute
     * @param result
     * @return
     */
    private AttributeVO valueToAttributeVO(Attribute attribute, Object result) {

        AttributeVO attributeVO = null;
        if (attribute.getAttributeType() == AttributeType.PROPERTY) {
            PropertyVO propertyVO = new PropertyVO();
            propertyVO.setValue(result);
            attributeVO = propertyVO;
        } else if (attribute.getAttributeType() == AttributeType.RELATIONSHIP) {

            if (attribute.getValueType() == AttributeValueType.ARRAY_STRING) {
                RelationshipsVO relationshipsVO = new RelationshipsVO();
                relationshipsVO.setObjects(result);
                attributeVO = relationshipsVO;
            } else {
                RelationshipVO relationshipVO = new RelationshipVO();
                relationshipVO.setObject(result);
                attributeVO = relationshipVO;
            }

        } else if (attribute.getAttributeType() == AttributeType.GEO_PROPERTY) {
            GeoPropertyVO geoPropertyVO = new GeoPropertyVO();
            geoPropertyVO.setValue(result);
            attributeVO = geoPropertyVO;
        }

        return attributeVO;
    }

    /**
     * map - arr, arr 형식을  arr -> map 으로 변환 (* congestionIndexPrediction 케이스)
     *
     * @param objectMemberMap
     * @return
     */
    private List<Map<String, Object>> objectMemberMapToArrayObject(Map<String, Object> objectMemberMap, Attribute rootAttribute){

        List<Map<String, Object>> arrayObject = new ArrayList<>();
        
        
        if(objectMemberMap != null && !objectMemberMap.isEmpty()){
        for (Map.Entry<String, Object> entry : objectMemberMap.entrySet()) {
//        objectMemberMap.forEach((key, obj) -> {
        	String[] values = ((String) entry.getValue()).replaceAll("\\[", "").replaceAll("\\]", "").split(",");
        	if (values != null) {
        		
        		List <Object> convertedValues = new ArrayList ();
        		
        		for (String eachValue : values) {
                    if (rootAttribute.getChildAttribute(entry.getKey()).getValueType() == AttributeValueType.INTEGER) {
                    	convertedValues.add(Integer.parseInt(eachValue));
                    } else if (rootAttribute.getChildAttribute(entry.getKey()).getValueType() == AttributeValueType.DOUBLE) {
                    	convertedValues.add(Double.parseDouble(eachValue));
                    } else if (rootAttribute.getChildAttribute(entry.getKey()).getValueType() == AttributeValueType.BOOLEAN) {
                    	convertedValues.add(Boolean.parseBoolean(eachValue));
                    } else if (rootAttribute.getChildAttribute(entry.getKey()).getValueType() == AttributeValueType.STRING) {
                    	convertedValues.add(eachValue.replace("{", "").replace("}", "").replace("\"", ""));
                    } else if (rootAttribute.getChildAttribute(entry.getKey()).getValueType() == AttributeValueType.DATE) {
                    	try {
							convertedValues.add(DateUtil.convertHiveTsToDate(eachValue));
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							continue;
						}
                    }
        		}
                    
//                  Object[] objectArr = (Object[]) obj;
                    if(convertedValues != null && convertedValues.size() > 0) {
                    	for (int idx = 0; idx < convertedValues.size(); idx++) {
                    		if(idx >= arrayObject.size()) {
                    			Map<String, Object> tmp = new HashMap<>();
                      		  tmp.put(entry.getKey(), convertedValues.get(idx));
                      		arrayObject.add(tmp);
                    		}
                    		else {
                              	Map<String, Object> tmp = (Map<String, Object>) arrayObject.get(idx);
                                  tmp.put(entry.getKey(), convertedValues.get(idx));
                                  arrayObject.set(idx, tmp);
                    		}
                      	  }
                        }
                      	}
      
        }
    }


        if(arrayObject.size() == 0) {
        	return null;
        }
        return arrayObject;
    }
    
    /**
     * FullRepresentationVO 필수 설정 (id, type, createAt, modifiedAt)
     * FullRepresentationVO 선택 설정 (datasetId)
     */
    private CommonEntityFullVO addDefaultFullRepresentationField(CommonEntityFullVO commonEntityFullVO,
                                                        DynamicEntityDaoVO dynamicEntityDaoVO,
                                                        DataModelCacheVO dataModelCacheVO,
                                                        boolean includeSysAttrs) {

        commonEntityFullVO.setId(dynamicEntityDaoVO.getId());
        commonEntityFullVO.setType(dataModelCacheVO.getDataModelVO().getType());

        if(includeSysAttrs) {
        	if (dynamicEntityDaoVO.containsKey(DefaultDbColumnName.CREATED_AT.getCode())) {
        		commonEntityFullVO.setCreatedAt((Date) dynamicEntityDaoVO.get(DefaultDbColumnName.CREATED_AT.getCode()));
            }
            if (dynamicEntityDaoVO.containsKey(DefaultDbColumnName.MODIFIED_AT.getCode())) {
        		commonEntityFullVO.setModifiedAt((Date) dynamicEntityDaoVO.get(DefaultDbColumnName.MODIFIED_AT.getCode()));
            }
        }
        // 전체 entity 조회 시 정렬에 사용 (modifiedAt파라미터는 sysAttrs에 따라 미 존재할 수도 있기 때문에 하기 정렬 키를 사용)
        if (dynamicEntityDaoVO.containsKey(DefaultDbColumnName.MODIFIED_AT.getCode())) {
    		commonEntityFullVO.setSortKey((Date) dynamicEntityDaoVO.get(DefaultDbColumnName.MODIFIED_AT.getCode()));
        }

        if (UseYn.YES.getCode().equals(retrieveIncludeDatasetid)
        		&& dynamicEntityDaoVO.containsKey(DefaultDbColumnName.DATASET_ID.getCode())) {
    		commonEntityFullVO.setDatasetId((String) dynamicEntityDaoVO.get(DefaultDbColumnName.DATASET_ID.getCode()));
        }
        return commonEntityFullVO;
    }
}