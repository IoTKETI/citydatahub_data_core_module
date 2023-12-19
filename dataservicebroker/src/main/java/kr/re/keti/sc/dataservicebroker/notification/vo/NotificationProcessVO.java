package kr.re.keti.sc.dataservicebroker.notification.vo;

import java.util.Date;
import java.util.List;

import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityFullVO;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionBaseDaoVO;
import lombok.Data;

/**
 * 비동기 Notification 처리를 위한 VO
 */
@Data
public class NotificationProcessVO {

	private String entityId;
	private String entityIdPattern;
	private String entityType;
	private String entityTypeUri;
	private String datasetId;
	private Date eventTime;
	/** change event기반 Notification 이 아닌 경우는 null */
	private CommonEntityFullVO requestEntityFullVO;
	private List<SubscriptionBaseDaoVO> subscriptionBaseDaoVOs;
	/** timeInterval 기반 Notification 여부 ( false인 경우(default) : change event )*/
	private boolean timeIntervalEvent = false;
}
