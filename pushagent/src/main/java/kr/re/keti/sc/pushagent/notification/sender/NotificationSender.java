package kr.re.keti.sc.pushagent.notification.sender;

import java.util.List;

import kr.re.keti.sc.pushagent.common.exception.NotificationException;
import kr.re.keti.sc.pushagent.notification.vo.KeyValuePair;
import kr.re.keti.sc.pushagent.notification.vo.NotificationVO;

public interface NotificationSender {

	public boolean sendNotification(String requestId,
									String endpointUri,
									String accept,
									List<KeyValuePair> notifierInfo,
									List<KeyValuePair> receiverInfo,
									NotificationVO notificationVO) throws NotificationException;
}
