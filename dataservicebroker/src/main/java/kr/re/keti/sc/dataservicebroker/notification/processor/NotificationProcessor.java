package kr.re.keti.sc.dataservicebroker.notification.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.datamodel.DataModelManager;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.dataservicebroker.common.bulk.IBulkProcessor;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.JsonLdType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.RetrieveOptions;
import kr.re.keti.sc.dataservicebroker.common.exception.CoreException;
import kr.re.keti.sc.dataservicebroker.common.exception.NotificationException;
import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityFullVO;
import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityVO;
import kr.re.keti.sc.dataservicebroker.common.vo.QueryVO;
import kr.re.keti.sc.dataservicebroker.entities.controller.kafka.producer.KafkaProducerManager;
import kr.re.keti.sc.dataservicebroker.entities.controller.kafka.producer.vo.KafkaProcessVO;
import kr.re.keti.sc.dataservicebroker.entities.controller.kafka.producer.vo.NotificationReqeustVO;
import kr.re.keti.sc.dataservicebroker.entities.service.EntityRetrieveSVC;
import kr.re.keti.sc.dataservicebroker.entities.service.rdb.RdbEntitySVC;
import kr.re.keti.sc.dataservicebroker.entities.vo.EntityRetrieveVO;
import kr.re.keti.sc.dataservicebroker.notification.vo.NotificationProcessVO;
import kr.re.keti.sc.dataservicebroker.notification.vo.NotificationVO;
import kr.re.keti.sc.dataservicebroker.subscription.service.SubscriptionSVC;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionBaseDaoVO;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionEntitiesDaoVO;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionRetrieveVO;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionVO.GeoQuery;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionVO.NotificationParams.Endpoint;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionVO.NotificationParams.KeyValuePair;
import kr.re.keti.sc.dataservicebroker.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NotificationProcessor implements IBulkProcessor<NotificationProcessVO> {

	public enum NotificationStatus {
		OK("ok"),
		FAILED("failed");

		private String code;

		NotificationStatus(String code) {
			this.code = code;
		}

		public String getCode() {
			return code;
		}
	}

	@Autowired
	private EntityRetrieveSVC entityRetrieveSVC;
	@Autowired
	private RdbEntitySVC entitySVC;
	@Autowired
	private SubscriptionSVC subscriptionSVC;
	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private KafkaProducerManager kafkaProducerManager;
	@Autowired
	private DataModelManager dataModelManager;
	
	@Value("${kafka.notification.topic:NOTIFICATION_REQUEST}")
	private String notificationTopic;
	
//	@Autowired
//	private MqttSender mqttSender;
//	@Autowired
//	private HttpSender httpSender;

	@Override
	public Object processSingle(NotificationProcessVO notificationProcessVO) {

		// 1. subscription 정보 조회
		List<SubscriptionBaseDaoVO> subscriptionBaseDaoVOs = getSubscriptionVOs(notificationProcessVO);

		if(ValidateUtil.isEmptyData(subscriptionBaseDaoVOs)) {
			return null;
		}

		// 2. Notification 유효성체크, 전송, 결과갱신
		for(SubscriptionBaseDaoVO subscriptionBaseDaoVO : subscriptionBaseDaoVOs) {
			processNotification(notificationProcessVO, subscriptionBaseDaoVO);
		}

		return null;
	}

	/**
	 * subscription 정보 조회
	 * @param notificationProcessVO
	 * @return
	 */
	private List<SubscriptionBaseDaoVO> getSubscriptionVOs(NotificationProcessVO notificationProcessVO) {

		List<SubscriptionBaseDaoVO> subscriptionBaseDaoVOs = null;
				
		// TimeInterval event 기반 Notification 인 경우
		// NotificationTimeIntervalProcessor 에서 subscription 정보 이미 조회하여 전달받았으므로 꺼내서 사용
		if(notificationProcessVO.isTimeIntervalEvent()) {
			subscriptionBaseDaoVOs = notificationProcessVO.getSubscriptionBaseDaoVOs();

		// Entity Change event 기반 Notification 인 경우
		} else {
			// entity에 등록된  subsciription 정보 조회
			//   - entityId, entityType, active, expires 기반 조회
			//	 - timeInterval 이 없는 정보만 조회
			SubscriptionRetrieveVO subscriptionRetrieveVO = new SubscriptionRetrieveVO();
			subscriptionRetrieveVO.setEntityId(notificationProcessVO.getEntityId());
			subscriptionRetrieveVO.setEntityType(notificationProcessVO.getEntityType());
			subscriptionRetrieveVO.setType(JsonLdType.SUBSCRIPTION.getCode());
			subscriptionRetrieveVO.setEntityTypeUri(notificationProcessVO.getEntityTypeUri());
			subscriptionRetrieveVO.setDatasetId(notificationProcessVO.getDatasetId());
			subscriptionRetrieveVO.setIsActive(true);
			subscriptionBaseDaoVOs = subscriptionSVC.retrieveSubscriptionByEntityId(subscriptionRetrieveVO);
		}

		return subscriptionBaseDaoVOs;
	}

	/**
	 * Notification 유효성체크, 전송, 결과갱신
	 * @param notificationProcessVO
	 * @param subscriptionBaseDaoVO
	 */
	private void processNotification(NotificationProcessVO notificationProcessVO, SubscriptionBaseDaoVO subscriptionBaseDaoVO) {
		boolean notificationResult = false;
		Date notifyDate = null;

		try {

			List<CommonEntityVO> entityVOs = null;

			// 1. change event 기반 Notification인 경우
			if(!notificationProcessVO.isTimeIntervalEvent()) {
				// 1-1. watchAttributes 체크
				if(!checkWatchAttributes(notificationProcessVO, subscriptionBaseDaoVO)) {
					return;
				}

				// 1-2. 쓰로틀링 시간 이내 발송 여부 체크
				if(!checkThrottling(subscriptionBaseDaoVO.getThrottling(), subscriptionBaseDaoVO.getNotificationLastSuccess())) {
					log.info("No send notification because throttling. subscriptionId={}, throttling={} second, lastSuccessDate=", 
							subscriptionBaseDaoVO.getId(), subscriptionBaseDaoVO.getThrottling(), subscriptionBaseDaoVO.getNotificationLastSuccess());
					return;
				}

				// 1-3. entityId 기반 Notification data 조회
				entityVOs = getChangeEventNotiData( notificationProcessVO.getEntityId(),
												    notificationProcessVO.getEntityType(),
												    subscriptionBaseDaoVO.getNotificationAttributes(),
												    subscriptionBaseDaoVO.getQ(),
												    subscriptionBaseDaoVO.getGeoQ(),
												    subscriptionBaseDaoVO.getNotificationFormat(),
												    subscriptionBaseDaoVO.getNotificationEndpointAccept() );

			// 2. TimeInterval 기반 Notification인 경우
			} else {

				// 2-1. subscription_entites 테이블 정보 기반 조회
				entityVOs = getTimeIntervalNotiData( subscriptionBaseDaoVO.getSubscriptionEntitiesDaoVOs(),
												     subscriptionBaseDaoVO.getQ(),
												     subscriptionBaseDaoVO.getGeoQ(),
												     subscriptionBaseDaoVO.getNotificationFormat(),
												     subscriptionBaseDaoVO.getNotificationEndpointAccept(),
												     subscriptionBaseDaoVO.getContext());
			}

			if(ValidateUtil.isEmptyData(entityVOs)) {
				return;
			}

			// 3. Notification 전송
			notifyDate = new Date();
			notificationResult = sendNotification(notifyDate,
										   notificationProcessVO.getEntityId(), 
										   subscriptionBaseDaoVO,
										   entityVOs);

		} catch (Exception e) {
			log.warn("ProcessNotification error. subscriptionId={}, entityId={}",
					subscriptionBaseDaoVO.getId(), notificationProcessVO.getEntityId(), e);
		}

		// 4. 전송 결과 갱신
		updateNotificationResult(subscriptionBaseDaoVO.getId(), notificationProcessVO.getEntityId(), notifyDate, notificationResult);
	}

	/**
	 * chagne event 대상 attribute와 watchAttribute 일치 여부 체크
	 * @param notificationProcessVO
	 * @param subscriptionBaseDaoVO
	 * @return
	 */
	private boolean checkWatchAttributes(NotificationProcessVO notificationProcessVO, SubscriptionBaseDaoVO subscriptionBaseDaoVO) {
		if(subscriptionBaseDaoVO.getWatchedAttributes() != null) {

			// request entity expandTerm
			List<String> entityAttributeFullUris = extractRequestEntityAttributeFullUris(notificationProcessVO);

			// watchAttribute expandTerm
			List<String> watchAttributeFullUris = dataModelManager.convertAttrNameToFullUri(
					subscriptionBaseDaoVO.getContext(),
					subscriptionBaseDaoVO.getWatchedAttributes()
			);

			// check full uri
			for(String watchAttributeFullUri : watchAttributeFullUris) {
				if(entityAttributeFullUris.contains(watchAttributeFullUri)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	private List<String> extractRequestEntityAttributeFullUris(NotificationProcessVO notificationProcessVO) {

		List<String> entityModelContext = dataModelManager.getDataModelVOCacheByContext(
				null,
				notificationProcessVO.getEntityTypeUri()
		).getDataModelVO().getContext();

		List<String> entityAttributes = notificationProcessVO.getRequestEntityFullVO().keySet().stream()
				.filter(key -> DataServiceBrokerCode.DefaultAttributeKey.parseType(key) == null)
				.collect(Collectors.toList());

		return dataModelManager.convertAttrNameToFullUri(entityModelContext, entityAttributes);
	}

	/**
	 * Throttling 으로 인한 발송제한 여부 체크
	 * @param throttling 쓰로틀링 시간 (단위:초)
	 * @param lastSuccessDate 마지막 전송 성공 시간
	 * @return
	 */
	private boolean checkThrottling(Integer throttling, Date lastSuccessDate) {

		if(throttling != null && throttling > 0 && lastSuccessDate != null) {

			Calendar calculateThrottlingTime = Calendar.getInstance();
	        calculateThrottlingTime.setTime(lastSuccessDate);
	        calculateThrottlingTime.add(Calendar.SECOND, throttling);

	        Date nowDate = new Date();
			if(nowDate.before(calculateThrottlingTime.getTime())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * entityId 기반 entity 정보 조회
	 * @param entityId
	 * @param entityType
	 * @param notificationAttributes
	 * @param q
	 * @param geoQ
	 * @param notificationFormat
	 * @param notificationEndpointAccept
	 * @return
	 */
	private List<CommonEntityVO> getChangeEventNotiData( String entityId, String entityType,
			List<String> notificationAttributes, String q, String geoQ, String notificationFormat, String notificationEndpointAccept) {

		if(ValidateUtil.isEmptyData(entityId)) {
			return null;
		}

		QueryVO queryVO = new QueryVO();
		queryVO.setId(entityId);
		queryVO.setType(entityType);
		queryVO.setAttrs(notificationAttributes);
		queryVO.setQ(q);
		setGeoQuery(queryVO, geoQ);
		setOptions(queryVO, notificationFormat);

		CommonEntityVO entityVO = entitySVC.selectById(queryVO, notificationEndpointAccept, true);
		if(entityVO != null) {
			return Arrays.asList(entityVO);
		}

		return null;
	}

	/**
	 * subscriptionEntities 정보 기반 Entity 정보 조회
	 * @param subscriptionEntitiesDaoVOs subscriptionEntities 정보
	 * @param q
	 * @param geoQ
	 * @param notificationFormat
	 * @param notificationEndpointAccept
	 * @return
	 */
	private List<CommonEntityVO> getTimeIntervalNotiData(List<SubscriptionEntitiesDaoVO> subscriptionEntitiesDaoVOs,
			String q, String geoQ, String notificationFormat, String notificationEndpointAccept, List<String> context) {

		if(subscriptionEntitiesDaoVOs == null) {
			return null;
		}

		// 중복 발송 제거를 위해 HashMap에 entity 목록을 모두 넣은 후 list로 변환하여 반환
		Map<String, CommonEntityVO> entityDataMap = new HashMap<>();
		QueryVO queryVO = new QueryVO();
		queryVO.setQ(q);
		queryVO.setLinks(context);
		setGeoQuery(queryVO, geoQ);
		setOptions(queryVO, notificationFormat);
		
		String link = contextToLink(context);

		if(subscriptionEntitiesDaoVOs != null && subscriptionEntitiesDaoVOs.size() > 0) {
			for(SubscriptionEntitiesDaoVO subscriptionEntitiesDaoVO : subscriptionEntitiesDaoVOs) {
				String entityId = subscriptionEntitiesDaoVO.getId();
				String entityIdPattern = subscriptionEntitiesDaoVO.getIdPattern();
				String entityType = subscriptionEntitiesDaoVO.getType();

				queryVO.setType(entityType);
				
				String queryString = buildQueryString(entityType, q, notificationFormat);

				// subscription.entityInfo.entityId가 null 이 아닌 경우 entityId+type으로 검색
				if(!ValidateUtil.isEmptyData(entityId)) {
					queryVO.setId(entityId);

					CommonEntityVO entityVO = entityRetrieveSVC.getEntityById(queryVO, queryString, notificationEndpointAccept, link);
					if(entityVO != null) {
						entityDataMap.put(entityVO.getId(), entityVO);
					}

				// subscription.entityInfo.entityIdPattern이 null 이 아닌 경우 entityIdPattern+type으로 검색
				} else if(!ValidateUtil.isEmptyData(entityIdPattern)) {
					queryVO.setIdPattern(entityIdPattern);

					EntityRetrieveVO entityRetrieveVO = entityRetrieveSVC.getEntity(queryVO, queryString, notificationEndpointAccept, link);
					if(entityRetrieveVO != null && entityRetrieveVO.getEntities() != null) {
						List<CommonEntityVO> entityVOs = entityRetrieveVO.getEntities();
						for(CommonEntityVO entityVO : entityVOs) {
							entityDataMap.put(entityVO.getId(), entityVO);
						}
					}

				// subscription.entityInfo.entityId와 pattern이 모두 null 인 경우
				} else if(!ValidateUtil.isEmptyData(entityType)) {

					EntityRetrieveVO entityRetrieveVO = entityRetrieveSVC.getEntity(queryVO, queryString, notificationEndpointAccept, link);
					if(entityRetrieveVO != null && entityRetrieveVO.getEntities() != null) {
						List<CommonEntityVO> entityVOs = entityRetrieveVO.getEntities();
						for(CommonEntityVO entityVO : entityVOs) {
							entityDataMap.put(entityVO.getId(), entityVO);
						}
					}
				}
			}

		} else {
			
			String queryString = buildQueryString(null, q, notificationFormat);
			
			EntityRetrieveVO entityRetrieveVO = entityRetrieveSVC.getEntity(queryVO, queryString, notificationEndpointAccept, link);
			if(entityRetrieveVO != null && entityRetrieveVO.getEntities() != null) {
				List<CommonEntityVO> entityVOs = entityRetrieveVO.getEntities();
				for(CommonEntityVO entityVO : entityVOs) {
					entityDataMap.put(entityVO.getId(), entityVO);
				}
			}
		}
		

		if(!entityDataMap.isEmpty()) {
			return new ArrayList<CommonEntityVO>(entityDataMap.values());
		}

		return null;
	}

	/**
	 * queryVO 에 notificationFormat 설정
	 * @param queryVO
	 * @param format
	 */
	private void setOptions(QueryVO queryVO, String format) {
		if(RetrieveOptions.KEY_VALUES.getCode().equals(format)) {
			queryVO.setOptions(RetrieveOptions.KEY_VALUES.getCode());
		} else {
			queryVO.setOptions(RetrieveOptions.NORMALIZED.getCode()); // default
		}
	}

	/**
	 * queryVO 에 geoQ 설정
	 * @param queryVO
	 * @param geoQ
	 */
	private void setGeoQuery(QueryVO queryVO, String geoQ) {
		if(!ValidateUtil.isEmptyData(geoQ)) {
			try {
				GeoQuery geoQuery = objectMapper.readValue(geoQ, GeoQuery.class);

				if(geoQuery != null
						&& geoQuery.getCoordinates() != null
						&& geoQuery.getGeometry() != null
						&& geoQuery.getGeorel() != null) {
					queryVO.setGeorel(geoQuery.getGeorel());
					queryVO.setGeometry(geoQuery.getGeometry());
					queryVO.setCoordinates(geoQuery.getCoordinates().toString());
					queryVO.setGeoproperty(geoQuery.getGeoproperty());
				}
			} catch (Exception e) {
				log.error("NotificationProcessor geoQuery parsing error. geoQ={}", geoQ, e);
			}
		}
	}

	/**
	 * Notification 전송
	 * @param notifyDate 전송일시
	 * @param entityId entity id
	 * @param subscriptionBaseDaoVO subscription base data
	 * @param sendData 전송데이터
	 * @return 전송결과
	 * @throws NotificationException notification 발송 에러
	 * @throws JsonProcessingException json 파싱 에러
	 */
	private boolean sendNotification(Date notifyDate, String entityId, SubscriptionBaseDaoVO subscriptionBaseDaoVO, List<CommonEntityVO> sendData) throws NotificationException, JsonProcessingException {
		NotificationVO notificationVO = new NotificationVO();
		notificationVO.setId(entityId);
		notificationVO.setNotifiedAt(notifyDate);
		notificationVO.setSubscriptionId(subscriptionBaseDaoVO.getId());
		notificationVO.setData(sendData);

		Endpoint endpoint = new Endpoint();
		endpoint.setUri(subscriptionBaseDaoVO.getNotificationEndpointUri());
		endpoint.setAccept(subscriptionBaseDaoVO.getNotificationEndpointAccept());
		
		if(subscriptionBaseDaoVO.getNotificationEndpointNotifierInfo() != null) {
			endpoint.setNotifierInfo(objectMapper.readValue(subscriptionBaseDaoVO.getNotificationEndpointNotifierInfo(), new TypeReference<List<KeyValuePair>>() {}));
		}
		if(subscriptionBaseDaoVO.getNotificationEndpointReceiverInfo() != null) {
			endpoint.setReceiverInfo(objectMapper.readValue(subscriptionBaseDaoVO.getNotificationEndpointReceiverInfo(), new TypeReference<List<KeyValuePair>>() {}));
		}
		
		NotificationReqeustVO notificationReqeustVO = new NotificationReqeustVO();
		notificationReqeustVO.setRequestId(UUID.randomUUID().toString());
		notificationReqeustVO.setRequestTime(notifyDate);
		notificationReqeustVO.setEndpoint(endpoint);
		notificationReqeustVO.setNotificationVO(notificationVO);
		KafkaProcessVO kafkaProcessVO = new KafkaProcessVO();
		kafkaProcessVO.setTopic(notificationTopic);
		kafkaProcessVO.setNotificationReqeustVO(notificationReqeustVO);
		kafkaProducerManager.produceData(kafkaProcessVO);
		
		return true;
	}

	/**
	 * Notification 전송 결과 갱신
	 * @param subscriptionId subscription id
	 * @param entityId entity id
	 * @param notifyDate 전송 일시
	 * @param notificationResult 전송 결과
	 */
	private void updateNotificationResult(String subscriptionId, String entityId, Date notifyDate, boolean notificationResult) {

		try {
			SubscriptionBaseDaoVO notificationResultVO = new SubscriptionBaseDaoVO();
			notificationResultVO.setId(subscriptionId);
			notificationResultVO.setNotificationLastNotification(notifyDate);
			if(notificationResult) {
				notificationResultVO.setNotificationLastSuccess(notifyDate);
				notificationResultVO.setNotificationStatus(NotificationStatus.OK.getCode());
			} else {
				notificationResultVO.setNotificationLastFailure(notifyDate);
				notificationResultVO.setNotificationStatus(NotificationStatus.FAILED.getCode());
			}
			subscriptionSVC.updateNotificationResult(notificationResultVO);
		} catch (Exception e) {
			log.warn("Notification update result error. subscriptionId={}, entityId={}",
					subscriptionId, entityId, e);
		}
	}

	private String buildQueryString(String type, String q, String format) {
		
		if(ValidateUtil.isEmptyData(type) 
				&& ValidateUtil.isEmptyData(q)
				&& ValidateUtil.isEmptyData(format)) {
			return null;
		}

		StringBuilder queryStringBuilder = new StringBuilder();

		if(!ValidateUtil.isEmptyData(type)) {
			queryStringBuilder.append("type=").append(type).append("&");
		}
		if(!ValidateUtil.isEmptyData(q)) {
			queryStringBuilder.append("q=").append(q).append("&");
		}
		
		if(!ValidateUtil.isEmptyData(format)) {
			queryStringBuilder.append("options=").append(format).append("&");
		}
		queryStringBuilder.deleteCharAt(queryStringBuilder.length()-1);
		
		return queryStringBuilder.toString();
	}

	private String contextToLink(List<String> context) {
		if(!ValidateUtil.isEmptyData(context)) {
			StringBuilder linkBuilder = new StringBuilder();
			for(String contextUri : context) {
				linkBuilder.append("<").append(contextUri).append(">")
						   .append("rel=\"http://www.w3.org/ns/json-ld#context\"; type=\"application/ld+json\"\n");
			}
			return linkBuilder.toString();
		}
		return null;
	}

	@Override
	public Object processBulk(List<NotificationProcessVO> notificationProcessVO) {
		throw new CoreException(ErrorCode.NOT_SUPPORTED_METHOD, "NotificationProcessor not support processBulk.");
	}
}
