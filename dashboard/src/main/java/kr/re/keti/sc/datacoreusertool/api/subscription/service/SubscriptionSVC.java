package kr.re.keti.sc.datacoreusertool.api.subscription.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import kr.re.keti.sc.datacoreusertool.api.datamodel.vo.Attribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import kr.re.keti.sc.datacoreusertool.api.datamodel.service.DataModelSVC;
import kr.re.keti.sc.datacoreusertool.api.datamodel.vo.DataModelVO;
import kr.re.keti.sc.datacoreusertool.api.subscription.vo.SubscriptionResponseVO;
import kr.re.keti.sc.datacoreusertool.api.subscription.vo.SubscriptionUIVO;
import kr.re.keti.sc.datacoreusertool.api.subscription.vo.SubscriptionVO;
import kr.re.keti.sc.datacoreusertool.api.subscription.vo.SubscriptionVO.EntityInfo;
import kr.re.keti.sc.datacoreusertool.api.subscription.vo.SubscriptionVO.NotificationParams;
import kr.re.keti.sc.datacoreusertool.api.subscription.vo.SubscriptionVO.NotificationParams.Endpoint;
import kr.re.keti.sc.datacoreusertool.common.code.Constants;
import kr.re.keti.sc.datacoreusertool.common.service.DataCoreRestSVC;
import kr.re.keti.sc.datacoreusertool.security.service.UserToolSecuritySVC;
import kr.re.keti.sc.datacoreusertool.util.ValidateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for management of subscription.
 * @FileName SubscriptionSVC.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SubscriptionSVC {
	
	@Value("${datacoreusertool.url}")
	private String datacoreusertoolUrl;
	
	@Value("${datacoreusertool.widget.url}")
	private String datacoreusertoolWidgetUrl;
	
	@Value("${dataservicebroker.url}")
	private String dataservicebrokerUrl;
	
	private final static String SUBSCRIPTION_URL = "subscriptions";
	private final static String DEFAULT_SUBSCRIPTION_TYPE = "Subscription";
	private final static String DEFAULT_NOTIFICATION_FORMAT = "normalized";
	private final static String LINK_PREFIX = "<";
	private final static String LINK_SUFFIX = ">";
	private final static String LINK_REL = "\"http://www.w3.org/ns/json-ld#context\"";
	private final static String LINK_TYPE = "\"application/ld+json\"";
	
	private final DataCoreRestSVC dataCoreRestSVC;
	
	@Autowired
	private UserToolSecuritySVC userToolSecuritySVC;
	
	@Autowired
	private DataModelSVC dataModelSVC;
	
	/**
	 * Create subscription
	 * @param subscriptionUIVOs		List of SubscriptionUIVO
	 * @return						Result of create subscription.
	 */
	public ResponseEntity<SubscriptionResponseVO> createSubscription(List<SubscriptionUIVO> subscriptionUIVOs, HttpServletRequest request) {
		String pathUri = SUBSCRIPTION_URL;
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(Constants.HTTP_HEADER_KEY_ACCEPT, Constants.ACCEPT_TYPE_APPLICATION_JSON);
		
		// Login ID
		String userId = null;
		ResponseEntity<String> requestUserId = userToolSecuritySVC.getUserId(request);
		if(requestUserId != null) {
			userId = requestUserId.getBody();
		}
		
		SubscriptionVO subscriptionVO = createSubscriptionBody(subscriptionUIVOs, userId);
		
		// 1. Get Subscription
		ResponseEntity<List<SubscriptionVO>> subscriptionVOs = getSubscriptions();
		if(subscriptionVOs != null && subscriptionVOs.getBody() != null) {
			for(SubscriptionVO subscription : subscriptionVOs.getBody()) {
				// If subscriptionId exists, it is deleted.
				if(subscription.getId().equals(subscriptionVO.getId())) {
					deleteSubscription(subscriptionVO.getId());
					break;
				}
			}
		}
		
		headers.put(Constants.HTTP_HEADER_LINK, convertLinkFormat(subscriptionVO.getContext()));
		
		// 2. Create Subscription
		ResponseEntity<SubscriptionResponseVO> response = dataCoreRestSVC.post(dataservicebrokerUrl, pathUri, headers, subscriptionVO, null, SubscriptionResponseVO.class);
		
		return response;
	}
	
	/**
	 * Delete subscription
	 * @param subscriptionId	Subscription id
	 * @return					Result of delete subscription.
	 */
	public ResponseEntity<Void> deleteSubscription(String subscriptionId) {
		String pathUri = SUBSCRIPTION_URL + "/" + subscriptionId;
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(Constants.HTTP_HEADER_KEY_ACCEPT, Constants.ACCEPT_TYPE_APPLICATION_JSON);
		
		ResponseEntity<Void> response = dataCoreRestSVC.delete(dataservicebrokerUrl, pathUri, headers, null, null, Void.class);
		
		return response;
	}
	
	/**
	 * Retrieve multiple subscription
	 * @return		All subscription information
	 */
	public ResponseEntity<List<SubscriptionVO>> getSubscriptions() {
		String pathUri = SUBSCRIPTION_URL;
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(Constants.HTTP_HEADER_KEY_ACCEPT, Constants.ACCEPT_TYPE_APPLICATION_JSON);
		
		ResponseEntity<List<SubscriptionVO>> response = dataCoreRestSVC.getList(dataservicebrokerUrl, pathUri, headers, null, null, new ParameterizedTypeReference<List<SubscriptionVO>>() {});
		
		return response;
	}

	/**
	 * Create request body of subscription
	 * @param subscriptionUIVOs		List of subscription UI VO
	 * @param userId				User ID
	 * @return						Request body for subscription
	 */
	private SubscriptionVO createSubscriptionBody(List<SubscriptionUIVO> subscriptionUIVOs, String userId) {
		SubscriptionVO subscriptionVO = new SubscriptionVO();
		NotificationParams notification = new NotificationParams();
		Endpoint endpoint = new Endpoint();
		List<EntityInfo> entities = new ArrayList<EntityInfo>();
		List<String> context = new ArrayList<String>();
		List<String> attrs = null;
		String widgetId = null;
		String dashboardId = null;
		List<String> notificationAttributeUris = new ArrayList<>();
		
		subscriptionVO.setType(DEFAULT_SUBSCRIPTION_TYPE);
		
		for(SubscriptionUIVO subscriptionUIVO : subscriptionUIVOs) {
			EntityInfo entityInfo = new EntityInfo();
			
			ResponseEntity<DataModelVO> dataModelVO = dataModelSVC.getDataModelbyTypeUri(subscriptionUIVO.getTypeUri());
			if(dataModelVO.getBody() != null) {
				entityInfo.setType(dataModelVO.getBody().getTypeUri());
				context.addAll(dataModelVO.getBody().getContext());

				expandAttributeUri(subscriptionUIVO.getAttrs(), dataModelVO.getBody().getAttributes(), notificationAttributeUris);

			} else {
				entityInfo.setType(subscriptionUIVO.getTypeUri());
			}
			entityInfo.setId(subscriptionUIVO.getId());
			
			entities.add(entityInfo);
			dashboardId = subscriptionUIVO.getDashboardId();
			widgetId = subscriptionUIVO.getWidgetId();
			attrs = subscriptionUIVO.getAttrs();

		}
		subscriptionVO.setEntities(entities);


		endpoint.setAccept(Constants.ACCEPT_TYPE_APPLICATION_JSON);
		if(!ValidateUtil.isEmptyData(dashboardId) && !ValidateUtil.isEmptyData(widgetId)) {
			endpoint.setUri(datacoreusertoolWidgetUrl);
			subscriptionVO.setId("urn" + ":" + userId + ":" + dashboardId + ":" + widgetId);
			if(!ValidateUtil.isEmptyData(notificationAttributeUris) && !ValidateUtil.isEmptyData(attrs)) {
				notification.setAttributes(notificationAttributeUris);
			}
		} else {
			endpoint.setUri(datacoreusertoolUrl);
		}
		notification.setFormat(DEFAULT_NOTIFICATION_FORMAT);
		notification.setEndpoint(endpoint);
		subscriptionVO.setNotification(notification);
		subscriptionVO.setContext(context);
		
		// Default Expiration Time (1 day)
		subscriptionVO.setExpires(afterOneDay());
		
		return subscriptionVO;
	}
	
	/**
	 * Get the date of after one day.
	 * @return	Date of after one day.
	 */
	private Date afterOneDay() {
		Calendar cal = Calendar.getInstance();
        Date date = new Date();
        cal.setTime(date);
        cal.add(Calendar.DATE, 1);
        
        return cal.getTime();
	}

	private void expandAttributeUri(List<String> shortNames, List<Attribute> attributes, List<String> uris) {
		for (Attribute attribute : attributes) {
			for (String shortName : shortNames) {
				if (attribute.getName().equals(shortName)) {
					uris.add(attribute.getAttributeUri());
				}
			}

			if (!ValidateUtil.isEmptyData(attribute.getChildAttributes())) {
				expandAttributeUri (shortNames, attribute.getChildAttributes(), uris);
			}
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
}