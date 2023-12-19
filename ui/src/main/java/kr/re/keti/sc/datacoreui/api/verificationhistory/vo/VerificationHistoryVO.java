package kr.re.keti.sc.datacoreui.api.verificationhistory.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * Verification history VO class
 * @FileName VerificationHistoryVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerificationHistoryVO {
	/** Quality inspection history identification serial number */
	private Long seq;
	/** Quality inspection time */
	private String testTime;
	/** Data set ID */
	private String datasetId;
	/** Data model ID */
	private String dataModelId;
	/** Entity ID */
	private String entityId;
	/** Quality Verification Results */
	private Boolean verified;
	/** Quality check error code */
	private String errorCode;
	/** Detailed error message as a result of quality inspection */
	private String errorCause;
	/** Quality inspection failure original data */
	private String data;
	/** Creation time */
	private String createdAt;
}
