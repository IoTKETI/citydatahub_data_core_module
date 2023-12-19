package kr.re.keti.sc.ingestinterface.provisioning.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import kr.re.keti.sc.ingestinterface.common.code.Constants;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.ProvisionEventType;

/**
 * Provisining notification VO class
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProvisionNotiVO {
	private String requestId;
	@JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
	private Date eventTime;
	private String to;
	private ProvisionEventType eventType;
	private String data;

	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public Date getEventTime() {
		return eventTime;
	}
	public void setEventTime(Date eventTime) {
		this.eventTime = eventTime;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public ProvisionEventType getEventType() {
		return eventType;
	}
	public void setEventType(ProvisionEventType eventType) {
		this.eventType = eventType;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
}
