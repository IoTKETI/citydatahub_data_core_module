package kr.re.keti.sc.datamanager.provisioning.vo;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonFormat;

import kr.re.keti.sc.datamanager.common.code.Constants;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ProvisionEventType;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ProvisionServerType;
import kr.re.keti.sc.datamanager.common.exception.ProvisionException;

/**
 * Provisining 처리 결과 VO 클래스
 */
public class ProvisionResultVO {
	private String requestId;
	@JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
	private Date eventTime;
	private String provisionServerId;
	private ProvisionServerType provisionServerType;
	private ProvisionEventType provisionEventType;
	private Boolean result;
	private ProvisionException ProvisionException;

	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
	}
	
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
	public String getProvisionServerId() {
		return provisionServerId;
	}
	public void setProvisionServerId(String provisionServerId) {
		this.provisionServerId = provisionServerId;
	}
	public ProvisionServerType getProvisionServerType() {
		return provisionServerType;
	}
	public void setProvisionServerType(ProvisionServerType provisionServerType) {
		this.provisionServerType = provisionServerType;
	}
	public ProvisionEventType getProvisionEventType() {
		return provisionEventType;
	}
	public void setProvisionEventType(ProvisionEventType provisionEventType) {
		this.provisionEventType = provisionEventType;
	}
	public Boolean getResult() {
		return result;
	}
	public void setResult(Boolean result) {
		this.result = result;
	}
	public ProvisionException getProvisionException() {
		return ProvisionException;
	}
	public void setProvisionException(ProvisionException provisionException) {
		ProvisionException = provisionException;
	}
}
