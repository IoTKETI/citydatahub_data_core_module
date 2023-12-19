package kr.re.keti.sc.pushagent.notification.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import kr.re.keti.sc.pushagent.common.code.Constants;
import lombok.Data;

import java.util.Date;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationReqeustVO {
    @JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
    private Date requestTime;
    private String requestId;
    private Endpoint endpoint;
    private NotificationVO notificationVO;
}
