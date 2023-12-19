package kr.re.keti.sc.datacoreui.api.verificationhistory.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * This is the VO class used when responding to the verification history.
 * @FileName VerificationHistoryResponseVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VerificationHistoryResponseVO {
	/** Number of cases by quality verification history result */
	VerificationHistoryCountVO verificationHistoryCount; 
	
	/** Quality verification history */
	List<VerificationHistoryVO> verificationHistorys;
}
