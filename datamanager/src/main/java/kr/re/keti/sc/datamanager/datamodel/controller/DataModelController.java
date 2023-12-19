package kr.re.keti.sc.datamanager.datamodel.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.datamanager.common.code.Constants;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode.AttributeValueType;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode.DefaultAttributeKey;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ErrorCode;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode.PropertyKey;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ProvisionEventType;
import kr.re.keti.sc.datamanager.common.exception.BadRequestException;
import kr.re.keti.sc.datamanager.datamodel.service.DataModelSVC;
import kr.re.keti.sc.datamanager.datamodel.vo.Attribute;
import kr.re.keti.sc.datamanager.datamodel.vo.DataModelBaseVO;
import kr.re.keti.sc.datamanager.datamodel.vo.DataModelVO;
import kr.re.keti.sc.datamanager.datamodel.vo.ObjectMember;
import kr.re.keti.sc.datamanager.dataset.vo.DatasetBaseVO;
import kr.re.keti.sc.datamanager.provisioning.vo.ProvisionNotiVO;
import kr.re.keti.sc.datamanager.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * <pre>
 * 데이터모델 관리 HTTP Controller 클래스
 *  - 데이터 모델 정보를 생성/수정/삭제하고 기 등록된 Provisioning 대상 서버로 생성/수정/삭제 이벤트를 Provisioning 한다
 *  - 데이터 모델 정보 조회 기능을 제공한다
 * </pre>
 */
@RestController
@Slf4j
public class DataModelController {

    @Autowired
    private DataModelSVC dataModelSVC;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Retrieve Data Model list
     * @param response HttpServletResponse
     * @param accept http request accept header
     * @param requestDataModelBaseVO retrieve condition
     * @throws Exception retrieve data model error
     */
    @GetMapping(value = "/datamodels")
    public @ResponseBody
    void getDataModels(HttpServletResponse response,
                       @RequestHeader(HttpHeaders.ACCEPT) String accept,
                       DataModelBaseVO requestDataModelBaseVO) throws Exception {

        List<DataModelVO> dataModelVOList = new ArrayList<>();

        // 1. 리소스 전체 갯수
        Integer totalCount = dataModelSVC.getDataModelBaseVOListTotalCount(requestDataModelBaseVO);

        // 2. 리소스 조회
        List<DataModelBaseVO> dataModelBaseVOList = dataModelSVC.getDataModelBaseVOList(requestDataModelBaseVO);
        if (dataModelBaseVOList != null) {
            for (DataModelBaseVO dataModelBaseVO : dataModelBaseVOList) {
                DataModelVO dataModelVO = objectMapper.readValue(dataModelBaseVO.getDataModel(), DataModelVO.class);
                dataModelVO.setCreatedAt(dataModelBaseVO.getCreateDatetime());
                dataModelVO.setCreatorId(dataModelBaseVO.getCreatorId());
                dataModelVO.setModifierId(dataModelBaseVO.getModifierId());
                dataModelVO.setModifiedAt(dataModelBaseVO.getModifyDatetime());
                dataModelVOList.add(dataModelVO);
            }
        }

        // 3. 리소스 전체 갯수 헤더에 추가
        response.addHeader(Constants.TOTAL_COUNT, Integer.toString(totalCount));
        // 4. 응답
        response.getWriter().print(objectMapper.writeValueAsString(dataModelVOList));
    }

    /**
     * 데이터모델 조회 by ID
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param accept http request accept header
     * @return DataModelVO retrieve dataModel data
     * @throws Exception retrieve data model error
     */
    @GetMapping("/datamodels/**")
    public @ResponseBody
    DataModelVO getDataModelById(HttpServletRequest request,
                                 HttpServletResponse response,
                                 @RequestHeader(HttpHeaders.ACCEPT) String accept) throws Exception {

    	log.info("Retrieve DataModel. requestUri={}, requestBody={}", request.getRequestURI());
    	
    	String id = request.getRequestURI().replaceFirst("/datamodels/", "");
    	
        DataModelVO dataModelVO = null;

        // 1. 데이터 모델 정보 조회
        DataModelBaseVO dataModelBaseVO = dataModelSVC.getDataModelBaseVOById(id);
        if (dataModelBaseVO != null) {
            dataModelVO = objectMapper.readValue(dataModelBaseVO.getDataModel(), DataModelVO.class);
            dataModelVO.setCreatedAt(dataModelBaseVO.getCreateDatetime());
            dataModelVO.setCreatorId(dataModelBaseVO.getCreatorId());
            dataModelVO.setModifierId(dataModelBaseVO.getModifierId());
            dataModelVO.setModifiedAt(dataModelBaseVO.getModifyDatetime());
        }

        if (dataModelVO == null) {
            throw new BadRequestException(ErrorCode.NOT_EXIST_ID,
                    "Not Exists. id=" + id);
        }
        return dataModelVO;

    }


