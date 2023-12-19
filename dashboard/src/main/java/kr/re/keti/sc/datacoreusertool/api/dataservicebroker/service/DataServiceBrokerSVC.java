package kr.re.keti.sc.datacoreusertool.api.dataservicebroker.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import kr.re.keti.sc.datacoreusertool.common.exception.DataCoreUIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import kr.re.keti.sc.datacoreusertool.api.datamodel.service.DataModelSVC;
import kr.re.keti.sc.datacoreusertool.api.datamodel.vo.Attribute;
import kr.re.keti.sc.datacoreusertool.api.datamodel.vo.DataModelVO;
import kr.re.keti.sc.datacoreusertool.api.datamodel.vo.ObjectMember;
import kr.re.keti.sc.datacoreusertool.api.dataservicebroker.vo.AttributeCountVO;
import kr.re.keti.sc.datacoreusertool.api.dataservicebroker.vo.CommonEntityListResponseVO;
import kr.re.keti.sc.datacoreusertool.api.dataservicebroker.vo.CommonEntityVO;
import kr.re.keti.sc.datacoreusertool.api.dataservicebroker.vo.EntityRetrieveVO;
import kr.re.keti.sc.datacoreusertool.api.dataservicebroker.vo.QVO;
import kr.re.keti.sc.datacoreusertool.common.code.Constants;
import kr.re.keti.sc.datacoreusertool.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.datacoreusertool.common.code.DataServiceBrokerCode.AttributeValueType;
import kr.re.keti.sc.datacoreusertool.common.code.DataServiceBrokerCode.QueryOperatorType;
import kr.re.keti.sc.datacoreusertool.common.component.Properties;
import kr.re.keti.sc.datacoreusertool.common.service.DataCoreRestSVC;
import kr.re.keti.sc.datacoreusertool.security.service.UserToolSecuritySVC;
import kr.re.keti.sc.datacoreusertool.util.DateUtil;
import kr.re.keti.sc.datacoreusertool.util.ValidateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for DataServiceBroker API calls.
 * @FileName DataServiceBrokerSVC.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 25.
 * @Author Elvin
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DataServiceBrokerSVC {
	
	@Value("${dataservicebroker.url}")
	private String dataservicebrokerUrl;
	
	private final static String DEFAULT_PATH_URL = "entities";
	private final static String ENTITY_COUNT_PATH_URL = "entitycount";
	private final static String ENTITY_HISTORY_PATH_URL = "temporal/entities";
	private final static String ENTITY_HISTORY_COUNT_PATH_URL = "temporal/entitycount";
	
	private final static String ATTRIBUTE_SEPARATOR = ".";
	private final static String OBJECTMEMBER_PREFIX = "[";
	private final static String OBJECTMEMBER_SUFFIX = "]";
	private final static String LINK_PREFIX = "<";
	private final static String LINK_SUFFIX = ">";
	private final static String LINK_REL = "\"http://www.w3.org/ns/json-ld#context\"";
	private final static String LINK_TYPE = "\"application/ld+json\"";
	
	private final static String DEFAULT_GEOMETRY = "Polygon";
	private final static String DEFAULT_GEOREL = "within";
	
	private final static String FILTER_CONDITION_OFFSET = "offset";
	private final static String FILTER_CONDITION_LIMIT = "limit";
	private final static String FILTER_CONDITION_ATTRS = "attrs";
	private final static String FILTER_CONDITION_OPTIONS = "options";
	private final static String FILTER_CONDITION_TIMEREL = "timerel";
	private final static String FILTER_CONDITION_TIME = "timeAt";
	private final static String FILTER_CONDITION_ENDTIME = "endtimeAt";
	private final static String FILTER_CONDITION_TIMEPROPERTY = "timeproperty";
	private final static String FILTER_CONDITION_Q = "q";
	private final static String FILTER_CONDITION_GEOPROPERTY = "geoproperty";
	private final static String FILTER_CONDITION_GEOREL = "georel";
	private final static String FILTER_CONDITION_GEOMETRY = "geometry";
	private final static String FILTER_CONDITION_COORDINATES = "coordinates";
	private final static String NORMALIZED_HISTORY = "normalizedHistory";
	private final static String ATTR_DISPLAY_VALUE = "displayValue";
	private final static String ATTR_VALUE = "value";
	private final static String TIMEPROPERTY_MODIFIEDAT = "modifiedAt";
	
	private final static String AUTHTOKEN = "authToken";
	private final static String BEARER = "Bearer ";
	
	private final static String GEOPROPERTY = "GeoProperty";
	private final static String GEOPROPERTY_UI = "geoproperty_ui";
	
	private final static int DEFAULT_OFFSET = 0;
	
	private final DataCoreRestSVC dataCoreRestSVC;
	
	public static Map<String, HttpServletRequest> userRequest = new HashMap<String, HttpServletRequest>();
	
	@Autowired
	Properties properties;
	
	@Autowired
	private DataModelSVC dataModelSVC;
	
	@Autowired
	private UserToolSecuritySVC userToolSecuritySVC;

	
	/**
	 * Retrieve list of entity by search condition.
	 * @param isMap					for map data or not
	 * @param entityRetrieveVO		EntityRetrieveVO
	 * @param userId				User ID
	 * @return						List of entity retrieved by EntityRetrieveVO.
	 */
	public ResponseEntity<CommonEntityListResponseVO> getEntities(boolean isMap, EntityRetrieveVO entityRetrieveVO, HttpServletRequest request, String userId) {
		if(request == null) {
			request = userRequest.get(userId);
		} else {
			// Save request by user (request information per user is not checked when connecting to widget websocket)
			saveHttpServletRequest(request);
		}
		
		DataModelVO dataModel = getDataModel(entityRetrieveVO);
		if(dataModel == null) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}
		
		entityRetrieveVO.setDataModelId(dataModel.getId());
		entityRetrieveVO.setType(dataModel.getType());
		entityRetrieveVO.setTypeUri(dataModel.getTypeUri());
		
		CommonEntityListResponseVO commonEntityListResponseVO = new CommonEntityListResponseVO();
		Map<String, Object> params = creatParams(entityRetrieveVO, false, request);
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(Constants.HTTP_HEADER_KEY_ACCEPT, Constants.ACCEPT_TYPE_APPLICATION_JSON);
		headers.put(Constants.HTTP_HEADER_LINK, convertLinkFormat(dataModel.getContext()));
		if (properties.getSpringSecurityEnabled()) {
			headers.put(Constants.HTTP_HEADER_AUTHORIZATION, BEARER + request.getSession().getAttribute(AUTHTOKEN));
		}
		
		// 1. Retrieve count
		ResponseEntity<AttributeCountVO> count = dataCoreRestSVC.getList(dataservicebrokerUrl, ENTITY_COUNT_PATH_URL, headers, null, params, AttributeCountVO.class);
		
		if(count != null &&HttpStatus.SERVICE_UNAVAILABLE.equals(count.getStatusCode())) {
			return ResponseEntity.status(count.getStatusCode()).build();
		}
		
		if(count == null || count.getBody() == null || count.getBody().getTotalCount() < 1) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}
		
		// 2. Retrieve latest entity value
		ResponseEntity<List<CommonEntityVO>> response = dataCoreRestSVC.getList(dataservicebrokerUrl, DEFAULT_PATH_URL, headers, null, params, new ParameterizedTypeReference<List<CommonEntityVO>>() {});
		if(response != null) {
			commonEntityListResponseVO.setCommonEntityVOs(response.getBody());
			commonEntityListResponseVO.setTotalCount(count.getBody().getTotalCount());
			
			if(!isMap) {
				commonEntityListResponseVO.setAttrsLabel(dataModelSVC.getDataModelAttrs(entityRetrieveVO.getDataModelId(), entityRetrieveVO.getTypeUri(), Constants.TOP_LEVEL_ATTR).getBody());
			}
		}
		
		return ResponseEntity.status(response.getStatusCode()).body(commonEntityListResponseVO);
	}

	/**
	 * Retrieve multiple Entity(latest) by model
	 * @param entityRetrieveVOs		List of EntityRetrieveVO
	 * @param userId				User ID
	 * @return						List of multiple entity retrieved by EntityRetrieveVO list.
	 */
	public ResponseEntity<List<CommonEntityListResponseVO>> getEntitiesbyMultiModel(List<EntityRetrieveVO> entityRetrieveVOs, HttpServletRequest request, String userId) {
		if(request == null) {
			request = userRequest.get(userId);
		} else {
			// Save request by user (request information per user is not checked when connecting to widget websocket)
			saveHttpServletRequest(request);
		}
		
		List<CommonEntityListResponseVO> commonEntityListResponseVOs = new ArrayList<CommonEntityListResponseVO>();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(Constants.HTTP_HEADER_KEY_ACCEPT, Constants.ACCEPT_TYPE_APPLICATION_JSON);

		if (properties.getSpringSecurityEnabled()) {
			headers.put(Constants.HTTP_HEADER_AUTHORIZATION, BEARER + request.getSession().getAttribute(AUTHTOKEN));
		}
		
		HttpStatus status = HttpStatus.OK;
		
		for(EntityRetrieveVO entityRetrieveVO : entityRetrieveVOs) {
			headers.remove(Constants.HTTP_HEADER_LINK);
			
			DataModelVO dataModel = getDataModel(entityRetrieveVO);
			if(dataModel == null) {
				return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
			}
			
			entityRetrieveVO.setDataModelId(dataModel.getId());
			entityRetrieveVO.setType(dataModel.getType());
			entityRetrieveVO.setTypeUri(dataModel.getTypeUri());
			
			headers.put(Constants.HTTP_HEADER_LINK, convertLinkFormat(dataModel.getContext()));
			
			Map<String, Object> params = creatParams(entityRetrieveVO, false, request);
			
			// Retrieve count
			ResponseEntity<AttributeCountVO> count = dataCoreRestSVC.getList(dataservicebrokerUrl, ENTITY_COUNT_PATH_URL, headers, null, params, AttributeCountVO.class);
			
			if(count == null || count.getBody() == null || count.getBody().getTotalCount() < 1) {
				continue;
			}
			
			// Retrieve latest entity value
			ResponseEntity<List<CommonEntityVO>> response = dataCoreRestSVC.getList(dataservicebrokerUrl, DEFAULT_PATH_URL, headers, null, params, new ParameterizedTypeReference<List<CommonEntityVO>>() {});
			if(response != null) {
				CommonEntityListResponseVO commonEntityListResponseVO = new CommonEntityListResponseVO();
				List<CommonEntityVO> commonEntityVOs = response.getBody();
				if(!ValidateUtil.isEmptyData(entityRetrieveVO.getDisplayAttribute())) {
					setDisplayValue(commonEntityVOs, entityRetrieveVO.getDisplayAttribute());
				}
				for(CommonEntityVO commonEntityVO : commonEntityVOs) {
					for(String key : commonEntityVO.keySet()) {
						if(commonEntityVO.get(key).toString().contains(GEOPROPERTY)) {
							commonEntityVO.put(GEOPROPERTY_UI, key);
							break;
						}
					}
				}
				commonEntityListResponseVO.setCommonEntityVOs(commonEntityVOs);
				commonEntityListResponseVO.setTotalCount(count.getBody().getTotalCount());
				commonEntityListResponseVOs.add(commonEntityListResponseVO);
				if(status != response.getStatusCode()) {
					status = response.getStatusCode();
				}
			}
		}
		
		if(ValidateUtil.isEmptyData(commonEntityListResponseVOs)) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}
		
		return ResponseEntity.status(status).body(commonEntityListResponseVOs);
	}
	
	/**
	 * Retrieve Entity(latest)
	 * @param id					Entity ID
	 * @param entityRetrieveVO		EntityRetrieveVO
	 * @param userId				User ID
	 * @return						Entity retrieved by entity ID and EntityRetrieveVO.
	 */
	public ResponseEntity<CommonEntityVO> getEntityById(String id, EntityRetrieveVO entityRetrieveVO, HttpServletRequest request, String userId) {
		if(request == null) {
			request = userRequest.get(userId);
		} else {
			// Save request by user (request information per user is not checked when connecting to widget websocket)
			saveHttpServletRequest(request);
		}
		
		String pathUri = DEFAULT_PATH_URL + "/" + id;
		
		DataModelVO dataModel = null;
		if(!ValidateUtil.isEmptyData(entityRetrieveVO.getDataModelId())) {
			dataModel = getDataModel(entityRetrieveVO);
			
			if(dataModel == null) {
				return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
			}
			
			entityRetrieveVO.setDataModelId(dataModel.getId());
			entityRetrieveVO.setType(dataModel.getType());
			entityRetrieveVO.setTypeUri(dataModel.getTypeUri());
		}
		
		Map<String, Object> params = creatParams(entityRetrieveVO, false, request);
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(Constants.HTTP_HEADER_KEY_ACCEPT, Constants.ACCEPT_TYPE_APPLICATION_JSON);
		if(dataModel != null) {
			headers.put(Constants.HTTP_HEADER_LINK, convertLinkFormat(dataModel.getContext()));
		}
		if (properties.getSpringSecurityEnabled()) {
			headers.put(Constants.HTTP_HEADER_AUTHORIZATION, BEARER + request.getSession().getAttribute(AUTHTOKEN));
		}
		
		// 1. Retrieve latest entity value by Entity ID
		ResponseEntity<CommonEntityVO> response = dataCoreRestSVC.getList(dataservicebrokerUrl, pathUri, headers, null, params, CommonEntityVO.class);
		
		return response;
	}
	
	/**
	 * Retrieve multiple Entity(historical)
	 * @param entityRetrieveVO		EntityRetrieveVO
	 * @param userId				User ID
	 * @return						List of entity retrieved by EntityRetrieveVO.
	 */
	public ResponseEntity<CommonEntityListResponseVO> getEntitiesHistory(EntityRetrieveVO entityRetrieveVO, HttpServletRequest request, String userId) {
		if(request == null) {
			request = userRequest.get(userId);
		} else {
			// Save request by user (request information per user is not checked when connecting to widget websocket)
			saveHttpServletRequest(request);
		}
		
		DataModelVO dataModel = getDataModel(entityRetrieveVO);
		if(dataModel == null) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}
		
		entityRetrieveVO.setDataModelId(dataModel.getId());
		entityRetrieveVO.setType(dataModel.getType());
		entityRetrieveVO.setTypeUri(dataModel.getTypeUri());
		
		CommonEntityListResponseVO commonEntityListResponseVO = new CommonEntityListResponseVO();
		Map<String, Object> params = null;

		try {
			params = creatParams(entityRetrieveVO, true, request);
		} catch (DataCoreUIException e) {
			log.error(String.format("Exception occurred while retrieving entity latest value from Data Service Broker. Response Status Code: %s, Response Payload: %s", e.getHttpStatus(), e.getErrorPayload()), e);
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}

		Map<String, String> headers = new HashMap<String, String>();
		headers.put(Constants.HTTP_HEADER_KEY_ACCEPT, Constants.ACCEPT_TYPE_APPLICATION_JSON);
		headers.put(Constants.HTTP_HEADER_LINK, convertLinkFormat(dataModel.getContext()));
		if (properties.getSpringSecurityEnabled()) {
			headers.put(Constants.HTTP_HEADER_AUTHORIZATION, BEARER + request.getSession().getAttribute(AUTHTOKEN));
		}

		// 1. Retrieve count
		ResponseEntity<AttributeCountVO> count = dataCoreRestSVC.getList(dataservicebrokerUrl, ENTITY_HISTORY_COUNT_PATH_URL, headers, null, params, AttributeCountVO.class);
		
		if(count != null && HttpStatus.SERVICE_UNAVAILABLE.equals(count.getStatusCode())) {
			return ResponseEntity.status(count.getStatusCode()).build();
		}
		
		if(count == null || count.getBody() == null || count.getBody().getTotalCount() < 1) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}

		// Search results is more than 1000, only up to 1000 are searched.
		if(count.getBody().getTotalCount() > properties.getEntityHistoryLimit()) {
			params.put(FILTER_CONDITION_OFFSET, DEFAULT_OFFSET);
			params.put(FILTER_CONDITION_LIMIT, properties.getEntityHistoryLimit());
		}
		
		// 2. Retrieve historical entity value
		ResponseEntity<List<CommonEntityVO>> response = dataCoreRestSVC.getList(dataservicebrokerUrl, ENTITY_HISTORY_PATH_URL, headers, null, params, new ParameterizedTypeReference<List<CommonEntityVO>>() {});
		if(response != null) {
			List<CommonEntityVO> commonEntityVOs = response.getBody();
			List<CommonEntityVO> renewalcommonEntityVOs = new ArrayList<CommonEntityVO>();
			
			if(commonEntityVOs == null) {
				return ResponseEntity.status(response.getStatusCode()).build();
			}
			
			for(CommonEntityVO commonEntityVO : commonEntityVOs) {
				// Reconfigured to group by Entity Level as the same Attribute is grouped 
				CommonEntityListResponseVO temp = reconstruction(dataModel, commonEntityVO, entityRetrieveVO);
				renewalcommonEntityVOs.addAll(temp.getCommonEntityVOs());
			}
			
			if(!ValidateUtil.isEmptyData(entityRetrieveVO.getDisplayAttribute())) {
				setDisplayValue(renewalcommonEntityVOs, entityRetrieveVO.getDisplayAttribute());
			}
			commonEntityListResponseVO.setCommonEntityVOs(renewalcommonEntityVOs);
			commonEntityListResponseVO.setTotalCount(renewalcommonEntityVOs.size());
			commonEntityListResponseVO.setAttrsLabel(dataModelSVC.getDataModelAttrs(entityRetrieveVO.getDataModelId(), entityRetrieveVO.getTypeUri(), Constants.OBSERVED_AT_ATTR).getBody());
		}
		
		return ResponseEntity.status(response.getStatusCode()).body(commonEntityListResponseVO);
	}
	
	/**
	 * Retrieve single Entity(historical)
	 * @param id				Entity ID
	 * @param entityRetrieveVO	EntityRetrieveVO
	 * @return					List of entity retrieved by entity ID and EntityRetrieveVO.
	 */
	public ResponseEntity<CommonEntityListResponseVO> getEntityHistoryById(String id, EntityRetrieveVO entityRetrieveVO, HttpServletRequest request) {
		String pathUri = ENTITY_HISTORY_PATH_URL + "/" + id;
		String countPathUri = ENTITY_HISTORY_COUNT_PATH_URL + "/" + id;
		
		DataModelVO dataModel = null;
		if(!ValidateUtil.isEmptyData(entityRetrieveVO.getDataModelId())) {
			dataModel = getDataModel(entityRetrieveVO);
			
			if(dataModel == null) {
				return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
			}
			
			entityRetrieveVO.setDataModelId(dataModel.getId());
			entityRetrieveVO.setType(dataModel.getType());
			entityRetrieveVO.setTypeUri(dataModel.getTypeUri());
		}
		entityRetrieveVO.setId(id);
		
		
		Map<String, Object> params = creatParams(entityRetrieveVO, true, request);
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(Constants.HTTP_HEADER_KEY_ACCEPT, Constants.ACCEPT_TYPE_APPLICATION_JSON);
		if(dataModel != null) {
			headers.put(Constants.HTTP_HEADER_LINK, convertLinkFormat(dataModel.getContext()));
		}
		if (properties.getSpringSecurityEnabled()) {
			headers.put(Constants.HTTP_HEADER_AUTHORIZATION, BEARER + request.getSession().getAttribute(AUTHTOKEN));
		}
		
		// Save request by user (request information per user is not checked when connecting to widget websocket)
		saveHttpServletRequest(request);
		
		// 1. Retrieve count
		ResponseEntity<AttributeCountVO> count = dataCoreRestSVC.getList(dataservicebrokerUrl, countPathUri, headers, null, params, AttributeCountVO.class);
		
		if(count != null &&HttpStatus.SERVICE_UNAVAILABLE.equals(count.getStatusCode())) {
			return ResponseEntity.status(count.getStatusCode()).build();
		}
		
		if(count == null || count.getBody() == null || count.getBody().getTotalCount() < 1) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}
		
		// Search results is more than 1000, only up to 1000 are searched.
		if(count.getBody().getTotalCount() > properties.getEntityHistoryLimit()) {
			params.put(FILTER_CONDITION_OFFSET, DEFAULT_OFFSET);
			params.put(FILTER_CONDITION_LIMIT, properties.getEntityHistoryLimit());
		}
		
		CommonEntityListResponseVO commonEntityListResponseVO = null;
		
		// 2. Retrieve historical entity by Entity ID
		if(NORMALIZED_HISTORY.equals(entityRetrieveVO.getOptions())) {
			params.put(DataServiceBrokerCode.DefaultAttributeKey.ID.getCode(), id);
			ResponseEntity<List<CommonEntityVO>> response = dataCoreRestSVC.getList(dataservicebrokerUrl, ENTITY_HISTORY_PATH_URL, headers, null, params, new ParameterizedTypeReference<List<CommonEntityVO>>() {});
			if(response == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
			}
			
			List<CommonEntityVO> commonEntityVOs = response.getBody();
			
			List<String> attrLabels = dataModelSVC.getDataModelAttrs(entityRetrieveVO.getDataModelId(), entityRetrieveVO.getTypeUri(), Constants.TOP_LEVEL_ATTR).getBody();
			
			commonEntityListResponseVO = new CommonEntityListResponseVO();
			commonEntityListResponseVO.setAttrsLabel(attrLabels);
			commonEntityListResponseVO.setCommonEntityVOs(commonEntityVOs);
			commonEntityListResponseVO.setTotalCount(commonEntityVOs.size());
			
			return ResponseEntity.status(response.getStatusCode()).body(commonEntityListResponseVO);
		} else {
			ResponseEntity<CommonEntityVO> response = dataCoreRestSVC.getList(dataservicebrokerUrl, pathUri, headers, null, params, CommonEntityVO.class);
			if(response == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
			}
			
			CommonEntityVO commonEntityVO = response.getBody();
			
			// Reconfigured to group by Entity Level as the same Attribute is grouped 
			commonEntityListResponseVO = reconstruction(dataModel, commonEntityVO, entityRetrieveVO);
			
			return ResponseEntity.status(response.getStatusCode()).body(commonEntityListResponseVO);
		}
	}
	
	/**
	 * Reconstruction entity VO
	 * @param dataModel		DataModelVO
	 * @param commonEntityVO		CommonEntityVO
	 * @param entityRetrieveVO		EntityRetrieveVO
	 * @return						List of entity retrieved by CommonEntityVO ando EntityRetrieveVO.
	 */
	private CommonEntityListResponseVO reconstruction(DataModelVO dataModel, CommonEntityVO commonEntityVO, EntityRetrieveVO entityRetrieveVO) {
		CommonEntityListResponseVO commonEntityListResponseVO = new CommonEntityListResponseVO();
		
		if(!ValidateUtil.isEmptyData(commonEntityVO)) {
			List<String> attributeNames = dataModelSVC.getDataModelAttrs(dataModel, Constants.OBSERVED_AT_ATTR);
			
			List<CommonEntityVO> result = new ArrayList<CommonEntityVO>();
			for(String attrLabel : attributeNames) {
				List<Object> objects = null;
				
				if("temporalValues".equals(entityRetrieveVO.getOptions())) {
					Map<String, Object> objectMap = (Map<String, Object>) commonEntityVO.get(attrLabel);
					objects = (List<Object>) objectMap.get("values");
				} else {
					objects = (List<Object>) commonEntityVO.get(attrLabel);
				}
				
				if(ValidateUtil.isEmptyData(objects)) {
					continue;
				}

				for (Object object : objects) {
					CommonEntityVO temp = new CommonEntityVO();
					temp.put(DataServiceBrokerCode.DefaultAttributeKey.ID.getCode(), commonEntityVO.getId());
					temp.put(DataServiceBrokerCode.DefaultAttributeKey.TYPE.getCode(), commonEntityVO.getType());

					if (object instanceof Map) {
						if (((Map) object).get("type") != null && DataServiceBrokerCode.AttributeType.GEO_PROPERTY.getCode().equals(((Map) object).get("type"))) {
							temp.put(GEOPROPERTY_UI, attrLabel);
						}
					}
					temp.put(attrLabel, object);
					result.add(temp);
				}
			}
			
			if(!ValidateUtil.isEmptyData(entityRetrieveVO.getAttrs()) && entityRetrieveVO.getAttrs().size() > 0) {
				Collections.reverse(result);
			}
			
			commonEntityListResponseVO.setAttrsLabel(attributeNames);
			commonEntityListResponseVO.setCommonEntityVOs(result);
			commonEntityListResponseVO.setTotalCount(result.size());
		}
		
		return commonEntityListResponseVO;
	}

	/**
	 * Create a param for request entity.
	 * @param entityRetrieveVO	EntityRetrieveVO
	 * @param isHistory			true if historical data, false otherwise.
	 * @return					request parameter
	 */
	private Map<String, Object> creatParams(EntityRetrieveVO entityRetrieveVO, boolean isHistory, HttpServletRequest request) {
		Map<String, Object> params = new HashMap<String, Object>();
		
		if(entityRetrieveVO == null) {
			return null;
		}
		
		// Common search conditions
		addCommonQuery(entityRetrieveVO, params);
		
		// History search period
		addTemporalQuery(entityRetrieveVO, params, isHistory, request);
		
		// Search value
		if(!ValidateUtil.isEmptyData(entityRetrieveVO.getSearchValue())) {
			addSearchValue(entityRetrieveVO, params);
		// Search Query
		} else {
			// Query
			addQuery(entityRetrieveVO, params);
			
			// GeoQuery
			addGeoQuery(entityRetrieveVO, params);
		}
		
		return params;
	}
	
	/**
	 * Common search conditions
	 * @param entityRetrieveVO		EntityRetrieveVO
	 * @param params				request parameter
	 */
	private void addCommonQuery(EntityRetrieveVO entityRetrieveVO, Map<String, Object> params) {
		if(!ValidateUtil.isEmptyData(entityRetrieveVO.getType())) {
			params.put(DataServiceBrokerCode.DefaultAttributeKey.TYPE.getCode(), entityRetrieveVO.getType());
		}

		if(!ValidateUtil.isEmptyData(entityRetrieveVO.getId())) {
			params.put(DataServiceBrokerCode.DefaultAttributeKey.ID.getCode(), entityRetrieveVO.getId());
		}

		if(!ValidateUtil.isEmptyData(entityRetrieveVO.getLimit()) && !ValidateUtil.isEmptyData(entityRetrieveVO.getOffset())) {
			params.put(FILTER_CONDITION_LIMIT, entityRetrieveVO.getLimit());
			params.put(FILTER_CONDITION_OFFSET, entityRetrieveVO.getOffset());
		}
		
		if(!ValidateUtil.isEmptyData(entityRetrieveVO.getAttrs())) {
			String attrs = null;
			for(String attr : entityRetrieveVO.getAttrs()) {
				if(attrs == null) {
					attrs = attr;
				} else {
					attrs += "," + attr;
				}
			}
			params.put(FILTER_CONDITION_ATTRS, attrs);
		}
		
		if(!ValidateUtil.isEmptyData(entityRetrieveVO.getOptions())) {
			params.put(FILTER_CONDITION_OPTIONS, entityRetrieveVO.getOptions() + ",sysAttrs");
		} else {
			params.put(FILTER_CONDITION_OPTIONS, "sysAttrs");
		}
	}
	
	/**
	 * History search period
	 * @param entityRetrieveVO	EntityRetrieveVO
	 * @param params			Request param
	 * @param isHistory			true if historical data, false otherwise.
	 */
	private void addTemporalQuery(EntityRetrieveVO entityRetrieveVO, Map<String, Object> params, boolean isHistory, HttpServletRequest request) {
		if(!ValidateUtil.isEmptyData(entityRetrieveVO.getTimerel()) && !ValidateUtil.isEmptyData(entityRetrieveVO.getTime())) {
			params.put(FILTER_CONDITION_TIMEREL, entityRetrieveVO.getTimerel());
			try {
				if(!ValidateUtil.isEmptyData(entityRetrieveVO.getTime())) {
					params.put(FILTER_CONDITION_TIME, convertTime(entityRetrieveVO.getTime()));
				}
				
				if(!ValidateUtil.isEmptyData(entityRetrieveVO.getEndtime())) {
					params.put(FILTER_CONDITION_ENDTIME, convertTime(entityRetrieveVO.getEndtime()));
				}
			} catch(Exception e) {
				log.warn("Fail to convert UTC to GMT.", e);
			}
			if(!ValidateUtil.isEmptyData(entityRetrieveVO.getTimeproperty())) {
				params.put(FILTER_CONDITION_TIMEPROPERTY, entityRetrieveVO.getTimeproperty());
			} else {
				params.put(FILTER_CONDITION_TIMEPROPERTY, TIMEPROPERTY_MODIFIEDAT);
			}
		} else {
			if(isHistory) {
				Date date = new Date();

				if(ValidateUtil.isEmptyData(entityRetrieveVO.getId())) {
					// 1. Retrieve latest values
					ResponseEntity<CommonEntityListResponseVO> responseEntity = getEntities(false, entityRetrieveVO, request, null);

					if(responseEntity != null && !ValidateUtil.isEmptyData(responseEntity.getBody())) {
						try {
							String modifiedAt = null;
							for(CommonEntityVO commonEntityVO : responseEntity.getBody().getCommonEntityVOs()) {
								if(modifiedAt == null) {
									modifiedAt = commonEntityVO.getModifiedAt();
								} else {
									if(modifiedAt.compareTo(commonEntityVO.getModifiedAt()) > 0) {
										modifiedAt = commonEntityVO.getModifiedAt();
									}
								}
							}

							if(modifiedAt != null) {
								date = DateUtil.strToDate(modifiedAt);
							}
						} catch (ParseException e) {
							log.warn("Fail to get the last modifiedAt.", e);
						}
					}

				} else {
					// 1. Retrieve latest value
					ResponseEntity<CommonEntityVO> responseEntity = getEntityById(entityRetrieveVO.getId(), entityRetrieveVO, request, null);

					if(responseEntity != null && !ValidateUtil.isEmptyData(responseEntity.getBody())) {
						try {
							CommonEntityVO commonEntityVO = responseEntity.getBody();
							String modifiedAt = commonEntityVO.getModifiedAt().toString();
							date = DateUtil.strToDate(modifiedAt);
						} catch (ParseException e) {
							log.warn("Fail to get the last modifiedAt.", e);
						}
					}
				}

				// 2. Last value -X days after set to be searchable
				String defaultDate = DateUtil.calcDate(date, -properties.getEntityHistoryDays(), 0, 0, 0);
				params.put(FILTER_CONDITION_TIMEREL, "after");
				params.put(FILTER_CONDITION_TIME,  defaultDate);
				params.put(FILTER_CONDITION_TIMEPROPERTY, TIMEPROPERTY_MODIFIEDAT);
			}
		}
	}
	
	/**
	 * Add search value to request paramter
	 * @param entityRetrieveVO		EntityRetrieveVO
	 * @param params				Request parameter
	 */
	private void addSearchValue(EntityRetrieveVO entityRetrieveVO, Map<String, Object> params) {
		DataModelVO dataModelVO = dataModelSVC.getDataModelbyId(entityRetrieveVO.getDataModelId()).getBody();
		Map<String, String> attrsType = new LinkedHashMap<String, String>();
		String q = "";
		
		if (!ValidateUtil.isEmptyData(dataModelVO)) {
			List<Attribute> attributes = dataModelVO.getAttributes();
			
			getAttrsType(null, attributes, attrsType);

			for(String key : attrsType.keySet()) {
				// Only when Attribute value type is String or ArrayString Search like before and after as a search term 
				if(AttributeValueType.STRING.name().equals(attrsType.get(key)) 
						|| AttributeValueType.ARRAY_STRING.name().equals(attrsType.get(key))) {
					if (!ValidateUtil.isEmptyData(q)) q += "|";
					q += key + "~=\"" + entityRetrieveVO.getSearchValue() + "\"";
				}
			}

			if(!ValidateUtil.isEmptyData(q)) {
				params.put(FILTER_CONDITION_Q, q);
			}
		}
	}
	
	/**
	 * Set Query
	 * @param entityRetrieveVO		EntityRetrieveVO
	 * @param params				Request parameter
	 */
	private void addQuery(EntityRetrieveVO entityRetrieveVO, Map<String, Object> params) {
		if(!ValidateUtil.isEmptyData(entityRetrieveVO.getQ())) {
			String q = null;
			
			DataModelVO dataModelVO = dataModelSVC.getDataModelbyId(entityRetrieveVO.getDataModelId()).getBody();
			Map<String, String> attrsType = new LinkedHashMap<String, String>();
			
			if (!ValidateUtil.isEmptyData(dataModelVO)) {
				List<Attribute> attributes = dataModelVO.getAttributes();
				
				getAttrsType(null, attributes, attrsType);
				
				for(QVO qVO : entityRetrieveVO.getQ()) {
					if(q == null) {
						q = qVO.getAttr();
					} else {
						if(ValidateUtil.isEmptyData(qVO.getCondition())) {
							q += ";";
						}
						else if("or".equals(qVO.getCondition().toLowerCase())) {
							q += "|";
						} else {
							q += ";";
						}
						q += qVO.getAttr();
					}
					
					if(!ValidateUtil.isEmptyData(qVO.getOperator())) {
						q += QueryOperatorType.valueOf(qVO.getOperator()).getCode();
					}
					
					String type = attrsType.get(qVO.getAttr());
					
					if(AttributeValueType.STRING.name().equals(type) || AttributeValueType.ARRAY_STRING.name().equals(type)) {
						q += attachDoubleQuotes(type, qVO.getValue());
					} else {
						q += qVO.getValue();
					}
				}
				
				if(q != null) {
					params.put("q", q);
				}
			}
		}
	}
	
	/**
	 * Set GeoQuery
	 * @param entityRetrieveVO	EntityRetrieveVO
	 * @param params			Request parameter
	 */
	private void addGeoQuery(EntityRetrieveVO entityRetrieveVO, Map<String, Object> params) {
		if(!ValidateUtil.isEmptyData(entityRetrieveVO.getCoordinates())) {
			params.put(FILTER_CONDITION_GEOREL, DEFAULT_GEOREL);
			params.put(FILTER_CONDITION_GEOMETRY, DEFAULT_GEOMETRY);
			params.put(FILTER_CONDITION_COORDINATES, entityRetrieveVO.getCoordinates());
		}
	}
	
	/**
	 * Attach double quotes
	 * @param type		attribute type
	 * @param value		Query value
	 * @return			Query value with double quotes
	 */
	private String attachDoubleQuotes(String type, String value) {
		String result = "";
		
		if(AttributeValueType.STRING.name().equals(type)) {
			result = "\"" + value + "\"";
		}
		else if(AttributeValueType.ARRAY_STRING.name().equals(type)) {
			String[] values = value.split(",");
			
			for(String temp : values) {
				if(result.length() > 0) {	
					result += ",";
				}
				result += "\"" + temp + "\"";
			}
		}
		
		return result;
	}

	/**
	 * Get attribute type
	 * @param parentAttrName	parent attribute name
	 * @param attributes		List of attribute
	 * @param attrsType			Attribute types
	 */
	private void getAttrsType(String parentAttrName, List<Attribute> attributes, Map<String, String> attrsType) {
		
		for(Attribute attribute : attributes) {
			if(!ValidateUtil.isEmptyData(attribute.getChildAttributes())) {
				if(parentAttrName != null) {
					getAttrsType(parentAttrName + ATTRIBUTE_SEPARATOR + attribute.getName(), attribute.getChildAttributes(), attrsType);
				} else {
					getAttrsType(attribute.getName(), attribute.getChildAttributes(), attrsType);
				}
			}
			
			if(!ValidateUtil.isEmptyData(attribute.getObjectMembers())) {
				if(parentAttrName != null) {
					getObjectMemberType(parentAttrName + ATTRIBUTE_SEPARATOR + attribute.getName(), attribute.getObjectMembers(), attrsType);
				} else {
					getObjectMemberType(attribute.getName(), attribute.getObjectMembers(), attrsType);
				}
			}
			
			if(attribute.getName() == null || attribute.getValueType() == null) {
				continue;
			}
			
			if(parentAttrName != null) {
				attrsType.put(parentAttrName + ATTRIBUTE_SEPARATOR + attribute.getName(), attribute.getValueType().name());
			} else {
				attrsType.put(attribute.getName(), attribute.getValueType().name());
			}
		}
	}
	
	/**
	 * Get object member type
	 * @param parentAttrName	Parent attribute name
	 * @param objectMembers		List of object member
	 * @param attrsType			Attribute types
	 */
	private void getObjectMemberType(String parentAttrName, List<ObjectMember> objectMembers, Map<String, String> attrsType) {
		
		for(ObjectMember objectMember : objectMembers) {			
			if(!ValidateUtil.isEmptyData(objectMember.getObjectMembers())) {
				if(parentAttrName.contains(OBJECTMEMBER_SUFFIX)) {
					parentAttrName = parentAttrName.replaceAll(OBJECTMEMBER_SUFFIX, "") + ATTRIBUTE_SEPARATOR + objectMember.getName() + OBJECTMEMBER_SUFFIX;
				} else {
					parentAttrName = parentAttrName + OBJECTMEMBER_PREFIX + objectMember.getName() + OBJECTMEMBER_SUFFIX;
				}
				 
				getObjectMemberType(parentAttrName, objectMember.getObjectMembers(), attrsType);
			}
			
			if(objectMember.getName() == null || objectMember.getValueType() == null) {
				continue;
			}
			
			if(parentAttrName.contains(OBJECTMEMBER_SUFFIX)) {
				attrsType.put(parentAttrName.replaceAll(OBJECTMEMBER_SUFFIX, "") + ATTRIBUTE_SEPARATOR + objectMember.getName() + OBJECTMEMBER_SUFFIX, objectMember.getValueType().name());
			} else {
				attrsType.put(parentAttrName + OBJECTMEMBER_PREFIX + objectMember.getName() + OBJECTMEMBER_SUFFIX, objectMember.getValueType().name());
			}
		}
	}
	
	/**
	 * Set Display value in entity VO
	 * @param commonEntityVOs		List of CommonEntityVO
	 * @param displayAttribute		Attribute full name to be displayed
	 * @return						List of entity with display value
	 */
	private List<CommonEntityVO> setDisplayValue(List<CommonEntityVO> commonEntityVOs, String displayAttribute) {
		List<CommonEntityVO> result = new ArrayList<CommonEntityVO>();
		List<String> attribute = new ArrayList<String>();
		
		while(true) {
			int nextAttributeOffset = displayAttribute.indexOf(ATTRIBUTE_SEPARATOR);
			int nextObjectMemberOffset = displayAttribute.indexOf(OBJECTMEMBER_PREFIX);
			
			if(nextAttributeOffset > 0 && nextObjectMemberOffset > 0) {
				int nextOffset = nextAttributeOffset < nextObjectMemberOffset ? nextAttributeOffset : nextObjectMemberOffset;
				attribute.add(displayAttribute.substring(0, nextOffset));
				displayAttribute = displayAttribute.substring(nextOffset + 1, displayAttribute.length());
				continue;
			}
			
			if(nextAttributeOffset > 0) {
				attribute.add(displayAttribute.substring(0, nextAttributeOffset));
				displayAttribute = displayAttribute.substring(nextAttributeOffset + 1, displayAttribute.length());
				continue;
			}
			
			if(nextObjectMemberOffset > 0) {
				attribute.add(displayAttribute.substring(0, nextObjectMemberOffset));
				displayAttribute = displayAttribute.substring(nextObjectMemberOffset + 1, displayAttribute.length());
				continue;
			}
			
			if(displayAttribute.endsWith(OBJECTMEMBER_SUFFIX)) {
				attribute.add(displayAttribute.substring(0, displayAttribute.length() - 1));
			} else {
				attribute.add(displayAttribute);
			}
			
			break;
		}
		
		if(ValidateUtil.isEmptyData(attribute)) {
			return commonEntityVOs;
		}
		
		for(CommonEntityVO commonEntityVO : commonEntityVOs) {
			int index = 0;
			String attrId = null;
			String displayVlaue = null;
			
			attrId = attribute.get(index++);
			HashMap<String, Object> attr = (HashMap<String, Object>) commonEntityVO.get(attrId);
			if(attr == null) {
				commonEntityVO.put(ATTR_DISPLAY_VALUE, null);
				result.add(commonEntityVO);
				continue;
			} else if(index == attribute.size()) {
				commonEntityVO.put(ATTR_DISPLAY_VALUE, attrId + ":" + attr.get(ATTR_VALUE));
				result.add(commonEntityVO);
				continue;
			}
			while(index < attribute.size()) {
				attrId = attribute.get(index++);
				HashMap<String, Object> temp = (HashMap<String, Object>) attr.get(attrId);
				if(temp == null) {
					String ObjectMemberId = attribute.get(index-1);
					temp = (HashMap<String, Object>) attr.get(ATTR_VALUE);
					if(index == attribute.size()) {
						displayVlaue = ObjectMemberId + ":" + getDisplayValue(temp.get(ObjectMemberId));
					}
				} else {
					if(index == attribute.size()) {
						displayVlaue = attrId + ":" + getDisplayValue(temp.get(ATTR_VALUE));
					}
				}
				attr = temp;
			}
			commonEntityVO.put(ATTR_DISPLAY_VALUE, displayVlaue);
			result.add(commonEntityVO);
		}
		
		return result;
	}
	
	/**
	 * Get display value
	 * @param value		Attribute value to be displayed
	 * @return			attribute value
	 */
	private String getDisplayValue(Object value) {
		String displayValue = null;
		
		if(value instanceof Double) {
			displayValue = String.valueOf(value);
		}
		else if(value instanceof Integer) {
			displayValue = String.valueOf(value);
		}
		else if(value instanceof Boolean) {
			displayValue = String.valueOf(value);
		}
		else {
			displayValue = (String) value;
		}
		
		return displayValue;
	}
	
	/**
	 * Save http servlet request
	 */
	private void saveHttpServletRequest(HttpServletRequest request) {
		ResponseEntity<String> userId = userToolSecuritySVC.getUserId(request);
		if(userId != null) {
			userRequest.put(userId.getBody(), request);
		}
	}
	
	/**
	 * Convert link format
	 * @param contexts	List of context
	 * @return	Link format context
	 */
	private String convertLinkFormat(List<String> contexts) {
		String link = "";
		for(String context : contexts) {
			
			if(!ValidateUtil.isEmptyData(link)) {
				link += ", ";
			}
			
			link += LINK_PREFIX + context + LINK_SUFFIX + " rel=" + LINK_REL + "; type=" + LINK_TYPE;
		}
		return link;
	}

	/**
	 * Get list of entity ID
	 * @param entityRetrieveVO		EntityRetrieveVO
	 * @return						List of entity ID
	 */
	public ResponseEntity<List<String>> getEntityIds(EntityRetrieveVO entityRetrieveVO, HttpServletRequest request) {
		
		DataModelVO dataModel = getDataModel(entityRetrieveVO);
		if(dataModel == null) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}
		entityRetrieveVO.setDataModelId(dataModel.getId());
		entityRetrieveVO.setTypeUri(dataModel.getTypeUri());
		entityRetrieveVO.setType(dataModel.getType());
		
		Map<String, Object> params = creatParams(entityRetrieveVO, false, request);
		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(Constants.HTTP_HEADER_KEY_ACCEPT, Constants.ACCEPT_TYPE_APPLICATION_JSON);
		headers.put(Constants.HTTP_HEADER_LINK, convertLinkFormat(dataModel.getContext()));
		
		if (properties.getSpringSecurityEnabled()) {
			headers.put(Constants.HTTP_HEADER_AUTHORIZATION, BEARER + request.getSession().getAttribute(AUTHTOKEN));
		}
		
		// Save request by user (request information per user is not checked when connecting to widget websocket)
		saveHttpServletRequest(request);
		
		// 1. Retrieve count
		ResponseEntity<AttributeCountVO> count = dataCoreRestSVC.getList(dataservicebrokerUrl, ENTITY_COUNT_PATH_URL, headers, null, params, AttributeCountVO.class);
		
		if(count != null &&HttpStatus.SERVICE_UNAVAILABLE.equals(count.getStatusCode())) {
			return ResponseEntity.status(count.getStatusCode()).build();
		}
		
		if(count == null || count.getBody() == null || count.getBody().getTotalCount() < 1) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}
		
		// 2. Retrieve latest entity value
		ResponseEntity<List<CommonEntityVO>> response = dataCoreRestSVC.getList(dataservicebrokerUrl, DEFAULT_PATH_URL, headers, null, params, new ParameterizedTypeReference<List<CommonEntityVO>>() {});
		List<String> entityIds = new ArrayList<String>();
		if(response != null) {
			List<CommonEntityVO> commonEntityVOs = response.getBody();
			
			for(CommonEntityVO commonEntityVO : commonEntityVOs) {
				entityIds.add(commonEntityVO.getId());
			}
		}
		
		return ResponseEntity.status(response.getStatusCode()).body(entityIds);
	}
	
	/**
	 * Get data model by ID
	 * @param entityRetrieveVO	EntityRetrieveVO
	 * @return				Data model
	 */
	private DataModelVO getDataModel(EntityRetrieveVO entityRetrieveVO) {
		ResponseEntity<DataModelVO> dataModel = null;
		if(entityRetrieveVO.getDataModelId() != null) {
			dataModel = dataModelSVC.getDataModelbyId(entityRetrieveVO.getDataModelId());
		} 
		else if(entityRetrieveVO.getTypeUri() != null) {
			dataModel = dataModelSVC.getDataModelbyTypeUri(entityRetrieveVO.getTypeUri());
		}
		
		if(dataModel == null || dataModel.getBody() == null) {
			return null;
		}
		
		return dataModel.getBody();
	}
	
	/**
	 * Convert time to CONTENT_DATE_FORMAT("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
	 * @param date				Date
	 * @return					The date in  CONTENT_DATE_FORMAT("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
	 * @throws ParseException	Exception is occurence when parsing fails.
	 */
	private String convertTime(String date) throws ParseException {
		String convertedTime = null;
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT);
		
		Date parseDate = sdf.parse(date);
		long milliseconds = parseDate.getTime();
		
		convertedTime = sdf.format(milliseconds);
		
		return convertedTime;
	}
}
