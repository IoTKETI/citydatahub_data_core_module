package kr.re.keti.sc.dataservicebroker.subscription.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.code.SubscriptionCode;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdBadRequestException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdNoExistTypeException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdResourceNotFoundException;
import kr.re.keti.sc.dataservicebroker.csource.service.CsourceRegistrationSVC;
import kr.re.keti.sc.dataservicebroker.csource.vo.CsourceRegistrationVO;
import kr.re.keti.sc.dataservicebroker.datamodel.DataModelManager;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelCacheVO;
import kr.re.keti.sc.dataservicebroker.subscription.dao.SubscriptionDAO;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionBaseDaoVO;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionEntitiesDaoVO;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionRetrieveVO;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionVO;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionVO.NotificationParams.KeyValuePair;
import kr.re.keti.sc.dataservicebroker.util.StringUtil;
import kr.re.keti.sc.dataservicebroker.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class SubscriptionSVC {

	@Autowired
    private RestTemplate restTemplate;
    @Autowired
    protected SubscriptionDAO subscriptionDAO;
    @Autowired
    private DataModelManager dataModelManager;
    @Autowired
    private CsourceRegistrationSVC csourceRegistrationSVC;
    @Autowired
    protected ObjectMapper objectMapper;

    @Value("${entity.validation.id-pattern.enabled:true}")
    private Boolean validateIdPatternEnabled;

    /**
     * 구독 입력값 validation 체크
     *
     * @param subscriptionVO
     * @param isCreateMode
     */
    private void validateCreateAndUpdateParameter(SubscriptionVO subscriptionVO, Boolean isCreateMode, List<String> links) {

        //생성 시에만 사용되는 유효성 체크

        // id format 체크
        validateSubscriptionIdFormat(subscriptionVO.getId());

        //status, Read-only. Provided by the system when querying the details of a subscription
        if (subscriptionVO.getStatus() != null) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "Status is read-only");
        }

        //notification.attributes 빈 Array가 오면 안됨
        if (subscriptionVO.getNotification() != null) {
            if (subscriptionVO.getNotification().getAttributes() != null && subscriptionVO.getNotification().getAttributes().size() == 0) {
                throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "Notification.attributes does not allow empty array");

            }
        }

        if (isCreateMode) {
//            //Entities 정보가 없는 경우
//            if (subscriptionVO.getEntities() == null && subscriptionVO.getDatasetIds() == null) {
//                throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "should include Entities or datasetIds");
//            }

//            if (subscriptionVO.getTimeInterval() == null) {
//                // TimeInterval이 없으면, WatchedAttributes가 있어야 됨.
//                if (subscriptionVO.getWatchedAttributes() == null || subscriptionVO.getWatchedAttributes().size() == 0) {
//                    throw new BadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "should include WatchedAttributes");
//                }
//            } else {
//                // TimeInterval이 있으면, WatchedAttributes가 없어야 됨.
//                if (subscriptionVO.getWatchedAttributes() != null) {
//                    throw new BadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "should not include WatchedAttributes");
//                }
//            }
        }

        if (!DataServiceBrokerCode.JsonLdType.SUBSCRIPTION.getCode().equals(subscriptionVO.getType())
            && !DataServiceBrokerCode.JsonLdType.CSOURCE_REGISTRATION_SUBSCRIPTION.getCode().equals(subscriptionVO.getType())) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER,
                    "should equal type=" + DataServiceBrokerCode.JsonLdType.SUBSCRIPTION.getCode());
        }

        Date now = new Date();
        if (subscriptionVO.getExpires() != null && subscriptionVO.getExpires().getTime() < now.getTime()) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "Expires should be greater than now");
        }


        // 데이터셋아이디 유효성 검증
        List<String> datasetIds = subscriptionVO.getDatasetIds();
        if(datasetIds != null && datasetIds.size() > 0) {
        	for(String datasetId : datasetIds) {
        		if(dataModelManager.getDatasetCache(datasetId) == null) {
        			throw new NgsiLdNoExistTypeException(DataServiceBrokerCode.ErrorCode.NOT_EXISTS_DATASET, "Not Exists Dataset. datasetId=" + datasetId);
        		}
        	}
        }

        if (subscriptionVO.getWatchedAttributes() != null && subscriptionVO.getWatchedAttributes().size() == 0) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "should include WatchedAttributes= "
                    + subscriptionVO.getWatchedAttributes().toString());

        }

        if (subscriptionVO.getWatchedAttributes() != null && subscriptionVO.getTimeInterval() != null) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "should include WatchedAttributes or TimeInterval");
        }

        // throttling : throttling > 0 , timeInterval 있으면 throttling는 있으면 안됨
        if (subscriptionVO.getThrottling() != null) {
            if (subscriptionVO.getThrottling() > 0) {
                if (subscriptionVO.getTimeInterval() != null && subscriptionVO.getTimeInterval() > 0) {
                    throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "should include throttling or timeInterval");
                }
            } else {
                throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "throttling should be greater than 0.");
            }

        }

        if (subscriptionVO.getTimeInterval() != null && subscriptionVO.getTimeInterval() <= 0) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "TimeInterval should be greater than 0.");
        }

        SubscriptionVO.TemporalQuery temporalQ = subscriptionVO.getTemporalQ();
        if (temporalQ != null) {

            SubscriptionCode.Timerel timerel = temporalQ.getTimerel();
            if (timerel != null) {
                if (temporalQ.getTime() == null) {
                    throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "check time : " + temporalQ.getTime());
                }
                if (timerel == SubscriptionCode.Timerel.BETWEEN) {

                    if (temporalQ.getEndTime() == null) {
                        throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "check endTime : " + temporalQ.getEndTime());

                    }

                    if (temporalQ.getTime().getTime() > temporalQ.getEndTime().getTime()) {
                        throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "check time and endTime : " + temporalQ.getTime() + ", " + temporalQ.getEndTime());
                    }
                }
            }
        }

        SubscriptionVO.GeoQuery geoQ = subscriptionVO.getGeoQ();
        if (geoQ != null && geoQ.getGeorel() != null) {
            String georelFullTxt = geoQ.getGeorel();
            try {
                String georelName = georelFullTxt.split(";")[0];

                if (georelName.startsWith(DataServiceBrokerCode.GeometryType.NEAR_REL.getCode())) {
                    String distanceText = georelFullTxt.split(";")[1];
                    String distanceColName = distanceText.split("==")[0];
                    Integer.parseInt(distanceText.split("==")[1]);

                    if (!(distanceColName.equals(DataServiceBrokerCode.GeometryType.MIN_DISTANCE.getCode()) || distanceColName.equals(DataServiceBrokerCode.GeometryType.MAX_DISTANCE.getCode()))) {
                        log.warn("invalid geo-query parameter");
                        throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "invalid geo-query parameter");
                    }
                }
                DataServiceBrokerCode.GeometryType geometryType = DataServiceBrokerCode.GeometryType.parseType(georelName);
                if (geometryType == null) {
                    log.warn("invalid geo-query parameter");
                    throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "invalid geo-query parameter");
                }
            } catch (Exception e) {
                throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "invalid geo-query parameter", e);
            }

        }

        //notification 검증
        if(subscriptionVO.getNotification() == null) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "should include notification");
        }
        //endpoint uri 검증
        SubscriptionVO.NotificationParams.Endpoint endPoint = subscriptionVO.getNotification().getEndpoint();
        if(ValidateUtil.isEmptyData(endPoint)) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "should include notification.endpoint");
        }
        String uri = endPoint.getUri();
        if(ValidateUtil.isEmptyData(uri)) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "should include notification.endpoint.uri");
        }
        if (!StringUtil.checkURIPattern(uri)){
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "invalid notification.endPoint.uri parameter. uri=" + uri);
        }

        // information 및 location 필드 유효성 체크
        validateParameterByContext(subscriptionVO, links);

    }

    /**
     * 구독 정보 생성 요청
     *
     * @param subscriptionVO
     * @return
     * @throws Exception
     */
    @Transactional(value = "dataSourceTransactionManager")
    public Integer createSubscription(SubscriptionVO subscriptionVO, List<String> links) throws Exception {

        // 1. 구독 id가 없는 경우 생성
        if (ValidateUtil.isEmptyData(subscriptionVO.getId())) {
            subscriptionVO.setId(makeRandomSubscriptionId());
        }

        // 2. 파라미터 유효성 체크
        validateCreateAndUpdateParameter(subscriptionVO, true, links);

        // 3. csource 정보 생성
        SubscriptionBaseDaoVO subscriptionBaseDaoVO = subscriptionVoToBaseDaoVO(subscriptionVO);
        List<SubscriptionEntitiesDaoVO> subscriptionVoToEntitiesDaoVOs = subscriptionVoToEntitiesDaoVO(subscriptionVO, links);

        Integer object = subscriptionDAO.createSubscriptionBase(subscriptionBaseDaoVO);

        if(subscriptionVoToEntitiesDaoVOs != null && subscriptionVoToEntitiesDaoVOs.size() > 0) {
        	for (SubscriptionEntitiesDaoVO subscriptionEntitiesDaoVO : subscriptionVoToEntitiesDaoVOs) {
                subscriptionDAO.createSubscriptionEntities(subscriptionEntitiesDaoVO);
            }
        }

        return object;
    }


    /**
     * 구독 단건 조회
     *
     * @param subscriptionId
     * @return
     * @throws JsonProcessingException
     */
    public SubscriptionVO retrieveSubscription(String subscriptionId) throws JsonProcessingException {

        // id format 체크
        validateSubscriptionIdFormat(subscriptionId);

        SubscriptionVO subscriptionVO = subscriptionDAO.retrieveSubscription(subscriptionId);

        return subscriptionVO;
    }

    public List<SubscriptionBaseDaoVO> retrieveSubscriptionByEntityId(SubscriptionRetrieveVO subscriptionRetrieveVO) {
        return subscriptionDAO.retrieveSubscriptionByEntity(subscriptionRetrieveVO);
    }

    public List<SubscriptionBaseDaoVO> retrieveTimeIntervalSubscription() {

        return subscriptionDAO.retrieveTimeIntervalSubscription();
    }

    /**
     * 구독 리스트 조회
     *
     * @param limit
     * @param offset
     * @param jsonLdType
     * @return
     * @throws JsonProcessingException
     */
    public List<SubscriptionVO> querySubscriptions(Integer limit, Integer offset, DataServiceBrokerCode.JsonLdType jsonLdType) throws JsonProcessingException {
        List<SubscriptionVO> subscriptionVOs = subscriptionDAO.querySubscriptions(limit, offset, jsonLdType);
        return subscriptionVOs;
    }

    /**
     * 구독 리스트 조회 (Count)
     *
     * @param limit
     * @param offset
     * @param jsonLdType
     * @return
     * @throws JsonProcessingException
     */
    public Integer querySubscriptionsCount(Integer limit, Integer offset, DataServiceBrokerCode.JsonLdType jsonLdType) throws JsonProcessingException {
        Integer totalCount = subscriptionDAO.querySubscriptionsCount(limit, offset, jsonLdType);
        return totalCount;
    }

    @Transactional(value = "dataSourceTransactionManager")
    public Integer deleteSubscription(String subscriptionId) {

        // id format 체크
        validateSubscriptionIdFormat(subscriptionId);

        Integer deleteSubscriptionEntitiesResult = subscriptionDAO.deleteSubscriptionEntities(subscriptionId);
        Integer deleteSubscriptionBaseResult = subscriptionDAO.deleteSubscriptionBase(subscriptionId);
        return deleteSubscriptionBaseResult;
    }

    @Transactional(value = "dataSourceTransactionManager")
    public Integer updateSubscription(String subscriptionId, SubscriptionVO subscriptionVO, List<String> links) throws Exception {

        validateCreateAndUpdateParameter(subscriptionVO, false, links);

        SubscriptionVO retrieveVO = retrieveSubscription(subscriptionId);

        // 3. 조회된 resource가 없을 경우, ResourceNotFound 처리
        if (retrieveVO == null) {
            throw new NgsiLdResourceNotFoundException(DataServiceBrokerCode.ErrorCode.NOT_EXIST_ID, "There is no an existing subscription which id");
        }
        Date now = new Date();

        //1. If the Subscription id is not present or it is not a valid URI, then an error of type BadRequestData shall be raised
        //2. 필수 항목 null요청 에러처리 BadRequestData
        //3. • Term to URI expansion of Attribute names shall be observed as mandated by clause 5.5.7.?
        //• Then, implementations shall modify the target Subscription as mandated by clause 5.5.8.?
        if (subscriptionVO.getIsActive() == null || subscriptionVO.getIsActive() == true) {
            // If isActive is equal to true or null and expires is not present, then status shall be updated to "active"
            // , if and only if, the previous value of status was different than "expired".
            if (subscriptionVO.getExpires() == null) {
                if (retrieveVO.getStatus() != SubscriptionCode.Status.EXPIRED) {
                    retrieveVO.setStatus(SubscriptionCode.Status.ACTIVE);
                }
            }

            //- If isActive is equal to true or null
            // and expires is null or corresponds to a DateTime in the future
            // , then status shall be updated to "active".
            if (subscriptionVO.getExpires() == null || subscriptionVO.getExpires().getTime() > now.getTime()) {

                if (retrieveVO != null) {

                    if (retrieveVO.getStatus() != null && retrieveVO.getStatus() != SubscriptionCode.Status.EXPIRED) {
                        retrieveVO.setStatus(SubscriptionCode.Status.ACTIVE);
                    }
                }
            }
        }

        //- If isActive is equal to False and expires is not present
        //, then status shall be updated to "paused"
        // , if and only if, the previous value of status was different than "expired".
        if ((subscriptionVO.getIsActive() != null && subscriptionVO.getIsActive() == false) && subscriptionVO.getExpires() != null) {
            if (retrieveVO.getStatus() != SubscriptionCode.Status.EXPIRED) {
                subscriptionVO.setStatus(SubscriptionCode.Status.PAUSED);
            }
        }

        // - If expires is included but referring to a DateTime in the past, then a BadRequestData error shall be raised, regardless the value of isActive.
        if (subscriptionVO.getExpires() != null && (subscriptionVO.getExpires().getTime() < now.getTime())) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "check Expires:" + subscriptionVO.getExpires());
        }

        SubscriptionBaseDaoVO subscriptionBaseDaoVO = subscriptionVoToBaseDaoVO(subscriptionVO);
        subscriptionBaseDaoVO.setSubscriptionId(subscriptionId);

        List<SubscriptionEntitiesDaoVO> subscriptionEntitiesDaoVOs = subscriptionVoToEntitiesDaoVO(subscriptionVO, links);

        Integer result = subscriptionDAO.updateSubscriptionBase(subscriptionBaseDaoVO);

        // entities 정보가 존재할 경우만 업데이트
        if(!ValidateUtil.isEmptyData(subscriptionEntitiesDaoVOs)) {
            subscriptionDAO.deleteSubscriptionEntities(subscriptionId);
            if (subscriptionEntitiesDaoVOs != null) {
                for (SubscriptionEntitiesDaoVO subscriptionEntitiesDaoVO : subscriptionEntitiesDaoVOs) {
                    subscriptionDAO.createSubscriptionEntities(subscriptionEntitiesDaoVO);
                }
            }
        }

        return result;
    }

    public Integer updateNotificationResult(SubscriptionBaseDaoVO subscriptionBaseDaoVO) {
        return subscriptionDAO.updateNotificationResult(subscriptionBaseDaoVO);
    }

    private SubscriptionBaseDaoVO subscriptionVoToBaseDaoVO(SubscriptionVO subscriptionVO) throws JsonProcessingException {

        SubscriptionVO.NotificationParams notificationParams = subscriptionVO.getNotification();

        SubscriptionBaseDaoVO subscriptionBaseDaoVO = new SubscriptionBaseDaoVO();
        subscriptionBaseDaoVO.setContext(subscriptionVO.getContext());
        subscriptionBaseDaoVO.setId(subscriptionVO.getId());
        subscriptionBaseDaoVO.setName(subscriptionVO.getName());
        subscriptionBaseDaoVO.setType(subscriptionVO.getType());
        subscriptionBaseDaoVO.setDescription(subscriptionVO.getDescription());
        subscriptionBaseDaoVO.setDatasetIds(subscriptionVO.getDatasetIds());
        subscriptionBaseDaoVO.setWatchedAttributes(subscriptionVO.getWatchedAttributes());
        subscriptionBaseDaoVO.setTimeInterval(subscriptionVO.getTimeInterval());
        subscriptionBaseDaoVO.setQ(subscriptionVO.getQ());
        if (subscriptionVO.getGeoQ() != null) {
            String geoQ = objectMapper.writeValueAsString(subscriptionVO.getGeoQ());
            subscriptionBaseDaoVO.setGeoQ(geoQ);
        }
        subscriptionBaseDaoVO.setCsf(subscriptionVO.getCsf());

        if (notificationParams != null) {
            if (notificationParams.getAttributes() != null) {
                subscriptionBaseDaoVO.setNotificationAttributes(notificationParams.getAttributes());
            }
            String notificationFormat = notificationParams.getFormat();
            if (notificationFormat != null) {
                if (!(notificationFormat.equals(DataServiceBrokerCode.RetrieveOptions.NORMALIZED.getCode())
                        || notificationFormat.equals(DataServiceBrokerCode.RetrieveOptions.KEY_VALUES.getCode()))) {
                    throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "should include notificationFormat= " + notificationFormat);
                }
                subscriptionBaseDaoVO.setNotificationFormat(notificationFormat);
            }
            if (notificationParams.getEndpoint() != null) {

                String endPointAccept = notificationParams.getEndpoint().getAccept();
                if (endPointAccept != null) {
                    if (!(endPointAccept.equals(Constants.APPLICATION_LD_JSON_VALUE)
                            || endPointAccept.equals(MediaType.APPLICATION_JSON_VALUE))) {
                        throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "should include endPointAccept= " + endPointAccept);
                    }
                    subscriptionBaseDaoVO.setNotificationEndpointAccept(endPointAccept);
                }

                subscriptionBaseDaoVO.setNotificationEndpointUri(notificationParams.getEndpoint().getUri());

                List<KeyValuePair> notifierInfos = notificationParams.getEndpoint().getNotifierInfo();
                if (notifierInfos != null && notifierInfos.size() > 0) {
                	String notifierInfoJson = objectMapper.writeValueAsString(notifierInfos);
                	subscriptionBaseDaoVO.setNotificationEndpointNotifierInfo(notifierInfoJson);
                }

                List<KeyValuePair> receiverInfos = notificationParams.getEndpoint().getReceiverInfo();
                if (receiverInfos != null && receiverInfos.size() > 0) {
                	String receiverInfoJson = objectMapper.writeValueAsString(receiverInfos);
                	subscriptionBaseDaoVO.setNotificationEndpointReceiverInfo(receiverInfoJson);
                }
            }


        }
        subscriptionBaseDaoVO.setIsActive(subscriptionVO.getIsActive());
        subscriptionBaseDaoVO.setExpire(subscriptionVO.getExpires());
        subscriptionBaseDaoVO.setThrottling(subscriptionVO.getThrottling());

        SubscriptionVO.TemporalQuery temporalQ = subscriptionVO.getTemporalQ();
        if (temporalQ != null) {
            if (temporalQ.getTimerel() != null) {
                subscriptionBaseDaoVO.setTemporalQTimerel(temporalQ.getTimerel().getCode());
            }
            if (temporalQ.getTime() != null) {
                subscriptionBaseDaoVO.setTemporalQTime(temporalQ.getTime());
            }
            if (temporalQ.getEndTime() != null) {
                subscriptionBaseDaoVO.setTemporalQEndTime(temporalQ.getEndTime());
            }
            if (temporalQ.getTimeproperty() != null) {
                subscriptionBaseDaoVO.setTemporalQTimeProperty(temporalQ.getTimeproperty());
            }
        }

        return subscriptionBaseDaoVO;
    }

    private List<SubscriptionEntitiesDaoVO> subscriptionVoToEntitiesDaoVO(SubscriptionVO subscriptionVO, List<String> links) {

        List<SubscriptionVO.EntityInfo> entityInfos = subscriptionVO.getEntities();
        List<SubscriptionEntitiesDaoVO> subscriptionEntitiesDaoVOs = new ArrayList<>();

        if (entityInfos == null) {
            return null;
        }

        List<String> context = subscriptionVO.getContext() != null ? subscriptionVO.getContext() : links;
        for (SubscriptionVO.EntityInfo entityInfo : entityInfos) {
            String type = entityInfo.getType();

            DataServiceBrokerCode.JsonLdType subscriptionType = DataServiceBrokerCode.JsonLdType.parseType(subscriptionVO.getType());

            DataModelCacheVO dataModelCacheVO = null;
            if (subscriptionType == DataServiceBrokerCode.JsonLdType.SUBSCRIPTION) {

                if (ValidateUtil.isEmptyData(type)) {
                    throw new NgsiLdNoExistTypeException(DataServiceBrokerCode.ErrorCode.NOT_EXISTS_DATAMODEL, "Not exists entities.type.");
                }

                dataModelCacheVO = dataModelManager.getDataModelVOCacheByContext(context, type);
                if (dataModelCacheVO == null) {
                    throw new NgsiLdNoExistTypeException(DataServiceBrokerCode.ErrorCode.NOT_EXISTS_DATAMODEL, "Invalid entities.type. type=" + type);
                }

            } else if (subscriptionType == DataServiceBrokerCode.JsonLdType.CSOURCE_REGISTRATION_SUBSCRIPTION) {

                /*
                    Instead of directly matching the entities and watched Attributes from the subscription with the Context Source registrations,
                    the entities specified in the subscription,
                    the watched Attributes and the Attributes specified in the notification parameter are matched
                    against the respective information property of the Context Source registrations.
                 */
                List<CsourceRegistrationVO> csourceRegistrationVOs = csourceRegistrationSVC.queryCsourceRegistrationsByEntityId(entityInfo.getId());
//                throw new NgsiLdNoExistTypeException(DataServiceBrokerCode.ErrorCode.NOT_EXIST_ENTITY, "Not Exist Information." +
//                        " entityType=" + type
//                        + " entityId=" + entityInfo.getIdPattern());

            } else {
                throw new NgsiLdNoExistTypeException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER,
                        "Invalid subscription type. type=" + subscriptionVO.getType());

            }


            SubscriptionEntitiesDaoVO subscriptionEntitiesDaoVO = new SubscriptionEntitiesDaoVO();
            subscriptionEntitiesDaoVO.setId(entityInfo.getId());
            subscriptionEntitiesDaoVO.setIdPattern(entityInfo.getIdPattern());
            subscriptionEntitiesDaoVO.setSubscriptionId(subscriptionVO.getId());
            subscriptionEntitiesDaoVO.setType(type);
            if(dataModelCacheVO != null) {
            	subscriptionEntitiesDaoVO.setType(dataModelCacheVO.getDataModelVO().getType());
            	subscriptionEntitiesDaoVO.setTypeUri(dataModelCacheVO.getDataModelVO().getTypeUri());
            }
            

            subscriptionEntitiesDaoVOs.add(subscriptionEntitiesDaoVO);
        }
        return subscriptionEntitiesDaoVOs;
    }

    /**
     * 5.2.12-1
     *  구독 생성 시, SubscriptionId가 없을 경우, 자동 생성
     *  If the subscription document does not include a Subscription identifier,
     *  a new identifier (URI) shall be automatically generated by the implementation.
     * @return
     */
    private String makeRandomSubscriptionId(){
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String id = Constants.PREFIX_SUBSCRIPTION_ID + uuid.substring(0, 10);

        return id;
    }


    private void validateParameterByContext(SubscriptionVO subscriptionVO, List<String> links) {
        List<String> context = subscriptionVO.getContext() != null ? subscriptionVO.getContext() : links;
        Map<String, String> contextMap = null;
        if(!ValidateUtil.isEmptyData(context)) {
            contextMap = dataModelManager.contextToFlatMap(context);
        }

        // validate entity type
        if(!ValidateUtil.isEmptyData(subscriptionVO.getEntities())) {
            for(SubscriptionVO.EntityInfo entityInfo : subscriptionVO.getEntities()) {
                if(!ValidateUtil.isEmptyData(entityInfo.getType())) {
                    // entityType이 full uri 인 경우 context 정보에 존재하는 지 유효성 검증
                    if(entityInfo.getType().startsWith("http")) {
                        if(contextMap == null || !contextMap.containsValue(entityInfo.getType())) {
                            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER,
                                    "Invalid Parameter. Not exists entityType in context. entityType=" + entityInfo.getType());
                        }

                    // entityType이 short name 인 경우 context 정보에 존재하는 지 유효성 검증
                    } else {
                        if(contextMap == null || !contextMap.containsKey(entityInfo.getType())) {
                            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER,
                                    "Invalid Parameter. Not exists entityType in context. entityType=" + entityInfo.getType());
                        }
                    }
                }
            }
        }

        // validate watchAttributeName
        if(!ValidateUtil.isEmptyData(subscriptionVO.getWatchedAttributes())) {
            for(String watchAttributeName : subscriptionVO.getWatchedAttributes()) {
                // watchAttributeName이 full uri 인 경우 context 정보에 존재하는 지 유효성 검증
                if(watchAttributeName.startsWith("http")) {
                    if(contextMap == null || !contextMap.containsValue(watchAttributeName)) {
                        throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER,
                                "Invalid Parameter. Not exists watchAttributeName in context. watchAttributeName=" + watchAttributeName);
                    }

                // watchAttributeName이 short name 인 경우 context 정보에 존재하는 지 유효성 검증
                } else {
                    if(contextMap == null || !contextMap.containsKey(watchAttributeName)) {
                        throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER,
                                "Invalid Parameter. Not exists watchAttributeName in context. watchAttributeName=" + watchAttributeName);
                    }
                }
            }
        }

        // validate notificationAttributeName
        if(subscriptionVO.getNotification() != null
                && !ValidateUtil.isEmptyData(subscriptionVO.getNotification().getAttributes())) {
            for(String notificationAttributeName : subscriptionVO.getNotification().getAttributes()) {
                // watchAttributeName이 full uri 인 경우 context 정보에 존재하는 지 유효성 검증
                if(notificationAttributeName.startsWith("http")) {
                    if(contextMap == null || !contextMap.containsValue(notificationAttributeName)) {
                        throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER,
                                "Invalid Parameter. Not exists notificationAttributeName in context. notificationAttributeName=" + notificationAttributeName);
                    }

                // notificationAttributeName이 short name 인 경우 context 정보에 존재하는 지 유효성 검증
                } else {
                    if(contextMap == null || !contextMap.containsKey(notificationAttributeName)) {
                        throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER,
                                "Invalid Parameter. Not exists notificationAttributeName in context. notificationAttributeName=" + notificationAttributeName);
                    }
                }
            }
        }
    }

    private void validateSubscriptionIdFormat(String subscriptionId) {
        // urn 패턴 여부 체크
        if(validateIdPatternEnabled && !ValidateUtil.isValidUrn(subscriptionId)) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER,
                    "Subscription id is not in URN format. subscriptionId=" + subscriptionId);
        }
    }
}