    /**
     * create dataModel
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param requestBody create dataModel data
     * @throws Exception create error
     */
    @PostMapping("/datamodels")
    public @ResponseBody void create(HttpServletRequest request,
                                     HttpServletResponse response,
                                     @RequestBody String requestBody) throws Exception {

        log.info("Datamodels CREATE request msg='{}'", requestBody);

        // 1. 파라미터 파싱 및 유효성 검사
        DataModelVO dataModelVO = objectMapper.readValue(requestBody, DataModelVO.class);
        
        // 2. 유효성 체크
        // 2-1) 필수파라미터 체크
 		validateParameter(dataModelVO);
 		// 2-2) 기 존재여부 체크
 		DataModelBaseVO retrieveDataModelBaseVO = dataModelSVC.getDataModelBaseVOById(dataModelVO.getId());
        if (retrieveDataModelBaseVO != null) {
            throw new BadRequestException(ErrorCode.ALREADY_EXISTS,
                    "Already Exists. @context=" + dataModelVO.getContext() + ", type=" + dataModelVO.getType());
        }
        // 2-3) attribute명 예약어 여부 체크
     	checkAttributeName(dataModelVO.getAttributes());
 		// 2-4) attribute type 및 value 체크
 		checkAttributeTypeAndValueType(dataModelVO);
 		// 2-5) URI 를 통해 @context 정보 조회
 		Map<String, String> contextMap = dataModelSVC.contextToFlatMap(dataModelVO.getContext(), null);
 		// 2-6) type 정보가 @context 내에 존재하는 지 여부 체크
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
 		// 2-7) attribute명이 context 내에 존재하는 지 체크
		checkAttributeNameByContext(dataModelVO.getAttributes(), contextMap);
		// 2-8) type이 이미 사용중인지 확인 (full uri 기반 체크)
		chechAlreadyUsingType(dataModelVO);

		// 3. set attribute context uri
		dataModelSVC.setAttributeContextUri(dataModelVO.getAttributes(), contextMap);

        DataModelBaseVO dataModelBaseVO = new DataModelBaseVO();
        dataModelBaseVO.setId(dataModelVO.getId());
        dataModelBaseVO.setType(dataModelVO.getType());
        dataModelBaseVO.setTypeUri(dataModelVO.getTypeUri());
        dataModelBaseVO.setName(dataModelVO.getName());
        dataModelBaseVO.setDescription(dataModelVO.getDescription());
        dataModelBaseVO.setEnabled(true);
        dataModelBaseVO.setCreatorId(dataModelVO.getCreatorId());
        try {
        	dataModelBaseVO.setDataModel(objectMapper.writeValueAsString(dataModelVO));
		} catch (IOException e) {
			throw new BadRequestException(ErrorCode.UNKNOWN_ERROR, "DataModel parsing error. body=" + requestBody);
		}

        // 4. dataModel Provisioning
        ProvisionNotiVO provisionNotiVO = dataModelSVC.provisionDataModel(requestBody, ProvisionEventType.CREATED, request.getRequestURI());

        // 5. dataModel 정보 저장
   		dataModelBaseVO.setProvisioningRequestId(provisionNotiVO.getRequestId());
        dataModelBaseVO.setProvisioningEventTime(provisionNotiVO.getEventTime());
        dataModelSVC.createDataModelBaseVO(dataModelBaseVO);

        // 4. 처리 결과 설정
        response.setStatus(HttpStatus.CREATED.value());
    }

