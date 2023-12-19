package kr.re.keti.sc.datacoreui.api.menu.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * This is the VO class used for menu retrieve.
 * @FileName MenuRetrieveVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuRetrieveVO {
	/** Menu ID */
	private String id;
	/** Menu name */
	private String name;
	/** Menu URL */
	private String url;
	/** Parent menu ID */
	private String upMenuId;
	/** Menu sort order */
	private Integer sortOrder;
	/** Enabled (TRUE, FALSE) */
	private Boolean enabled;
	/** Menu level */
	private Integer level;
	/** Language code */
	private String langCd;
}
