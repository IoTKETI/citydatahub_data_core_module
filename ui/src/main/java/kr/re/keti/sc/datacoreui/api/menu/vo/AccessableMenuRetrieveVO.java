package kr.re.keti.sc.datacoreui.api.menu.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * This is the VO class used for accessable menu retrieve.
 * @FileName AccessableMenuRetrieveVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 6. 12.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessableMenuRetrieveVO {
	/** MenuRole ID */
	private String menuRoleId;
	/** Language code */
	private String langCd;
}
