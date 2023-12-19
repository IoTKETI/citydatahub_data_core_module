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
import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityVO;

/**
 * Notification 전송 데이터 VO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationVO {
	private String id;
	private String type = JsonLdType.NOTIFICATION.getCode();
	private String subscriptionId;
	@JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
	private Date notifiedAt;
	private List<CommonEntityVO> data;

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
	public List<CommonEntityVO> getData() {
		return data;
	}
	public void setData(List<CommonEntityVO> data) {
		this.data = data;
	}
	public void setType(String type) {
		this.type = type;
	}
}
