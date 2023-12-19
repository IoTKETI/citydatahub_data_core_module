package kr.re.keti.sc.datacoreui.api.verificationhistory.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * Verification history count VO class
 * @FileName VerificationHistoryCountVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VerificationHistoryCountVO {
	/** Number of normal cases as a result of quality inspection */
	private Integer successCount;
	/** Number of abnormal cases as a result of quality inspection */
	private Integer failureCount;
}
