package kr.re.keti.sc.dataservicebroker.subscription.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.code.SubscriptionCode;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionBaseDaoVO;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionEntitiesDaoVO;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionRetrieveVO;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionVO;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionVO.NotificationParams.KeyValuePair;

@Component
public class SubscriptionDAO implements SubscriptionDAOInterface {

    @Autowired
    private SqlSessionTemplate sqlSession;
    //    @Autowired
//	@Qualifier("batchSqlSession")
//    private SqlSessionTemplate batchSqlSession;
    @Autowired
    @Qualifier("retrieveSqlSession")
    private SqlSessionTemplate retrieveSqlSession;
    @Autowired
    protected ObjectMapper objectMapper;


    /**
     * 구독 단건 조회
     * @param subscriptionId
     * @return
     * @throws JsonProcessingException
     */
    @Override
    public SubscriptionVO retrieveSubscription(String subscriptionId) throws JsonProcessingException {

        HashMap<String, Object> param = new HashMap<>();
        param.put("subscriptionId", subscriptionId);
        List<SubscriptionBaseDaoVO> subscriptionBaseDaoVOs = retrieveSqlSession.selectList("dataservicebroker.subscription.selectSubscription", param);
        List<SubscriptionVO> subscriptionVOs = subscriptionDaoVOToVo(subscriptionBaseDaoVOs);

        if (subscriptionVOs != null && subscriptionVOs.size() > 0) {
            return subscriptionVOs.get(0);
        } else {
            return null;
        }
    }

    /**
     * 구독 리스트 DB 조회(Count)
     * @param limit
     * @return
     * @throws JsonProcessingException
     */
    @Override
    public Integer querySubscriptionsCount(Integer limit, Integer offset, DataServiceBrokerCode.JsonLdType jsonLdType) throws JsonProcessingException {

        HashMap<String, Object> param = new HashMap<>();
        param.put("limit", limit);
        param.put("offset", offset);
        param.put("type", jsonLdType.getCode());

        Integer totalCount  = retrieveSqlSession.selectOne("dataservicebroker.subscription.selectSubscriptionCount", param);

        return totalCount;

    }
    /**
     * 구독 리스트 DB 조회
     * @param limit
     * @return
     * @throws JsonProcessingException
     */
    @Override
    public List<SubscriptionVO> querySubscriptions(Integer limit, Integer offset, DataServiceBrokerCode.JsonLdType jsonLdType) throws JsonProcessingException {

        HashMap<String, Object> param = new HashMap<>();
        param.put("limit", limit);
        param.put("offset", offset);
        param.put("type", jsonLdType.getCode());

        List<SubscriptionBaseDaoVO> subscriptionBaseDaoVOs = retrieveSqlSession.selectList("dataservicebroker.subscription.selectSubscription", param);
        List<SubscriptionVO> subscriptionVOs = subscriptionDaoVOToVo(subscriptionBaseDaoVOs);

        return subscriptionVOs;

    }


    @Override
    public Integer createSubscriptionBase(SubscriptionBaseDaoVO subscriptionBaseDaoVO) {
        return sqlSession.update("dataservicebroker.subscription.createSubscriptionBase", subscriptionBaseDaoVO);
    }

    @Override
    public Integer createSubscriptionEntities(SubscriptionEntitiesDaoVO subscriptionEntitiesDaoVO) {
        return sqlSession.update("dataservicebroker.subscription.createSubscriptionEntities", subscriptionEntitiesDaoVO);
    }

    @Override
    public Integer deleteSubscriptionBase(String subscriptionId) {
        return sqlSession.delete("dataservicebroker.subscription.deleteSubscriptionBase", subscriptionId);
    }

    @Override
    public Integer deleteSubscriptionEntities(String subscriptionId) {
        return sqlSession.delete("dataservicebroker.subscription.deleteSubscriptionEntities", subscriptionId);
    }

    @Override
    public Integer updateSubscriptionBase(SubscriptionBaseDaoVO subscriptionBaseDaoVO) {
        return sqlSession.update("dataservicebroker.subscription.updateSubscriptionBase", subscriptionBaseDaoVO);
    }

    @Override
    public Integer updateSubscriptionEntities(SubscriptionEntitiesDaoVO subscriptionEntitiesDaoVO) {
        return sqlSession.update("dataservicebroker.subscription.updateSubscriptionEntities", subscriptionEntitiesDaoVO);
    }

    @Override
	public List<SubscriptionBaseDaoVO> retrieveSubscriptionByEntity(SubscriptionRetrieveVO subscriptionRetrieveVO) {
		return retrieveSqlSession.selectList("dataservicebroker.subscription.selectSubscriptionsByEntity", subscriptionRetrieveVO);
	}

    @Override
	public List<SubscriptionBaseDaoVO> retrieveTimeIntervalSubscription() {
		return retrieveSqlSession.selectList("dataservicebroker.subscription.selectTimeIntervalSubscription");
	}
    
    @Override
    public Integer updateNotificationResult(SubscriptionBaseDaoVO subscriptionBaseDaoVO) {
    	return sqlSession.update("dataservicebroker.subscription.updateNotificationResult", subscriptionBaseDaoVO);
    }

