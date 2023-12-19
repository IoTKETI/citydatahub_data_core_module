package kr.re.keti.sc.datacoreui.api.menu.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * This is the VO class used for menu role retrieve.
 * @FileName MenuRoleRetrieveVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuRoleRetrieveVO {
	/** Menu role name */
	private String name;
	/** Enabled (TRUE, FALSE) */
	private Boolean enabled;
}
