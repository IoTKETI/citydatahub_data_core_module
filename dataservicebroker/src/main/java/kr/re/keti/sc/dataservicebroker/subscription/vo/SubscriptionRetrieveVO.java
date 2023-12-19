package kr.re.keti.sc.dataservicebroker.subscription.vo;

import lombok.Data;

@Data
public class SubscriptionRetrieveVO extends SubscriptionBaseDaoVO {

	private String entityId;
    private String entityIdPattern;
    private String entityType;
    private String entityTypeUri;
    private String datasetId;
}
