package kr.re.keti.sc.dataservicebroker.subscription.vo;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class SubscriptionBaseDaoVO {

	private List<String> context;
    private String id;
    private String name;
    private String type;
    private String description;
    private List<String> watchedAttributes;
    private Integer timeInterval;
    private String q;
    private String geoQ;
    private String csf;
    private Boolean isActive;
    private List<String> notificationAttributes;
    private String notificationFormat;
    private String notificationEndpointUri;
    private String notificationEndpointAccept;
    private String notificationEndpointNotifierInfo;
    private String notificationEndpointReceiverInfo;
    private String notificationStatus;
    private Integer notificationTimeSent;
    private Date notificationLastNotification;
    private Date notificationLastFailure;
    private Date notificationLastSuccess;
    private Date expire;
    private Integer throttling;
    private String temporalQTimerel;
    private Date temporalQTime;
    private Date temporalQEndTime;
    private String temporalQTimeProperty;
    private String status;
    private Date createDatetime;
    private String creatorId;
    private Date modifyDatetime;
    private String modifierId;

	private String subscriptionId;
	private List<String> datasetIds;

	private List<SubscriptionEntitiesDaoVO> subscriptionEntitiesDaoVOs;
}
