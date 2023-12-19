package kr.re.keti.sc.datacoreui.api.externalplatformauth.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * This is the VO class used for external platform auth retrieve.
 * @FileName ExternalPlatformAuthRetrieveVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 23.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalPlatformAuthRetrieveVO {
	/** limit */
	private Integer limit;
	/** offset */
	private Integer offset;
}