	private void chechAlreadyUsingType(DataModelVO dataModelVO) {
		DataModelBaseVO retrieveVO = new DataModelBaseVO();
		retrieveVO.setTypeUri(dataModelVO.getTypeUri());
		List<DataModelBaseVO> dataModelBaseVOs = dataModelSVC.getDataModelBaseVOList(retrieveVO);
        if (dataModelBaseVOs != null && dataModelBaseVOs.size() > 0) {
            throw new BadRequestException(ErrorCode.ALREADY_EXISTS,
                    "Already Exists. @context=" + dataModelVO.getContext() + ", type=" + dataModelVO.getType() + ", typeUri=" + dataModelVO.getTypeUri());
        }
	}

    
    /**
     * update data model
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param requestBody update dataModel data
     * @throws Exception update error
     */
    @PutMapping("/datamodels/**")
    public void updateDataModel(HttpServletRequest request,
                                HttpServletResponse response,
                                @RequestBody String requestBody) throws Exception {

    	log.info("Update DataModel. requestUri={}, requestBody={}", request.getRequestURI(), requestBody);
    	
    	String id = request.getRequestURI().replaceFirst("/datamodels/", "");

        // 1. 기 존재여부 확인
        DataModelBaseVO retrieveDataModelBaseVO = dataModelSVC.getDataModelBaseVOById(id);
        if(retrieveDataModelBaseVO == null) {
            throw new BadRequestException(ErrorCode.NOT_EXISTS_DATAMODEL, "Not Exists. id=" + id);
        }

        // 2. 데이터 모델 수정 객체 생성
        DataModelBaseVO dataModelBaseVO = alterDataModel(requestBody, id);

        // 3. dataModel Provisioning
        ProvisionNotiVO provisionNotiVO = dataModelSVC.provisionDataModel(requestBody, ProvisionEventType.UPDATED, request.getRequestURI());
    	
        // 4. 데이터 모델 업데이트
        dataModelBaseVO.setProvisioningRequestId(provisionNotiVO.getRequestId());
        dataModelBaseVO.setProvisioningEventTime(provisionNotiVO.getEventTime());
        dataModelSVC.fullUpdateDataModelBaseVO(dataModelBaseVO);

        response.setStatus(HttpStatus.NO_CONTENT.value());
    }

    /**
     * delete data model
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws Exception delete error
     */
    @DeleteMapping("/datamodels/**")
    public void delete(HttpServletRequest request, HttpServletResponse response) throws Exception {

        log.info("Delete DataModel. requestUri={}", request.getRequestURI());

        String id = request.getRequestURI().replaceFirst("/datamodels/", "");

        // 1. 유효성 체크
        // 1-1. 기 존재여부 확인
        DataModelBaseVO retrieveDataModelBaseVO = dataModelSVC.getDataModelBaseVOById(id);
        if (retrieveDataModelBaseVO == null) {
            throw new BadRequestException(ErrorCode.NOT_EXIST_ID, "Not Exists. id=" + id);
        }

        // 1-2. 삭제 대상 데이터 모델을 사용중인 데이터셋 존재 여부 체크 (없는 경우만 삭제 가능)
        DatasetBaseVO retrieveDatasetBaseVO = new DatasetBaseVO();
        retrieveDatasetBaseVO.setDataModelId(retrieveDataModelBaseVO.getId());
        DatasetBaseVO datasetBaseVO = dataModelSVC.getEnabledDatasetByDataModelBaseVO(retrieveDatasetBaseVO);
        if (datasetBaseVO != null) {
            throw new BadRequestException(DataManagerCode.ErrorCode.INVALID_PARAMETER,
                    "Using in dataset. Dataset id=" + datasetBaseVO.getId()
                            + ", dataModelId=" + datasetBaseVO.getDataModelId());
        }

        // 3. dataModel Provisioning
    	dataModelSVC.provisionDataModel(null, ProvisionEventType.DELETED, request.getRequestURI());

        // 4. 데이터 모델 삭제
        DataModelBaseVO dataModelBaseVO = new DataModelBaseVO();
        dataModelBaseVO.setId(id);
        dataModelSVC.deleteDataModelBaseVO(dataModelBaseVO);

        response.setStatus(HttpStatus.NO_CONTENT.value());
    }

    /**
     * provisioning data model to provision server
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param id
     * @throws Exception
     */
    @PostMapping("/datamodels/{id:.+}/provisioning")
    public void provisioningDataModel( HttpServletRequest request,
						    		   HttpServletResponse response,
						    		   @PathVariable String id) throws Exception {

        log.info("Provisioning DataModels request id={}", id);

        // 1. Provision 대상 dataModelVO 조회
        DataModelBaseVO retrieveDataModelBaseVO = dataModelSVC.getDataModelBaseVOById(id);
        if(retrieveDataModelBaseVO == null) {
        	throw new BadRequestException(ErrorCode.NOT_EXISTS_DATAMODEL, "Not Exists. id=" + id);
        }

       	// 2. dataModel Provisioning
        String requestUri = request.getRequestURI().replace("/provisioning", "");
        ProvisionNotiVO provisionNotiVO = dataModelSVC.provisionDataModel(retrieveDataModelBaseVO.getDataModel(), ProvisionEventType.UPDATED, requestUri);

        DataModelBaseVO updateDataModelBaseVO = new DataModelBaseVO();
        updateDataModelBaseVO.setId(id);
        updateDataModelBaseVO.setProvisioningRequestId(provisionNotiVO.getRequestId());
        updateDataModelBaseVO.setProvisioningEventTime(provisionNotiVO.getEventTime());
        dataModelSVC.updateDataModelProvisioning(updateDataModelBaseVO);

       	// 3. 처리 결과 설정
        response.setStatus(HttpStatus.NO_CONTENT.value());
    }

