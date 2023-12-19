package kr.re.keti.sc.ingestinterface.controller.kafka.producer.vo;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import kr.re.keti.sc.ingestinterface.common.code.Constants;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.Operation;
import kr.re.keti.sc.ingestinterface.common.vo.CommonEntityFullVO;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IngestEventKafkaMessageVO {
	/** Operation */
	private Operation operation; // Mandatory
	/** Content Type */
	private String contentType; // Optional
	/** Entity */
	private CommonEntityFullVO content; // Optional
	/** ingest일시 */
	@JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
	private Date ingestTime; // Optional

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public CommonEntityFullVO getContent() {
		return content;
	}

	public void setContent(CommonEntityFullVO content) {
		this.content = content;
	}

	public Date getIngestTime() {
		return ingestTime;
	}

	public void setIngestTime(Date ingestTime) {
		this.ingestTime = ingestTime;
	}
	
}