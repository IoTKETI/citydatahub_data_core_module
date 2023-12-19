package kr.re.keti.sc.datacoreui.api.verificationhistory.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * Verification history list VO class
 * @FileName VerificationHistoryListVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerificationHistoryListVO {
	/** Total count */
	private Integer totalCount;
	/** Quality inspection history list */
	private List<VerificationHistoryVO> verificationHistoryVOs;
}
