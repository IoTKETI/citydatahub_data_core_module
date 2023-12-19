package kr.re.keti.sc.dataservicebroker.entities.service;

import java.io.IOException;
import java.math. BigDecimal;
import java.text.ParseException;
import java.util.*;

import org.apache.commons.lang.SerializationUtils;
import org.geojson.GeoJsonObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.AttributeType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.AttributeValueType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.BigDataStorageType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.DefaultAttributeKey;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.GeoJsonValueType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.GeometryType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.Operation;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.OperationOption;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.PropertyKey;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.RetrieveOptions;
import kr.re.keti.sc.dataservicebroker.common.exception.BaseException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdBadRequestException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdInternalServerErrorException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdNoExistTypeException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdResourceNotFoundException;
import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityFullVO;
import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityVO;
import kr.re.keti.sc.dataservicebroker.common.vo.EntityProcessVO;
import kr.re.keti.sc.dataservicebroker.common.vo.IngestMessageVO;
import kr.re.keti.sc.dataservicebroker.common.vo.ProcessResultVO;
import kr.re.keti.sc.dataservicebroker.common.vo.QueryVO;
import kr.re.keti.sc.dataservicebroker.common.vo.entities.DynamicEntityDaoVO;
import kr.re.keti.sc.dataservicebroker.common.vo.entities.DynamicEntityFullVO;
import kr.re.keti.sc.dataservicebroker.datamodel.DataModelManager;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.Attribute;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelCacheVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelStorageMetadataVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.ObjectMember;
import kr.re.keti.sc.dataservicebroker.entities.dao.EntityDAOInterface;
import kr.re.keti.sc.dataservicebroker.entities.vo.EntityDataModelVO;
import kr.re.keti.sc.dataservicebroker.util.CommonParamUtil;
import kr.re.keti.sc.dataservicebroker.util.DateUtil;
import kr.re.keti.sc.dataservicebroker.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

/**
 * Entity 공통 서비스 클래스
 */
@Slf4j
public abstract class DefaultEntitySVC implements EntitySVCInterface<DynamicEntityFullVO, DynamicEntityDaoVO> {

    protected abstract String getTableName(DataModelCacheVO dataModelCacheVO);

    protected abstract BigDataStorageType getStorageType();

    public abstract void setEntityDAOInterface(EntityDAOInterface<DynamicEntityDaoVO> entityDAO);

    protected EntityDAOInterface<DynamicEntityDaoVO> entityDAO;
    @Autowired
    protected DataModelManager dataModelManager;
    @Autowired
    protected EntityDataModelSVC entityDataModelSVC;
    @Autowired
    protected ObjectMapper objectMapper;
    @Value("${entity.default.context-uri}")
    private String defaultContextUri;
    @Value("${entity.validation.id-pattern.enabled:true}")
    private Boolean validateIdPatternEnabled;

    /**
     * Entity 데이터 Operation 별 벌크 처리
     *
     * @param requestMessageVOList 요청수신메시지VO리스트
     * @return 처리내역 및 결과포함VO리스트
     */
    @Override
    public List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> processBulk(List<IngestMessageVO> requestMessageVOList) {

        // 1. 요청수신 VO -> 서비스 처리 VO 로 파싱
        List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> processVOList = requestMessageVOToProcessVO(requestMessageVOList);

        // 2. 파라미터 유효성 및 권한 체크
        processValidate(processVOList);

        // 3. Operation 별 벌크 처리
        processOperation(processVOList);

        // 4. Entity 별 DataModel 정보 저장
        storeEntityDataModel(processVOList);

        // 5. Operation 처리 성공 이력 저장
        storeEntityStatusHistory(processVOList);

        return processVOList;
    }

    /**
     * 요청수신 VO -> 실제 서비스 처리 VO 파싱
     *
     * @param requestMessageVOList 요청 수신 VO
     * @return List<OffStreetParkingProcessVO> 서비스 처리 VO 리스트
     */
    @Override
    public List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> requestMessageVOToProcessVO(List<IngestMessageVO> requestMessageVOList) {

        List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> entityProcessVOList = new ArrayList<>();

        for (IngestMessageVO ingestMessageVO : requestMessageVOList) {

            // 1. 서비스 로직 처리 과정 중 값을 담아 처리할 서비스 객체 생성
            EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO = new EntityProcessVO<>();

            String content = ingestMessageVO.getContent();
            Operation opertaion = ingestMessageVO.getOperation();

            // NGSI-LD 규격 처리 시 사용
            String attrId = CommonParamUtil.extractAttrId(opertaion, ingestMessageVO.getTo());

            entityProcessVO.setDatasetId(ingestMessageVO.getDatasetId());
            entityProcessVO.setContent(content);
            entityProcessVO.setOperation(opertaion);
            entityProcessVO.setOperationOptions(ingestMessageVO.getOperationOptions());

            // 2. 수신 content -> entityFullVO 파싱
            DynamicEntityFullVO entityFullVO = null;
            try {

                // Partial Attrbute Update 처리 임시 로직
                // 추가로 Property도 임시임 , geo, rel 분기 해야함
                if (opertaion == Operation.PARTIAL_ATTRIBUTE_UPDATE) {
                    HashMap<String, Object> params = objectMapper.readValue(content, new TypeReference<HashMap<String, Object>>() {});
//        	        params.put(PropertyKey.TYPE.getCode(), DataServiceBrokerCode.AttributeType.PROPERTY.getCode());
//        	        HashMap<String, Object> convertedHashMap = new HashMap<>();
//        	        convertedHashMap.put(attrId, params);
                    content = objectMapper.writeValueAsString(params);
                }

                entityFullVO = deserializeContent(content);

                // eventTime이 없는 경우 수신 시간 설정
                Date eventTime = null;
                if (ingestMessageVO.getIngestTime() != null) {
                    eventTime = ingestMessageVO.getIngestTime();
                } else {
                    eventTime = new Date();
                }
                entityFullVO.setCreatedAt(eventTime);
                entityFullVO.setModifiedAt(eventTime);
                if (entityFullVO.getId() == null && ingestMessageVO.getId() != null) {
                    entityFullVO.setId(ingestMessageVO.getId());
                }
                if (entityFullVO.getType() == null && ingestMessageVO.getEntityType() != null) {
                    entityFullVO.setType(ingestMessageVO.getEntityType());
                }
                entityFullVO.setDatasetId(entityProcessVO.getDatasetId());
                entityProcessVO.setEntityId(entityFullVO.getId());

                // contentType이 application/json인 경우
                if(!ValidateUtil.isEmptyData(ingestMessageVO.getContentType())) {
                    if(ingestMessageVO.getContentType().contains(Constants.APPLICATION_JSON_VALUE)) {
                        // contentType이 application/json인 경우 @context 입력불가
                        if(!ValidateUtil.isEmptyData(entityFullVO.getContext())) {
                            throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                                    "Invalid Request Content. @context parameter cannot be used when contentType=application/json");
                        }

                        if(!ValidateUtil.isEmptyData(ingestMessageVO.getLinks())) {
                            entityFullVO.setContext(ingestMessageVO.getLinks());
                        }

                        // contentType이 application/ld+json인 경우
                    } else if(ingestMessageVO.getContentType().contains(Constants.APPLICATION_LD_JSON_VALUE)) {
                        // contentType이 application/ld+json인 경우 link header 입력불가
                        if(!ValidateUtil.isEmptyData(ingestMessageVO.getLinks())) {
                            throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                                    "Invalid Request Content. Link Header cannot be used when contentType=application/ld+json");
                        }
                    }
                }

                // @context가 존재하지 않을 경우 default context 사용
                setDefaultContextIfEmpty(entityFullVO);

                validateEntityId(entityFullVO.getId());

                EntityDataModelVO retrieveEntityDataModelVO = entityDataModelSVC.getEntityDataModelVOById(entityFullVO.getId());
                if (opertaion == Operation.CREATE_ENTITY) {
                    if (retrieveEntityDataModelVO != null) {
                        throw new NgsiLdBadRequestException(ErrorCode.ALREADY_EXISTS,
                                "Invalid Request Content. Already exists entityId=" + entityFullVO.getId());
                    }
                } else if (opertaion == Operation.DELETE_ENTITY
                        || opertaion == Operation.APPEND_ENTITY_ATTRIBUTES
                        || opertaion == Operation.PARTIAL_ATTRIBUTE_UPDATE
                        || opertaion == Operation.UPDATE_ENTITY_ATTRIBUTES
                        || opertaion == Operation.REPLACE_ENTITY_ATTRIBUTES
                        || opertaion == Operation.DELETE_ENTITY_ATTRIBUTES) {
                    if (retrieveEntityDataModelVO == null) {
                        throw new NgsiLdResourceNotFoundException(ErrorCode.INVALID_PARAMETER,
                                "Invalid Request Content. Not exists entityId=" + entityFullVO.getId());
                    }
                }

                // 데이터모델 유효성 체크
                DataModelCacheVO dataModelCacheVO = null;
                if(retrieveEntityDataModelVO != null) {
                    dataModelCacheVO = dataModelManager.getDataModelVOCacheById(retrieveEntityDataModelVO.getDataModelId());

                    if (entityFullVO.getType() == null || !entityFullVO.getType().endsWith(retrieveEntityDataModelVO.getDataModelType())) {
                        throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                                "Invalid Request Content. Invalid dataModel type. request type=" + entityFullVO.getType()
                                        + ", exists entity type=" + retrieveEntityDataModelVO.getDataModelType()
                                        + ", entityId=" + retrieveEntityDataModelVO.getId()
                                        + ", datasetId=" + retrieveEntityDataModelVO.getDatasetId());
                    }
                }
                if (dataModelCacheVO == null) {
                    dataModelCacheVO = dataModelManager.getDataModelCacheByDatasetId(ingestMessageVO.getDatasetId());
                }
                if (dataModelCacheVO == null) {
                    dataModelCacheVO = dataModelManager.getDataModelVOCacheByContext(entityFullVO.getContext(), entityFullVO.getType());
                }
                if (dataModelCacheVO == null) {
                    throw new NgsiLdBadRequestException(ErrorCode.REQUEST_MESSAGE_PARSING_ERROR,
                            "Not Found DataModel. datasetId=" + ingestMessageVO.getDatasetId() + ", type=" + entityFullVO.getType());
                }

                entityFullVO.setType(dataModelCacheVO.getDataModelVO().getType());

                // 3. entityFullVO -> DB 처리용 daoVO로 파싱
                DynamicEntityDaoVO entityDaoVO = fullVOToDaoVO(entityFullVO, dataModelCacheVO, opertaion);
                entityDaoVO.setAttrId(attrId);

