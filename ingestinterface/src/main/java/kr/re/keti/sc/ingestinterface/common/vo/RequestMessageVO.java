package kr.re.keti.sc.ingestinterface.common.vo;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import kr.re.keti.sc.ingestinterface.common.code.Constants;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.Operation;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.OperationOption;
import kr.re.keti.sc.ingestinterface.common.serialize.ContentDeserializer;

/**
 * Entity Operation Processing VO class
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestMessageVO {
	/** Operation */
	private Operation operation; // Mandatory
	/** 대상 */
	private String to; // Mandatory
	/** Content Type */
	private String contentType; // Optional
	@JsonDeserialize(using = ContentDeserializer.class)
	/** Entity */
	private String content; // Optional
	/** event발생일시 */
	@JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
	private Date ingestTime; // Optional
	/** Operation options */
	private List<OperationOption> OperationOptions;

	/** Entity Type */
	private String entityType;
	/** ID */
	private String id;
	/** datasetId */
	private String datasetId;

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
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Date getIngestTime() {
		return ingestTime;
	}
	public void setIngestTime(Date ingestTime) {
		this.ingestTime = ingestTime;
	}
	public List<OperationOption> getOperationOptions() {
		return OperationOptions;
	}
	public void setOperationOptions(List<OperationOption> operationOptions) {
		OperationOptions = operationOptions;
	}
	public String getEntityType() {
		return entityType;
	}
	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDatasetId() {
		return datasetId;
	}
	public void setDatasetId(String datasetId) {
		this.datasetId = datasetId;
	}
}