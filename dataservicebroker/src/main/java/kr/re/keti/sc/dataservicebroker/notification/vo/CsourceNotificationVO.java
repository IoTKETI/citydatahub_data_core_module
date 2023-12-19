package kr.re.keti.sc.dataservicebroker.notification.vo;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.JsonLdType;
import kr.re.keti.sc.dataservicebroker.common.code.SubscriptionCode.TriggerReason;
import kr.re.keti.sc.dataservicebroker.csource.vo.CsourceRegistrationVO;

/**
 * Notification 전송 데이터 VO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CsourceNotificationVO {
	private String id;
	private String type = JsonLdType.CSOURCE_NOTIFICATION.getCode();
	private String subscriptionId;
	@JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
	private Date notifiedAt;
	private List<CsourceRegistrationVO> data;
	private TriggerReason triggerReason; 

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public String getSubscriptionId() {
		return subscriptionId;
	}
	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
	}
	public Date getNotifiedAt() {
		return notifiedAt;
	}
	public void setNotifiedAt(Date notifiedAt) {
		this.notifiedAt = notifiedAt;
	}
	public List<CsourceRegistrationVO> getData() {
		return data;
	}
	public void setData(List<CsourceRegistrationVO> data) {
		this.data = data;
	}
	public void setType(String type) {
		this.type = type;
	}
	public TriggerReason getTriggerReason() {
		return triggerReason;
	}
	public void setTriggerReason(TriggerReason triggerReason) {
		this.triggerReason = triggerReason;
	}
}
