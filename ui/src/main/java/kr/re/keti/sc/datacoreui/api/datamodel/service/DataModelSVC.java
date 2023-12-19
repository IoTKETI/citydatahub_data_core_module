package kr.re.keti.sc.datacoreui.api.datamodel.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import kr.re.keti.sc.datacoreui.api.datamodel.vo.AttributeVO;
import kr.re.keti.sc.datacoreui.api.datamodel.vo.ContextVO;
import kr.re.keti.sc.datacoreui.api.datamodel.vo.DataModelListResponseVO;
import kr.re.keti.sc.datacoreui.api.datamodel.vo.DataModelResponseVO;
import kr.re.keti.sc.datacoreui.api.datamodel.vo.DataModelRetrieveVO;
import kr.re.keti.sc.datacoreui.api.datamodel.vo.DataModelVO;
import kr.re.keti.sc.datacoreui.api.datamodel.vo.ObjectMemberVO;
import kr.re.keti.sc.datacoreui.api.datamodel.vo.UiTreeVO;
import kr.re.keti.sc.datacoreui.api.dataset.service.DataSetSVC;
import kr.re.keti.sc.datacoreui.api.dataset.vo.DataSetListResponseVO;
import kr.re.keti.sc.datacoreui.api.dataset.vo.DataSetRetrieveVO;
import kr.re.keti.sc.datacoreui.api.datasetflow.service.DataSetFlowSVC;
import kr.re.keti.sc.datacoreui.api.datasetflow.vo.DataSetFlowResponseVO;
import kr.re.keti.sc.datacoreui.common.code.Constants;
import kr.re.keti.sc.datacoreui.common.code.DataCoreUiCode.ErrorCode;
import kr.re.keti.sc.datacoreui.common.exception.BadRequestException;
import kr.re.keti.sc.datacoreui.common.exception.DataCoreUIException;
import kr.re.keti.sc.datacoreui.common.service.DataCoreRestSVC;
import kr.re.keti.sc.datacoreui.common.vo.ClientExceptionPayloadVO;
import kr.re.keti.sc.datacoreui.security.service.DataCoreUiSVC;
import kr.re.keti.sc.datacoreui.util.AttributeCompareUtil;
import kr.re.keti.sc.datacoreui.util.ObjectMemberCompareUtil;
import kr.re.keti.sc.datacoreui.util.ValidateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A service class for data model management API calls.
 * @FileName DataModelSVC.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 22.
 * @Author Elvin
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DataModelSVC {
	
	@Value("${datamanager.url}")
	private String datamodelUrl;

	@Autowired
    private ObjectMapper objectMapper;
	
	@Autowired
	private DataSetSVC dataSetSVC;
	
	@Autowired
	private DataSetFlowSVC dataSetFlowSVC;

	@Autowired
	private DataCoreUiSVC dataCoreUiSVC;
	
	private final static String DEFAULT_PATH_URL = "datamodels";
	private final DataCoreRestSVC dataCoreRestSVC;

	/**
	 * Get data model context
	 * @param context	Context
	 * @return			Data model context information
	 */
	public ResponseEntity<Map<String, String>> getDataModelContext(Object context) {
		TreeMap<String, String> contextMap = new TreeMap<>();

		contextToFlatMap(context, contextMap);
		
		return ResponseEntity.ok().body(contextMap);
	}
	
	/**
	 * Create data model
	 * @param dataModelVO	DataModelVO
	 * @return				Result of data model creation.
	 */
	public <T> ResponseEntity<T> createDataModel(HttpServletRequest request, DataModelVO dataModelVO) {
		Object principal = dataCoreUiSVC.getPrincipal(request);
		if(principal != null) {
			dataModelVO.setCreatorId(principal.toString());
		}
		String dataModel = new Gson().toJson(dataModelVO);
		ResponseEntity<T> response = (ResponseEntity<T>) dataCoreRestSVC.post(datamodelUrl, DEFAULT_PATH_URL, null, dataModel, null, Void.class);
		
		return response;
	}
	
	/**
	 * Update data model
	 * @param dataModelVO	DataModelVO
	 * @return				Result of update data model.
	 */
	public <T> ResponseEntity<T> updateDataModel(HttpServletRequest request,DataModelVO dataModelVO) {
		// Data model cannot be modified when data flow is created
		// 1. Retrieve Dataset
		Object principal = dataCoreUiSVC.getPrincipal(request);
		if(principal != null) {
			dataModelVO.setModifierId(principal.toString());
		}
		DataSetRetrieveVO dataSetRetrieveVO = new DataSetRetrieveVO();
		dataSetRetrieveVO.setDataModelId(dataModelVO.getId());
		ResponseEntity<DataSetListResponseVO> dataSetListResponseVO = dataSetSVC.getDataSets(dataSetRetrieveVO);
		
		// 2. Retrieve Datasetflow if Dataset exists
		if(dataSetListResponseVO != null) {
			DataSetListResponseVO dataSet = dataSetListResponseVO.getBody();
			if (dataSet != null && dataSet.getTotalCount() > 0 && dataSet.getDataSetResponseVO() != null) {
				ResponseEntity<DataSetFlowResponseVO> dataSetFlowResponseVO = dataSetFlowSVC.getDataSetFlow(dataSet.getDataSetResponseVO().get(0).getId());
				// 2-1. Handle Datasetflow to prevent modification if it exists
				if(dataSetFlowResponseVO != null && dataSetFlowResponseVO.getBody() != null) {
					log.warn("DataModel can't be modified because the dataset flow has been created. DataModelId: {}", dataModelVO.getId());
					throw new BadRequestException(ErrorCode.BAD_REQUEST, "데이터셋 흐름이 생성된 모델은 수정할 수 없습니다.");
				}
			}
		}
		
		String pathUri = DEFAULT_PATH_URL + "/" + dataModelVO.getId();
		String dataModel = new Gson().toJson(dataModelVO);
		
		ResponseEntity<T> response = (ResponseEntity<T>) dataCoreRestSVC.put(datamodelUrl, pathUri, null, dataModel, null, Void.class);
		
		return response;
	}

	/**
	 * Delete data model
	 * @param id			Data model ID
	 * @return				Result of delete data model.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	public <T> ResponseEntity<T> deleteDataModel(String id) throws Exception {
		String pathUri = DEFAULT_PATH_URL + "/" + id;
		
		ResponseEntity<T> response = (ResponseEntity<T>) dataCoreRestSVC.delete(datamodelUrl, pathUri, null, null, null, Void.class);
		
		return response;
	}

	/**
	 * Retrieve data model
	 * @param id			Data model ID
	 * @return				Data model information retrieved by id.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	public ResponseEntity<DataModelResponseVO> getDataModel(String id) throws Exception {
		String pathUri = DEFAULT_PATH_URL + "/" + id;
		Map<String, String> header = new HashMap<String, String>();
		header.put("Accept", "application/json");
		
		ResponseEntity<DataModelResponseVO> response = dataCoreRestSVC.get(datamodelUrl, pathUri, header, null, null, DataModelResponseVO.class);
		
		// Entity property Tree information is set.
		if(response != null && response.getBody() != null) {
			List<AttributeVO> attributes = response.getBody().getAttributes();
			
			if(attributes != null) {
				List<UiTreeVO> treeStructure = makeUiAttrTreeStructure(attributes);
				response.getBody().setTreeStructure(treeStructure);
			}
		}
		
		return response;
	}
	
	/**
	 * Retrieve attribute of data model
	 * @param id		Data model ID
	 * @param attrName	Attribute name
	 * @return			Data model information retrieved by id and attribute name.
	 */
	public ResponseEntity<DataModelResponseVO> getDataModelAttr(String id, String attrName) {
		String pathUri = DEFAULT_PATH_URL + "/" + id;
		Map<String, String> header = new HashMap<String, String>();
		header.put("Accept", "application/json");
		
		ResponseEntity<DataModelResponseVO> response = dataCoreRestSVC.get(datamodelUrl, pathUri, header, null, null, DataModelResponseVO.class);
		
		// Set object member tree information.
		if(response != null && response.getBody() != null) {
			List<AttributeVO> attributes = response.getBody().getAttributes();
			
			if(attributes != null) {
				List<ObjectMemberVO> objectMemberVOs = getObjectMembers(attributes, attrName);
				if (objectMemberVOs != null && !objectMemberVOs.isEmpty()) {
					List<UiTreeVO> treeStructure = makeUiObjMemTreeStructure(objectMemberVOs);
					response.getBody().setTreeStructure(treeStructure);
				}
			}
		}
		
		return response;
	}
	
	/**
	 * Get object member in attributes
	 * @param attributes	List of attribute
	 * @param attrName		Attribute name
	 * @return				Object member information in attributes searched by attribute name.
	 */
	private List<ObjectMemberVO> getObjectMembers(List<AttributeVO> attributes, String attrName) {
		for(AttributeVO attribute : attributes) {
			if(attribute != null 
					&& attribute.getChildAttributes() != null 
					&& !attribute.getChildAttributes().isEmpty()) {
				List<ObjectMemberVO> objectMembers = getObjectMembers(attribute.getChildAttributes(), attrName);
				if (objectMembers != null) {
					return objectMembers;
				}
				
				if(attribute !=null && attrName.equals(attribute.getName())) {
					if(attribute.getObjectMembers() != null) {
						return attribute.getObjectMembers();
					}
				}
			} else {
				if(attribute !=null && attrName.equals(attribute.getName())) {
					if(attribute.getObjectMembers() != null) {
						return attribute.getObjectMembers();
					}
				}
			}
		}
		
		return null;
	}

	/**
	 * Retrieve multiple data model
	 * @param dataModelRetrieveVO		DataModelRetrieveVO
	 * @return							List of data model information retrieved by DataModelRetrieveVO.
	 * @throws Exception				Throw an exception when an error occurs.
	 */
	public ResponseEntity<DataModelListResponseVO> getDataModels(DataModelRetrieveVO dataModelRetrieveVO) throws Exception {
		DataModelListResponseVO dataModelListResponseVO = new DataModelListResponseVO();
		Map<String, Object> param = createParams(dataModelRetrieveVO);
		Map<String, String> header = new HashMap<String, String>();
		header.put("Accept", "application/json");
		
		ResponseEntity<List<DataModelResponseVO>> response = dataCoreRestSVC.getList(datamodelUrl, DEFAULT_PATH_URL, header, null, param, new ParameterizedTypeReference<List<DataModelResponseVO>>() {});
		if (response != null) {
			dataModelListResponseVO.setDataModelResponseVOs(response.getBody());
			if(response.getHeaders() != null) {
				List<String> totalCountHeader = (response.getHeaders().get(Constants.TOTAL_COUNT));
				if (totalCountHeader != null && totalCountHeader.size() > 0) {
					dataModelListResponseVO.setTotalCount(Integer.valueOf(totalCountHeader.get(0)));
				}
			}
		}
		
		return ResponseEntity.status(response.getStatusCode()).body(dataModelListResponseVO);
	}

	/**
	 * Request data model provision
	 * @param id	Data model ID
	 * @return		Data Model Provisioning Results
	 */
	public <T> ResponseEntity<T> requestDataModelProvision(String id) {
		String pathUri = DEFAULT_PATH_URL + "/" + id + "/provisioning";
		
		ResponseEntity<T> response = (ResponseEntity<T>) dataCoreRestSVC.post(datamodelUrl, pathUri, null, null, null, Void.class);
		
		return response;
	}
	
	/**
	 * Retrieve data model ID
	 * @return				List of data model ID
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	public ResponseEntity<List<String>> getDataModelsId() throws Exception {
		ResponseEntity<DataModelListResponseVO> response = getDataModels(new DataModelRetrieveVO());
		DataModelListResponseVO dataModelListResponseVO = response.getBody();
		
		if(dataModelListResponseVO == null || ValidateUtil.isEmptyData(dataModelListResponseVO.getDataModelResponseVOs())) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}
		
		List<DataModelResponseVO> dataModelResponseList = dataModelListResponseVO.getDataModelResponseVOs();
		List<String> idList = new ArrayList<String>();
		for(DataModelResponseVO dataModelResponseVO : dataModelResponseList) {
			idList.add(dataModelResponseVO.getId());
		}
		
		Collections.sort(idList);
		
		return ResponseEntity.status(response.getStatusCode()).body(idList);
	}
	
	/**
	 * Create Entity property tree structure for UI
	 * @param attributes	List of attribute
	 * @return				List of attribute tree structure for UI
	 */
	private List<UiTreeVO> makeUiAttrTreeStructure(List<AttributeVO> attributes) {
		List<UiTreeVO> treeStructure = new ArrayList<UiTreeVO>();
		
		AttributeCompareUtil attributeCompare = new AttributeCompareUtil();
		Collections.sort(attributes, attributeCompare);
		
		for(AttributeVO attribute : attributes) {
			UiTreeVO uiTree = new UiTreeVO();
			uiTree.setId(attribute.getName());
			uiTree.setLabel(attribute.getName() + "(" + attribute.getAttributeType() + ")");
			if(attribute.getChildAttributes() != null && attribute.getChildAttributes().size() > 0) {
				List<UiTreeVO> uiTreeVO = makeUiAttrTreeStructure(attribute.getChildAttributes());
				uiTree.setChildren(uiTreeVO);
			}
			treeStructure.add(uiTree);
		}
		
		return treeStructure;
	}
	
	/**
	 * Create ObjectMember tree structure for UI
	 * @param objectMembers		List of object member
	 * @return					List of object member tree structure for UI
	 */
	private List<UiTreeVO> makeUiObjMemTreeStructure(List<ObjectMemberVO> objectMembers) {
		List<UiTreeVO> treeStructure = new ArrayList<UiTreeVO>();
		
		ObjectMemberCompareUtil objectMemberCompare = new ObjectMemberCompareUtil();
		Collections.sort(objectMembers, objectMemberCompare);
		
		for(ObjectMemberVO objectMember : objectMembers) {
			UiTreeVO uiTree = new UiTreeVO();
			uiTree.setId(objectMember.getName());
			uiTree.setLabel(objectMember.getName() + "(" + objectMember.getValueType() + ")");
			if(objectMember.getObjectMembers() != null && objectMember.getObjectMembers().size() > 0) {
				List<UiTreeVO> uiTreeVO = makeUiObjMemTreeStructure(objectMember.getObjectMembers());
				uiTree.setChildren(uiTreeVO);
			}
			treeStructure.add(uiTree);
		}
		
		return treeStructure;
	}
	
	/**
	 * Create http param from DataModelRetrieveVO
	 * @param dataModelRetrieveVO	DataModelRetrieveVO
	 * @return						Http request parameter
	 */
	private Map<String, Object> createParams(DataModelRetrieveVO dataModelRetrieveVO) {
		Map<String, Object> param = new HashMap<String, Object>();
		
		if(dataModelRetrieveVO == null) {
			return null;
		}
		
		if(!ValidateUtil.isEmptyData(dataModelRetrieveVO.getId())) {
			param.put("id", dataModelRetrieveVO.getId().trim());
		}
		if(!ValidateUtil.isEmptyData(dataModelRetrieveVO.getType())) {
			param.put("type", dataModelRetrieveVO.getType().trim());
		}
		if(!ValidateUtil.isEmptyData(dataModelRetrieveVO.getTypeUri())) {
			param.put("typeUri", dataModelRetrieveVO.getTypeUri().trim());
		}
		if(!ValidateUtil.isEmptyData(dataModelRetrieveVO.getName())) {
			param.put("name", dataModelRetrieveVO.getName().trim());
		}
		if(!ValidateUtil.isEmptyData(dataModelRetrieveVO.getLimit())) {
			param.put("limit", dataModelRetrieveVO.getLimit());
		}
		if(!ValidateUtil.isEmptyData(dataModelRetrieveVO.getOffset())) {
			param.put("offset", dataModelRetrieveVO.getOffset());
		}
		
		return param;
	}
	
	/**
	 * Convert context to flat map
	 * @param context		Context
	 * @param contextMap	Result object(map type)
	 */
	private void contextToFlatMap(Object context, TreeMap<String, String> contextMap) {
		if(context instanceof String) {
			if(((String) context).startsWith("http")) {
				// If it starts with http, it searches http get because it has uri information.
				Map<String, String> headerMap = new HashMap<>();
		        headerMap.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		        headerMap.put(HttpHeaders.ACCEPT, MediaType.ALL_VALUE);
				
		        
		        ResponseEntity<String> responseEntity = null;
		        try {
		        	responseEntity = dataCoreRestSVC.get((String)context, "", headerMap, null, null, String.class);
		        } catch (Exception e) {
		        	log.warn("Context information is incorrect. Please check the Context. context:{}", (String)context, e);
		        	ClientExceptionPayloadVO clientExceptionPayloadVO = new ClientExceptionPayloadVO();
		        	clientExceptionPayloadVO.setTitle("Context information is incorrect.");
		        	clientExceptionPayloadVO.setDetail("Please check the Context. context:" + context);
		        	throw new DataCoreUIException(HttpStatus.BAD_REQUEST, clientExceptionPayloadVO);
		        }
				
				if(responseEntity.getStatusCode() == HttpStatus.OK) {
					if(ValidateUtil.isEmptyData(responseEntity.getBody())) {
						throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Retrieve @context error. body is empty. contextUri=" + context);
					}
					ContextVO contextVO = null;
					try {
						contextVO = objectMapper.readValue(responseEntity.getBody(), ContextVO.class);
						if(contextVO == null) {
							throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Retrieve @context error. body is empty. contextUri=" + context);
						}
					} catch (IOException e) {
						throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Retrieve @context error. body is empty. contextUri=" + context, e);
					}
					contextToFlatMap(contextVO.getContext(), contextMap);
				} else {
					throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Retrieve @context error. "
							+ "Invalid responseCode=" + responseEntity.getStatusCode() + ". contextUri=" + context);
				}
			}
		} else if(context instanceof Map) {
			Map<String, Object> contextInnerMap = (Map)context;
			for(Map.Entry<String, Object> entry : contextInnerMap.entrySet()) {
				String entryKey = entry.getKey();
				Object entryValue = entry.getValue();

				String value = null;
				if(entryValue instanceof String) {
					value = (String)entryValue;
				} else if(entryValue instanceof Map) {
					value = (String)((Map)entryValue).get("@id");
					// @type ?
				}

				if(value != null && !value.startsWith("http") && value.contains(":")) {
					String[] valueArr = value.split(":", 2);
					String valueReferenceUri = (String)contextInnerMap.get(valueArr[0]);
					if(!ValidateUtil.isEmptyData(valueReferenceUri)) {
						value = valueReferenceUri + valueArr[1];
					}
				}

				if(value != null && value.startsWith("http")) {
					contextMap.put(entryKey, value);
				} else {
					log.debug("DataModel @context attribute value is not uri. entryKey={}, entryValue={}, value={}", entryKey, entryValue, value);
				}
			}
		} else if(context instanceof List) {
			for(Object innerContext : ((List)context)) {
				contextToFlatMap(innerContext, contextMap);
			}
		} else {
			throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Retrieve @context error. Unsupported class type. type=" + context.getClass());
		}
	}
}
