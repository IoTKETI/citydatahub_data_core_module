package kr.re.keti.sc.dataservicebroker.entities.controller.kafka.producer.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.notification.vo.NotificationVO;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionVO.NotificationParams.Endpoint;
import lombok.Data;

@Data
public class NotificationReqeustVO {
    @JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
    private Date requestTime;
    private String requestId;
    private Endpoint endpoint;
    private NotificationVO notificationVO;
}
