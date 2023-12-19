package kr.re.keti.sc.datacoreui.api.verificationhistory.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

/**
 * This is the VO class used for verification history request.
 * @FileName VerificationHistoryRequestVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class VerificationHistoryRequestVO {
	/** Search start time */
	private String startTime;
	/** Search end time */
	private String endTime;
	/** Data set ID */
	private String datasetId;
	/** Data model ID */
	private String dataModelId;
	/** Entity ID */
	private String entityId;
	/** Quality Verification Results */
	private Boolean verified;
	/** Smart search value */
	private String smartSearchValue;
	/** limit */
	private Integer limit;
	/** offset */
	private Integer offset;
}
