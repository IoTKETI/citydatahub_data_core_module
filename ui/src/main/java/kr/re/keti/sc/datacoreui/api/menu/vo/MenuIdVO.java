package kr.re.keti.sc.datacoreui.api.menu.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * Menu ID VO class
 * @FileName MenuIdVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuIdVO {
	/** Menu ID */
	private String id;
	/** Menu name */
	private String name;
}