    /**
     * subscription_base 와 subscription_entities 결합한 정보로 변환
     * @param subscriptionBaseDaoVOs
     * @return
     * @throws JsonProcessingException
     */
    private List<SubscriptionVO> subscriptionDaoVOToVo(List<SubscriptionBaseDaoVO> subscriptionBaseDaoVOs) throws JsonProcessingException {

        List<SubscriptionVO> subscriptionVOs = new ArrayList<>();

        for (SubscriptionBaseDaoVO subscriptionBaseDaoVO : subscriptionBaseDaoVOs) {
            String id = subscriptionBaseDaoVO.getId();

            SubscriptionVO subscriptionVO = new SubscriptionVO();
            subscriptionVO.setId(id);
            subscriptionVO.setContext(subscriptionBaseDaoVO.getContext());
            subscriptionVO.setName(subscriptionBaseDaoVO.getName());
            subscriptionVO.setType(DataServiceBrokerCode.JsonLdType.SUBSCRIPTION.getCode());
            subscriptionVO.setDescription(subscriptionBaseDaoVO.getDescription());
            subscriptionVO.setDatasetIds(subscriptionBaseDaoVO.getDatasetIds());
            subscriptionVO.setWatchedAttributes(subscriptionBaseDaoVO.getWatchedAttributes());
            subscriptionVO.setTimeInterval(subscriptionBaseDaoVO.getTimeInterval());
            subscriptionVO.setQ(subscriptionBaseDaoVO.getQ());
            subscriptionVO.setCsf(subscriptionBaseDaoVO.getCsf());

            SubscriptionVO.NotificationParams notificationParams = new SubscriptionVO.NotificationParams();
            notificationParams.setAttributes(subscriptionBaseDaoVO.getNotificationAttributes());
            notificationParams.setFormat(subscriptionBaseDaoVO.getNotificationFormat());
            subscriptionVO.setNotification(notificationParams);

            SubscriptionVO.NotificationParams.Endpoint endpoint = new SubscriptionVO.NotificationParams.Endpoint();
            endpoint.setUri(subscriptionBaseDaoVO.getNotificationEndpointUri());
            endpoint.setAccept(subscriptionBaseDaoVO.getNotificationEndpointAccept());
            if(subscriptionBaseDaoVO.getNotificationEndpointNotifierInfo() != null) {
            	endpoint.setNotifierInfo(objectMapper.readValue(subscriptionBaseDaoVO.getNotificationEndpointNotifierInfo()
            			, new TypeReference<List<KeyValuePair>>() {}));
            }
            if(subscriptionBaseDaoVO.getNotificationEndpointReceiverInfo() != null) {
            	endpoint.setReceiverInfo(objectMapper.readValue(subscriptionBaseDaoVO.getNotificationEndpointReceiverInfo()
            			, new TypeReference<List<KeyValuePair>>() {}));
            }
            notificationParams.setEndpoint(endpoint);

            subscriptionVO.setExpiresAt(subscriptionBaseDaoVO.getExpire());
            subscriptionVO.setThrottling(subscriptionBaseDaoVO.getThrottling());

            subscriptionVO.setIsActive(subscriptionBaseDaoVO.getIsActive());
            SubscriptionCode.Status status = generateStatus(subscriptionBaseDaoVO.getIsActive(), subscriptionBaseDaoVO.getExpire());
            subscriptionVO.setStatus(status);

            List<SubscriptionEntitiesDaoVO> subscriptionEntitiesDaoVOs = subscriptionBaseDaoVO.getSubscriptionEntitiesDaoVOs();
            if(subscriptionEntitiesDaoVOs != null && subscriptionEntitiesDaoVOs.size() > 0) {
            	List<SubscriptionVO.EntityInfo> entities = new ArrayList<>();
            	for (SubscriptionEntitiesDaoVO subscriptionEntitiesDaoVO : subscriptionEntitiesDaoVOs) {
                    SubscriptionVO.EntityInfo entityInfo = new SubscriptionVO.EntityInfo();
                    entityInfo.setType(subscriptionEntitiesDaoVO.getType());
                    entityInfo.setTypeUri(subscriptionEntitiesDaoVO.getTypeUri());
                    entityInfo.setId(subscriptionEntitiesDaoVO.getId());
                    entityInfo.setIdPattern(subscriptionEntitiesDaoVO.getIdPattern());
                    entities.add(entityInfo);
                }
            	subscriptionVO.setEntities(entities);
            }
            
            String geoQ = subscriptionBaseDaoVO.getGeoQ();
            subscriptionVO.setGeoQ(stringToGeoQuery(geoQ));

            subscriptionVOs.add(subscriptionVO);
        }

        return subscriptionVOs;
    }

    private SubscriptionCode.Status generateStatus(Boolean isActive, Date expiresAt) {

        if(isActive != null && !isActive) {
            return SubscriptionCode.Status.PAUSED;
        }

        if(expiresAt != null && expiresAt.before(new Date())) {
            return SubscriptionCode.Status.EXPIRED;
        }

        return SubscriptionCode.Status.ACTIVE;
    }


    private SubscriptionVO.GeoQuery stringToGeoQuery(String geoQ) throws JsonProcessingException {


        if (geoQ != null) {
            JsonNode geoQJsonNode = this.objectMapper.readTree(geoQ);
            Map<String, String> geoQMap = this.objectMapper.readValue(geoQ, HashMap.class);
            JsonNode coordinatesJsonNode = geoQJsonNode.get("coordinates");

            SubscriptionVO.GeoQuery geoQuery = new SubscriptionVO.GeoQuery();

            if (geoQuery != null) {

                geoQuery.setGeometry(geoQMap.get("geometry"));
                geoQuery.setGeoproperty(geoQMap.get("geoproperty"));
                geoQuery.setGeorel(geoQMap.get("georel"));
                if (coordinatesJsonNode != null) {
                    geoQuery.setCoordinates(coordinatesJsonNode);

                }
            }
            return geoQuery;

        }

        return null;


    }
}