    private void validateParameter(DataModelVO dataModelVO) {
    	if(ValidateUtil.isEmptyData(dataModelVO.getId())) {
 			throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Not exists id.");
 		}
 		if(ValidateUtil.isEmptyData(dataModelVO.getContext())) {
 			throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Not exists context.");
 		}
 		if(ValidateUtil.isEmptyData(dataModelVO.getType())) {
 			throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Not exists type.");
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

        DataManagerCode.AttributeValueType attributeValueType = attribute.getValueType();

        if (attributeValueType.equals(DataManagerCode.AttributeValueType.INTEGER)
                || attributeValueType.equals(DataManagerCode.AttributeValueType.LONG)
                || attributeValueType.equals(DataManagerCode.AttributeValueType.DOUBLE)
                || attributeValueType.equals(DataManagerCode.AttributeValueType.ARRAY_INTEGER)
                || attributeValueType.equals(DataManagerCode.AttributeValueType.ARRAY_LONG)
                || attributeValueType.equals(DataManagerCode.AttributeValueType.ARRAY_DOUBLE)) {
        	
            if (attribute.getGreaterThan() != null && attribute.getGreaterThanOrEqualTo() != null) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "You should have only greaterThan or greaterThanOrEqualTo");
            }

            if (attribute.getLessThan() != null && attribute.getLessThanOrEqualTo() != null) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "You should have only lessThan or lessThanOrEqualTo");
            }

        } else {
            if (attribute.getGreaterThan() != null
                    || attribute.getGreaterThanOrEqualTo() != null
                    || attribute.getLessThan() != null
                    || attribute.getLessThanOrEqualTo() != null
            ) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "greaterThan, greaterThanOrEqualTo, lessThan, or lessThanOrEqualTo not allowed for this data type");
            }
        }
    }

    /**
     * AttributeType과 valueType 검증
     * @param dataModelVO
     */
    private void checkAttributeTypeAndValueType(DataModelVO dataModelVO) {

        List<Attribute> attributes = dataModelVO.getAttributes();

        for (Attribute attribute : attributes) {

            //Attribute 필수 파라미터 누락 체크
            if(ValidateUtil.isEmptyData(attribute.getName()) || ValidateUtil.isEmptyData(attribute.getName().trim()) || ValidateUtil.isEmptyData(attribute.getValueType()) || ValidateUtil.isEmptyData(attribute.getAttributeType())) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER,
                    "Invaild Attribute. " + "name=" + attribute.getName() + ", attributeValueType=" + attribute.getValueType()  + ", attributeType=" + attribute.getAttributeType());
            }

            String name = attribute.getName().trim();
            DataManagerCode.AttributeValueType attributeValueType = attribute.getValueType();
            DataManagerCode.AttributeType attributeType = attribute.getAttributeType();

            if (attributeType.equals(DataManagerCode.AttributeType.PROPERTY)) {

                if (attributeValueType != null) {

                    if (attributeValueType.equals(DataManagerCode.AttributeValueType.STRING)
                            || attributeValueType.equals(DataManagerCode.AttributeValueType.INTEGER)
                            || attributeValueType.equals(DataManagerCode.AttributeValueType.LONG)
                            || attributeValueType.equals(DataManagerCode.AttributeValueType.DOUBLE)
                            || attributeValueType.equals(DataManagerCode.AttributeValueType.OBJECT)
                            || attributeValueType.equals(DataManagerCode.AttributeValueType.DATE)
                            || attributeValueType.equals(DataManagerCode.AttributeValueType.BOOLEAN)
                            || attributeValueType.equals(DataManagerCode.AttributeValueType.ARRAY_STRING)
                            || attributeValueType.equals(DataManagerCode.AttributeValueType.ARRAY_INTEGER)
                            || attributeValueType.equals(DataManagerCode.AttributeValueType.ARRAY_LONG)
                            || attributeValueType.equals(DataManagerCode.AttributeValueType.ARRAY_DOUBLE)
                            || attributeValueType.equals(DataManagerCode.AttributeValueType.ARRAY_BOOLEAN)
                            || attributeValueType.equals(DataManagerCode.AttributeValueType.ARRAY_OBJECT)) {

                        checkAttributeInequality(attribute);
                    }

                    // ObjectMember 빈값 체크
                    if(attributeValueType.equals(AttributeValueType.ARRAY_OBJECT)
                            || attributeValueType.equals(AttributeValueType.OBJECT)) {
                        if(ValidateUtil.isEmptyData(attribute.getObjectMembers())) {
                            throw new BadRequestException(ErrorCode.INVALID_PARAMETER,
                                    "Not exists ObjectMember. " + "name=" + name + ", attributeType=" + attributeType + ", attributeValueType=" + attributeValueType);
                        }

                        //ObjectMember 필수 파라미터 누락 체크
                        for (ObjectMember objectMember : attribute.getObjectMembers()) {
                            if(ValidateUtil.isEmptyData(objectMember.getName()) || ValidateUtil.isEmptyData(objectMember.getName().trim()) || ValidateUtil.isEmptyData(objectMember.getValueType())) {
                                throw new BadRequestException(ErrorCode.INVALID_PARAMETER,
                                    "Invaild ObjectMembers. " + "name=" + objectMember.getName() + ", attributeValueType=" + objectMember.getValueType());
                            }

                            //ObjectMember 이름 trim 처리
                            objectMember.setName(objectMember.getName().trim());
                        }
                        

                    } else {
                        if(attribute.getObjectMembers() != null) {
                            throw new BadRequestException(ErrorCode.INVALID_PARAMETER,
                                    "'" + attributeValueType.getCode() + "' type cannot have ObjectMember. " + "name=" + name);
                            
                        }
                    }

                    // arrayObject 인 경우 하위 objectMember로 array 형태의 valueType이 올 수 없음
                    // 현재 RDB 저장구조 상 예외처리
                    if(attributeValueType.equals(DataManagerCode.AttributeValueType.ARRAY_OBJECT)) {
                    	checkArrayAttribute(attribute.getObjectMembers());
                    }
                    continue;
                }

            } else if (attributeType.equals(DataManagerCode.AttributeType.RELATIONSHIP)) {

                if (attributeValueType != null
                        && (attributeValueType.equals(DataManagerCode.AttributeValueType.STRING)
                            || attributeValueType.equals(DataManagerCode.AttributeValueType.ARRAY_STRING))
                ) {
                    continue;
                }
            } else if (attributeValueType != null && attributeType.equals(DataManagerCode.AttributeType.GEO_PROPERTY)) {
                if (attributeValueType.equals(DataManagerCode.AttributeValueType.GEO_JSON)) {
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
     * 데이터 모델 정보 수정
     * @param requestBody 요청 body
     * @param dataModelId data model id
     * @throws JsonProcessingException json 파싱 에러
     */
    private DataModelBaseVO alterDataModel(String requestBody, String dataModelId) throws JsonProcessingException {

        // 1. 수신 데이터 파싱
        DataModelVO requestDataModelVO = objectMapper.readValue(requestBody, DataModelVO.class);

        // 2. 유효성 검증
        // 2-1) 필수값 체크
  		// 2-2) @context 정보 필수 체크
  		List<String> contextUriList = requestDataModelVO.getContext();
  		if(ValidateUtil.isEmptyData(contextUriList)) {
  			throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Not exists @Context.");
  		}
  		// 2-3) URI 를 통해 @context 정보 조회
  		Map<String, String> contextMap = dataModelSVC.contextToFlatMap(contextUriList, null);
  		// 2-4) type 정보가 @context 내에 존재하는 지 여부 체크
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
  		// 2-5) attribute type 및 value 체크
        checkAttributeTypeAndValueType(requestDataModelVO);

        // 3. 데이터 모델 정보 수정
        DataModelBaseVO dataModelBaseVO = new DataModelBaseVO();
        dataModelBaseVO.setId(dataModelId);
        dataModelBaseVO.setDescription(requestDataModelVO.getDescription());
        dataModelBaseVO.setEnabled(true);
        dataModelBaseVO.setDataModel(objectMapper.writeValueAsString(requestDataModelVO));
        dataModelBaseVO.setModifierId(requestDataModelVO.getModifierId());

        return dataModelBaseVO;
    }
}
