package kr.re.keti.sc.ingestinterface.ingest.service;

import static kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.Operation.CREATE_ENTITY_OR_REPLACE_ENTITY_ATTRIBUTES;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.geojson.GeoJsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.AttributeType;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.AttributeValueType;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.DefaultAttributeKey;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.ErrorCode;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.GeoJsonValueType;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.GeometryType;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.Operation;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.PropertyKey;
import kr.re.keti.sc.ingestinterface.common.exception.BadRequestException;
import kr.re.keti.sc.ingestinterface.common.exception.BaseException;
import kr.re.keti.sc.ingestinterface.common.vo.entities.DynamicEntityFullVO;
import kr.re.keti.sc.ingestinterface.datamodel.vo.Attribute;
import kr.re.keti.sc.ingestinterface.datamodel.vo.DataModelCacheVO;
import kr.re.keti.sc.ingestinterface.datamodel.vo.ObjectMember;
import kr.re.keti.sc.ingestinterface.util.DateUtil;
import kr.re.keti.sc.ingestinterface.util.ValidateUtil;

/**
 * Entity operation Service class
 */
@Service
public class IngestInterfaceSVC extends IngestProcessor<DynamicEntityFullVO> {

	@Autowired
    protected ObjectMapper objectMapper;

    /**
     * 수신받은 객체를 EntityFullVO 형태로 파싱하여 반환
     */
    @Override
    public DynamicEntityFullVO deserializeContent(String content) throws BadRequestException {

        if (ValidateUtil.isEmptyData(content)) {
            return new DynamicEntityFullVO();
        }

        try {
            return objectMapper.readValue(content, DynamicEntityFullVO.class);
        } catch (IOException e) {
            new BadRequestException(ErrorCode.REQUEST_MESSAGE_PARSING_ERROR,
                    "Content Parsing ERROR. content=" + content, e);
        }
        return new DynamicEntityFullVO();
    }


