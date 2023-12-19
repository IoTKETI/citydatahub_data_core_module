package kr.re.keti.sc.pushagent.notification.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import kr.re.keti.sc.pushagent.common.code.Constants;
import kr.re.keti.sc.pushagent.common.code.DataServiceBrokerCode;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;
import java.util.List;

/**
 * Notification 전송 데이터 VO
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationVO {
	private String id;
	private String type = DataServiceBrokerCode.JsonLdType.NOTIFICATION.getCode();
	private String subscriptionId;
	@JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
	private Date notifiedAt;
	private List<CommonEntityVO> data;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}
}
