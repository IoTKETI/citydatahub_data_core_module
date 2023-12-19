package kr.re.keti.sc.dataservicebroker.proxy;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.Operation;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestRequestMessageVO {
	/** 요청아이디 */
	private String requestId; // Optional
	/** E2E요청아이디 */
	private String e2eRequestId; // Optional
	/** Operation */
	private Operation operation; // Mandatory
	/** 대상 */
	private String to; // Mandatory
	/** Content Type */
	private String contentType; // Optional
	/** Entity */
	private Object content; // Optional
	/** Owner */
	private String owner; // Optional
	/** event발생일시 */
	@JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
	private Date eventTime; // Optional

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public String getE2eRequestId() {
		return e2eRequestId;
	}
	public void setE2eRequestId(String e2eRequestId) {
		this.e2eRequestId = e2eRequestId;
	}
	public Operation getOperation() {
		return operation;
	}
	public void setOperation(Operation operation) {
		this.operation = operation;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public Object getContent() {
		return content;
	}
	public void setContent(Object content) {
		this.content = content;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public Date getEventTime() {
		return eventTime;
	}
	public void setEventTime(Date eventTime) {
		this.eventTime = eventTime;
	}
}