    /**
     * 수신받은 content 기반으로 생성된 FullVO 를 DB용 daoVO로 파싱
     *
     * @param dynamicEntityFullVO 수신받은 content 기반으로 생성된 FullVO
     * @throws BaseException
     */
    @Override
    public void verification(DynamicEntityFullVO dynamicEntityFullVO, DataModelCacheVO dataModelCacheVO, Operation operation) throws BaseException {
        try {

            // 3. Operation 처리 시 사용될 동적 attribute 정보 추출 및 Dao정보 입력
            List<Attribute> rootAttributes = dataModelCacheVO.getAttributes();

            if (operation == CREATE_ENTITY_OR_REPLACE_ENTITY_ATTRIBUTES) {
                checkRequiredAttribute(dynamicEntityFullVO, rootAttributes);
            }
            
            if(operation != Operation.DELETE_ENTITY) {
            	attributeToDynamicDaoVO(dynamicEntityFullVO, dynamicEntityFullVO.getContext(), null, rootAttributes);
                checkInvalidAttribute(dynamicEntityFullVO, rootAttributes);
            }
            
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER,
                    "fullVO to daoVO parsing ERROR. entityType=" + dynamicEntityFullVO.getType() + ", id=" + dynamicEntityFullVO.getId(), e);
        }
    }

    /**
     * Operation 처리 시 사용될 동적 attribute 정보 추출 및 Dao정보 입력
     *
     * @param currentEntityVO    수신받은 content 기반으로 생성된 FullVO
     * @param parentHierarchyIds FullVO 기반으로 생성된 DaoVO (계층구조의 attribute 를 flat 하게 펼침)
     * @param parentHierarchyIds 계층구조 attributeId (부모레벨)
     * @param rootAttributes     RootAttbitues 목록
     * @throws ParseException
     */
    @SuppressWarnings("unchecked")
    private void attributeToDynamicDaoVO(Map<String, Object> currentEntityVO, List<String> contextUris,
                                         List<String> parentHierarchyIds, List<Attribute> rootAttributes) throws ParseException, BadRequestException {

        if (rootAttributes == null) return;

        Map<String, String> contextMap = dataModelManager.contextToFlatMap(contextUris);

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
            if (AttributeType.PROPERTY == rootAttribute.getAttributeType()) {

                AttributeValueType valueType = rootAttribute.getValueType();
                // 3-1-1. value type이 ArrayObject인 경우
                if (valueType == AttributeValueType.ARRAY_OBJECT) {

                    List<Map<String, Object>> arrayObject = null;
                    try {
                        arrayObject = (List<Map<String, Object>>) attribute.get(PropertyKey.VALUE.getCode());
                    } catch (ClassCastException e) {
                        throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER,
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
                    }

                    // 3-1-2. value type이 Object인 경우
                } else if (valueType == AttributeValueType.OBJECT) {

                    Map<String, Object> object = null;
                    try {
                        object = (Map<String, Object>) attribute.get(PropertyKey.VALUE.getCode());
                    } catch (ClassCastException e) {
                        throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER,
                                "Invalid Request Content. attributeId=" + rootAttribute.getName() +
                                        ", valueType=" + rootAttribute.getValueType().getCode() + ", value=" + attribute.get(PropertyKey.VALUE.getCode()));
                    }

                    objectTypeParamToDaoVO(currentHierarchyIds, rootAttribute.getObjectMembers(), object);

                    // 3-1-3. value type이 String, Integer, Double, Date, ArrayString, ArrayInteger, ArrayDouble인 경우
                } else {
                    Object value = attribute.get(PropertyKey.VALUE.getCode());
                    // 세부 파라미터 유효성 체크
                    checkObjectType(rootAttribute.getName(), valueType, value, rootAttribute);
                }

                // 3-2. type이 GeoProperty인 경우
            } else if (AttributeType.GEO_PROPERTY == rootAttribute.getAttributeType()) {

                Object value = attribute.get(PropertyKey.VALUE.getCode());
                checkGeometryObjectType(value);

                try {
                    String geoJson = objectMapper.writeValueAsString(value);
                    GeoJsonObject object = objectMapper.readValue(geoJson, GeoJsonObject.class);
                } catch (JsonProcessingException e) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER,
                            "Invalid Request Content. GeoJson parsing ERROR. attributeId=" + rootAttribute.getName() +
                                    ", valueType=" + AttributeValueType.GEO_JSON.getCode() + ", value=" + value);
                }
            } else if (AttributeType.RELATIONSHIP == rootAttribute.getAttributeType()) {
                if (rootAttribute.getValueType() == AttributeValueType.ARRAY_STRING
                        && attribute.containsKey(PropertyKey.OBJECT.getCode())) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER,
                            "Invalid Request Content. only allow objects. attribute=" + rootAttribute.getName());
                }

                if (rootAttribute.getValueType() == AttributeValueType.STRING
                        && attribute.containsKey(PropertyKey.OBJECTS.getCode())) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER,
                            "Invalid Request Content. only allow object. attribute=" + rootAttribute.getName());
                }

                PropertyKey propertyKey = PropertyKey.OBJECT;
                if (rootAttribute.getValueType() == AttributeValueType.ARRAY_STRING) {
                    propertyKey = PropertyKey.OBJECTS;
                }

                Object object = attribute.get(propertyKey.getCode());

                // 세부 파라미터 유효성 체크
                checkObjectType(rootAttribute.getName(), rootAttribute.getValueType(), object, rootAttribute);
            }

            // 3-3. ObservedAt 을 포함한 Attribute 인 경우
            if (rootAttribute.getHasObservedAt() != null && rootAttribute.getHasObservedAt()) {
                Object value = attribute.get(PropertyKey.OBSERVED_AT.getCode());
                if (value != null) {
                    // Date 파라미터 유효성 체크
                    if (!ValidateUtil.isDateObject(value)) {
                        throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER,
                                "Invalid Request Content. attributeId=" + rootAttribute.getName() + "." + PropertyKey.OBSERVED_AT.getCode() +
                                        ", valueType=" + AttributeValueType.DATE.getCode() + ", value=" + value);
                    }
                }
            }

            // 3-4. has property or relationship 이 존재하는 경우
            if (rootAttribute.getChildAttributes() != null) {
                attributeToDynamicDaoVO(attribute, contextUris, currentHierarchyIds, rootAttribute.getChildAttributes());
            }
            
            // 3-5. unitCode 을 포함한 Attribute 인 경우
		    if (rootAttribute.getHasUnitCode() != null && rootAttribute.getHasUnitCode()) {
		        Object value = attribute.get(PropertyKey.UNIT_CODE.getCode());
		        if (value != null) {
		        	checkObjectType(rootAttribute.getName(), AttributeValueType.STRING, value, rootAttribute);
		        }
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
            throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER,
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
                throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER,
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

    private void checkDefaultParam(Map<String, Object> attribute, String attributeId) throws BadRequestException {
        if (attribute == null) {
            throw new BadRequestException(ErrorCode.VERIFICATION_NOT_EXIST_MANDATORY_PARAMETER,
                    "Not found attribute. attributeId=" + attributeId);
        }
        if (attribute.get(PropertyKey.TYPE.getCode()) == null) {
            throw new BadRequestException(ErrorCode.VERIFICATION_NOT_EXIST_MANDATORY_PARAMETER,
                    "Not found attribute type. attributeId=" + attributeId);
        }

        if (AttributeType.parseType(attribute.get(PropertyKey.TYPE.getCode()).toString()) == null) {
            throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_TYPE,
                    "invalid attribute type. attribute Type=" + attribute.get(PropertyKey.TYPE.getCode()));
        }

        if (attribute.get(PropertyKey.TYPE.getCode()) == AttributeType.PROPERTY
                || attribute.get(PropertyKey.TYPE.getCode()) == AttributeType.GEO_PROPERTY) {
            if (attribute.get(PropertyKey.VALUE.getCode()) == null) {
                throw new BadRequestException(ErrorCode.VERIFICATION_NOT_EXIST_MANDATORY_PARAMETER,
                        "Not found Property value. attributeId=" + attributeId);
            }
        } else if (attribute.get(PropertyKey.TYPE.getCode()) == AttributeType.RELATIONSHIP) {
            if (attribute.get(PropertyKey.OBJECT.getCode()) == null
                    && attribute.get(PropertyKey.OBJECTS.getCode()) == null) {
                throw new BadRequestException(ErrorCode.VERIFICATION_NOT_EXIST_MANDATORY_PARAMETER,
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
     * @throws ParseException
     */
    private void objectTypeParamToDaoVO(List<String> parentHierarchyIds, List<ObjectMember> objectMembers, Map<String, Object> object) throws ParseException {

        for (ObjectMember objectMember : objectMembers) {
            List<String> currentHierarchyIds = new ArrayList<>(parentHierarchyIds);
            currentHierarchyIds.add(objectMember.getName());

            Object value = object.get(objectMember.getName());
            // 세부 파라미터 유효성 체크
            checkObjectType(objectMember.getName(), objectMember.getValueType(), value, objectMember);

            if (objectMember.getValueType() == AttributeValueType.OBJECT) {
                objectTypeParamToDaoVO(currentHierarchyIds, objectMember.getObjectMembers(), (Map<String, Object>) value);
            }
        }
    }


    private static boolean checkObjectType(String id, AttributeValueType valueType, Object value, ObjectMember attribute) throws BadRequestException {

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
                throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER, "Invalid Request Content. attributeId=" + id + " is null");
            }
        } else {
            if (value == null) {
                return true;
            }
        }

        switch (valueType) {
            case STRING:
                if (!ValidateUtil.isStringObject(value)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_VALUE, "Invalid Attribute Type. attributeId=" + id + ", valueType=" + valueType + ", value=" + value);
                }
                if (!ValidateUtil.isValidStringMinLength(value, minLength)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_RANGE,
                            "underflow Attribute MinLength. attributeId=" + id + ", valueType=" + valueType + ", minLength=" + minLength + ", value=" + value);
                }
                if (!ValidateUtil.isValidStringMaxLength(value, maxLength)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_RANGE,
                            "Overflow Attribute MaxLength. attributeId=" + id + ", valueType=" + valueType + ", maxLength=" + maxLength + ", value=" + value);
                }
                if (!ValidateUtil.isValidEnum(value, valueEnum)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_VALUE,
                            "Not match Attribute valueEnum. attributeId=" + id + ", valueType=" + valueType + ", valueEnum=" + valueEnum + ", value=" + value);
                }
                break;
            case INTEGER:
                if (!ValidateUtil.isIntegerObject(value)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_VALUE, "Invalid Attribute Type. attributeId=" + id + ", valueType=" + valueType + ", value=" + value);
                }
                if (!ValidateUtil.isValidGreaterThanOrEqualTo(value, greaterThanOrEqualTo)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_RANGE,
                            "Greater or equal to Attribute value. attributeId=" + id + ", valueType=" + valueType + ", greaterThanOrEqualTo=" + greaterThanOrEqualTo + ", value=" + value);
                }
                if (!ValidateUtil.isValidGreaterThan(value, greaterThan)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_RANGE,
                            "Greater Attribute value. attributeId=" + id + ", valueType=" + valueType + ", greaterThan=" + greaterThan + ", value=" + value);
                }

                if (!ValidateUtil.isValidLessThanOrEqualTo(value, lessThanOrEqualTo)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_RANGE,
                            "Less or equal to Attribute value. attributeId=" + id + ", valueType=" + valueType + ", lessThanOrEqualTo=" + lessThanOrEqualTo + ", value=" + value);
                }
                if (!ValidateUtil.isValidLessThan(value, lessThan)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_RANGE,
                            "Less Attribute value. attributeId=" + id + ", valueType=" + valueType + ", lessThan=" + lessThan + ", value=" + value);
                }
                if (!ValidateUtil.isValidEnum(value, valueEnum)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_VALUE,
                            "Not match Attribute valueEnum. attributeId=" + id + ", valueType=" + valueType + ", valueEnum=" + valueEnum + ", value=" + value);
                }
                break;
            case LONG:
                if (!ValidateUtil.isLongObject(value)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_VALUE, "Invalid Attribute Type. attributeId=" + id + ", valueType=" + valueType + ", value=" + value);
                }
                if (!ValidateUtil.isValidGreaterThanOrEqualTo(value, greaterThanOrEqualTo)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_RANGE,
                            "Greater or equal to Attribute value. attributeId=" + id + ", valueType=" + valueType + ", greaterThanOrEqualTo=" + greaterThanOrEqualTo + ", value=" + value);
                }
                if (!ValidateUtil.isValidGreaterThan(value, greaterThan)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_RANGE,
                            "Greater Attribute value. attributeId=" + id + ", valueType=" + valueType + ", greaterThan=" + greaterThan + ", value=" + value);
                }

                if (!ValidateUtil.isValidLessThanOrEqualTo(value, lessThanOrEqualTo)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_RANGE,
                            "Less or equal to Attribute value. attributeId=" + id + ", valueType=" + valueType + ", lessThanOrEqualTo=" + lessThanOrEqualTo + ", value=" + value);
                }
                if (!ValidateUtil.isValidLessThan(value, lessThan)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_RANGE,
                            "Less Attribute value. attributeId=" + id + ", valueType=" + valueType + ", lessThan=" + lessThan + ", value=" + value);
                }
                if (!ValidateUtil.isValidEnum(value, valueEnum)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_VALUE,
                            "Not match Attribute valueEnum. attributeId=" + id + ", valueType=" + valueType + ", valueEnum=" + valueEnum + ", value=" + value);
                }
                break;
            case DOUBLE:
                if (!ValidateUtil.isBigDecimalObject(value)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_VALUE, "Invalid Attribute Type. attributeId=" + id + ", valueType=" + valueType + ", value=" + value);
                }
                if (!ValidateUtil.isValidGreaterThanOrEqualTo(value, greaterThanOrEqualTo)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_RANGE,
                            "Greater or equal to Attribute value. attributeId=" + id + ", valueType=" + valueType + ", greaterThanOrEqualTo=" + greaterThanOrEqualTo + ", value=" + value);
                }
                if (!ValidateUtil.isValidGreaterThan(value, greaterThan)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_RANGE,
                            "Greater Attribute value. attributeId=" + id + ", valueType=" + valueType + ", greaterThan=" + greaterThan + ", value=" + value);
                }

                if (!ValidateUtil.isValidLessThanOrEqualTo(value, lessThanOrEqualTo)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_RANGE,
                            "Less or equal to Attribute value. attributeId=" + id + ", valueType=" + valueType + ", lessThanOrEqualTo=" + lessThanOrEqualTo + ", value=" + value);
                }
                if (!ValidateUtil.isValidLessThan(value, lessThan)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_RANGE,
                            "Less Attribute value. attributeId=" + id + ", valueType=" + valueType + ", lessThan=" + lessThan + ", value=" + value);
                }
                if (!ValidateUtil.isValidEnum(value, valueEnum)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_VALUE,
                            "Not match Attribute valueEnum. attributeId=" + id + ", valueType=" + valueType + ", valueEnum=" + valueEnum + ", value=" + value);
                }
                break;
            case DATE:
                if (!ValidateUtil.isDateObject(value)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_VALUE, "Invalid Attribute Type. attributeId=" + id + ", valueType=" + valueType + ", value=" + value);
                }
                break;
            case BOOLEAN:
                if (!ValidateUtil.isBooleanObject(value)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_VALUE, "Invalid Attribute Type. attributeId=" + id + ", valueType=" + valueType + ", value=" + value);
                }
                break;
            case ARRAY_STRING:
                if (!ValidateUtil.isArrayStringObject(value)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_VALUE, "Invalid Attribute Type. attributeId=" + id + ", valueType=" + valueType + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayStringMinLength(value, minLength)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_RANGE,
                            "underflow Attribute MinLength. attributeId=" + id + ", valueType=" + valueType + ", minLength=" + minLength + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayStringMaxLength(value, maxLength)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_RANGE,
                            "Overflow Attribute MaxLength. attributeId=" + id + ", valueType=" + valueType + ", maxLength=" + maxLength + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayEnum(value, valueEnum)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_VALUE,
                            "Not match Attribute valueEnum. attributeId=" + id + ", valueType=" + valueType + ", valueEnum=" + valueEnum + ", value=" + value);
                }
                break;
            case ARRAY_INTEGER:
                if (!ValidateUtil.isArrayIntegerObject(value)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_VALUE, "Invalid Attribute Type. attributeId=" + id + ", valueType=" + valueType + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayGreaterThanOrEqualTo(value, greaterThanOrEqualTo)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_RANGE,
                            "Greater or equal to Attribute value. attributeId=" + id + ", valueType=" + valueType + ", greaterThanOrEqualTo=" + greaterThanOrEqualTo + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayGreaterThan(value, greaterThan)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_RANGE,
                            "Greater  Attribute value. attributeId=" + id + ", valueType=" + valueType + ", greaterThan=" + greaterThan + ", value=" + value);
                }

                if (!ValidateUtil.isValidArrayLessThenOrEqualTo(value, lessThanOrEqualTo)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_RANGE,
                            "Less or equal to Attribute value. attributeId=" + id + ", valueType=" + valueType + ", lessThanOrEqualTo=" + lessThanOrEqualTo + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayLessThan(value, lessThan)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_RANGE,
                            "Less Attribute value. attributeId=" + id + ", valueType=" + valueType + ", lessThan=" + lessThan + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayEnum(value, valueEnum)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_VALUE,
                            "Not match Attribute valueEnum. attributeId=" + id + ", valueType=" + valueType + ", valueEnum=" + valueEnum + ", value=" + value);
                }
                break;
            case ARRAY_LONG:
                if (!ValidateUtil.isArrayLongObject(value)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_VALUE, "Invalid Attribute Type. attributeId=" + id + ", valueType=" + valueType + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayGreaterThanOrEqualTo(value, greaterThanOrEqualTo)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_RANGE,
                            "Greater or equal to Attribute value. attributeId=" + id + ", valueType=" + valueType + ", greaterThanOrEqualTo=" + greaterThanOrEqualTo + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayGreaterThan(value, greaterThan)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_RANGE,
                            "Greater  Attribute value. attributeId=" + id + ", valueType=" + valueType + ", greaterThan=" + greaterThan + ", value=" + value);
                }

                if (!ValidateUtil.isValidArrayLessThenOrEqualTo(value, lessThanOrEqualTo)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_RANGE,
                            "Less or equal to Attribute value. attributeId=" + id + ", valueType=" + valueType + ", lessThanOrEqualTo=" + lessThanOrEqualTo + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayLessThan(value, lessThan)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_RANGE,
                            "Less Attribute value. attributeId=" + id + ", valueType=" + valueType + ", lessThan=" + lessThan + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayEnum(value, valueEnum)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_VALUE,
                            "Not match Attribute valueEnum. attributeId=" + id + ", valueType=" + valueType + ", valueEnum=" + valueEnum + ", value=" + value);
                }
                break;
            case ARRAY_DOUBLE:
                if (!ValidateUtil.isArrayBigDecimalObject(value)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_VALUE, "Invalid Attribute Type. attributeId=" + id + ", valueType=" + valueType + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayGreaterThanOrEqualTo(value, greaterThanOrEqualTo)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_RANGE,
                            "Greater or equal to Attribute value. attributeId=" + id + ", valueType=" + valueType + ", greaterThanOrEqualTo=" + greaterThanOrEqualTo + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayGreaterThan(value, greaterThan)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_RANGE,
                            "Greater Attribute value. attributeId=" + id + ", valueType=" + valueType + ", greaterThan=" + greaterThan + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayLessThenOrEqualTo(value, lessThanOrEqualTo)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_RANGE,
                            "Less or equal to Attribute value. attributeId=" + id + ", valueType=" + valueType + ", lessThanOrEqualTo=" + lessThanOrEqualTo + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayLessThan(value, lessThan)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_RANGE,
                            "Less Attribute value. attributeId=" + id + ", valueType=" + valueType + ", lessThan=" + lessThan + ", value=" + value);
                }
                if (!ValidateUtil.isValidArrayEnum(value, valueEnum)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_VALUE,
                            "Not match Attribute valueEnum. attributeId=" + id + ", valueType=" + valueType + ", valueEnum=" + valueEnum + ", value=" + value);
                }
                break;
            case ARRAY_BOOLEAN:
                if (!ValidateUtil.isArrayBooleanObject(value)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_VALUE, "Invalid Attribute Type. attributeId=" + id + ", valueType=" + valueType + ", value=" + value);
                }
                break;
            case OBJECT:
                if (!ValidateUtil.isMapObject(value)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_VALUE, "Invalid Attribute Type. attributeId=" + id + ", valueType=" + valueType + ", value=" + value);
                }
                break;
            default:
                throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER, "Invalid Attribute valueType. attributeId=" + id + ", valueType=" + valueType + ", value=" + value);
        }

        return true;
    }

    /**
     * Geometry Object Type 체크 (Point, MultiPoint, LineString, MultiLineString, Polygon, MultiPolygon)
     *
     * @param value
     * @return
     * @throws BadRequestException
     */
    private static boolean checkGeometryObjectType(Object value) throws BadRequestException {

        HashMap<String, Object> map = (HashMap<String, Object>) value;
        String geoType = map.get(PropertyKey.TYPE.getCode()).toString();
        Object geoCoordinates = map.get(PropertyKey.COORDINATES.getCode());
        if (GeoJsonValueType.parseType(geoType) == null) {
            throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_TYPE, "invalid attribute type. Geometry Object Type=" + geoType);
        }

        if (geoCoordinates instanceof ArrayList == false) {
            throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER_TYPE, "invalid attribute value. Geometry coordinates=" + geoCoordinates.toString());
        }

        return true;
    }

    /**
     * 필수(isRequired=true) 속성 체크
     *
     * @param dynamicEntityFullVO
     * @param rootAttributes
     * @return
     */
    private static boolean checkRequiredAttribute(DynamicEntityFullVO dynamicEntityFullVO, List<Attribute> rootAttributes) {

        for (Attribute rootAttribute : rootAttributes) {
            if (rootAttribute.getIsRequired() != null && rootAttribute.getIsRequired()) {
                String name = rootAttribute.getName();
                String attributeUri = rootAttribute.getAttributeUri();
                if (!dynamicEntityFullVO.containsKey(name) && !dynamicEntityFullVO.containsKey(attributeUri)) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_NOT_EXIST_MANDATORY_PARAMETER, "required Attribute. attributeId=" + name + "("+attributeUri+")");
                }

            }
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
                throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER, "invalid key : " + key);
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
                throw new BadRequestException(ErrorCode.VERIFICATION_NOT_EXIST_MANDATORY_PARAMETER, "Not found value : " + entry.getKey());
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
                        throw new BadRequestException(ErrorCode.VERIFICATION_NOT_EXIST_MANDATORY_PARAMETER, "Not found key : " + entry.getKey());
                    }
                }
            } else {
                // 1-레벨 체크
                if (!attrKey.equals(attribute.getName()) && !attrKey.equals(attribute.getAttributeUri())) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_NOT_EXIST_MANDATORY_PARAMETER, "Not found key : " + entry.getKey());
                }
            }

            //동일 레벨 attribute 체크
            checkInnerAttribute(attrKey, attrValue, attribute);

        } else if (type.equalsIgnoreCase(AttributeType.RELATIONSHIP.getCode())) {
            // RELATIONSHIP 형 item 체크
            Object objectItem = attrValue.get(PropertyKey.OBJECT.getCode());
            Object objectsItem = attrValue.get(PropertyKey.OBJECTS.getCode());
            if (objectItem == null && objectsItem == null) {
                throw new BadRequestException(ErrorCode.VERIFICATION_NOT_EXIST_MANDATORY_PARAMETER, "Not found object and objects. attribute= " + entry.getKey());
            }
            //동일 레벨 attribute 체크
            checkInnerAttribute(attrKey, attrValue, attribute);

        } else if (type.equalsIgnoreCase(AttributeType.GEO_PROPERTY.getCode())) {
            // GEO_PROPERTY 형 item 체크
            LinkedHashMap<String, Object> valueItem = (LinkedHashMap<String, Object>) attrValue.get(PropertyKey.VALUE.getCode());
            String geoType = valueItem.get(PropertyKey.TYPE.getCode()).toString();
            if (GeometryType.parseType(geoType) == null) {
                throw new BadRequestException(ErrorCode.VERIFICATION_NOT_EXIST_MANDATORY_PARAMETER, "Not found geo-type : " + entry.getKey());
            }
            Object coordinatesItem = valueItem.get(PropertyKey.COORDINATES.getCode());
            if (coordinatesItem == null) {
                throw new BadRequestException(ErrorCode.VERIFICATION_NOT_EXIST_MANDATORY_PARAMETER, "Not found coordinates : " + entry.getKey());
            }
        }

        // ObservedAt 체크
        if (attribute != null && attribute.getHasObservedAt() != null) {
            if (attribute.getHasObservedAt() && !attrValue.containsKey(DefaultAttributeKey.OBSERVED_AT.getCode())) {
                throw new BadRequestException(ErrorCode.VERIFICATION_NOT_EXIST_MANDATORY_PARAMETER, "Not found observedAt : " + entry.getKey());
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
                            throw new BadRequestException(ErrorCode.VERIFICATION_NOT_EXIST_MANDATORY_PARAMETER, "Not found key : " + key);
                        }
                    }
                } else {

                    // 같은 레벨 하위 attribute 필터링 unit
                    if (attribute.getChildAttributes() == null) {
                        throw new BadRequestException(ErrorCode.VERIFICATION_NOT_EXIST_MANDATORY_PARAMETER, "Not found key : " + propertyMapKey);
                    }
                    Attribute innerAttribute = findAttribute(attribute.getChildAttributes(), propertyMapKey);
                    if (innerAttribute == null) {
                        throw new BadRequestException(ErrorCode.VERIFICATION_NOT_EXIST_MANDATORY_PARAMETER, "Not found key : " + propertyMapKey);
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
                    throw new BadRequestException(ErrorCode.VERIFICATION_NOT_EXIST_MANDATORY_PARAMETER, "Not found key : " + relationshipMapKey);
                }
                isExistAttribute(relationshipMap, innerAttribute);
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
        throw new BadRequestException(ErrorCode.VERIFICATION_NOT_EXIST_MANDATORY_PARAMETER, "Not found key : " + attrKey);
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
                throw new BadRequestException(ErrorCode.VERIFICATION_NOT_EXIST_MANDATORY_PARAMETER, "Not found key : " + innerKey);
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
}