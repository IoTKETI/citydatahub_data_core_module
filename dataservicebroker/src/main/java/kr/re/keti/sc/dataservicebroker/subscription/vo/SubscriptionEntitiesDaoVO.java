package kr.re.keti.sc.dataservicebroker.subscription.vo;

import lombok.Data;

@Data
public class SubscriptionEntitiesDaoVO {

    private String id;
    private String subscriptionId;
    private String idPattern;
    private String type;
    private String typeUri;
}
