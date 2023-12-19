package kr.re.keti.sc.pushagent.notification.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.re.keti.sc.pushagent.common.bulk.IBulkProcessor;
import kr.re.keti.sc.pushagent.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.pushagent.common.exception.CoreException;
import kr.re.keti.sc.pushagent.common.exception.NotificationException;
import kr.re.keti.sc.pushagent.notification.sender.http.HttpSender;
import kr.re.keti.sc.pushagent.notification.sender.mqtt.MqttSender;
import kr.re.keti.sc.pushagent.notification.vo.Endpoint;
import kr.re.keti.sc.pushagent.notification.vo.KeyValuePair;
import kr.re.keti.sc.pushagent.notification.vo.NotificationReqeustVO;
import kr.re.keti.sc.pushagent.notification.vo.NotificationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class NotificationProcessor implements IBulkProcessor<NotificationReqeustVO> {

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
    private MqttSender mqttSender;
    @Autowired
    private HttpSender httpSender;

    @Override
    public Object processSingle(NotificationReqeustVO notificationReqeustVO) {
        try {
            sendNotification(notificationReqeustVO);
        } catch (Exception e) {
            log.error("NotificationProcessor sendNotification error. ", e);

        }
        return null;
    }


    private boolean sendNotification(NotificationReqeustVO notificationRequestVO) throws NotificationException {

        boolean notificationResult = false;
        NotificationVO notificationVO = notificationRequestVO.getNotificationVO();
        Endpoint endpoint = notificationRequestVO.getEndpoint();
        String endpointUri = endpoint.getUri();
        String accept = endpoint.getAccept();
        List<KeyValuePair> notifierInfo = endpoint.getNotifierInfo();
        List<KeyValuePair> receiverInfo = endpoint.getReceiverInfo();

        if(endpointUri.startsWith("http")) {
            notificationResult = httpSender.sendNotification(notificationRequestVO.getRequestId(), endpointUri, accept, notifierInfo, receiverInfo, notificationVO);
        } else if(endpointUri.startsWith("mqtt")) {
            notificationResult = mqttSender.sendNotification(notificationRequestVO.getRequestId(), endpointUri, accept, notifierInfo, receiverInfo, notificationVO);
        } else {
            throw new NotificationException(ErrorCode.INVALID_NOTIFICATION_URI, "Invalid notification endpointUri. endpointUri=" + endpointUri);
        }

        return notificationResult;
    }

    @Override
    public Object processBulk(List<NotificationReqeustVO> notificationReqeustVOs) {
        throw new CoreException(ErrorCode.NOT_SUPPORTED_METHOD, "NotificationProcessor not support processBulk.");
    }
}
