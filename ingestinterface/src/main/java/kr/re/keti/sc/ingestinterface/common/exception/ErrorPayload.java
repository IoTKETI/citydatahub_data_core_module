package kr.re.keti.sc.ingestinterface.common.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * HTTP Response 오류 응답 시 payload 클래스
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorPayload {

	private String type;
	private String title;
	private String detail;
	private String debugMessage;
	
    public ErrorPayload(String type, String title, String detail) {
        this.type = type;
        this.title = title;
        this.detail = detail;
    }

    public ErrorPayload(String type, String title, String detail, String debugMessage) {
    	this.type = type;
        this.title = title;
        this.detail = detail;
        this.debugMessage = debugMessage;
    }

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getDebugMessage() {
		return debugMessage;
	}

	public void setDebugMessage(String debugMessage) {
		this.debugMessage = debugMessage;
	}
   
}