                entityProcessVO.setEntityFullVO(entityFullVO);
                entityProcessVO.setEntityDaoVO(entityDaoVO);
                entityProcessVO.setDataModelCacheVO(dataModelCacheVO);
                entityProcessVOList.add(entityProcessVO);

            } catch (BaseException e) {
                ProcessResultVO processResultVO = new ProcessResultVO();
                processResultVO.setProcessResult(false);
                processResultVO.setException(e);
                processResultVO.setErrorDescription("Content Parsing Error. message=" + e.getMessage());
                entityProcessVO.setProcessResultVO(processResultVO);
                entityProcessVOList.add(entityProcessVO);
                continue;

            } catch (Exception e) {
                ProcessResultVO processResultVO = new ProcessResultVO();
                processResultVO.setProcessResult(false);
                processResultVO.setException(new NgsiLdBadRequestException(ErrorCode.REQUEST_MESSAGE_PARSING_ERROR, e));
                processResultVO.setErrorDescription("Content Parsing Error. message=" + e.getMessage());
                entityProcessVO.setProcessResultVO(processResultVO);
                entityProcessVOList.add(entityProcessVO);
                continue;
            }
        }

        return entityProcessVOList;
    }

    private void setDefaultContextIfEmpty(DynamicEntityFullVO entityFullVO) {
        if(ValidateUtil.isEmptyData(entityFullVO.getContext())) {
            entityFullVO.setContext(Arrays.asList(defaultContextUri));
        }
    }

    private void validateEntityId(String entityId) {
        if (ValidateUtil.isEmptyData(entityId)) {
            throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                    "Invalid Request Content. Not found 'id'");
        }

        if (validateIdPatternEnabled && !ValidateUtil.isValidUrn(entityId)) {
            throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                    "Invalid Request Content. entityId is not in URN format. id=" + entityId);
        }
    }

    /**
     * 수신받은 content 기반으로 생성된 FullVO 를 DB용 daoVO로 파싱
     *
     * @param dynamicEntityFullVO 수신받은 content 기반으로 생성된 FullVO
     * @return EntityDaoVO DB용 daoVO
     * @throws BaseException
     */
    public DynamicEntityDaoVO fullVOToDaoVO(DynamicEntityFullVO dynamicEntityFullVO, DataModelCacheVO dataModelCacheVO,
                                            Operation opertaion) throws BaseException {

        try {

            // 1. daoVO 객체 생성 및 기본값 입력
            DynamicEntityDaoVO dynamicEntityDaoVO = new DynamicEntityDaoVO();
            dynamicEntityDaoVO.setContext(dynamicEntityFullVO.getContext());
            dynamicEntityDaoVO.setId(dynamicEntityFullVO.getId());
            dynamicEntityDaoVO.setDatasetId(dynamicEntityFullVO.getDatasetId());
            dynamicEntityDaoVO.setCreatedAt(dynamicEntityFullVO.getCreatedAt());
            dynamicEntityDaoVO.setModifiedAt(dynamicEntityFullVO.getModifiedAt());
            dynamicEntityDaoVO.setEntityType(dynamicEntityFullVO.getType());
            // 2. Dynamic Query 생성에 필요한 Meta정보 입력
            dynamicEntityDaoVO.setDbTableName(this.getTableName(dataModelCacheVO));
            dynamicEntityDaoVO.setDbColumnInfoVOMap(dataModelCacheVO.getDataModelStorageMetadataVO().getDbColumnInfoVOMap());

            // 3. Operation 처리 시 사용될 동적 attribute 정보 추출 및 Dao정보 입력
            if(opertaion != Operation.DELETE_ENTITY) {
                List<Attribute> rootAttributes = dataModelCacheVO.getDataModelVO().getAttributes();
                attributeToDynamicDaoVO(dynamicEntityFullVO, dynamicEntityDaoVO, null, rootAttributes, dynamicEntityFullVO.getModifiedAt(), dataModelCacheVO.getDataModelStorageMetadataVO());
                checkInvalidAttribute(dynamicEntityFullVO, rootAttributes);
            }

            return dynamicEntityDaoVO;
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new NgsiLdBadRequestException(ErrorCode.REQUEST_MESSAGE_PARSING_ERROR,
                    "fullVO to daoVO parsing ERROR. entityType=" + dynamicEntityFullVO.getType() + ", id=" + dynamicEntityFullVO.getId(), e);
        }
    }

    /**
     * Operation 처리 시 사용될 동적 attribute 정보 추출 및 Dao정보 입력
     *
     * @param dynamicEntityFullVO 수신받은 content 기반으로 생성된 FullVO
     * @param dynamicEntityDaoVO  FullVO 기반으로 생성된 DaoVO (계층구조의 attribute 를 flat 하게 펼침)
     * @param parentHierarchyIds  계층구조 attributeId (부모레벨)
     * @param rootAttributes      RootAttbitues 목록
     * @throws ParseException
     */

    /**
     * Operation 처리 시 사용될 동적 attribute 정보 추출 및 Dao정보 입력
     * @param currentEntityVO 수신받은 content 기반으로 생성된 FullVO
     * @param dynamicEntityDaoVO FullVO 기반으로 생성된 DaoVO (계층구조의 attribute 를 flat 하게 펼침)
     * @param parentHierarchyIds 계층구조 attributeId (부모레벨)
     * @param rootAttributes RootAttbitues 목록
     * @param eventTime 수신시간
     * @param storageMetadataVO dataModel 기반 Storage 적재 메타정보
     * @throws ParseException 파싱에러
     * @throws NgsiLdBadRequestException BadRequest 에러
     */
    @SuppressWarnings("unchecked")
    private void attributeToDynamicDaoVO(Map<String, Object> currentEntityVO, DynamicEntityDaoVO dynamicEntityDaoVO,
                                         List<String> parentHierarchyIds, List<Attribute> rootAttributes, Date eventTime,
                                         DataModelStorageMetadataVO storageMetadataVO) throws ParseException, NgsiLdBadRequestException {

        if (rootAttributes == null) return;

        Map<String, String> contextMap = dataModelManager.contextToFlatMap(dynamicEntityDaoVO.getContext());

        for (Attribute rootAttribute : rootAttributes) {

            // 1. get attribute key name
            // request entity attribute 중에 dataModel에 속한 attribute Key 값 추출
            //  - short name 일수도 있고, full uri 형태일 수도 있음 (request 로 인입된 값 그대로 추출)
            String attributeKey = getAttributeKey(currentEntityVO, rootAttribute, contextMap);
            if (attributeKey == null) {
                continue;
            }

            List<String> currentHierarchyIds = new ArrayList<>();
            if (parentHierarchyIds != null && parentHierarchyIds.size() > 0) {
                currentHierarchyIds.addAll(parentHierarchyIds);
            }
            currentHierarchyIds.add(rootAttribute.getName());

            // 2. get attribute data
            Map<String, Object> attribute = getAttributeData(currentEntityVO, rootAttribute, attributeKey);

            // 3. check mandatory field
            checkDefaultParam(attribute, attributeKey);

            // 3-1. type이 Property인 경우
            if (DataServiceBrokerCode.AttributeType.PROPERTY == rootAttribute.getAttributeType()) {

                AttributeValueType valueType = rootAttribute.getValueType();
                // 3-1-1. value type이 ArrayObject인 경우
                if (valueType == AttributeValueType.ARRAY_OBJECT) {

                    List<Map<String, Object>> arrayObject = null;
                    try {
                        arrayObject = (List<Map<String, Object>>) attribute.get(PropertyKey.VALUE.getCode());
                    } catch (ClassCastException e) {
                        throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                                "Invalid Request Content. attributeId=" + rootAttribute.getName() +
                                        ", valueType=" + rootAttribute.getValueType().getCode() + ", value=" + attribute.get(PropertyKey.VALUE.getCode()));
                    }

                    List<ObjectMember> objectMembers = rootAttribute.getObjectMembers();
                    for (ObjectMember objectMember : objectMembers) {
                        List<Object> objectMemberValueList = null;
                        for (Map<String, Object> object : arrayObject) {

                            Object value = object.get(objectMember.getName());
                            // 세부 파라미터 유효성 체크
                            checkObjectType(objectMember.getName(), objectMember.getValueType(), value, objectMember);

                            if (value == null) {
                                continue;
                            }

                            if (objectMemberValueList == null) {
                                objectMemberValueList = new ArrayList<>();
                            }

                            if (objectMember.getValueType() == AttributeValueType.DATE) {
                                objectMemberValueList.add(DateUtil.strToDate((String) value));
                            } else {
                                objectMemberValueList.add(value);
                            }
                        }
                        List<String> hierarchyAttributeIds = new ArrayList<>(currentHierarchyIds);
                        hierarchyAttributeIds.add(objectMember.getName());
                        String id = dataModelManager.getColumnNameByStorageMetadata(storageMetadataVO, hierarchyAttributeIds);
                        dynamicEntityDaoVO.put(id, objectMemberValueList);
                    }

                    // 3-1-2. value type이 Object인 경우
                } else if (valueType == AttributeValueType.OBJECT) {

                    Map<String, Object> object = null;
                    try {
                        object = (Map<String, Object>) attribute.get(PropertyKey.VALUE.getCode());
                    } catch (ClassCastException e) {
                        throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                                "Invalid Request Content. attributeId=" + rootAttribute.getName() +
                                        ", valueType=" + rootAttribute.getValueType().getCode() + ", value=" + attribute.get(PropertyKey.VALUE.getCode()));
                    }

                    objectTypeParamToDaoVO(currentHierarchyIds, rootAttribute.getObjectMembers(), object, dynamicEntityDaoVO, storageMetadataVO);

                    // 3-1-3. value type이 String, Integer, Double, Date, Boolean, ArrayString, ArrayInteger, ArrayDouble, ArrayBoolean, Object 인 경우
                } else {
                    Object value = attribute.get(PropertyKey.VALUE.getCode());
                    // 세부 파라미터 유효성 체크
                    checkObjectType(rootAttribute.getName(), valueType, value, rootAttribute);

                    if (valueType == AttributeValueType.DATE) {
                        String id = dataModelManager.getColumnNameByStorageMetadata(storageMetadataVO, currentHierarchyIds);
                        dynamicEntityDaoVO.put(id, DateUtil.strToDate((String) value));
                    } else {
                        String id = dataModelManager.getColumnNameByStorageMetadata(storageMetadataVO, currentHierarchyIds);
                        dynamicEntityDaoVO.put(id, value);
                    }
                }

                // 3-2. type이 GeoProperty인 경우
            } else if (DataServiceBrokerCode.AttributeType.GEO_PROPERTY == rootAttribute.getAttributeType()) {
                Object value = attribute.get(PropertyKey.VALUE.getCode());

                checkGeometryObjectType(value);

                try {
                    String geoJson = objectMapper.writeValueAsString(value);
                    GeoJsonObject object = objectMapper.readValue(geoJson, GeoJsonObject.class);
                    List<String> ids = dataModelManager.getColumnNamesByStorageMetadata(storageMetadataVO, currentHierarchyIds);
                    for(String id : ids) {
                        dynamicEntityDaoVO.put(id, geoJson);
                    }
                } catch (JsonProcessingException e) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Invalid Request Content. GeoJson parsing ERROR. attributeId=" + rootAttribute.getName() +
                                    ", valueType=" + AttributeValueType.GEO_JSON.getCode() + ", value=" + value);
                }
            } else if (DataServiceBrokerCode.AttributeType.RELATIONSHIP == rootAttribute.getAttributeType()) {

                if (rootAttribute.getValueType() == AttributeValueType.ARRAY_STRING
                    && attribute.containsKey(PropertyKey.OBJECT.getCode())) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Invalid Request Content. only allow objects. attribute=" + rootAttribute.getName());
                }

                if (rootAttribute.getValueType() == AttributeValueType.STRING
                        && attribute.containsKey(PropertyKey.OBJECTS.getCode())) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Invalid Request Content. only allow object. attribute=" + rootAttribute.getName());
                }

                PropertyKey propertyKey = PropertyKey.OBJECT;
                if (rootAttribute.getValueType() == AttributeValueType.ARRAY_STRING) {
                    propertyKey = PropertyKey.OBJECTS;
                }

                Object object = attribute.get(propertyKey.getCode());

                // 세부 파라미터 유효성 체크
                checkObjectType(rootAttribute.getName(), rootAttribute.getValueType(), object, rootAttribute);

                String id = dataModelManager.getColumnNameByStorageMetadata(storageMetadataVO, currentHierarchyIds);
                dynamicEntityDaoVO.put(id, object);
            }

            // 3-3. ObservedAt 을 포함한 Attribute 인 경우
            if (rootAttribute.getHasObservedAt() != null && rootAttribute.getHasObservedAt()) {
                Object value = attribute.get(PropertyKey.OBSERVED_AT.getCode());
                if (value != null) {
                    // Date 파라미터 유효성 체크
                    if (!ValidateUtil.isDateObject(value)) {
                        throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                                "Invalid Request Content. attributeId=" + rootAttribute.getName() + "." + PropertyKey.OBSERVED_AT.getCode() +
                                        ", valueType=" + AttributeValueType.DATE.getCode() + ", value=" + value);
                    }

                    List<String> hierarchyAttributeIds = new ArrayList<>(currentHierarchyIds);
                    hierarchyAttributeIds.add(PropertyKey.OBSERVED_AT.getCode());
                    String id = dataModelManager.getColumnNameByStorageMetadata(storageMetadataVO, hierarchyAttributeIds);
                    dynamicEntityDaoVO.put(id, DateUtil.strToDate((String) value));
                }
            }

            // 3-4. has property or relationship 이 존재하는 경우
            if (rootAttribute.getChildAttributes() != null) {
                attributeToDynamicDaoVO(attribute, dynamicEntityDaoVO, currentHierarchyIds, rootAttribute.getChildAttributes(), eventTime, storageMetadataVO);
            }

            // 3-5. unitCode 을 포함한 Attribute 인 경우
            if (rootAttribute.getHasUnitCode() != null && rootAttribute.getHasUnitCode()) {
                Object value = attribute.get(PropertyKey.UNIT_CODE.getCode());
                if (value != null) {
                    checkObjectType(rootAttribute.getName(), AttributeValueType.STRING, value, rootAttribute);

                    List<String> hierarchyAttributeIds = new ArrayList<>(currentHierarchyIds);
                    hierarchyAttributeIds.add(PropertyKey.UNIT_CODE.getCode());
                    String id = dataModelManager.getColumnNameByStorageMetadata(storageMetadataVO, hierarchyAttributeIds);
                    dynamicEntityDaoVO.put(id, (String) value);
                }
            }

            // 3-6. createdAt 속성 추가
            {
                List<String> hierarchyAttributeIds = new ArrayList<>(currentHierarchyIds);
                hierarchyAttributeIds.add(PropertyKey.CREATED_AT.getCode());
                String id = dataModelManager.getColumnNameByStorageMetadata(storageMetadataVO, hierarchyAttributeIds);
                dynamicEntityDaoVO.put(id, eventTime);
            }

            // 3-7. modifiedAt 속성 추가
            {
                List<String> hierarchyAttributeIds = new ArrayList<>(currentHierarchyIds);
                hierarchyAttributeIds.add(PropertyKey.MODIFIED_AT.getCode());
                String id = dataModelManager.getColumnNameByStorageMetadata(storageMetadataVO, hierarchyAttributeIds);
                dynamicEntityDaoVO.put(id, eventTime);
            }
        }
    }

    private Map<String, Object> getAttributeData(Map<String, Object> currentEntityVO, Attribute rootAttribute, String attributeKey) {
        Map<String, Object> attribute = null;
        Object attributeValue = null;
        try {
            attributeValue = currentEntityVO.get(attributeKey);
            attribute = (Map<String, Object>) attributeValue;
        } catch (ClassCastException e) {
            throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                    "Invalid Request Content. attributeId=" + rootAttribute.getName() +
                            ", valueType=" + rootAttribute.getValueType().getCode() + ", value=" + attributeValue);
        }
        return attribute;
    }

    private String getAttributeKey(Map<String, Object> currentEntityVO, Attribute rootAttribute, Map<String, String> contextMap) {

        String attributeKey = null;
        // short name 으로 조회
        if (currentEntityVO.containsKey(rootAttribute.getName())) {
            attributeKey = rootAttribute.getName();
            // request attribute 와 model의 attribute full uri 가 다른 경우
            if (!rootAttribute.getAttributeUri().equals(contextMap.get(attributeKey))) {
                throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                        "Invalid Request Content. No match attribute full uri. attribute name=" + rootAttribute.getName()
                                + ", dataModel attribute uri=" + rootAttribute.getAttributeUri() + " but ingest attribute uri=" + contextMap.get(attributeKey));
            }
        } else {
            // full uri로 조회
            if (currentEntityVO.containsKey(rootAttribute.getAttributeUri())) {
                attributeKey = rootAttribute.getAttributeUri();
            }
        }
        return attributeKey;
    }

    /**
     * 필수값 체크
     *
     * @param attribute
     * @param attributeId
     * @throws NgsiLdBadRequestException
     */
    private void checkDefaultParam(Map<String, Object> attribute, String attributeId) throws NgsiLdBadRequestException {
        if (attribute == null) {
            throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                    "Not found attribute. attributeId=" + attributeId);
        }
        if (attribute.get(PropertyKey.TYPE.getCode()) == null) {
            throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                    "Not found attribute type. attributeId=" + attributeId);
        }

        if (AttributeType.parseType(attribute.get(PropertyKey.TYPE.getCode()).toString()) == null) {
            throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                    "invalid attribute type. attribute Type=" + attribute.get(PropertyKey.TYPE.getCode()));
        }

        if (attribute.get(PropertyKey.TYPE.getCode()) == AttributeType.PROPERTY
                || attribute.get(PropertyKey.TYPE.getCode()) == AttributeType.GEO_PROPERTY) {
            if (attribute.get(PropertyKey.VALUE.getCode()) == null) {
                throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                        "Not found Property value. attributeId=" + attributeId);
            }
        } else if (attribute.get(PropertyKey.TYPE.getCode()) == AttributeType.RELATIONSHIP) {
            if (attribute.get(PropertyKey.OBJECT.getCode()) == null
                && attribute.get(PropertyKey.OBJECTS.getCode()) == null) {
                throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                        "Not found Relationship object and objects. attributeId=" + attributeId);
            }
        }
    }

    /**
     * Object 형태의 Property 를 DaoVO 로 파싱
     *
     * @param parentHierarchyIds 계층구조 부모 AttributeId 리스트
     * @param objectMembers      ChildAttribute
     * @param object             property value
     * @param dynamicEntityDaoVO DynamicEntityDaoVO
     * @throws ParseException
     */
    private void objectTypeParamToDaoVO(List<String> parentHierarchyIds, List<ObjectMember> objectMembers,
                                        Map<String, Object> object, DynamicEntityDaoVO dynamicEntityDaoVO, DataModelStorageMetadataVO storageMetadataVO) throws ParseException {

        for (ObjectMember objectMember : objectMembers) {
            List<String> currentHierarchyIds = new ArrayList<>(parentHierarchyIds);
            currentHierarchyIds.add(objectMember.getName());

            Object value = object.get(objectMember.getName());
            // 세부 파라미터 유효성 체크
            checkObjectType(objectMember.getName(), objectMember.getValueType(), value, objectMember);

            if (objectMember.getValueType() == AttributeValueType.OBJECT) {

                Map<String, Object> objectInObject = (Map<String, Object>) value;
                objectTypeParamToDaoVO(currentHierarchyIds, objectMember.getObjectMembers(), objectInObject, dynamicEntityDaoVO, storageMetadataVO);

            } else if (objectMember.getValueType() == AttributeValueType.DATE) {
                String id = dataModelManager.getColumnNameByStorageMetadata(storageMetadataVO, currentHierarchyIds);
                dynamicEntityDaoVO.put(id, DateUtil.strToDate((String) value));
            } else {
                String id = dataModelManager.getColumnNameByStorageMetadata(storageMetadataVO, currentHierarchyIds);
                dynamicEntityDaoVO.put(id, object.get(objectMember.getName()));
            }
        }
    }



    /**
     * primitive object 유효성 체크
     * @param id attributeId
     * @param valueType attributeValueType
     * @param value attributeValue
     * @param attribute ObjectMember
     * @return
     * @throws NgsiLdBadRequestException
     */
    private static boolean checkObjectType(String id, AttributeValueType valueType, Object value, ObjectMember attribute) throws NgsiLdBadRequestException {

        Boolean required = attribute.getIsRequired();
        String minLength = attribute.getMinLength();
        String maxLength = attribute.getMaxLength();
        BigDecimal greaterThanOrEqualTo = attribute.getGreaterThanOrEqualTo();
        BigDecimal greaterThan = attribute.getGreaterThan();
        BigDecimal lessThanOrEqualTo = attribute.getLessThanOrEqualTo();
        BigDecimal lessThan = attribute.getLessThan();
        List<Object> valueEnum = attribute.getValueEnum();

        if (required != null && required) {
            if (value == null) {
                throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Invalid Request Content. attributeId=" + id + " is null");
            }
        } else {
            if (value == null) {
                return true;
            }
        }

        switch (valueType) {
            case STRING:
                if (!ValidateUtil.isStringObject(value)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Invalid Attribute Type. attributeId=" + id + ", valueType=" + valueType + ", value=" + value);
                }
                if (!ValidateUtil.isValidStringMinLength(value, minLength)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "underflow Attribute MinLength. attributeId=" + id + ", valueType=" + valueType + ", minLength=" + minLength + ", value=" + value);
                }
                if (!ValidateUtil.isValidStringMaxLength(value, maxLength)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Overflow Attribute MaxLength. attributeId=" + id + ", valueType=" + valueType + ", maxLength=" + maxLength + ", value=" + value);
                }
                if (!ValidateUtil.isValidEnum(value, valueEnum)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Not match Attribute valueEnum. attributeId=" + id + ", valueType=" + valueType + ", valueEnum=" + valueEnum + ", value=" + value);
                }
                break;
            case INTEGER:
                if (!ValidateUtil.isIntegerObject(value)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Invalid Attribute Type. attributeId=" + id + ", valueType=" + valueType + ", value=" + value);
                }
                if (!ValidateUtil.isValidGreaterThanOrEqualTo(value, greaterThanOrEqualTo)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Greater or equal to Attribute value. attributeId=" + id + ", valueType=" + valueType + ", greaterThanOrEqualTo=" + greaterThanOrEqualTo + ", value=" + value);
                }
                if (!ValidateUtil.isValidGreaterThan(value, greaterThan)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Greater Attribute value. attributeId=" + id + ", valueType=" + valueType + ", greaterThan=" + greaterThan + ", value=" + value);
                }

                if (!ValidateUtil.isValidLessThanOrEqualTo(value, lessThanOrEqualTo)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Less or equal to Attribute value. attributeId=" + id + ", valueType=" + valueType + ", lessThanOrEqualTo=" + lessThanOrEqualTo + ", value=" + value);
                }
                if (!ValidateUtil.isValidLessThan(value, lessThan)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Less Attribute value. attributeId=" + id + ", valueType=" + valueType + ", lessThan=" + lessThan + ", value=" + value);
                }
                if (!ValidateUtil.isValidEnum(value, valueEnum)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Not match Attribute valueEnum. attributeId=" + id + ", valueType=" + valueType + ", valueEnum=" + valueEnum + ", value=" + value);
                }
                break;
            case LONG:
                if (!ValidateUtil.isLongObject(value)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Invalid Attribute Type. attributeId=" + id + ", valueType=" + valueType + ", value=" + value);
                }
                if (!ValidateUtil.isValidGreaterThanOrEqualTo(value, greaterThanOrEqualTo)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Greater or equal to Attribute value. attributeId=" + id + ", valueType=" + valueType + ", greaterThanOrEqualTo=" + greaterThanOrEqualTo + ", value=" + value);
                }
                if (!ValidateUtil.isValidGreaterThan(value, greaterThan)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Greater Attribute value. attributeId=" + id + ", valueType=" + valueType + ", greaterThan=" + greaterThan + ", value=" + value);
                }

                if (!ValidateUtil.isValidLessThanOrEqualTo(value, lessThanOrEqualTo)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Less or equal to Attribute value. attributeId=" + id + ", valueType=" + valueType + ", lessThanOrEqualTo=" + lessThanOrEqualTo + ", value=" + value);
                }
                if (!ValidateUtil.isValidLessThan(value, lessThan)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Less Attribute value. attributeId=" + id + ", valueType=" + valueType + ", lessThan=" + lessThan + ", value=" + value);
                }
                if (!ValidateUtil.isValidEnum(value, valueEnum)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Not match Attribute valueEnum. attributeId=" + id + ", valueType=" + valueType + ", valueEnum=" + valueEnum + ", value=" + value);
                }
                break;
            case DOUBLE:
                if (!ValidateUtil.isBigDecimalObject(value)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Invalid Attribute Type. attributeId=" + id + ", valueType=" + valueType + ", value=" + value);
                }
                if (!ValidateUtil.isValidGreaterThanOrEqualTo(value, greaterThanOrEqualTo)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Greater or equal to Attribute value. attributeId=" + id + ", valueType=" + valueType + ", greaterThanOrEqualTo=" + greaterThanOrEqualTo + ", value=" + value);
                }
                if (!ValidateUtil.isValidGreaterThan(value, greaterThan)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Greater Attribute value. attributeId=" + id + ", valueType=" + valueType + ", greaterThan=" + greaterThan + ", value=" + value);
                }

                if (!ValidateUtil.isValidLessThanOrEqualTo(value, lessThanOrEqualTo)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Less Attribute value. attributeId=" + id + ", valueType=" + valueType + ", lessThanOrEqualTo=" + lessThanOrEqualTo + ", value=" + value);
                }
                if (!ValidateUtil.isValidLessThan(value, lessThan)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Less or equal to Attribute value. attributeId=" + id + ", valueType=" + valueType + ", lessThan=" + lessThan + ", value=" + value);
                }
                if (!ValidateUtil.isValidEnum(value, valueEnum)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Not match Attribute valueEnum. attributeId=" + id + ", valueType=" + valueType + ", valueEnum=" + valueEnum + ", value=" + value);
                }
                break;
            case DATE:
                if (!ValidateUtil.isDateObject(value)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Invalid Attribute Type. attributeId=" + id + ", valueType=" + valueType + ", value=" + value);
                }
                break;
            case BOOLEAN:
                if (!ValidateUtil.isBooleanObject(value)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Invalid Attribute Type. attributeId=" + id + ", valueType=" + valueType + ", value=" + value);
                }
                break;
            case ARRAY_STRING:
                if (!ValidateUtil.isArrayStringObject(value)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Invalid Attribute Type. attributeId=" + id + ", valueType=" + valueType + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayStringMinLength(value, minLength)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "underflow Attribute MinLength. attributeId=" + id + ", valueType=" + valueType + ", minLength=" + minLength + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayStringMaxLength(value, maxLength)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Overflow Attribute MaxLength. attributeId=" + id + ", valueType=" + valueType + ", maxLength=" + maxLength + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayEnum(value, valueEnum)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Not match Attribute valueEnum. attributeId=" + id + ", valueType=" + valueType + ", valueEnum=" + valueEnum + ", value=" + value);
                }
                break;
            case ARRAY_INTEGER:
                if (!ValidateUtil.isArrayIntegerObject(value)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Invalid Attribute Type. attributeId=" + id + ", valueType=" + valueType + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayGreaterThanOrEqualTo(value, greaterThanOrEqualTo)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Greater or equal to Attribute value. attributeId=" + id + ", valueType=" + valueType + ", greaterThanOrEqualTo=" + greaterThanOrEqualTo + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayGreaterThan(value, greaterThan)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Greater  Attribute value. attributeId=" + id + ", valueType=" + valueType + ", greaterThan=" + greaterThan + ", value=" + value);
                }

                if (!ValidateUtil.isValidArrayLessThenOrEqualTo(value, lessThanOrEqualTo)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Less or equal to Attribute value. attributeId=" + id + ", valueType=" + valueType + ", lessThanOrEqualTo=" + lessThanOrEqualTo + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayLessThan(value, lessThan)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Less Attribute value. attributeId=" + id + ", valueType=" + valueType + ", lessThan=" + lessThan + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayEnum(value, valueEnum)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Not match Attribute valueEnum. attributeId=" + id + ", valueType=" + valueType + ", valueEnum=" + valueEnum + ", value=" + value);
                }
                break;
            case ARRAY_LONG:
                if (!ValidateUtil.isArrayLongObject(value)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Invalid Attribute Type. attributeId=" + id + ", valueType=" + valueType + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayGreaterThanOrEqualTo(value, greaterThanOrEqualTo)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Greater or equal to Attribute value. attributeId=" + id + ", valueType=" + valueType + ", greaterThanOrEqualTo=" + greaterThanOrEqualTo + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayGreaterThan(value, greaterThan)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Greater  Attribute value. attributeId=" + id + ", valueType=" + valueType + ", greaterThan=" + greaterThan + ", value=" + value);
                }

                if (!ValidateUtil.isValidArrayLessThenOrEqualTo(value, lessThanOrEqualTo)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Less or equal to Attribute value. attributeId=" + id + ", valueType=" + valueType + ", lessThanOrEqualTo=" + lessThanOrEqualTo + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayLessThan(value, lessThan)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Less Attribute value. attributeId=" + id + ", valueType=" + valueType + ", lessThan=" + lessThan + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayEnum(value, valueEnum)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Not match Attribute valueEnum. attributeId=" + id + ", valueType=" + valueType + ", valueEnum=" + valueEnum + ", value=" + value);
                }
                break;
            case ARRAY_DOUBLE:
                if (!ValidateUtil.isArrayBigDecimalObject(value)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Invalid Attribute Type. attributeId=" + id + ", valueType=" + valueType + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayGreaterThanOrEqualTo(value, greaterThanOrEqualTo)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Greater or equal to Attribute value. attributeId=" + id + ", valueType=" + valueType + ", greaterThanOrEqualTo=" + greaterThanOrEqualTo + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayGreaterThan(value, greaterThan)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Greater Attribute value. attributeId=" + id + ", valueType=" + valueType + ", greaterThan=" + greaterThan + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayLessThenOrEqualTo(value, lessThanOrEqualTo)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Less or equal to Attribute value. attributeId=" + id + ", valueType=" + valueType + ", lessThanOrEqualTo=" + lessThanOrEqualTo + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayLessThan(value, lessThan)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Less Attribute value. attributeId=" + id + ", valueType=" + valueType + ", lessThan=" + lessThan + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayEnum(value, valueEnum)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                            "Not match Attribute valueEnum. attributeId=" + id + ", valueType=" + valueType + ", valueEnum=" + valueEnum + ", value=" + value);
                }
                break;
            case ARRAY_BOOLEAN:
                if (!ValidateUtil.isArrayBooleanObject(value)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Invalid Attribute Type. attributeId=" + id + ", valueType=" + valueType + ", value=" + value);
                }
                break;
            case OBJECT:
                if (!ValidateUtil.isMapObject(value)) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Invalid Attribute Type. attributeId=" + id + ", valueType=" + valueType + ", value=" + value);
                }
                break;
            default:
                throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Invalid Attribute valueType. attributeId=" + id + ", valueType=" + valueType + ", value=" + value);
        }

        return true;
    }

    /**
     * Geometry Object Type 체크 (Point, MultiPoint, LineString, MultiLineString, Polygon, MultiPolygon)
     *
     * @param value
     * @return
     * @throws NgsiLdBadRequestException
     */
    private static boolean checkGeometryObjectType(Object value) throws NgsiLdBadRequestException {

        HashMap<String, Object> map = (HashMap<String, Object>) value;
        String geoType = map.get(PropertyKey.TYPE.getCode()).toString();
        if (GeoJsonValueType.parseType(geoType) == null) {
            throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "invalid attribute type. Geometry Object Type=" + geoType);
        }
        return true;
    }


    /**
     * 등록 되지 않은 attribute 추출
     *
     * @param dynamicEntityFullVO
     * @param rootAttributes
     * @return
     */
    private static void checkInvalidAttribute(DynamicEntityFullVO dynamicEntityFullVO, List<Attribute> rootAttributes) {

        for (Map.Entry<String, Object> entry : dynamicEntityFullVO.entrySet()) {

            String key = entry.getKey();

            //기본 구성요소(@context, id, createdAt ,modifiedAt ,operation ,type) 검증 SKIP
            if (DefaultAttributeKey.parseType(key) != null) {
                continue;
            }
            Attribute attribute = findAttribute(rootAttributes, entry.getKey());
            if (attribute == null) {
                // rootAttribute 체크
                throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "invalid key : " + key);
            }
            isExistAttribute(entry, attribute);
        }
    }

    /**
     * attribute 존재여부 판단
     *
     * @param entry
     * @param attribute
     */
    private static void isExistAttribute(Map.Entry<String, Object> entry, Attribute attribute) {

        String attrKey = entry.getKey();
        LinkedHashMap<String, Object> attrValue = (LinkedHashMap<String, Object>) entry.getValue();
        String type = attrValue.get(PropertyKey.TYPE.getCode()).toString();


        if (type.equalsIgnoreCase(AttributeType.PROPERTY.getCode())) {
            // PROPERTY 형 item 체크
            Object valueItem = attrValue.get(PropertyKey.VALUE.getCode());
            if (valueItem == null) {
                throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Not found value : " + entry.getKey());
            }


            if (valueItem instanceof LinkedHashMap) {
                checkInnerAttribute(attrKey, attrValue, attribute);
            } else if (valueItem instanceof ArrayList) {
                // n-레벨 Object 타입 체크
                ArrayList arrayObject = (ArrayList) valueItem;
                Object innerItem = arrayObject.get(0);
                if (innerItem instanceof LinkedHashMap) {
                    // n-레벨 ArrayObject 타입 체크
                    for (Object item : arrayObject) {
                        checkArrayObject((LinkedHashMap<String, Object>) item, attribute);
                    }
                } else {
                    // 1-레벨 내 array 형 체크
                    if (!attrKey.equals(attribute.getName()) && !attrKey.equals(attribute.getAttributeUri())) {
                        throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Not found key : " + entry.getKey());
                    }
                }
            } else {
                // 1-레벨 체크
                if (!attrKey.equals(attribute.getName()) && !attrKey.equals(attribute.getAttributeUri())) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Not found key : " + entry.getKey());
                }
            }

            //동일 레벨 attribute 체크
            checkInnerAttribute(attrKey, attrValue, attribute);

        } else if (type.equalsIgnoreCase(AttributeType.RELATIONSHIP.getCode())) {
            // RELATIONSHIP 형 item 체크
            Object objectItem = attrValue.get(PropertyKey.OBJECT.getCode());
            Object objectsItem = attrValue.get(PropertyKey.OBJECTS.getCode());
            if (objectItem == null && objectsItem == null) {
                throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Not found object and objects. attribute=: " + entry.getKey());
            }
            //동일 레벨 attribute 체크
            checkInnerAttribute(attrKey, attrValue, attribute);

        } else if (type.equalsIgnoreCase(AttributeType.GEO_PROPERTY.getCode())) {
            // GEO_PROPERTY 형 item 체크
            LinkedHashMap<String, Object> valueItem = (LinkedHashMap<String, Object>) attrValue.get(PropertyKey.VALUE.getCode());
            String geoType = valueItem.get(PropertyKey.TYPE.getCode()).toString();
            if (GeometryType.parseType(geoType) == null) {
                throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Not found geo-type : " + entry.getKey());
            }
            Object coordinatesItem = valueItem.get(PropertyKey.COORDINATES.getCode());
            if (coordinatesItem == null) {
                throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Not found coordinates : " + entry.getKey());
            }

        }

        // ObservedAt 체크
        if (attribute != null && attribute.getHasObservedAt() != null) {
            if (attribute.getHasObservedAt() && !attrValue.containsKey(DefaultAttributeKey.OBSERVED_AT.getCode())) {
                throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Not found observedAt : " + entry.getKey());
            }
        }
    }

    /**
     * 하위 attribute 체크
     *
     * @param key
     * @param attrValue
     * @param attribute
     */
    private static void checkInnerAttribute(String key, LinkedHashMap<String, Object> attrValue, Attribute attribute) {
        String type = attrValue.get(PropertyKey.TYPE.getCode()).toString();

        if (type.equalsIgnoreCase(AttributeType.PROPERTY.getCode())) {


            for (Map.Entry<String, Object> propertyMap : attrValue.entrySet()) {

                String propertyMapKey = propertyMap.getKey();
                if (propertyMapKey.equals(PropertyKey.TYPE.getCode())
                        || propertyMapKey.equals(PropertyKey.OBSERVED_AT.getCode())
                        || propertyMapKey.equals(PropertyKey.UNIT_CODE.getCode()) ) {
                    continue;
                } else if (propertyMapKey.equals(PropertyKey.VALUE.getCode())) {
                    Object attrObjectValue = attrValue.get(PropertyKey.VALUE.getCode());
                    if (attrObjectValue instanceof LinkedHashMap) {
                        LinkedHashMap<String, Object> tmpMap = (LinkedHashMap<String, Object>) attrObjectValue;
                        tmpMap.entrySet();
                        for (Map.Entry<String, Object> entry : tmpMap.entrySet()) {
                            if (attribute != null) {
                                // ex) address 레벨
                                checkObjectMember(entry.getKey(), attribute);
                            }
                        }
                    } else {
                        if (attribute == null || (!key.equals(attribute.getName()) && !key.equals(attribute.getAttributeUri()))) {
                            throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Not found key : " + key);
                        }
                    }
                } else {

                    // 같은 레벨 하위 attribute 필터링 unit
                    if (attribute.getChildAttributes() == null) {
                        throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Not found key : " + propertyMapKey);
                    }
                    Attribute innerAttribute = findAttribute(attribute.getChildAttributes(), propertyMapKey);
                    if (innerAttribute == null) {
                        throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Not found key : " + propertyMapKey);
                    }
                    isExistAttribute(propertyMap, innerAttribute);
                }
            }


        } else if (type.equalsIgnoreCase(AttributeType.RELATIONSHIP.getCode())) {

            for (Map.Entry<String, Object> relationshipMap : attrValue.entrySet()) {

                String relationshipMapKey = relationshipMap.getKey();
                if (relationshipMapKey.equals(PropertyKey.TYPE.getCode())
                        || relationshipMapKey.equals(PropertyKey.OBJECT.getCode())
                        || relationshipMapKey.equals(PropertyKey.OBJECTS.getCode())
                        || relationshipMapKey.equals(PropertyKey.OBSERVED_AT.getCode())) {
                    continue;
                }
                Attribute innerAttribute = findAttribute(attribute.getChildAttributes(), relationshipMapKey);
                if (innerAttribute == null) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Not found key : " + relationshipMapKey);
                }
                isExistAttribute(relationshipMap, innerAttribute);
            }

        }
    }

    /**
     * 연관 attribute 객체 가져오기
     *
     * @param attributes
     * @param name
     * @return
     */
    private static Attribute findAttribute(List<Attribute> attributes, String name) {
        for (Attribute attribute : attributes) {
            if (attribute.getName().equals(name)) {
                return attribute;
            }
            if (attribute.getAttributeUri().equals(name)) {
                return attribute;
            }
        }
        return null;
    }

    /**
     * Property 내 ArrayObject 케이스 점검
     *
     * @param attrValue
     * @param attribute
     */
    private static void checkArrayObject(LinkedHashMap<String, Object> attrValue, Attribute attribute) {

        for (Map.Entry<String, Object> entry : attrValue.entrySet()) {
            boolean isOK = false;
            String innerKey = entry.getKey();
            for (ObjectMember objectMember : attribute.getObjectMembers()) {
                if (innerKey.equals(objectMember.getName())) {
                    isOK = true;
                    break;
                }
            }
            if (!isOK) {
                throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Not found key : " + innerKey);
            }
        }
    }

    /**
     * Property 내 ObjectMember 케이스 점검
     *
     * @param attrKey
     * @param attribute
     */
    private static void checkObjectMember(String attrKey, Attribute attribute) {

        for (ObjectMember objectMember : attribute.getObjectMembers()) {
            if (attrKey.equals(objectMember.getName())) {
                return;
            }
        }
        throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "invalid key : " + attribute.getName() + "." + attrKey);
    }

    /**
     * 수신받은 객체를 EntityFullVO 형태로 파싱하여 반환
     */
    public DynamicEntityFullVO deserializeContent(String content) throws NgsiLdBadRequestException {

        if (ValidateUtil.isEmptyData(content)) {
            return new DynamicEntityFullVO();
        }

        try {
            return objectMapper.readValue(content, new TypeReference<DynamicEntityFullVO>() {
            });
        } catch (IOException e) {
            new NgsiLdBadRequestException(ErrorCode.REQUEST_MESSAGE_PARSING_ERROR,
                    "Content Parsing ERROR. content=" + content, e);
        }
        return new DynamicEntityFullVO();
    }

    /**
     * 파라미터 유효성 및 권한 체크
     *
     * @param processVOList 서비스 처리 VO 리스트
     */
    @Override
    public void processValidate(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> processVOList) {

        for (EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> processVO : processVOList) {

            // 파싱 에러난 항목은 유호성 체크 제외
            if (processVO.getProcessResultVO().isProcessResult() != null
                    && !processVO.getProcessResultVO().isProcessResult()) {
                continue;
            }

            try {

                checkValidate(processVO.getEntityFullVO(),
                        processVO.getEntityDaoVO(),
                        processVO.getOperation(),
                        processVO.getDataModelCacheVO());

            } catch (NgsiLdBadRequestException e) {
                ProcessResultVO processResultVO = new ProcessResultVO();
                processResultVO.setProcessResult(false);
                processResultVO.setException(e);
                processResultVO.setErrorDescription(e.getMessage());
                processVO.setProcessResultVO(processResultVO);

                continue;
            }
        }
    }

    /**
     * 파라미터 유효성 및 권한 체크
     *
     * @param dynamicEntityFullVO Kafka로 부터 전달받은 EntityVO
     * @param dynamicEntityDaoVO  dynamicEntityFullVO 기반으로 생성된 DaoVO
     * @param operation           요청받은 처리 Operation
     * @throws NgsiLdBadRequestException
     */
    private void checkValidate(DynamicEntityFullVO dynamicEntityFullVO, DynamicEntityDaoVO dynamicEntityDaoVO, Operation operation, DataModelCacheVO dataModelCacheVO) throws NgsiLdBadRequestException {

        if (operation == Operation.CREATE_ENTITY
                || operation == Operation.REPLACE_ENTITY_ATTRIBUTES
                || operation == Operation.CREATE_ENTITY_OR_REPLACE_ENTITY_ATTRIBUTES) {

            // 필수 rootAttbiute 여부 유효성 체크 (나머지 세부 유효성은 파싱 시점에서 체크함)
            List<Attribute> rootAttributes = dataModelCacheVO.getDataModelVO().getAttributes();
            for (Attribute rootAttribute : rootAttributes) {

                boolean rootRequired = rootAttribute.getIsRequired() == null ? false : rootAttribute.getIsRequired();

                // rootAttribute인 해당 필드가 필수인 경우
                if (rootRequired) {
                    if (dynamicEntityFullVO.get(rootAttribute.getName()) == null
                            && dynamicEntityFullVO.get(rootAttribute.getAttributeUri()) == null) {
                        throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "'" + rootAttribute.getName() + "' is null");
                    }

                    // rootAttribute가 필수가 아닌 경우
                } else {
                    if (dynamicEntityFullVO.get(rootAttribute.getName()) == null) {
                        continue;
                    }
                }
            }

            // update 가능 attribute 인지 여부 체크
        } else if (operation == Operation.PARTIAL_ATTRIBUTE_UPDATE) {
            String entityId = dynamicEntityDaoVO.getId();
            String attrId = dynamicEntityDaoVO.getAttrId();

            // AttrId 유효성 체크
            // TODO: 계층구조 attrId 처리 구현
            boolean isValid = false;
            List<Attribute> rootAttributes = dataModelCacheVO.getDataModelVO().getAttributes();
            for (Attribute rootAttribute : rootAttributes) {
                if (rootAttribute.getName().equals(attrId)) {
                    isValid = true;
                    break;
                }
            }
            if (!isValid) {
                throw new NgsiLdResourceNotFoundException(ErrorCode.NOT_EXIST_ENTITY_ATTR,
                        "Not exists Entity Attribute. entityId=" + entityId + ", attrId=" + attrId);
            }

            // 삭제 가능 attribute 인지 여부 체크
        } else if (operation == Operation.DELETE_ENTITY_ATTRIBUTES) {
            String entityId = dynamicEntityDaoVO.getId();
            String attrId = dynamicEntityDaoVO.getAttrId();

            // AttrId 유효성 체크
            // TODO: 계층구조 attrId 처리 구현
            boolean isValid = false;
            List<Attribute> rootAttributes = dataModelCacheVO.getDataModelVO().getAttributes();
            for (Attribute rootAttribute : rootAttributes) {
                if (rootAttribute.getName().equals(attrId)) {
                    isValid = true;
                    break;
                }
            }
            if (!isValid) {
                throw new NgsiLdResourceNotFoundException(ErrorCode.NOT_EXIST_ENTITY_ATTR,
                        "Not exists Entity Attribute. entityId=" + entityId + ", attrId=" + attrId);
            }
        }
    }

    /**
     * Operation 별 묶음처리 리스트로 분류
     *
     * @param createList              CREATE 묶음처리 리스트
     * @param replaceAttrList         REPLACE ATTRIBUTES 묶음처리 리스트
     * @param appendAttrList          APPEND ATTRIBUTES 묶음처리 리스트
     * @param createOrReplaceAttrList CREATE OR REPLACE ATTRIBUTES 묶음처리 리스트
     * @param createOrAppendAttrList  CREATE OR APPEND ATTRIBUTES 묶음처리 리스트
     * @param deleteList              DELETE 묶음처리 리스트
     * @param entityProcessVOList     분류대상인 서비스 처리 VO 리스트
     */
    private void setProcessVOByOperation(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> createList,
                                         List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> replaceAttrList,
                                         List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> appendAttrList,
                                         List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> appendNoOverwriteEntityList,
                                         List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> updateAttrList,
                                         List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> partialAttrUpdateList,
                                         List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> createOrReplaceAttrList,
                                         List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> createOrAppendAttrList,
                                         List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> deleteList,
                                         List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> deleteAttrList,
                                         List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> entityProcessVOList) {

        for (EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> processVO : entityProcessVOList) {

            // 유효성 체크 실패나 권한없음으로 오류 처리된 항목들은 제외
            if (processVO.getProcessResultVO().isProcessResult() != null
                    && !processVO.getProcessResultVO().isProcessResult()) {
                continue;
            }

            // Operation 별 분리
            Operation operation = processVO.getOperation();
            List<OperationOption> operationOptions = processVO.getOperationOptions();

            switch (operation) {
                case CREATE_ENTITY:
                    createList.add(processVO);
                    break;
                case APPEND_ENTITY_ATTRIBUTES:
                    boolean noOverwrite = false;
                    if (!ValidateUtil.isEmptyData(operationOptions)) {
                        for (OperationOption operationOption : operationOptions) {
                            if (OperationOption.NO_OVERWRITE == operationOption) {
                                noOverwrite = true;
                                break;
                            }
                        }
                    }
                    if (noOverwrite) {
                        appendNoOverwriteEntityList.add(processVO);
                    } else {
                        appendAttrList.add(processVO);
                    }
                    break;
                case REPLACE_ENTITY_ATTRIBUTES:
                    replaceAttrList.add(processVO);
                    break;
                case UPDATE_ENTITY_ATTRIBUTES:
                    updateAttrList.add(processVO);
                    break;
                case PARTIAL_ATTRIBUTE_UPDATE:
                    partialAttrUpdateList.add(processVO);
                    break;
                case CREATE_ENTITY_OR_APPEND_ENTITY_ATTRIBUTES:
                    createOrAppendAttrList.add(processVO);
                    break;
                case CREATE_ENTITY_OR_REPLACE_ENTITY_ATTRIBUTES:
                    createOrReplaceAttrList.add(processVO);
                    break;
                case DELETE_ENTITY:
                    deleteList.add(processVO);
                    break;
                case DELETE_ENTITY_ATTRIBUTES:
                    deleteAttrList.add(processVO);
                    break;
                default:
                    // Error Handling 추가
                    break;
            }
        }
    }

    /**
     * Operation 별 묶음 처리
     *
     * @param entityProcessVOList 처리VO리스트
     */
    @Override
    public void processOperation(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> entityProcessVOList) {

        // 1. Operation 별 벌크처리를 위해 List 생성
        List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> createList = new ArrayList<>();
        List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> replaceEntityList = new ArrayList<>();
        List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> appendEntityList = new ArrayList<>();
        List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> appendNoOverwriteEntityList = new ArrayList<>();
        List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> updateAttrList = new ArrayList<>();
        List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> partialAttrUpdateList = new ArrayList<>();
        List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> createOrAppendAttrList = new ArrayList<>();
        List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> createOrReplaceAttrList = new ArrayList<>();
        List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> deleteList = new ArrayList<>();
        List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> deleteAttrList = new ArrayList<>();

        // 2. Operation 별 벌크처리를 위해 VO 분리
        setProcessVOByOperation(createList, replaceEntityList, appendEntityList, appendNoOverwriteEntityList, updateAttrList, partialAttrUpdateList,
                createOrReplaceAttrList, createOrAppendAttrList, deleteList, deleteAttrList, entityProcessVOList);

        // 3. Create Entity 벌크처리
        processCreate(createList);

        // 4. Replace Entity Attributes 벌크처리
        processReplaceAttr(replaceEntityList);

        // 5. Append Entity Attributes 벌크처리
        processAppendAttr(appendEntityList);

        // 5-1. Append Entity (noOverwrite) Attributes 벌크처리
        processAppendNoOverwriteAttr(appendNoOverwriteEntityList);

        // 6. Update Entity Attributes 벌크처리
        processUpdateAttr(updateAttrList);

        // 7. Partial Attribute Update 벌크처리
        processPartialAttrUpdate(partialAttrUpdateList);

        // 8. Create Entity or Replace Entity Attributes 벌크처리
        processFullUpsert(createOrReplaceAttrList);

        // 9. Create Entity or Append Entity Attributes 벌크처리
        processPartialUpsert(createOrAppendAttrList);

        // 10. Delete Entity 벌크처리
        processDelete(deleteList);

        // 11. Delete Attribute Entity
        processDeleteAttr(deleteAttrList);
    }

    private void storeEntityDataModel(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> entityProcessVOList) {
        for (EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO : entityProcessVOList) {
            if (entityProcessVO.getProcessResultVO().isProcessResult()) {

                try {
                    DataModelCacheVO dataModelCacheVO = entityProcessVO.getDataModelCacheVO();

                    String entityId = entityProcessVO.getEntityFullVO().getId();
                    String datasetId = entityProcessVO.getDatasetId();
                    String dataModelId = dataModelCacheVO.getDataModelVO().getId();
                    String dataModelType = dataModelCacheVO.getDataModelVO().getType();

                    // entity가 삭제된 경우 entity별 데이터 모델 관리 테이블에서 삭제
                    if (Operation.DELETE_ENTITY == entityProcessVO.getProcessResultVO().getProcessOperation()) {
                        entityDataModelSVC.deleteEntityDataModel(entityId);

                        // 생성 또는 갱신인 경우
                    } else {

                        EntityDataModelVO retrieveEntityDataModelVO = entityDataModelSVC.getEntityDataModelVOById(entityId);
                        // 기존 정보가 없는 경우 신규 Insert
                        if (retrieveEntityDataModelVO == null) {
                            EntityDataModelVO entityDataModelVO = new EntityDataModelVO();
                            entityDataModelVO.setId(entityId);
                            entityDataModelVO.setDatasetId(datasetId);
                            entityDataModelVO.setDataModelId(dataModelId);
                            entityDataModelVO.setDataModelType(dataModelType);

                            entityDataModelSVC.createEntityDataModel(entityDataModelVO);

                            // 기존 정보가 존재하는 경우
                        } else {

                            // 변경된 정보가 있는 경우만 갱신
                            if ((datasetId != null && !datasetId.equals(retrieveEntityDataModelVO.getDatasetId()))
                                    || !retrieveEntityDataModelVO.getDataModelId().equals(dataModelId)
                                    || !retrieveEntityDataModelVO.getDataModelType().equals(dataModelType)) {

                                EntityDataModelVO entityDataModelVO = new EntityDataModelVO();
                                entityDataModelVO.setId(entityId);
                                entityDataModelVO.setDatasetId(datasetId);
                                entityDataModelVO.setDataModelId(dataModelId);
                                entityDataModelVO.setDataModelType(dataModelType);

                                entityDataModelSVC.updateEntityDataModel(entityDataModelVO);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("storeEntityDataModel error. datasetId=" + entityProcessVO.getDatasetId()
                            + ", entityId=" + entityProcessVO.getEntityDaoVO().getId(), e);
                }
            }
        }
    }


    /**
     * Create Entity 처리
     * - 벌크 처리 시도 중 실패 시 단건 처리
     *
     * @param createList CREATE 대상 VO 리스트
     */
    private void processCreate(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> createList) {
        if (createList == null || createList.size() == 0) return;

        // 벌크 Create Entity 처리
        boolean bulkResult = bulkCreate(createList);

        if (!bulkResult) {
            // 벌크처리 Query 수행 중 에러가 발생한 경우 (단건 처리)
            for (EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO : createList) {
                singleCreate(entityProcessVO);
            }
        }
    }

    /**
     * Entity 벌크 Create 처리
     *
     * @param createList CREATE 대상 VO 리스트
     * @return 처리 결과
     */
    private boolean bulkCreate(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> createList) {

        List<DynamicEntityDaoVO> daoVOList = new ArrayList<>(createList.size());
        for (EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO : createList) {
            daoVOList.add(entityProcessVO.getEntityDaoVO());
        }

        try {
            // 벌크 CREATE 처리
            List<ProcessResultVO> processResultVOList = entityDAO.bulkCreate(daoVOList);

            if (processResultVOList == null) return false;

            for (int i = 0; i < createList.size(); i++) {
                createList.get(i).setProcessResultVO(processResultVOList.get(i));
            }

            return true;
        } catch (Exception e) {
            if (e instanceof org.springframework.dao.DuplicateKeyException) {
                log.warn("bulkCreate Entity duplicate error", e);
            } else {
                log.error("bulkCreate Entity error", e);
            }
            return false;
        }
    }

    /**
     * Entity 단건 Create 처리
     *
     * @param entityProcessVO CREATE 대상 VO
     * @return 처리 결과
     */
    private boolean singleCreate(EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO) {
        ProcessResultVO processResultVO = null;

        try {
            processResultVO = entityDAO.create(entityProcessVO.getEntityDaoVO());

        } catch (Exception e1) {

            String id = entityProcessVO.getEntityDaoVO().getId();

            processResultVO = new ProcessResultVO();
            processResultVO.setProcessOperation(Operation.CREATE_ENTITY);
            processResultVO.setProcessResult(false);

            if (e1 instanceof org.springframework.dao.DuplicateKeyException) {
                log.warn(entityProcessVO.getDatasetId() + " Single Create Entity duplicate error", e1);
                processResultVO.setErrorDescription("Create Entity duplicate ERROR(Create Entity). id=" + id);
                processResultVO.setException(new NgsiLdBadRequestException(ErrorCode.ALREADY_EXISTS, "Create duplicate ERROR(Create Entity). id=" + id, e1));
            } else {
                log.error(entityProcessVO.getDatasetId() + " Single Create Entity error", e1);
                processResultVO.setErrorDescription("SQL ERROR(Create Entity). id=" + id);
                processResultVO.setException(new NgsiLdInternalServerErrorException(ErrorCode.SQL_ERROR, "SQL ERROR(Create Entity). id=" + id, e1));
            }
        }

        entityProcessVO.setProcessResultVO(processResultVO);
        return processResultVO.isProcessResult();
    }


    /**
     * Replace Entity Attributes 처리
     * - 벌크 처리 시도 중 실패 시 단건 처리
     *
     * @param replaceEntity Replace Entity Attributes VO 대상 리스트
     */
    private void processReplaceAttr(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> replaceEntity) {
        if (replaceEntity == null || replaceEntity.size() == 0) return;

        // 벌크 Replace Entity Attributes 처리
        boolean bulkResult = bulkReplaceEntity(replaceEntity);

        if (!bulkResult) {
            // 벌크처리 Query 수행 중 에러가 발생한 경우 (단건 처리)
            for (EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO : replaceEntity) {
                singleReplaceEntity(entityProcessVO);
            }
        }
    }

    /**
     * Replace Entity Attributes 처리
     *
     * @param replaceEntityList Replace Entity Attributes VO 대상 리스트
     * @return 처리 결과
     */
    private boolean bulkReplaceEntity(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> replaceEntityList) {

        List<DynamicEntityDaoVO> daoVOList = new ArrayList<>(replaceEntityList.size());
        for (EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO : replaceEntityList) {
            daoVOList.add(entityProcessVO.getEntityDaoVO());
        }

        try {
            // 벌크 Replace Entity Attributes 처리
            List<ProcessResultVO> processResultVOList = entityDAO.bulkReplaceEntity(daoVOList);

            if (processResultVOList == null) return false;

            for (int i = 0; i < replaceEntityList.size(); i++) {
                replaceEntityList.get(i).setProcessResultVO(processResultVOList.get(i));
            }

            return true;
        } catch (Exception e) {
            log.error("Replace Entity Attributes error", e);
            return false;
        }
    }

    /**
     * Entity 단건 Replace Entity Attributes 처리
     *
     * @param entityProcessVO Replace Entity Attributes 대상 VO
     * @return 처리 결과
     */
    private boolean singleReplaceEntity(EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO) {
        ProcessResultVO processResultVO = null;

        try {
            processResultVO = entityDAO.replaceEntity(entityProcessVO.getEntityDaoVO());

        } catch (Exception e1) {
            log.error(entityProcessVO.getDatasetId() + " Single Replace Entity Attributes error", e1);

            String id = entityProcessVO.getEntityDaoVO().getId();

            processResultVO = new ProcessResultVO();
            processResultVO.setProcessOperation(Operation.REPLACE_ENTITY_ATTRIBUTES);
            processResultVO.setProcessResult(false);
            processResultVO.setException(new NgsiLdInternalServerErrorException(ErrorCode.SQL_ERROR,
                    "SQL ERROR(Replace Entity Attributes). id=" + id));
            processResultVO.setErrorDescription("SQL ERROR(Replace Entity Attributes). id=" + id);
        }

        entityProcessVO.setProcessResultVO(processResultVO);
        return processResultVO.isProcessResult();
    }


    /**
     * Entity Append Entity Attributes 처리
     * - 벌크 처리 시도 중 실패 시 단건 처리
     *
     * @param appendAttrList Append Entity Attributes 대상 VO 리스트
     */
    private void processAppendAttr(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> appendAttrList) {
        if (appendAttrList == null || appendAttrList.size() == 0) return;

        // 벌크 Append Entity Attributes 처리
        boolean bulkResult = bulkAppendAttr(appendAttrList);

        if (!bulkResult) {
            // 벌크처리 Query 수행 중 에러가 발생한 경우 (단건 처리)
            for (EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO : appendAttrList) {
                singleAppendAttr(entityProcessVO);
            }
        }
    }

    /**
     * Entity Append Entity Attributes 처리
     * - 벌크 처리 시도 중 실패 시 단건 처리
     *
     * @param appendNoOverwriteEntityList Append Entity (noOverwrite) Attributes 대상 VO 리스트
     */
    private void processAppendNoOverwriteAttr(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> appendNoOverwriteEntityList) {
        if (appendNoOverwriteEntityList == null || appendNoOverwriteEntityList.size() == 0) return;

        // 벌크 Append Entity Attributes 처리
        boolean bulkResult = bulkAppendNoOverwriteAttr(appendNoOverwriteEntityList);

        if (!bulkResult) {
            // 벌크처리 Query 수행 중 에러가 발생한 경우 (단건 처리)
            for (EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO : appendNoOverwriteEntityList) {
                singleAppendNoOverwriteAttr(entityProcessVO);
            }
        }
    }

    /**
     * Entity 벌크 Append Entity Attributes 처리
     *
     * @param appendAttrList Append Entity Attributes 대상 VO 리스트
     * @return 처리 결과
     */
    private boolean bulkAppendAttr(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> appendAttrList) {

        List<DynamicEntityDaoVO> daoVOList = new ArrayList<>(appendAttrList.size());
        for (EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO : appendAttrList) {
            daoVOList.add(entityProcessVO.getEntityDaoVO());
        }

        try {
            // 벌크 Append Entity Attributes 처리
            List<ProcessResultVO> processResultVOList = entityDAO.bulkAppendAttr(daoVOList);

            if (processResultVOList == null) return false;

            for (int i = 0; i < appendAttrList.size(); i++) {
                appendAttrList.get(i).setProcessResultVO(processResultVOList.get(i));
            }

            return true;
        } catch (Exception e) {
            log.error("bulkAppendAttr error", e);
            return false;
        }
    }

    /**
     * Entity 단건 Append Entity Attributes 처리
     *
     * @param entityProcessVO Append Entity Attributes 대상 VO
     * @return 처리 결과
     */
    private boolean singleAppendAttr(EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO) {
        ProcessResultVO processResultVO = null;

        try {
            processResultVO = entityDAO.appendAttr(entityProcessVO.getEntityDaoVO());

        } catch (Exception e1) {
            log.error(entityProcessVO.getDatasetId() + " Single Append Entity Attributes error", e1);

            String id = entityProcessVO.getEntityDaoVO().getId();

            processResultVO = new ProcessResultVO();
            processResultVO.setProcessOperation(Operation.APPEND_ENTITY_ATTRIBUTES);
            processResultVO.setProcessResult(false);
            processResultVO.setException(new NgsiLdInternalServerErrorException(ErrorCode.SQL_ERROR,
                    "SQL ERROR(Append Entity Attributes). id=" + id));
            processResultVO.setErrorDescription("SQL ERROR(Append Entity Attributes). id=" + id);
        }

        entityProcessVO.setProcessResultVO(processResultVO);
        return processResultVO.isProcessResult();
    }


    /**
     * Entity 벌크 Append Entity (noOverwrite) Attributes 처리
     *
     * @param appendNoOverwriteEntityList Append Entity Attributes 대상 VO 리스트
     * @return 처리 결과
     */
    private boolean bulkAppendNoOverwriteAttr(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> appendNoOverwriteEntityList) {

        List<DynamicEntityDaoVO> daoVOList = new ArrayList<>(appendNoOverwriteEntityList.size());
        for (EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO : appendNoOverwriteEntityList) {
            daoVOList.add(entityProcessVO.getEntityDaoVO());
        }

        try {
            // 벌크 Append Entity Attributes 처리
            List<ProcessResultVO> processResultVOList = entityDAO.bulkAppendNoOverwriteAttr(daoVOList);

            if (processResultVOList == null) return false;

            for (int i = 0; i < appendNoOverwriteEntityList.size(); i++) {
                appendNoOverwriteEntityList.get(i).setProcessResultVO(processResultVOList.get(i));
            }

            return true;
        } catch (Exception e) {
            log.error("bulkAppendAttr error", e);
            return false;
        }
    }

    /**
     * Entity 단건 Append Entity (noOverwrite) Attributes 처리
     *
     * @param entityProcessVO Append Entity Attributes 대상 VO
     * @return 처리 결과
     */
    private boolean singleAppendNoOverwriteAttr(EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO) {
        ProcessResultVO processResultVO = null;

        try {
            processResultVO = entityDAO.appendAttr(entityProcessVO.getEntityDaoVO());

        } catch (Exception e1) {
            log.error(entityProcessVO.getDatasetId() + " Single Append Entity (noOverwrite) Attributes error", e1);

            String id = entityProcessVO.getEntityDaoVO().getId();

            processResultVO = new ProcessResultVO();
            processResultVO.setProcessOperation(Operation.APPEND_ENTITY_ATTRIBUTES);
            processResultVO.setProcessResult(false);
            processResultVO.setException(new NgsiLdInternalServerErrorException(ErrorCode.SQL_ERROR,
                    "SQL ERROR(Append Entity Attributes (noOverwrite)). id=" + id));
            processResultVO.setErrorDescription("SQL ERROR(Append Entity (noOverwrite) Attributes). id=" + id);
        }

        entityProcessVO.setProcessResultVO(processResultVO);
        return processResultVO.isProcessResult();
    }

    /**
     * Update Entity Attributes 처리
     * - 벌크 처리 시도 중 실패 시 단건 처리
     *
     * @param updateAttrList Update Entity Attributes 대상 VO 리스트
     */
    private void processUpdateAttr(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> updateAttrList) {
        if (updateAttrList == null || updateAttrList.size() == 0) return;

        // 벌크 Update Entity Attributes 처리
        boolean bulkResult = bulkUpdateAttr(updateAttrList);

        if (!bulkResult) {
            // 벌크처리 Query 수행 중 에러가 발생한 경우 (단건 처리)
            for (EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO : updateAttrList) {
                singleUpdateAttr(entityProcessVO);
            }
        }
    }

    /**
     * 벌크 Update Entity Attributes 처리
     *
     * @param entityProcessVOList Update Entity Attributes 대상 VO 리스트
     * @return 처리 결과
     */
    private boolean bulkUpdateAttr(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> entityProcessVOList) {

        List<DynamicEntityDaoVO> daoVOList = new ArrayList<>(entityProcessVOList.size());
        for (EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO : entityProcessVOList) {
            daoVOList.add(entityProcessVO.getEntityDaoVO());
        }

        try {
            // 벌크 Update Entity Attributes 처리
            List<ProcessResultVO> processResultVOList = entityDAO.bulkUpdateAttr(daoVOList);

            if (processResultVOList == null) return false;

            for (int i = 0; i < entityProcessVOList.size(); i++) {
                entityProcessVOList.get(i).setProcessResultVO(processResultVOList.get(i));
            }

            return true;
        } catch (Exception e) {
            log.error("bulkUpdateAttr error", e);
            return false;
        }
    }

    /**
     * 단건 Update Entity Attributes 처리
     *
     * @param entityProcessVO Update Entity Attributes 대상 VO
     * @return 처리 결과
     */
    private boolean singleUpdateAttr(EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO) {
        ProcessResultVO processResultVO = null;

        try {
            processResultVO = entityDAO.updateAttr(entityProcessVO.getEntityDaoVO());

        } catch (Exception e1) {
            log.error(entityProcessVO.getDatasetId() + " singleUpdateEntityAttr error", e1);

            String id = entityProcessVO.getEntityDaoVO().getId();

            processResultVO = new ProcessResultVO();
            processResultVO.setProcessOperation(Operation.UPDATE_ENTITY_ATTRIBUTES);
            processResultVO.setProcessResult(false);
            processResultVO.setException(new NgsiLdInternalServerErrorException(ErrorCode.SQL_ERROR,
                    "SQL ERROR(UpdateEntityAttr). id=" + id));
            processResultVO.setErrorDescription("SQL ERROR(UpdateEntityAttr). id=" + id);
        }

        entityProcessVO.setProcessResultVO(processResultVO);
        return processResultVO.isProcessResult();
    }

    /**
     * Partial Attribute Update 처리
     * - 벌크 처리 시도 중 실패 시 단건 처리
     *
     * @param partialAttrUpdateList Partial Attribute Update 대상 VO 리스트
     */
    private void processPartialAttrUpdate(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> partialAttrUpdateList) {
        if (partialAttrUpdateList == null || partialAttrUpdateList.size() == 0) return;

        // 벌크 Update Entity Attributes 처리
        boolean bulkResult = bulkPartialAttrUpdate(partialAttrUpdateList);

        if (!bulkResult) {
            // 벌크처리 Query 수행 중 에러가 발생한 경우 (단건 처리)
            for (EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO : partialAttrUpdateList) {
                singlePartialAttrUpdate(entityProcessVO);
            }
        }
    }

    /**
     * 벌크 Partial Attribute Update 처리
     *
     * @param entityProcessVOList Partial Attribute Update 대상 VO 리스트
     * @return 처리 결과
     */
    private boolean bulkPartialAttrUpdate(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> entityProcessVOList) {

        List<DynamicEntityDaoVO> daoVOList = new ArrayList<>(entityProcessVOList.size());
        for (EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO : entityProcessVOList) {
            daoVOList.add(entityProcessVO.getEntityDaoVO());
        }

        try {
            // 벌크 Partial Attribute Update 처리
            List<ProcessResultVO> processResultVOList = entityDAO.bulkPartialAttrUpdate(daoVOList);

            if (processResultVOList == null) return false;

            for (int i = 0; i < entityProcessVOList.size(); i++) {
                entityProcessVOList.get(i).setProcessResultVO(processResultVOList.get(i));
            }

            return true;
        } catch (Exception e) {
            log.error("bulkPartialAttrUpdate error", e);
            return false;
        }
    }

    /**
     * 단건 Partial Attribute Update 처리
     *
     * @param entityProcessVO Partial Attribute Update 대상 VO
     * @return 처리 결과
     */
    private boolean singlePartialAttrUpdate(EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO) {
        ProcessResultVO processResultVO = null;

        try {
            processResultVO = entityDAO.partialAttrUpdate(entityProcessVO.getEntityDaoVO());

        } catch (Exception e1) {
            log.error(entityProcessVO.getDatasetId() + " singlePartialAttrUpdate error", e1);

            String id = entityProcessVO.getEntityDaoVO().getId();

            processResultVO = new ProcessResultVO();
            processResultVO.setProcessOperation(Operation.PARTIAL_ATTRIBUTE_UPDATE);
            processResultVO.setProcessResult(false);
            processResultVO.setException(new NgsiLdInternalServerErrorException(ErrorCode.SQL_ERROR,
                    "SQL ERROR(PartialAttrUpdate). id=" + id));
            processResultVO.setErrorDescription("SQL ERROR(PartialAttrUpdate). id=" + id);
        }

        entityProcessVO.setProcessResultVO(processResultVO);
        return processResultVO.isProcessResult();
    }

    /**
     * Entity FULL UPSERT 처리
     * - 벌크 처리 시도 중 실패 시 단건 처리
     *
     * @param fullUpsertList FULL UPSERT 대상 VO 리스트
     */
    private void processFullUpsert(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> fullUpsertList) {
        if (fullUpsertList == null || fullUpsertList.size() == 0) return;

        // 벌크 FULL UPSERT 처리
        boolean bulkResult = bulkFullUpsert(fullUpsertList);

        if (!bulkResult) {
            // 벌크처리 Query 수행 중 에러가 발생한 경우 (단건 처리)
            for (EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO : fullUpsertList) {
                singleFullUpsert(entityProcessVO);
            }
        }
    }

    /**
     * Entity 벌크 FULL UPSERT 처리
     *
     * @param fullUpsertList FULL UPSERT 대상 VO 리스트
     * @return 처리 결과
     */
    private boolean bulkFullUpsert(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> fullUpsertList) {

        List<DynamicEntityDaoVO> daoVOList = new ArrayList<>(fullUpsertList.size());
        for (EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO : fullUpsertList) {
            daoVOList.add(entityProcessVO.getEntityDaoVO());
        }

        try {
            // 벌크 Full UPSERT 처리
            List<ProcessResultVO> processResultVOList = entityDAO.bulkFullUpsert(daoVOList);

            if (processResultVOList == null) return false;

            for (int i = 0; i < fullUpsertList.size(); i++) {
                fullUpsertList.get(i).setProcessResultVO(processResultVOList.get(i));
            }

            return true;
        } catch (Exception e) {
            if (e instanceof org.springframework.dao.DuplicateKeyException) {
                log.warn("bulkFullUpsert create duplicate error. daoVOList size={}", daoVOList.size());
            } else {
                log.error("bulkFullUpsert error", e);
            }
            return false;
        }
    }

    /**
     * Entity 단건 FULL UPSERT 처리
     *
     * @param entityProcessVO FULL UPSERT 대상 VO
     * @return 처리 결과
     */
    private boolean singleFullUpsert(EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO) {
        ProcessResultVO processResultVO = null;

        try {

            processResultVO = entityDAO.fullUpsert(entityProcessVO.getEntityDaoVO());

        } catch (Exception e1) {

            String id = entityProcessVO.getEntityDaoVO().getId();

            processResultVO = new ProcessResultVO();
            processResultVO.setProcessOperation(Operation.CREATE_ENTITY_OR_REPLACE_ENTITY_ATTRIBUTES);
            processResultVO.setProcessResult(false);

            if (e1 instanceof org.springframework.dao.DuplicateKeyException) {
//                log.warn(entityProcessVO.getDatasetId() + " singleFullUpsert create duplicate error", e1);
//                processResultVO.setErrorDescription("Create duplicate ERROR(FullUpsert). id=" + id);
//                processResultVO.setException(new NgsiLdBadRequestException(ErrorCode.ALREADY_EXISTS, "Create duplicate ERROR(FullUpsert). id=" + id, e1));

                // Upsert 인데 Dupl이 발생한 경우 정상으로 처리
                //  - 수집 데이터가 최종데이터보다 이전 데이터이기 때문에 업데이트 되지 않은 케이스
                processResultVO.setProcessOperation(Operation.REPLACE_ENTITY_ATTRIBUTES);
                processResultVO.setProcessResult(true);
            } else {
                log.error(entityProcessVO.getDatasetId() + " singleFullUpsert error", e1);
                processResultVO.setErrorDescription("SQL ERROR(FullUpsert). id=" + id);
                processResultVO.setException(new NgsiLdInternalServerErrorException(ErrorCode.SQL_ERROR, "SQL ERROR(FullUpsert). id=" + id));
            }
        }

        entityProcessVO.setProcessResultVO(processResultVO);
        return processResultVO.isProcessResult();
    }

    /**
     * Entity Partial Upsert 처리
     * 벌크 처리 시도 중 실패 시 단건 처리
     *
     * @param partialUpsertList
     */
    private void processPartialUpsert(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> partialUpsertList) {
        if (partialUpsertList == null || partialUpsertList.size() == 0) return;

        // 벌크 PARTIAL UPSERT 처리
        boolean bulkResult = bulkPartialUpsert(partialUpsertList);

        if (!bulkResult) {
            // 벌크처리 Query 수행 중 에러가 발생한 경우 (단건 처리)
            for (EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO : partialUpsertList) {
                singlePartialUpsert(entityProcessVO);
            }
        }
    }

    /**
     * Entity 벌크 PARTIAL UPSERT 처리
     *
     * @param partialUpsertList PARTIAL UPSERT 대상 VO 리스트
     * @return 처리 결과
     */
    private boolean bulkPartialUpsert(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> partialUpsertList) {

        List<DynamicEntityDaoVO> daoVOList = new ArrayList<>(partialUpsertList.size());
        for (EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO : partialUpsertList) {
            daoVOList.add(entityProcessVO.getEntityDaoVO());
        }

        try {
            // 벌크 PARTIAL UPSERT 처리
            List<ProcessResultVO> processResultVOList = entityDAO.bulkPartialUpsert(daoVOList);

            if (processResultVOList == null) return false;

            for (int i = 0; i < partialUpsertList.size(); i++) {
                partialUpsertList.get(i).setProcessResultVO(processResultVOList.get(i));
            }

            return true;
        } catch (Exception e) {
            if (e instanceof org.springframework.dao.DuplicateKeyException) {
                log.warn("bulkPartialUpsert create duplicate error. daoVOList size={}", daoVOList.size());
            } else {
                log.error("bulkPartialUpsert error", e);
            }
            return false;
        }
    }

    /**
     * Entity 단건 PARTIAL UPSERT 처리
     *
     * @param entityProcessVO PARTIAL UPSERT 대상 VO
     * @return 처리 결과
     */
    private boolean singlePartialUpsert(EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO) {
        ProcessResultVO processResultVO = null;

        try {
            processResultVO = entityDAO.partialUpsert(entityProcessVO.getEntityDaoVO());

        } catch (Exception e1) {

            String id = entityProcessVO.getEntityDaoVO().getId();

            processResultVO = new ProcessResultVO();
            processResultVO.setProcessOperation(Operation.CREATE_ENTITY_OR_APPEND_ENTITY_ATTRIBUTES);
            processResultVO.setProcessResult(false);

            if (e1 instanceof org.springframework.dao.DuplicateKeyException) {
//                log.warn(entityProcessVO.getDatasetId() + " singlePartialUpsert create duplicate error", e1);
//                processResultVO.setErrorDescription("Create duplicate ERROR(PartialUpsert). id=" + id);
//                processResultVO.setException(new NgsiLdBadRequestException(ErrorCode.ALREADY_EXISTS, "Create duplicate ERROR(PartialUpsert). id=" + id, e1));

                // Upsert 인데 Dupl이 발생한 경우 정상으로 처리
                //  - 수집 데이터가 최종데이터보다 이전 데이터이기 때문에 업데이트 되지 않은 케이스
                processResultVO.setProcessOperation(Operation.REPLACE_ENTITY_ATTRIBUTES);
                processResultVO.setProcessResult(true);
            } else {
                log.error(entityProcessVO.getDatasetId() + " singlePartialUpsert error", e1);
                processResultVO.setErrorDescription("SQL ERROR(PartialUpsert). id=" + id);
                processResultVO.setException(new NgsiLdInternalServerErrorException(ErrorCode.SQL_ERROR, "SQL ERROR(PartialUpsert). id=" + id));
            }
        }

        entityProcessVO.setProcessResultVO(processResultVO);
        return processResultVO.isProcessResult();
    }


    /**
     * Entity Delete 처리
     * 벌크 처리 시도 중 실패 시 단건 처리
     *
     * @param deleteList
     */
    private void processDelete(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> deleteList) {
        if (deleteList == null || deleteList.size() == 0) return;

        // 벌크 DELETE 처리
        boolean bulkResult = bulkDelete(deleteList);

        if (!bulkResult) {
            // 벌크처리 Query 수행 중 에러가 발생한 경우 (단건 처리)
            for (EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO : deleteList) {
                singleDelete(entityProcessVO);
            }
        }
    }

    /**
     * Entity 벌크 Delete 처리
     *
     * @param deleteList DELETE 대상 VO 리스트
     * @return 처리 결과
     */
    private boolean bulkDelete(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> deleteList) {

        List<DynamicEntityDaoVO> daoVOList = new ArrayList<>(deleteList.size());
        for (EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO : deleteList) {
            daoVOList.add(entityProcessVO.getEntityDaoVO());
        }

        try {
            // 벌크 DELETE 처리
            List<ProcessResultVO> processResultVOList = entityDAO.bulkDelete(daoVOList);

            if (processResultVOList == null) return false;

            for (int i = 0; i < deleteList.size(); i++) {
                deleteList.get(i).setProcessResultVO(processResultVOList.get(i));
            }

            return true;
        } catch (Exception e) {
            log.error("BulkDelete error", e);
            return false;
        }
    }

    /**
     * Entity 단건 Delete 처리
     *
     * @param entityProcessVO DELETE 대상 VO
     * @return 처리 결과
     */
    private boolean singleDelete(EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO) {
        ProcessResultVO processResultVO = null;

        try {
            processResultVO = entityDAO.delete(entityProcessVO.getEntityDaoVO());

        } catch (Exception e1) {
            log.error(entityProcessVO.getDatasetId() + " delete error", e1);

            String id = entityProcessVO.getEntityDaoVO().getId();

            processResultVO = new ProcessResultVO();
            processResultVO.setProcessOperation(Operation.DELETE_ENTITY);
            processResultVO.setProcessResult(false);
            processResultVO.setErrorDescription("SQL ERROR(Delete). id=" + id);
            processResultVO.setException(new NgsiLdInternalServerErrorException(ErrorCode.SQL_ERROR,
                    "SQL ERROR(Delete). id=" + id, e1));
        }

        entityProcessVO.setProcessResultVO(processResultVO);
        return processResultVO.isProcessResult();
    }

    /**
     * Entity DeleteAttr 처리
     *
     * @param deleteAttrList
     */
    private void processDeleteAttr(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> deleteAttrList) {
        if (deleteAttrList == null || deleteAttrList.size() == 0) return;

        for (EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO : deleteAttrList) {

            // DELETE ATTRIBUTES 처리
            singleDeleteAttr(entityProcessVO);
        }
    }

    /**
     * Entity 단건 DeleteAttr 처리
     *
     * @param entityProcessVO DELETE 대상 VO
     * @return 처리 결과
     */
    private boolean singleDeleteAttr(EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO) {
        ProcessResultVO processResultVO = null;

        try {
            // 2. Attribute Delete 처리
            processResultVO = entityDAO.deleteAttr(entityProcessVO.getEntityDaoVO());

        } catch (Exception e1) {
            log.error(entityProcessVO.getDatasetId() + " deleteAttr error", e1);

            String id = entityProcessVO.getEntityDaoVO().getId();

            processResultVO = new ProcessResultVO();
            processResultVO.setProcessOperation(Operation.DELETE_ENTITY_ATTRIBUTES);
            processResultVO.setProcessResult(false);
            processResultVO.setErrorDescription("SQL ERROR(DeleteAttr). id=" + id);
            processResultVO.setException(new NgsiLdInternalServerErrorException(ErrorCode.SQL_ERROR,
                    "SQL ERROR(DeleteAttr). id=" + id, e1));
        }

        entityProcessVO.setProcessResultVO(processResultVO);
        return processResultVO.isProcessResult();
    }


    /**
     * 최종데이터 리스트 처리
     *
     * @param queryVO 요청 파라미터
     * @param accept  accept 타입
     * @return
     */
    @Override
    public List<CommonEntityVO> selectAll(QueryVO queryVO, String accept) {

        if (log.isDebugEnabled()) {
            StringBuilder requestParams = new StringBuilder();
            requestParams.append("entityType : ").append(queryVO.getType())
                    .append(", params(queryVO) : ").append(queryVO.toString());

            //조회 조건 확인
            log.debug("request msg='{}'", requestParams);
        }

        // 데이터셋 유효성 체크
        if(!ValidateUtil.isEmptyData(queryVO.getDatasetId())) {
            if(dataModelManager.getDatasetCache(queryVO.getDatasetId()) == null) {
                throw new NgsiLdBadRequestException(ErrorCode.NOT_EXISTS_DATASET, "Not exist dataset. datasetId=" + queryVO.getDatasetId());
            }
        }

        if(ValidateUtil.isEmptyData(queryVO.getType())) {
            List<DataModelCacheVO> dataModelCacheVOs = dataModelManager.getTargetDataModelByQueryUri(queryVO.getLinks(), queryVO);
            return selectAllWithMultiType(dataModelCacheVOs, queryVO, accept);
        } else if(queryVO.getType().contains(",")) {
            String[] typeArrs = queryVO.getType().split(",");
            List<DataModelCacheVO> dataModelCacheVOs = new ArrayList<>();
            for(String type : typeArrs) {
                DataModelCacheVO dataModelCacheVO = dataModelManager.getDataModelVOCacheByContext(queryVO.getLinks(), type);
                if(dataModelCacheVO != null) {
                    dataModelCacheVOs.add(dataModelCacheVO);
                }
            }
            return selectAllWithMultiType(dataModelCacheVOs, queryVO, accept);
        } else {
            return selectAllWithType(queryVO, accept);
        }
    }

    private List<CommonEntityVO> selectAllWithMultiType(List<DataModelCacheVO> dataModelCacheVOs, QueryVO queryVO, String accept) {

        if (ValidateUtil.isEmptyData(dataModelCacheVOs)) {
            throw new NgsiLdBadRequestException(ErrorCode.NOT_EXIST_ENTITY, "Not exist entityTypes. type=" + queryVO.getType() + ", Link=" + queryVO.getLinks());
        }

        List<CommonEntityVO> totalCommonEntityVOs = new ArrayList<>();

        for (DataModelCacheVO dataModelCacheVO : dataModelCacheVOs) {
            if(dataModelCacheVO.getCreatedStorageTypes() != null
                    && dataModelCacheVO.getCreatedStorageTypes().contains(this.getStorageType())) {

                if(dataModelCacheVO.getDataModelVO().getTypeUri() == null) {
                    log.warn("selectAll Invalid DataModel. typeUri is null. dataModelId={}", dataModelCacheVO.getDataModelVO().getId());
                    continue;
                }

                QueryVO innerQueryVO = (QueryVO) SerializationUtils.clone(queryVO);
                innerQueryVO.setType(dataModelCacheVO.getDataModelVO().getTypeUri());
                innerQueryVO.setLinks(null);
                innerQueryVO.setOffset(queryVO.getOffset());
                innerQueryVO.setLimit(queryVO.getLimit());
                List<CommonEntityVO> commonEntityVOs = this.selectAllWithType(innerQueryVO, accept);
                if (!ValidateUtil.isEmptyData(commonEntityVOs)) {
                    totalCommonEntityVOs.addAll(commonEntityVOs);
                }
            }
        }

        Collections.sort(totalCommonEntityVOs);
        return extractSubListWithoutType(totalCommonEntityVOs, queryVO.getLimit(), queryVO.getOffset());
    }

    public List<CommonEntityVO> selectAllWithType(QueryVO queryVO, String accept) {

        // 1. type 조회 및 검증
        DataModelCacheVO dataModelCacheVO = dataModelManager.getDataModelVOCacheByContext(queryVO.getLinks(), queryVO.getType());
        if (dataModelCacheVO == null) {
            throw new NgsiLdBadRequestException(ErrorCode.NOT_EXIST_ENTITY, "Invalid Type. entityType=" + queryVO.getType() + ", link=" + queryVO.getLinks());
        }

        // 2. q-query 파라미터가 type에 속하는 지 검증
        dataModelManager.validateGeoAndQQuery(queryVO.getLinks(), queryVO, dataModelCacheVO);

        queryVO.setDataModelCacheVO(dataModelCacheVO);

        // 데이터모델의 테이블이 아직 생성되지 않은 경우 (데이터셋 흐름 설정이 되지 않거나 해당 storageType과 일치하지 않는 경우)
        if(dataModelCacheVO.getCreatedStorageTypes() == null
                || !dataModelCacheVO.getCreatedStorageTypes().contains(this.getStorageType())) {
            return new ArrayList<>();
        }

        // 1. Entity 목록 DB 조회
        List<DynamicEntityDaoVO> entityDaoVOList = entityDAO.selectAll(queryVO);

        List<CommonEntityVO> commonEntityVOList = new ArrayList<>();

        if (entityDaoVOList != null) {
            for (DynamicEntityDaoVO entityDaoVO : entityDaoVOList) {

                CommonEntityVO commonEntityVO = null;

                // 2. options 조건에 따라 분기 처리
                if (queryVO.getOptions() != null && queryVO.getOptions().contains(RetrieveOptions.KEY_VALUES.getCode())) {
                    // options = keyValues 일 경우 처리, Simplified Representation
                    commonEntityVO = this.daoVOToSimpleRepresentationVO(entityDaoVO, dataModelCacheVO, queryVO.getAttrs());
                } else {

                    boolean includeSysAttrs = false;
                    if (queryVO.getOptions() != null && queryVO.getOptions().contains(RetrieveOptions.SYS_ATTRS.getCode())) {
                        includeSysAttrs = true;
                    }
                    // options이 없을 경우 처리, Full Representation
                    commonEntityVO = this.daoVOToFullRepresentationVO(entityDaoVO, dataModelCacheVO, includeSysAttrs, queryVO.getAttrs());
                }

                // 3. 요청 header의 accept가 'application/ld+json' 일 경우 @context 정보 추가
                if (commonEntityVO != null && accept.equals(Constants.APPLICATION_LD_JSON_VALUE)) {
                    List<String> link = getLinkOrDefault(queryVO.getLinks());
                    if (!ValidateUtil.isEmptyData(link)) {
                        commonEntityVO.setContext(link);
                    }
                }

                if(commonEntityVO != null) {
                    commonEntityVOList.add(commonEntityVO);
                }
            }

            // 4. term expand
            commonEntityVOList.forEach(
                    commonEntityVO -> commonEntityVO.expandTerm(
                            dataModelManager.contextToFlatMap(getLinkOrDefault(queryVO.getLinks())),
                            dataModelManager.contextToFlatMap(dataModelCacheVO.getDataModelVO().getContext())
                    )
            );
        }

        return commonEntityVOList;
    }


    /**
     * 최종데이터 단건 처리
     *
     * @param queryVO    요청 파라미터
     * @return
     */
    @Override
    public CommonEntityVO selectById(QueryVO queryVO, String accept, Boolean useForCreateOperation) {

        if (log.isDebugEnabled()) {
            StringBuilder requestParams = new StringBuilder();
            requestParams.append("params(queryVO)=").append(queryVO.toString());

            //조회 조건 확인
            log.debug("request msg='{}'", requestParams);
        }

        // 1. 데이터셋 유효성 체크
        if(!ValidateUtil.isEmptyData(queryVO.getDatasetId())) {
            if(dataModelManager.getDatasetCache(queryVO.getDatasetId()) == null) {
                throw new NgsiLdBadRequestException(ErrorCode.NOT_EXISTS_DATASET, "Not exist dataset. datasetId=" + queryVO.getDatasetId());
            }
        }

        // 2. 데이터모델 정보 조회
        EntityDataModelVO entityDataModelVO = entityDataModelSVC.getEntityDataModelVOById(queryVO.getId());
        if (entityDataModelVO == null) {
            throw new NgsiLdResourceNotFoundException(DataServiceBrokerCode.ErrorCode.NOT_EXIST_ID, "There is no Entity instance with the requested identifier.");
        }

        queryVO.setDataModelId(entityDataModelVO.getDataModelId());
        queryVO.setType(entityDataModelVO.getDataModelType());

        DataModelCacheVO dataModelCacheVO = dataModelManager.getDataModelVOCacheById(entityDataModelVO.getDataModelId());
        queryVO.setDataModelCacheVO(dataModelCacheVO);

        // 데이터모델의 테이블이 아직 생성되지 않은 경우 (데이터셋 흐름 설정이 되지 않거나 해당 storageType과 일치하지 않는 경우)
        if(dataModelCacheVO.getCreatedStorageTypes() == null
                || !dataModelCacheVO.getCreatedStorageTypes().contains(this.getStorageType())) {
            return null;
        }

        CommonEntityVO commonEntityVO = null;

        // 3. entity 목록 DB 조회
        DynamicEntityDaoVO entityDaoVO = entityDAO.selectById(queryVO, useForCreateOperation);

        if (entityDaoVO != null) {

            // 4. options 조건에 따라 분기 처리
            if (queryVO.getOptions() != null && queryVO.getOptions().contains(RetrieveOptions.KEY_VALUES.getCode())) {
                // options = keyValues 일 경우 처리, Simplified Representation
                commonEntityVO = this.daoVOToSimpleRepresentationVO(entityDaoVO, dataModelCacheVO, queryVO.getAttrs());

            } else {

                boolean includeSysAttrs = false;
                if (queryVO.getOptions() != null && queryVO.getOptions().contains(RetrieveOptions.SYS_ATTRS.getCode())) {
                    includeSysAttrs = true;
                }
                // options이 없을 경우 처리, Full Representation
                commonEntityVO = this.daoVOToFullRepresentationVO(entityDaoVO, dataModelCacheVO, includeSysAttrs, queryVO.getAttrs());
            }

            // 5. term expand
            commonEntityVO.expandTerm(
                    dataModelManager.contextToFlatMap(getLinkOrDefault(queryVO.getLinks())),
                    dataModelManager.contextToFlatMap(dataModelCacheVO.getDataModelVO().getContext())
            );
        }

        // 5. 요청 header의 accept가 'application/ld+json' 일 경우 @context 정보 추가
        if (commonEntityVO != null && accept.equals(Constants.APPLICATION_LD_JSON_VALUE)) {
            List<String> link = getLinkOrDefault(queryVO.getLinks());
            if (!ValidateUtil.isEmptyData(link)) {
                commonEntityVO.setContext(link);
            }
        }

        return commonEntityVO;
    }

    /**
     * 이력데이터 리스트 처리
     *
     * @param queryVO 요청 파라미터
     * @param accept  entity 타입
     * @return
     */
    @Override
    public List<CommonEntityVO> selectTemporal(QueryVO queryVO, String accept) {

        if (log.isDebugEnabled()) {
            StringBuilder requestParams = new StringBuilder();
            requestParams.append("entityType : ").append(queryVO.getType())
                    .append(", params(queryVO) : ").append(queryVO.toString());

            //조회 조건 확인
            log.debug("request msg='{}'", requestParams);
        }

        // 데이터셋 유효성 체크
        if(!ValidateUtil.isEmptyData(queryVO.getDatasetId())) {
            if(dataModelManager.getDatasetCache(queryVO.getDatasetId()) == null) {
                throw new NgsiLdBadRequestException(ErrorCode.NOT_EXISTS_DATASET, "Not exist dataset. datasetId=" + queryVO.getDatasetId());
            }
        }

        if(ValidateUtil.isEmptyData(queryVO.getType())) {
            List<DataModelCacheVO> dataModelCacheVOs = dataModelManager.getTargetDataModelByQueryUri(queryVO.getLinks(), queryVO);
            if (dataModelCacheVOs == null) {
                throw new NgsiLdBadRequestException(ErrorCode.NOT_EXIST_ENTITY, "Not Exist EntityTypes . Link=" + String.join(",", queryVO.getLinks()));
            }

            List<CommonEntityVO> totalCommonEntityVOs = new ArrayList<>();

            for (DataModelCacheVO dataModelCacheVO : dataModelCacheVOs) {
                if(dataModelCacheVO.getCreatedStorageTypes() != null
                        && dataModelCacheVO.getCreatedStorageTypes().contains(this.getStorageType())) {

                    if(dataModelCacheVO.getDataModelVO().getTypeUri() == null) {
                        log.warn("selectTemporal Invalid DataModel. typeUri is null. dataModelId={}", dataModelCacheVO.getDataModelVO().getId());
                        continue;
                    }

                    QueryVO copiedQueryVO = (QueryVO) SerializationUtils.clone(queryVO);
                    copiedQueryVO.setType(dataModelCacheVO.getDataModelVO().getTypeUri());
                    copiedQueryVO.setLinks(null);
                    List<CommonEntityVO> commonEntityVOs = this.selectTemporalWithType(copiedQueryVO, accept);
                    if (!ValidateUtil.isEmptyData(commonEntityVOs)) {
                        totalCommonEntityVOs.addAll(commonEntityVOs);
                    }
                }
            }

            Collections.sort(totalCommonEntityVOs);
            return extractSubListWithoutType(totalCommonEntityVOs, queryVO.getLimit(), queryVO.getOffset());
        } else {
            return selectTemporalWithType(queryVO, accept);
        }

    }

    public List<CommonEntityVO> selectTemporalWithType(QueryVO queryVO, String accept) {

        if (log.isDebugEnabled()) {

            StringBuilder requestParams = new StringBuilder();
            requestParams.append("entityType=").append(queryVO.getType())
                    .append(", params(queryVO)=").append(queryVO.toString());

            //조회 조건 확인
            log.debug("request msg='{}'", requestParams);
        }

        // 1. type 조회 및 검증
        DataModelCacheVO dataModelCacheVO = dataModelManager.getDataModelVOCacheByContext(queryVO.getLinks(), queryVO.getType());
        if (dataModelCacheVO == null) {
            throw new NgsiLdBadRequestException(ErrorCode.NOT_EXIST_ENTITY, "Invalid Type. entityType=" + queryVO.getType() + ", link=" + queryVO.getLinks());
        }

        // 2. q-query 파라미터가 type에 속하는 지 검증
        dataModelManager.validateGeoAndQQuery(queryVO.getLinks(), queryVO, dataModelCacheVO);

        queryVO.setDataModelCacheVO(dataModelCacheVO);

        // 데이터모델의 테이블이 아직 생성되지 않은 경우 (데이터셋 흐름 설정이 되지 않거나 해당 storageType과 일치하지 않는 경우)
        if(dataModelCacheVO.getCreatedStorageTypes() == null
                || !dataModelCacheVO.getCreatedStorageTypes().contains(this.getStorageType())) {
            return new ArrayList<>();
        }

        // 1. entity 목록 DB 조회
        List<DynamicEntityDaoVO> entityDaoVOList = entityDAO.selectAllHist(queryVO);
        List<CommonEntityVO> commonEntityVOList = new ArrayList<>();

        // 2. options 조건에 따라 분기 처리
        if (entityDaoVOList != null) {

            if (queryVO.getOptions() != null && queryVO.getOptions().contains(RetrieveOptions.NORMALIZED_HISTORY.getCode())) {
                commonEntityVOList = this.daoVOToTemporalNormalizedHistoryRepresentationVO(entityDaoVOList, dataModelCacheVO, accept);

            } else if (queryVO.getOptions() != null && queryVO.getOptions().contains(RetrieveOptions.TEMPORAL_VALUES.getCode())) {
                Integer lastN = queryVO.getLastN();
                commonEntityVOList = this.daoVOToTemporalFullRepresentationVO(entityDaoVOList, dataModelCacheVO, lastN, accept);
                commonEntityVOList = this.daoVOToTemporalTemporalValuesRepresentationVO(commonEntityVOList);

            } else {
                Integer lastN = queryVO.getLastN();
                commonEntityVOList = this.daoVOToTemporalFullRepresentationVO(entityDaoVOList, dataModelCacheVO, lastN, accept);
            }
        }

        // 요청 header의 accept가 'application/ld+json' 일 경우 @context 정보 추가
        if (accept.equals(Constants.APPLICATION_LD_JSON_VALUE)) {
            List<String> link = getLinkOrDefault(queryVO.getLinks());
            if (!ValidateUtil.isEmptyData(link)) {
                commonEntityVOList.forEach(commonEntityVO -> commonEntityVO.setContext(link));
            }
        }

        // term expand
        commonEntityVOList.forEach(commonEntityVO -> commonEntityVO.expandTerm(
                dataModelManager.contextToFlatMap(getLinkOrDefault(queryVO.getLinks())),
                dataModelManager.contextToFlatMap(dataModelCacheVO.getDataModelVO().getContext())
        ));

        return commonEntityVOList;
    }


    /**
     * 이력데이터 딘건 처리
     *
     * @param queryVO    요청 파라미터
     * @param accept
     * @return
     */
    @Override
    public CommonEntityVO selectTemporalById(QueryVO queryVO, String accept) {

        if (log.isDebugEnabled()) {
            StringBuilder requestParams = new StringBuilder();
            requestParams.append("entityType=").append(queryVO.getType())
                    .append(", params(queryVO)=").append(queryVO.toString());

            //조회 조건 확인
            log.debug("request msg='{}'", requestParams);
        }

        // 데이터셋 유효성 체크
        if(!ValidateUtil.isEmptyData(queryVO.getDatasetId())) {
            if(dataModelManager.getDatasetCache(queryVO.getDatasetId()) == null) {
                throw new NgsiLdBadRequestException(ErrorCode.NOT_EXISTS_DATASET, "Not exist dataset. datasetId=" + queryVO.getDatasetId());
            }
        }

        // 1. 데이터모델 정보 조회
        EntityDataModelVO entityDataModelVO = entityDataModelSVC.getEntityDataModelVOById(queryVO.getId());
        if (entityDataModelVO == null) {
            throw new NgsiLdResourceNotFoundException(DataServiceBrokerCode.ErrorCode.NOT_EXIST_ID, "There is no Entity instance with the requested identifier.﻿");
        }

        queryVO.setDataModelId(entityDataModelVO.getDataModelId());
        queryVO.setType(entityDataModelVO.getDataModelType());

        DataModelCacheVO dataModelCacheVO = dataModelManager.getDataModelVOCacheById(entityDataModelVO.getDataModelId());
        queryVO.setDataModelCacheVO(dataModelCacheVO);

        // 데이터모델의 테이블이 아직 생성되지 않은 경우 (데이터셋 흐름 설정이 되지 않거나 해당 storageType과 일치하지 않는 경우)
        if(dataModelCacheVO.getCreatedStorageTypes() == null
                || !dataModelCacheVO.getCreatedStorageTypes().contains(this.getStorageType())) {
            return new CommonEntityVO();
        }

        // 2. entity 목록 DB 조회
        List<DynamicEntityDaoVO> entityDaoVOList = entityDAO.selectHistById(queryVO);

        // 3. options 조건에 따라 분기 처리
        List<CommonEntityVO> commonEntityVOList = new ArrayList<>();

        if (entityDaoVOList != null) {

            if (queryVO.getOptions() != null && queryVO.getOptions().contains(RetrieveOptions.NORMALIZED_HISTORY.getCode())) {
                commonEntityVOList = this.daoVOToTemporalNormalizedHistoryRepresentationVO(entityDaoVOList, dataModelCacheVO, accept);

            } else if (queryVO.getOptions() != null && queryVO.getOptions().contains(RetrieveOptions.TEMPORAL_VALUES.getCode())) {
                Integer lastN = queryVO.getLastN();
                commonEntityVOList = this.daoVOToTemporalFullRepresentationVO(entityDaoVOList, dataModelCacheVO, lastN, accept);
                commonEntityVOList = this.daoVOToTemporalTemporalValuesRepresentationVO(commonEntityVOList);

            } else {
                Integer lastN = queryVO.getLastN();
                commonEntityVOList = this.daoVOToTemporalFullRepresentationVO(entityDaoVOList, dataModelCacheVO, lastN, accept);
            }
        }

        CommonEntityVO commonEntityVO;

        if (commonEntityVOList != null && commonEntityVOList.size() > 0) {
            commonEntityVO = commonEntityVOList.get(0);

            // 요청 header의 accept가 'application/ld+json' 일 경우 @context 정보 추가
            if (accept.equals(Constants.APPLICATION_LD_JSON_VALUE)) {
                List<String> link = getLinkOrDefault(queryVO.getLinks());
                if (!ValidateUtil.isEmptyData(link)) {
                    commonEntityVO.setContext(link);
                }
            }

        } else {
            //조회된 결과값이 없을 경우, 규격을 맞추기 위해 빈 객체 리턴함
            commonEntityVO = new CommonEntityVO();
        }

        // term expand
        commonEntityVO.expandTerm(
                dataModelManager.contextToFlatMap(getLinkOrDefault(queryVO.getLinks())),
                dataModelManager.contextToFlatMap(dataModelCacheVO.getDataModelVO().getContext())
        );

        return commonEntityVO;
    }


    @Override
    public Integer selectCount(QueryVO queryVO) {

        if (log.isDebugEnabled()) {
            StringBuilder requestParams = new StringBuilder();
            requestParams.append("entityType : ").append(queryVO.getType())
                    .append(", params(queryVO) : ").append(queryVO.toString());

            //조회 조건 확인
            log.debug("request msg='{}'", requestParams);
        }

        // 데이터셋 유효성 체크
        if(!ValidateUtil.isEmptyData(queryVO.getDatasetId())) {
            if(dataModelManager.getDatasetCache(queryVO.getDatasetId()) == null) {
                throw new NgsiLdBadRequestException(ErrorCode.NOT_EXISTS_DATASET, "Not exist dataset. datasetId=" + queryVO.getDatasetId());
            }
        }

        if(ValidateUtil.isEmptyData(queryVO.getType())) {
            List<DataModelCacheVO> dataModelCacheVOs = dataModelManager.getTargetDataModelByQueryUri(queryVO.getLinks(), queryVO);
            return getEntityCountWithMultiType(queryVO, dataModelCacheVOs);

        } else if(queryVO.getType().contains(",")) {
            String[] typeArrs = queryVO.getType().split(",");
            List<DataModelCacheVO> dataModelCacheVOs = new ArrayList<>();
            for(String type : typeArrs) {
                DataModelCacheVO dataModelCacheVO = dataModelManager.getDataModelVOCacheByContext(queryVO.getLinks(), type);
                if(dataModelCacheVO != null) {
                    dataModelCacheVOs.add(dataModelCacheVO);
                }
            }
            return getEntityCountWithMultiType(queryVO, dataModelCacheVOs);
        } else {
            return getEntityCountWithType(queryVO);
        }
    }

    private Integer getEntityCountWithMultiType(QueryVO queryVO, List<DataModelCacheVO> dataModelCacheVOs) {
        if (dataModelCacheVOs == null) {
            throw new NgsiLdBadRequestException(ErrorCode.NOT_EXIST_ENTITY, "Not Exist EntityTypes . Context=" + String.join(",", queryVO.getLinks()));
        }

        Integer totalCount = 0;

        try {
            for (DataModelCacheVO dataModelCacheVO : dataModelCacheVOs) {
                if(dataModelCacheVO.getCreatedStorageTypes() != null
                        && dataModelCacheVO.getCreatedStorageTypes().contains(this.getStorageType())) {

                    if(dataModelCacheVO.getDataModelVO().getTypeUri() == null) {
                        log.warn("SelectCount Invalid DataModel. typeUri is null. dataModelId={}", dataModelCacheVO.getDataModelVO().getId());
                        continue;
                    }

                    QueryVO innerQueryVO = (QueryVO) SerializationUtils.clone(queryVO);
                    innerQueryVO.setType(dataModelCacheVO.getDataModelVO().getTypeUri());
                    innerQueryVO.setLinks(null);
                    Integer cnt = this.getEntityCountWithType(innerQueryVO);
                    if (!ValidateUtil.isEmptyData(cnt)) {
                        totalCount = totalCount + cnt;
                    }
                }
            }
        } catch (NgsiLdBadRequestException ne) {
            log.warn("selectCount error", ne);
        }

        // Entity 목록 건수 조회
        return totalCount;
    }


    private Integer getEntityCountWithType(QueryVO queryVO) {

        // 1. type 조회 및 검증
        DataModelCacheVO dataModelCacheVO = dataModelManager.getDataModelVOCacheByContext(queryVO.getLinks(), queryVO.getType());
        if (dataModelCacheVO == null) {
            throw new NgsiLdBadRequestException(ErrorCode.NOT_EXIST_ENTITY, "Invalid Type. entityType=" + queryVO.getType() + ", link=" + queryVO.getLinks());
        }

        // 2. q-query 파라미터가 type에 속하는 지 검증
        dataModelManager.validateGeoAndQQuery(queryVO.getLinks(), queryVO, dataModelCacheVO);

        queryVO.setDataModelCacheVO(dataModelCacheVO);

        // 데이터모델의 테이블이 아직 생성되지 않은 경우 (데이터셋 흐름 설정이 되지 않거나 해당 storageType과 일치하지 않는 경우)
        if(dataModelCacheVO.getCreatedStorageTypes() == null
                || !dataModelCacheVO.getCreatedStorageTypes().contains(this.getStorageType())) {
            return 0;
        }

        // Entity 목록 건수 조회
        return entityDAO.selectCount(queryVO);
    }



    @Override
    public Integer selectTemporalCount(QueryVO queryVO) {

        if (log.isDebugEnabled()) {
            StringBuilder requestParams = new StringBuilder();
            requestParams.append(", params(queryVO) : ").append(queryVO.toString());
            //조회 조건 확인
            log.debug("request msg='{}'", requestParams);
        }

        // 데이터셋 유효성 체크
        if(!ValidateUtil.isEmptyData(queryVO.getDatasetId())) {
            if(dataModelManager.getDatasetCache(queryVO.getDatasetId()) == null) {
                throw new NgsiLdBadRequestException(ErrorCode.NOT_EXISTS_DATASET, "Not exist dataset. datasetId=" + queryVO.getDatasetId());
            }
        }

        if(ValidateUtil.isEmptyData(queryVO.getType())) {
            List<DataModelCacheVO> dataModelCacheVOs = dataModelManager.getTargetDataModelByQueryUri(queryVO.getLinks(), queryVO);
            if (dataModelCacheVOs == null) {
                throw new NgsiLdBadRequestException(ErrorCode.NOT_EXIST_ENTITY, "Not Exist EntityTypes . Link=" + String.join(",", queryVO.getLinks()));
            }

            Integer totalCount = 0;

            for (DataModelCacheVO dataModelCacheVO : dataModelCacheVOs) {
                if(dataModelCacheVO.getCreatedStorageTypes() != null
                        && dataModelCacheVO.getCreatedStorageTypes().contains(this.getStorageType())) {

                    if(dataModelCacheVO.getDataModelVO().getTypeUri() == null) {
                        log.warn("selectTemporalCount Invalid DataModel. typeUri is null. dataModelId={}", dataModelCacheVO.getDataModelVO().getId());
                        continue;
                    }

                    QueryVO innerQueryVO = (QueryVO) SerializationUtils.clone(queryVO);
                    innerQueryVO.setType(dataModelCacheVO.getDataModelVO().getTypeUri());
                    innerQueryVO.setLinks(null);
                    Integer cnt = this.selectTemporalCountWithType(innerQueryVO);
                    if (!ValidateUtil.isEmptyData(cnt)) {
                        totalCount = totalCount + cnt;
                    }
                }
            }
            return totalCount;
        } else {
            return selectTemporalCountWithType(queryVO);
        }
    }

    public Integer selectTemporalCountWithType(QueryVO queryVO) {

        if (log.isDebugEnabled()) {

            StringBuilder requestParams = new StringBuilder();
            requestParams.append("entityType=").append(queryVO.getType())
                    .append(", params(queryVO)=").append(queryVO.toString());

            //조회 조건 확인
            log.debug("request msg='{}'", requestParams);
        }
        if (queryVO.getDataModelCacheVO() != null) {
            queryVO.setDataModelCacheVO(null);
        }
        QueryVO copiedQueryVO = (QueryVO) SerializationUtils.clone(queryVO);
        copiedQueryVO.setLimit(null);
        copiedQueryVO.setOffset(null);

        List<CommonEntityVO> commonEntityVOs = this.selectTemporal(copiedQueryVO, Constants.APPLICATION_LD_JSON_VALUE);
        int totalCount = 0;
        if (commonEntityVOs != null) {
            totalCount = commonEntityVOs.size();
        }
        return totalCount;
    }


    /**
     * type 없이 query 시, limit & offset 구현
     *
     * @param totalCommonEntityVOs
     * @param limit
     * @param offset
     * @return
     */
    private List<CommonEntityVO> extractSubListWithoutType(List<CommonEntityVO> totalCommonEntityVOs, Integer limit, Integer offset) {
        Integer startIndex = 0;
        Integer endIndex = totalCommonEntityVOs.size();

        if (limit != null && offset != null) {
            if (endIndex > (limit + offset)) {
                endIndex = limit + offset;
            }
            if (offset > endIndex) {
                startIndex = endIndex;
            } else {
                startIndex = offset;
            }
            totalCommonEntityVOs = totalCommonEntityVOs.subList(startIndex, endIndex);
        } else if (limit != null && offset == null) {
            if (endIndex > limit) {
                endIndex = limit;
            }
            totalCommonEntityVOs = totalCommonEntityVOs.subList(startIndex, endIndex);
        }

        return totalCommonEntityVOs;
    }
    
	 protected boolean validateAttrsFiltering(List<String> attrs, CommonEntityFullVO commonEntityFullVO) {
	        if(ValidateUtil.isEmptyData(attrs)) {
	           return false;
	        }

            boolean isMatch = false;
            if (this.getClass().toString().contains("hive")) {
                for(String attr : attrs) {
                    if(commonEntityFullVO.containsKey(attr.toLowerCase())) {
                        isMatch = true;
                        break;
                    }
                }
            } else {
                for (String attr : attrs) {
                    if (commonEntityFullVO.containsKey(attr)) {
                        isMatch = true;
                        break;
                    }
                }
            }
	        if(isMatch) {
	            return false;
	        }
	        return true;
    }

    private List<String> getLinkOrDefault(List<String> link) {
        if (!ValidateUtil.isEmptyData(link)) {
            return link;
        }
        return Collections.singletonList(defaultContextUri);
    }
}
