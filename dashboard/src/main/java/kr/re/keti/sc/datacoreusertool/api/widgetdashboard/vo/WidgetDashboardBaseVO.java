package kr.re.keti.sc.datacoreusertool.api.widgetdashboard.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * WidgetDashboardBaseVO class
 * @FileName WidgetDashboardBaseVO.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WidgetDashboardBaseVO {
	/** Dashboard ID */
	private String dashboardId;
	/** User ID */
	private String userId;
	/** Dashboard Title */
	private String dashboardName;
	/** Create date */
	private String createDatetime;
	/** Update date */
	private String modifyDatetime;
}
