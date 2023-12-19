package kr.re.keti.sc.datacoreui.api.verificationhistory.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * This is the VO class used when responding to the list of verification history.
 * @FileName VerificationHistoryListResponseVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VerificationHistoryListResponseVO {
	/** Total count */
	private Integer totalCount;
	/** Number of cases and quality verification history by quality verification history result */
	VerificationHistoryResponseVO verificationHistoryResponseVO;
}
