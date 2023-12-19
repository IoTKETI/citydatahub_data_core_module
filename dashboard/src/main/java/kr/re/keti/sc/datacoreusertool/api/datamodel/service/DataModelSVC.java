package kr.re.keti.sc.datacoreusertool.api.datamodel.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import kr.re.keti.sc.datacoreusertool.api.datamodel.vo.Attribute;
import kr.re.keti.sc.datacoreusertool.api.datamodel.vo.DataModelVO;
import kr.re.keti.sc.datacoreusertool.api.datamodel.vo.ObjectMember;
import kr.re.keti.sc.datacoreusertool.api.datamodel.vo.UiTreeVO;
import kr.re.keti.sc.datacoreusertool.common.code.Constants;
import kr.re.keti.sc.datacoreusertool.common.code.DataServiceBrokerCode.AttributeType;
import kr.re.keti.sc.datacoreusertool.common.code.DataServiceBrokerCode.AttributeValueType;
import kr.re.keti.sc.datacoreusertool.common.service.DataCoreRestSVC;
import kr.re.keti.sc.datacoreusertool.util.ValidateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service to get DataModel information.
 * @FileName DataModelSVC.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 25.
 * @Author Elvin
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DataModelSVC {
	
	@Value("${datamanager.url}")
	private String datamodelUrl;
	
	private final static String DEFAULT_PATH_URL = "datamodels";
	private final DataCoreRestSVC dataCoreRestSVC;

	/**
	 * Retrieve data model
	 * @param id	Data model ID
	 * @return		Data model information retrieved by data model ID.
	 */
	public ResponseEntity<DataModelVO> getDataModelbyId(String id) {
		String pathUri = DEFAULT_PATH_URL + "/" + id.trim();
		Map<String, String> header = new HashMap<String, String>();
		header.put(Constants.HTTP_HEADER_KEY_ACCEPT, Constants.ACCEPT_TYPE_APPLICATION_JSON);
		
		ResponseEntity<DataModelVO> response = dataCoreRestSVC.get(datamodelUrl, pathUri, header, null, null, DataModelVO.class);
		
		return response;
	}

	/**
	 * Retrieve data model by entity type (short name)
	 * @param type	Entity type
	 * @return		Data model information retrieved by entity type.
	 */
	public ResponseEntity<DataModelVO> getDataModelByEntityType(String type) {
		ResponseEntity<DataModelVO> result = null;
		Map<String, String> header = new HashMap<String, String>();
		Map<String, Object> param = new HashMap<String, Object>();

		header.put(Constants.HTTP_HEADER_KEY_ACCEPT, Constants.ACCEPT_TYPE_APPLICATION_JSON);
		param.put("type", type);

		ResponseEntity<List<DataModelVO>> response = dataCoreRestSVC.getList(datamodelUrl, DEFAULT_PATH_URL, header,
				null, param, new ParameterizedTypeReference<List<DataModelVO>>() {});

		if (response != null && response.getBody() != null) {
			result = ResponseEntity.status(response.getStatusCode()).body(response.getBody().get(0));
		} else {
			result = ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}

		return result;
	}
	
	/**
	 * Retrieve data model
	 * @param typeUri	Data model TypeUri
	 * @return			Data model information retrieved by data model TypeUri.
	 */
	public ResponseEntity<DataModelVO> getDataModelbyTypeUri(String typeUri) {
		ResponseEntity<DataModelVO> result = null;
		Map<String, String> header = new HashMap<String, String>();
		Map<String, Object> param = new HashMap<String, Object>();
		header.put(Constants.HTTP_HEADER_KEY_ACCEPT, Constants.ACCEPT_TYPE_APPLICATION_JSON);
		param.put("typeUri", typeUri);
		
		ResponseEntity<List<DataModelVO>> response = dataCoreRestSVC.getList(datamodelUrl, DEFAULT_PATH_URL, header, null, param, new ParameterizedTypeReference<List<DataModelVO>>() {});
		
		if(response != null && response.getBody() != null) {
			result = ResponseEntity.status(response.getStatusCode()).body(response.getBody().get(0)); 
		} else {
			result = ResponseEntity.status(response.getStatusCode()).build();
		}
		
		return result;
	}
	
	/**
	 * Retrieve multiple data model
	 * @return	All data model
	 */
	public ResponseEntity<List<String>> getDataModels() {
		Map<String, String> header = new HashMap<String, String>();
		header.put(Constants.HTTP_HEADER_KEY_ACCEPT, Constants.ACCEPT_TYPE_APPLICATION_JSON);
		
		List<String> dataModelIds = new ArrayList<String>();
		
		ResponseEntity<List<DataModelVO>> response = dataCoreRestSVC.getList(datamodelUrl, DEFAULT_PATH_URL, header, null, null, new ParameterizedTypeReference<List<DataModelVO>>() {});
		
		if(response == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
		
		if(response.getBody() == null) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}
		
		for(DataModelVO dataModelVO : response.getBody()) {
			dataModelIds.add(dataModelVO.getId());
		}
		
		Collections.sort(dataModelIds);
		
		return ResponseEntity.status(response.getStatusCode()).body(dataModelIds);
	}
	
	/**
	 * Retrieve type uri of data model
	 * @return	All data model type uri
	 */
	public ResponseEntity<List<String>> getDataModelTypeUri() {
		Map<String, String> header = new HashMap<String, String>();
		header.put(Constants.HTTP_HEADER_KEY_ACCEPT, Constants.ACCEPT_TYPE_APPLICATION_JSON);
		
		List<String> dataModelTypeUri = new ArrayList<String>();
		
		ResponseEntity<List<DataModelVO>> response = dataCoreRestSVC.getList(datamodelUrl, DEFAULT_PATH_URL, header, null, null, new ParameterizedTypeReference<List<DataModelVO>>() {});
		
		if(response == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
		
		for(DataModelVO dataModelVO : response.getBody()) {
			dataModelTypeUri.add(dataModelVO.getTypeUri());
		}
		
		Collections.sort(dataModelTypeUri);
		
		return ResponseEntity.status(response.getStatusCode()).body(dataModelTypeUri);
	}
	
	/**
	 * Retrive attribute of data model
	 * @param id		Data model ID
	 * @param attrType	Attribute type
	 * @return			List of attribute name retrieved by data model ID and attribute type.
	 */
	public ResponseEntity<List<String>> getDataModelAttrs(String id, String typeUri, String attrType) {
		ResponseEntity<DataModelVO> response = null;
		DataModelVO dataModelVO = null;
		List<String> attributeList = new ArrayList<String>();
		
		if(!ValidateUtil.isEmptyData(id)) {
			response = getDataModelbyId(id);
		}
		else if(!ValidateUtil.isEmptyData(typeUri)) {
			response = getDataModelbyTypeUri(typeUri);
		}
		
		if(response != null) {
			dataModelVO = response.getBody();
		}
		
		if (!ValidateUtil.isEmptyData(dataModelVO)) {
			List<Attribute> attributes = dataModelVO.getAttributes();
			
			if(Constants.TOP_LEVEL_ATTR.equals(attrType)) {
				for(Attribute attribute : attributes) {
					attributeList.add(attribute.getName());
				}
			} 
			else if(Constants.OBSERVED_AT_ATTR.equals(attrType)) {
				for(Attribute attribute : attributes) {
					if(attribute.getHasObservedAt() != null && attribute.getHasObservedAt()) {
						attributeList.add(attribute.getName());
					}
				}
			}
			else if(Constants.ALL_LEVEL_ATTR.equals(attrType)) {
				getAttrsType(null, attributes, attributeList);
			}
			else {
				log.info("Unsupported retrieve attrType: " + attrType);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
			}
			
			Collections.sort(attributeList);
		}
		
		return ResponseEntity.status(response.getStatusCode()).body(attributeList);
	}

	/**
	 * Retrive attribute of data model
	 * @param dataModelVO	Data model VO
	 * @param attrType	Attribute type
	 * @return			List of attribute name of Data Model
	 */
	public List<String> getDataModelAttrs(DataModelVO dataModelVO, String attrType) {
		return getDataModelAttrs (dataModelVO, attrType, null);
	}

	/**
	 * Retrive attribute of data model
	 * @param dataModelVO	Data model VO
	 * @param attrType	Attribute type
     * @param propertyType	Property type
	 * @return			List of attribute name of Data Model
	 */
	public List<String> getDataModelAttrs(DataModelVO dataModelVO, String attrType, String propertyType) {
		List<String> attributeList = new ArrayList<String>();

		if (!ValidateUtil.isEmptyData(dataModelVO)) {
			List<Attribute> attributes = dataModelVO.getAttributes();

			if(Constants.TOP_LEVEL_ATTR.equals(attrType)) {
				for(Attribute attribute : attributes) {
					attributeList.add(attribute.getName());
				}
			}
			else if(Constants.OBSERVED_AT_ATTR.equals(attrType)) {
				//TODO: Recursive
				for(Attribute attribute : attributes) {
					if(attribute.getHasObservedAt() != null && attribute.getHasObservedAt()) {
						if (propertyType == null || propertyType.equals(attribute.getAttributeType().getCode()) ) {
							attributeList.add(attribute.getName());
						}
					}
				}
			}
			else if(Constants.ALL_LEVEL_ATTR.equals(attrType)) {
				getAttrsType(null, attributes, attributeList);
			}
			else {
				log.error("Unsupported retrieve attrType: " + attrType);
				//TODO: handle ERROR
				return attributeList;
			}
			Collections.sort(attributeList);
		}

		return attributeList;
	}

	
	/**
	 * Get list of attribute type
	 * @param parentAttrName	Parent attribute name
	 * @param attributes		List of attribute
	 * @param attributeList		List of attribute full type
	 */
	private void getAttrsType(String parentAttrName, List<Attribute> attributes, List<String> attributeList) {
		
		for(Attribute attribute : attributes) {
			if(!ValidateUtil.isEmptyData(attribute.getChildAttributes())) {
				getAttrsType(attribute.getName(), attribute.getChildAttributes(), attributeList);
			}
			
			if(!ValidateUtil.isEmptyData(attribute.getObjectMembers())) {
				getObjectMemberType(attribute.getName(), null, attribute.getObjectMembers(), attributeList);
			}
			
			if(attribute.getName() == null || attribute.getValueType() == null) {
				continue;
			}
			
			if(parentAttrName != null) {
				attributeList.add(parentAttrName + "." + attribute.getName() + " (" + attribute.getAttributeType() + ":" + attribute.getValueType() + ")");
			} else {
				attributeList.add(attribute.getName() + " (" + attribute.getAttributeType() + ":" + attribute.getValueType() + ")");
			}
		}
	}
	
	/**
	 * Get a type of object member
	 * @param parentAttrName			Parent attribute name
	 * @param parentObjectMemberName	Parent object member name
	 * @param objectMembers				List of object member
	 * @param attributeList				List of attribute full name
	 */
	private void getObjectMemberType(String parentAttrName, String parentObjectMemberName, List<ObjectMember> objectMembers, List<String> attributeList) {
		
		for(ObjectMember objectMember : objectMembers) {			
			if(!ValidateUtil.isEmptyData(objectMember.getObjectMembers())) {
				getObjectMemberType(parentAttrName, objectMember.getName(), objectMember.getObjectMembers(), attributeList);
			}
			
			if(objectMember.getName() == null || objectMember.getValueType() == null) {
				continue;
			}
			
			if(parentObjectMemberName != null) {
				attributeList.add(parentAttrName + "[" + parentObjectMemberName + "." + objectMember.getName() + "]" + " (" + objectMember.getValueType() + ")");
			} else {
				attributeList.add(parentAttrName + "[" + objectMember.getName() + "]" + " (" + objectMember.getValueType() + ")");
			}
		}
	}
	
	/**
	 * Create attribute tree structure for UI
	 * @param id	Data model ID
	 * @return		Attributes in the tree structure
	 */
	public ResponseEntity<List<UiTreeVO>> getAttrsTree(String id, String typeUri) {
		ResponseEntity<DataModelVO> response = null;
		DataModelVO dataModelVO = null;
		List<UiTreeVO> treeStructure = new ArrayList<UiTreeVO>();
		
		if(!ValidateUtil.isEmptyData(id)) {
			response = getDataModelbyId(id);
		}
		else if(!ValidateUtil.isEmptyData(typeUri)) {
			response = getDataModelbyTypeUri(typeUri);
		}
		
		if(response != null) {
			dataModelVO = response.getBody();
		}
		
		if (!ValidateUtil.isEmptyData(dataModelVO)) {
			List<Attribute> attributes = dataModelVO.getAttributes();
			
			makeUiAttrTreeStructure(attributes, treeStructure, null);
		}
		
		return ResponseEntity.status(response.getStatusCode()).body(treeStructure);
	}
	
	/**
	 * Create entity attribute tree structure for UI
	 * @param attributes		List of attribute
	 * @param treeStructure		Tree structure object for UI
	 * @param fullId			Full attribute ID
	 * @return					List of tree structure object for UI
	 */	
	private List<UiTreeVO> makeUiAttrTreeStructure(List<Attribute> attributes, List<UiTreeVO> treeStructure, String fullId) {
		
		for(Attribute attribute : attributes) {
			String currentId = getFullAttributeId(fullId, attribute.getName());
			if(!ValidateUtil.isEmptyData(attribute.getChildAttributes()) && !ValidateUtil.isEmptyData(attribute.getObjectMembers())) {
				List<UiTreeVO> childAttrStructure = new ArrayList<UiTreeVO>();
				List<UiTreeVO> uiAttrTreeVO = makeUiAttrTreeStructure(attribute.getChildAttributes(), childAttrStructure, currentId);				
				List<UiTreeVO> objectMemberStructure = new ArrayList<UiTreeVO>();
				List<UiTreeVO> uiObjectMemberTreeVO = makeUiObjectMemberTreeStructure(attribute.getObjectMembers(), objectMemberStructure, currentId, attribute.getValueType());
				
				UiTreeVO uiTree = setTreeAttrLabel(attribute, currentId, attribute.getValueType());
				
				uiTree.setChild(uiAttrTreeVO);
				// No valueType, it is treated as a String.
				if(attribute.getValueType() == null) {
					uiTree.setValueType(AttributeValueType.STRING.getCode());
				} else {
					uiTree.setValueType(attribute.getValueType().getCode());
				}
				uiTree.getChild().addAll(uiObjectMemberTreeVO);
				
				treeStructure.add(uiTree);
			}
			else if(!ValidateUtil.isEmptyData(attribute.getChildAttributes())) {
				List<UiTreeVO> childAttrStructure = new ArrayList<UiTreeVO>();
				List<UiTreeVO> uiTreeVO = makeUiAttrTreeStructure(attribute.getChildAttributes(), childAttrStructure, currentId);
				UiTreeVO uiTree = setTreeAttrLabel(attribute, currentId, attribute.getValueType());
				
				uiTree.setChild(uiTreeVO);
				// No valueType, it is treated as a String.
				if(attribute.getValueType() == null) {
					uiTree.setValueType(AttributeValueType.STRING.getCode());
				} else {
					uiTree.setValueType(attribute.getValueType().getCode());
				}
				treeStructure.add(uiTree);
			}
			else if(!ValidateUtil.isEmptyData(attribute.getObjectMembers())) {
				List<UiTreeVO> objectMemberStructure = new ArrayList<UiTreeVO>();
				List<UiTreeVO> uiTreeVO = makeUiObjectMemberTreeStructure(attribute.getObjectMembers(), objectMemberStructure, currentId, attribute.getValueType());
				UiTreeVO uiTree = setTreeAttrLabel(attribute, currentId, attribute.getValueType());
				
				uiTree.setChild(uiTreeVO);
				// No valueType, it is treated as a String.
				if(attribute.getValueType() == null) {
					uiTree.setValueType(AttributeValueType.STRING.getCode());
				} else {
					uiTree.setValueType(attribute.getValueType().getCode());
				}
				treeStructure.add(uiTree);
			} else {
				UiTreeVO uiTree = setTreeAttrLabel(attribute, currentId, attribute.getValueType());
				// No valueType, it is treated as a String.
				if(attribute.getValueType() == null) {
					uiTree.setValueType(AttributeValueType.STRING.getCode());
				} else {
					uiTree.setValueType(attribute.getValueType().getCode());
				}
				treeStructure.add(uiTree);
			}
		}
		
		return treeStructure;
	}
	
	/**
	 * Creating object member tree structure objects for UI.
	 * @param objectMembers		List of object member
	 * @param treeStructure		List of tree structure object for UI
	 * @param fullId			Full object member ID
	 * @param parentValueType	Parent attribute type 
	 * @return					List of object member tree structure object for UI
	 */
	private List<UiTreeVO> makeUiObjectMemberTreeStructure(List<ObjectMember> objectMembers, List<UiTreeVO> treeStructure, String fullId, AttributeValueType parentValueType) {
		
		for(ObjectMember objectMember : objectMembers) {
			String currentId = getFullObjectMemberId(fullId, objectMember.getName());
			if(!ValidateUtil.isEmptyData(objectMember.getObjectMembers())) {
				List<UiTreeVO> objectMemberStructure = new ArrayList<UiTreeVO>();
				List<UiTreeVO> uiTreeVO = makeUiObjectMemberTreeStructure(objectMember.getObjectMembers(), objectMemberStructure, currentId, objectMember.getValueType());
				UiTreeVO uiTree = setTreeObjectMemberLabel(objectMember, currentId, parentValueType);
				
				uiTree.setChild(uiTreeVO);
				// No valueType, it is treated as a String.
				if(objectMember.getValueType() == null) {
					uiTree.setValueType(AttributeValueType.STRING.getCode());
				} else {
					uiTree.setValueType(objectMember.getValueType().getCode());
				}
				treeStructure.add(uiTree);
			} else {
				UiTreeVO uiTree = setTreeObjectMemberLabel(objectMember, currentId, parentValueType);
				// No valueType, it is treated as a String.
				if(objectMember.getValueType() == null) {
					uiTree.setValueType(AttributeValueType.STRING.getCode());
				} else {
					uiTree.setValueType(objectMember.getValueType().getCode());
				}
				treeStructure.add(uiTree);
			}
		}
		
		return treeStructure;
	}
	
	/**
	 * Set attribute label to tree structure for UI
	 * @param attribute			Attribute
	 * @param fullId			Attribute full name
	 * @param parentValueType	Parent attribute value type
	 * @return 					List of attribute label tree structure object for UI
	 */
	private UiTreeVO setTreeAttrLabel(Attribute attribute, String fullId, AttributeValueType parentValueType) {
		UiTreeVO uiTree = new UiTreeVO();
		String attrName = attribute.getName();
		AttributeType attrType = attribute.getAttributeType();
		AttributeValueType valueType = attribute.getValueType();
		
		uiTree.setId(attrName);
		if(fullId == null) {
			uiTree.setFullId(attrName);
		} else {
			uiTree.setFullId(fullId);
		}
		
		if(!ValidateUtil.isEmptyData(attrType) && !ValidateUtil.isEmptyData(valueType)) {
			uiTree.setLabel(attrName + "(" + attrType.getCode() + ":" + valueType.getCode() + ")");
		}
		else if(!ValidateUtil.isEmptyData(attrType) && ValidateUtil.isEmptyData(valueType)) {
			uiTree.setLabel(attrName + "(" + attrType.getCode() + ")");
		}
		else if(ValidateUtil.isEmptyData(attrType) && !ValidateUtil.isEmptyData(valueType)) {
			uiTree.setLabel(attrName + "(" + valueType.getCode() + ")");
		} else {
			// Nothing
		}
		
		// GEOJSON, OBJECT, and DATE types are excluded from the query target.
		if (AttributeValueType.GEO_JSON.equals(attribute.getValueType()) ||
				AttributeValueType.OBJECT.equals(attribute.getValueType()) ||
				AttributeValueType.ARRAY_OBJECT.equals(attribute.getValueType()) ||
				AttributeValueType.ARRAY_STRING.equals(attribute.getValueType()) ||
				AttributeValueType.ARRAY_INTEGER.equals(attribute.getValueType()) ||
				AttributeValueType.ARRAY_DOUBLE.equals(attribute.getValueType()) ||
				AttributeValueType.ARRAY_BOOLEAN.equals(attribute.getValueType()) ||
				AttributeValueType.DATE.equals(attribute.getValueType())) {
			uiTree.setSearchable(false);
		} else {
			uiTree.setSearchable(true);
		}
		
		// Only when valueType is Integer or Double, it is treated as a value that can be expressed in graph.
		if(parentValueType == null ||
				(!parentValueType.equals(AttributeValueType.ARRAY_BOOLEAN) &&
				!parentValueType.equals(AttributeValueType.ARRAY_DOUBLE) &&
				!parentValueType.equals(AttributeValueType.ARRAY_INTEGER) &&
				!parentValueType.equals(AttributeValueType.ARRAY_OBJECT) &&
				!parentValueType.equals(AttributeValueType.ARRAY_STRING))) {
			if(attribute.getHasObservedAt() != null && attribute.getHasObservedAt() && 
					(AttributeValueType.INTEGER.equals(attribute.getValueType()) || AttributeValueType.DOUBLE.equals(attribute.getValueType()))) {
				uiTree.setGraphable(true);
			}
		} else {
			// In case of parent attribute is an array type, search is not allowed.
			uiTree.setSearchable(false);
		}
		
		return uiTree;
	}
	
	/**
	 * Set object member label to tree structure for UI
	 * @param objectMember		object member
	 * @param fullId			Full ID of the object member
	 * @param parentValueType	Parent attribute value type
	 * @return					List of object member label tree structure object for UI
	 */
	private UiTreeVO setTreeObjectMemberLabel(ObjectMember objectMember, String fullId, AttributeValueType parentValueType) {
		UiTreeVO uiTree = new UiTreeVO();
		String objectMemberName = objectMember.getName();
		AttributeValueType objectMemberValueType = objectMember.getValueType();
		
		uiTree.setId(objectMemberName);
		uiTree.setFullId(fullId);
		uiTree.setLabel(objectMemberName + "(" + objectMemberValueType.getCode() + ")");
		
		// GEOJSON, OBJECT, and DATE types are excluded from the query target.
		if (AttributeValueType.GEO_JSON.equals(objectMember.getValueType()) ||
				AttributeValueType.OBJECT.equals(objectMember.getValueType()) ||
				AttributeValueType.ARRAY_OBJECT.equals(objectMember.getValueType()) ||
				AttributeValueType.ARRAY_STRING.equals(objectMember.getValueType()) ||
				AttributeValueType.ARRAY_INTEGER.equals(objectMember.getValueType()) ||
				AttributeValueType.ARRAY_DOUBLE.equals(objectMember.getValueType()) ||
				AttributeValueType.ARRAY_BOOLEAN.equals(objectMember.getValueType()) ||
				AttributeValueType.DATE.equals(objectMember.getValueType())) {
			uiTree.setSearchable(false);
		} else {
			uiTree.setSearchable(true);
		}
		
		// Only when valueType is Integer or Double, it is treated as a value that can be expressed in graph.
		if(parentValueType == null ||
				(!parentValueType.equals(AttributeValueType.ARRAY_BOOLEAN) &&
				!parentValueType.equals(AttributeValueType.ARRAY_DOUBLE) &&
				!parentValueType.equals(AttributeValueType.ARRAY_INTEGER) &&
				!parentValueType.equals(AttributeValueType.ARRAY_OBJECT) &&
				!parentValueType.equals(AttributeValueType.ARRAY_STRING))) {
			if(AttributeValueType.INTEGER.equals(objectMember.getValueType()) || AttributeValueType.DOUBLE.equals(objectMember.getValueType())) {
				uiTree.setGraphable(true);
			}
		} else {
			// In case of parent attribute is an array type, search is not allowed.
			uiTree.setSearchable(false);
		}
		
		return uiTree;
	}
	
	/**
	 * Get full ID of attribute
	 * @param fullId	Full ID of parent attribute
	 * @param name		Attribute name
	 * @return			Full ID of attribute
	 */
	private String getFullAttributeId(String fullId, String name) {
		String currentId = null;
		if(fullId == null) {
			currentId = name;
		} else {
			currentId = fullId + "." + name;
		}
		
		return currentId;
	}
	
	/**
	 * Get full ID of object member
	 * @param fullId	Full ID of parent object member
	 * @param name		Object member name
	 * @return			Full ID of object member
	 */
	private String getFullObjectMemberId(String fullId, String name) {
		String currentId = null;
		
		if(fullId.contains("]")) {
			currentId = fullId.replaceAll("]", "") + "." + name + "]";
		} else {
			currentId = fullId + "[" + name + "]";
		}
		
		return currentId;
	}
	
}
