package kr.re.keti.sc.dataservicebroker.subscription.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionBaseDaoVO;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionEntitiesDaoVO;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionRetrieveVO;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionVO;

import java.util.List;

public interface SubscriptionDAOInterface {

    public SubscriptionVO retrieveSubscription(String subscriptionId) throws JsonProcessingException;
    
    public List<SubscriptionBaseDaoVO> retrieveSubscriptionByEntity(SubscriptionRetrieveVO subscriptionRetrieveVO);

    public List<SubscriptionBaseDaoVO> retrieveTimeIntervalSubscription();

    public Integer querySubscriptionsCount(Integer limit, Integer offset, DataServiceBrokerCode.JsonLdType jsonLdType) throws JsonProcessingException;

    public List<SubscriptionVO> querySubscriptions(Integer limit, Integer offset, DataServiceBrokerCode.JsonLdType jsonLdType) throws JsonProcessingException;

    public Integer createSubscriptionBase(SubscriptionBaseDaoVO subscriptionBaseDaoVO);

    public Integer createSubscriptionEntities(SubscriptionEntitiesDaoVO subscriptionEntitiesDaoVO);


    public Integer deleteSubscriptionBase(String subscriptionId);

    public Integer deleteSubscriptionEntities(String subscriptionId);

    public Integer updateSubscriptionBase(SubscriptionBaseDaoVO subscriptionBaseDaoVO);

    public Integer updateSubscriptionEntities(SubscriptionEntitiesDaoVO subscriptionEntitiesDaoVO);

    public Integer updateNotificationResult(SubscriptionBaseDaoVO subscriptionBaseDaoVO);
}
