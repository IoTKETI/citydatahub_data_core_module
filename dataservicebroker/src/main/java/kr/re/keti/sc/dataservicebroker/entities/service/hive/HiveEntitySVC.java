package kr.re.keti.sc.dataservicebroker.entities.service.hive;

import static kr.re.keti.sc.dataservicebroker.common.code.Constants.DEFAULT_SRID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

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
import kr.re.keti.sc.dataservicebroker.common.vo.AttributeVO;
import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityFullVO;
import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityVO;
import kr.re.keti.sc.dataservicebroker.common.vo.EntityProcessVO;
import kr.re.keti.sc.dataservicebroker.common.vo.GeoPropertyVO;
import kr.re.keti.sc.dataservicebroker.common.vo.ProcessResultVO;
import kr.re.keti.sc.dataservicebroker.common.vo.PropertyVO;
import kr.re.keti.sc.dataservicebroker.common.vo.RelationshipVO;
import kr.re.keti.sc.dataservicebroker.common.vo.entities.DynamicEntityDaoVO;
import kr.re.keti.sc.dataservicebroker.common.vo.entities.DynamicEntityFullVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.Attribute;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelCacheVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelDbColumnVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.ObjectMember;
import kr.re.keti.sc.dataservicebroker.entities.dao.EntityDAOInterface;
import kr.re.keti.sc.dataservicebroker.entities.service.DefaultEntitySVC;
import kr.re.keti.sc.dataservicebroker.util.DateUtil;
import kr.re.keti.sc.dataservicebroker.util.ObservedAtReverseOrder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HiveEntitySVC extends DefaultEntitySVC {

	@Value("${entity.retrieve.include.datasetid:N}")
	private String retrieveIncludeDatasetid; // ?????? ??? datasetId ????????????
	@Value("${entity.default.history.store.type:full}")
	private String defaultHistoryStoreType; // ????????? ??? ????????? ?????? ?????? ?????? ?????? ?????? ??????

    @Override
	protected String getTableName(DataModelCacheVO dataModelCacheVO) {
		return dataModelCacheVO.getDataModelStorageMetadataVO().getHiveTableName();
	}
    
    @Override
    protected BigDataStorageType getStorageType() {
    	return BigDataStorageType.HIVE;
    }

    @Override
	public void setEntityDAOInterface(EntityDAOInterface<DynamicEntityDaoVO> entityDAO) {
    	this.entityDAO = entityDAO;
	}

    /**
     * Operation ???????????? ?????? ?????? ??????
     *
     * @param entityProcessVOList ??????VO?????????
     */
    @Override
    public void storeEntityStatusHistory(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> entityProcessVOList) {
        // 1. ???????????? ?????? ????????? ?????? ??????
        List<DynamicEntityDaoVO> createPartialHistoryVOList = new ArrayList<>();
        List<DynamicEntityDaoVO> createFullHistoryTargetVOList = new ArrayList<>();

        StringBuilder logMessage = new StringBuilder();

        for (EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO : entityProcessVOList) {

            DynamicEntityDaoVO entityDaoVO = entityProcessVO.getEntityDaoVO();
            ProcessResultVO processResultVO = entityProcessVO.getProcessResultVO();

            // 2. ?????? ????????? ?????????????????? ??????
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

            // 3. ?????? ????????? Operation ??? ??????
            entityDaoVO.setOperation(processResultVO.getProcessOperation());

            // 4. ???????????? ?????? ??????
            HistoryStoreType historyStoreType = null;
            if(entityProcessVO.getDatasetId() != null) {
            	historyStoreType = dataModelManager.getHistoryStoreType(entityProcessVO.getDatasetId());
            } else {
            	historyStoreType = HistoryStoreType.parseType(defaultHistoryStoreType);
            }

            // PARTIAL, FULL ?????? ??????
            if(historyStoreType == HistoryStoreType.ALL) {
            	createFullHistoryTargetVOList.add(entityDaoVO);
                createPartialHistoryVOList.add(entityDaoVO);
            // FULL ????????? ??????
            } else if(historyStoreType == HistoryStoreType.FULL) {
            	createFullHistoryTargetVOList.add(entityDaoVO);
            // PARTIAL ????????? ??????
            } else if(historyStoreType == HistoryStoreType.PARTIAL) {
            	createPartialHistoryVOList.add(entityDaoVO);
            // ?????? ???????????? ??????
            } else if(historyStoreType == HistoryStoreType.NONE) {
            	
            } else {
            	// default 'PARTIAL'
            	createFullHistoryTargetVOList.add(entityDaoVO);
            }
        }

        if (logMessage.length() > 0) log.info(logMessage.toString());

        // 5. ?????? ?????? ??????
        // 5-1 Partial ?????? ?????? (???????????? Entity ?????????????????? ??????)
        if (createPartialHistoryVOList != null && createPartialHistoryVOList.size() > 0) {
            try {
            	entityDAO.bulkCreateHist(createPartialHistoryVOList);
            } catch (Exception e) {
                log.error("Store entity PARTIAL history error", e);
            }
        }

        // 5-2. Full ?????? ?????? (Entity ??? ????????? ?????? ??? ????????? ?????? ??????????????? ??????)
        /*
        if (createFullHistoryTargetVOList != null && createFullHistoryTargetVOList.size() > 0) {
            List<DynamicEntityDaoVO> createFullHistoryVOList = new ArrayList<>();
            // Full ??????????????? ?????? Entity ????????? ??????
            for (DynamicEntityDaoVO entityDaoVO : createFullHistoryTargetVOList) {

                // Delete ??? Full ?????? ???????????? ??????
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

                // DB????????? ?????? Array ??? Geo?????? ????????? ?????? ??????
                // lower case -> camel(?????? ??????)?????? ?????? (TODO: ??????)
                Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = entityDaoVO.getDbColumnInfoVOMap();
                for (Map.Entry<String, DataModelDbColumnVO> entry : dbColumnInfoVOMap.entrySet()) {
                    String columnName = entry.getKey().toLowerCase();
                    DataModelDbColumnVO dbColumnInfoVO = entry.getValue();
                    Object value = entityFullDaoVO.get(columnName.toLowerCase());
                    if (value != null) {
                        // String[] -> List<String>, Integer[] -> List<Integer>, Float[] -> List<Float> ????????????
                        if (dbColumnInfoVO.getColumnType() == DbColumnType.ARRAY_VARCHAR
                                || dbColumnInfoVO.getColumnType() == DbColumnType.ARRAY_INTEGER
                                || dbColumnInfoVO.getColumnType() == DbColumnType.ARRAY_FLOAT) {
                            Object[] valueArr = (Object[]) value;
                            List<Object> valueList = new ArrayList<>(valueArr.length);
                            for (int i = 0; i < valueArr.length; i++) {
                                valueList.add(valueArr[i]);
                            }
                            entityFullDaoVO.put(columnName, valueList);

                            // Timestamp[] ??? List<Date> ????????????
                        } else if (dbColumnInfoVO.getColumnType() == DbColumnType.ARRAY_TIMESTAMP) {
                            Object[] dateArr = (Object[]) value;
                            List<Date> dateList = new ArrayList<>(dateArr.length);
                            for (int i = 0; i < dateArr.length; i++) {
                                dateList.add((Date) dateArr[i]);

                            }
                            entityFullDaoVO.put(columnName, dateList);

                            // PGgeometry -> GeoJson ????????????
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
    	CommonEntityFullVO commonEntityFullVO = initFullRepresentationVO(dynamicEntityDaoVO, dataModelCacheVO);

        Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = dataModelCacheVO.getDataModelStorageMetadataVO().getDbColumnInfoVOMap();

        for (DataModelDbColumnVO dbColumnInfoVO : dbColumnInfoVOMap.values()) {
            String columnName = dbColumnInfoVO.getColumnName();
            Object attributeValue = dynamicEntityDaoVO.get(columnName); // rdb????????? lowercase ??? ?????????, hive????????? ?????? ??????
            List<String> attributeIds = dbColumnInfoVO.getHierarchyAttributeIds();

            //?????? attributeId ????????????
            String rootAttrId = attributeIds.get(0);
            Attribute rootAttribute = dataModelCacheVO.getRootAttribute(rootAttrId);

            //?????? ????????? SKIP
            // 1. DB?????? ????????? attribute value??? ?????? ??????, SKIP
            if (attributeValue == null || commonEntityFullVO.containsKey(rootAttrId)) {
                continue;
            }

            HashMap<String, AttributeVO> resultMap = (HashMap<String, AttributeVO>) convertDaoToAttribute(dynamicEntityDaoVO, dbColumnInfoVOMap, rootAttribute, null);
            commonEntityFullVO.putAll(resultMap);
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


            //observedAt??? ?????? ?????? ??????!
            for (String key : commonEntityVO.keySet()) {

                Attribute rootAttribute = dataModelCacheVO.getRootAttribute(key);
                if (rootAttribute == null || rootAttribute.getHasObservedAt() == null || !rootAttribute.getHasObservedAt()) {
                    observedAtEntityVO.remove(key);
                }
            }


            if (filterdMap.containsKey(id)) {
                CommonEntityVO innerCommonEntityVO = filterdMap.get(id);
                for (String key : observedAtEntityVO.keySet()) {

                    if (innerCommonEntityVO.get(key) instanceof List) {
                        // ??? ???????????? ???????????? ?????? ??????
                        AttributeVO attributeVO = (AttributeVO) observedAtEntityVO.get(key);
                        List<AttributeVO> attributeVOList = (List<AttributeVO>) innerCommonEntityVO.get(key);
                        attributeVOList.add(attributeVO);
                        observedAtEntityVO.replace(key, attributeVOList);

                    } else if (innerCommonEntityVO.get(key) instanceof AttributeVO) {

                        // ???????????? ???????????? ??? ??????
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

            // lastN ?????? ??????
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
    	throw new UnsupportedOperationException("HiveEntitySVC not supported 'daoVOToTemporalNormalizedHistoryRepresentationVO'");
    }

    @Override
    public List<CommonEntityVO> daoVOToTemporalTemporalValuesRepresentationVO(List<CommonEntityVO> commonEntityVOList) {
        throw new UnsupportedOperationException("HiveEntitySVC not supported 'daoVOToTemporalTemporalValuesRepresentationVO'");
    }



    /**
     * DB ?????? ??????(dao) -> Map(attribute)?????? ??????
     *
     * @param upperId
     * @param rootAttribute
     * @param dbColumnInfoVOMap
     * @param dynamicEntityDaoVO
     * @return
     */
    private Map convertDaoToAttribute(DynamicEntityDaoVO dynamicEntityDaoVO, Map<String, DataModelDbColumnVO> dbColumnInfoVOMap, Attribute rootAttribute, String upperId) {
        String id;
        if (upperId != null) {
            id = upperId + "_" + rootAttribute.getName();
        } else {
            id = rootAttribute.getName();
        }

        List<Attribute> hasAttributes = rootAttribute.getChildAttributes();
        List<ObjectMember> objectMembers = rootAttribute.getObjectMembers();
        HashMap<String, Object> convertedMap = new HashMap<>();

        if (hasAttributes != null) {

            HashMap<String, Object> childAttributeMap = new HashMap<>();
            for (Attribute hasAttribute : hasAttributes) {
                //?????? value -> AttributeVO??? ??????
                HashMap<String, Object> subChildAttributeMap = (HashMap<String, Object>) convertDaoToAttribute(dynamicEntityDaoVO, dbColumnInfoVOMap, hasAttribute, id);
                childAttributeMap.putAll(subChildAttributeMap);
            }

            DataModelDbColumnVO dbColumnInfoVO = dbColumnInfoVOMap.get(id);
            if(dbColumnInfoVO != null) {
            	Object value = dynamicEntityDaoVO.get(dbColumnInfoVO.getColumnName()); // rdb????????? lowercase??? ????????? hive ????????? ?????? ??????

                AttributeVO attributeVO = null;
                if (rootAttribute.getAttributeType() == AttributeType.PROPERTY && value != null) {

                    PropertyVO propertyVO = new PropertyVO();
                    propertyVO.setValue(value);
                    attributeVO = propertyVO;

                    childAttributeMap.put(DataServiceBrokerCode.AttributeResultType.VALUE.getCode(), value);

                } else if (rootAttribute.getAttributeType() == AttributeType.RELATIONSHIP && value != null) {
                    RelationshipVO relationshipVO = new RelationshipVO();
                    relationshipVO.setObject(value);
                    attributeVO = relationshipVO;
                    childAttributeMap.put(DataServiceBrokerCode.AttributeResultType.OBJECT.getCode(), value);
                } else {
                    attributeVO = new AttributeVO();
                }

                attributeVO.putAll(childAttributeMap);
                convertedMap.put(id, attributeVO);
            }
        }

        if (objectMembers != null) {

            HashMap<String, Object> objectMemberMap = new HashMap<>();
            for (ObjectMember objectMember : objectMembers) {

                String objectMemberId = objectMember.getName();
                DataModelDbColumnVO vo = dbColumnInfoVOMap.get(id + "_" + objectMemberId);
                Object value = dynamicEntityDaoVO.get(vo.getColumnName()); // rdb?????? lowercase??? ??????????????? hive????????? ?????? ??????
                objectMemberMap.put(objectMemberId, value);
            }

            AttributeVO attributeVO;
            if (rootAttribute.getValueType() == AttributeValueType.ARRAY_OBJECT) {
            	// TODO: Hive?????? ????????? ?????? Array???????????? ?????????, String???????????? ?????? ?????????. ?????? ??????
//                attributeVO = valueToAttributeVO(rootAttribute, objectMemberMapToArrayObject(objectMemberMap));
            	attributeVO = null;
            	
            	
            } else {
                attributeVO = valueToAttributeVO(rootAttribute, objectMemberMap);
            }
            
            if (rootAttribute.getHasObservedAt() != null && rootAttribute.getHasObservedAt()) {
                attributeVO = addObservedAt(dynamicEntityDaoVO, dbColumnInfoVOMap, attributeVO, id);
            }
            convertedMap.put(id, attributeVO);

        }

        if (rootAttribute.getObjectMembers() == null && rootAttribute.getChildAttributes() == null) {

            AttributeVO attributeVO = null;

            if (rootAttribute.getValueType() == AttributeValueType.GEO_JSON) {
            	DataModelDbColumnVO dbColumnInfoVO = dbColumnInfoVOMap.get(id + "_" + DEFAULT_SRID);

            	if (dynamicEntityDaoVO.get(dbColumnInfoVO.getColumnName()) != null && dynamicEntityDaoVO.get(dbColumnInfoVO.getColumnName()) instanceof String) {
            	    Object geometry = dynamicEntityDaoVO.get(dbColumnInfoVO.getColumnName());

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
            } else if (isArrayType(rootAttribute.getValueType())) {
                DataModelDbColumnVO dbColumnInfoVO = dbColumnInfoVOMap.get(id);
                String arrayValue = (String) dynamicEntityDaoVO.get(dbColumnInfoVO.getColumnName()); // rdb????????? lowercase??? ????????? hive ????????? ?????? ??????

                if (arrayValue != null) {
                    String[] values = arrayValue.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
                    List<Object> castedValues = new ArrayList<>();

                    for (String value : values) {
                        if (rootAttribute.getValueType() == AttributeValueType.ARRAY_INTEGER) {
                            castedValues.add(Integer.parseInt(value));
                        } else if (rootAttribute.getValueType() == AttributeValueType.ARRAY_DOUBLE) {
                            castedValues.add(Double.parseDouble(value));
                        } else if (rootAttribute.getValueType() == AttributeValueType.ARRAY_BOOLEAN) {
                            castedValues.add(Boolean.parseBoolean(value));
                        } else if (rootAttribute.getValueType() == AttributeValueType.ARRAY_STRING) {
                            castedValues.add(value.replace("{", "").replace("}", "").replace("\"", ""));
                        }
                    }

                    attributeVO = valueToAttributeVO(rootAttribute, castedValues);
                }
            } else {
            	DataModelDbColumnVO dbColumnInfoVO = dbColumnInfoVOMap.get(id);
                Object value = dynamicEntityDaoVO.get(dbColumnInfoVO.getColumnName()); // rdb????????? lowercase??? ????????? hive ????????? ?????? ??????

                if (value != null) {
                    attributeVO = valueToAttributeVO(rootAttribute, value);
                }
            }

            if (attributeVO != null) {
                convertedMap.put(rootAttribute.getName(), attributeVO);
            }

            if (rootAttribute.getHasObservedAt() != null && rootAttribute.getHasObservedAt()) {
                addObservedAt(dynamicEntityDaoVO, dbColumnInfoVOMap, attributeVO, id);
            }
        }

        return convertedMap;
    }

    private boolean isArrayType(AttributeValueType valueType) {
        return (valueType == AttributeValueType.ARRAY_STRING
                || valueType == AttributeValueType.ARRAY_INTEGER
                || valueType == AttributeValueType.ARRAY_BOOLEAN
                || valueType == AttributeValueType.ARRAY_DOUBLE
                || valueType == AttributeValueType.ARRAY_OBJECT);
    }
    
    private AttributeVO addObservedAt(DynamicEntityDaoVO dynamicEntityDaoVO, Map<String, DataModelDbColumnVO> dbColumnInfoVOMap, AttributeVO attributeVO, String id) {
    	DataModelDbColumnVO dbColumnInfoVO = dbColumnInfoVOMap.get(id + "_" + PropertyKey.OBSERVED_AT.getCode());
        Object value = dynamicEntityDaoVO.get(dbColumnInfoVO.getColumnName()); // rdb????????? lowercase??? ????????? hive ????????? ?????? ??????
        if(value != null && attributeVO != null) {
        	attributeVO.setObservedAt(new Date(((java.sql.Timestamp)value).getTime()));
        }

        return attributeVO;
    }
    
    
    /**
     * map - arr, arr ?????????  arr -> map ?????? ?????? (* congestionIndexPrediction ?????????)
     *
     * @param objectMemberMap
     * @return
     */
    private Object objectMemberMapToArrayObject(HashMap<String, Object> objectMemberMap){

        List<Object> arrayObject = new ArrayList<>();

        objectMemberMap.forEach((key, obj) -> {

            Object[] objectArr = (Object[]) obj;
            if(objectArr != null) {
            	for (int idx = 0; idx < objectArr.length; idx++) {

                    if (idx >= arrayObject.size()) {
                        HashMap tmp = new HashMap();
                        tmp.put(key, objectArr[idx]);

                        arrayObject.add(tmp);

                    } else {
                        HashMap tmp = (HashMap) arrayObject.get(idx);
                        tmp.put(key, objectArr[idx]);

                        arrayObject.set(idx, tmp);
                    }

                }
            }
        });

        return arrayObject;
    }
    
    /**
     * attribute ?????????  AttributeVO(*PropertyVO or RelationShipVO) ????????? ??????
     * @param rootAttribute
     * @param result
     * @return
     */
    private AttributeVO valueToAttributeVO(Attribute rootAttribute, Object result) {

        AttributeVO attributeVO = null;
        if (rootAttribute.getAttributeType() == AttributeType.PROPERTY) {
            PropertyVO propertyVO = new PropertyVO();
            propertyVO.setValue(result);
            attributeVO = propertyVO;
        } else if (rootAttribute.getAttributeType() == AttributeType.RELATIONSHIP) {

            RelationshipVO relationshipVO = new RelationshipVO();
            relationshipVO.setObject(result);
            attributeVO = relationshipVO;

        } else if (rootAttribute.getAttributeType() == AttributeType.GEO_PROPERTY) {
            GeoPropertyVO geoPropertyVO = new GeoPropertyVO();
            geoPropertyVO.setValue(result);
            attributeVO = geoPropertyVO;
        }

        return attributeVO;
    }
    
    /**
     * lastN ?????? ??????, ?????? ??? ?????? ????????? ????????? N?????? ?????????????????? ???????????? ????????? ?????????????????? ??????
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

            //key??? @context??? ??????, ???????????? ??????
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
     * FullRepresentationVO ?????? ?????? (id, type, createAt, modifiedAt)
     * FullRepresentationVO ?????? ?????? (datasetId)
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
}