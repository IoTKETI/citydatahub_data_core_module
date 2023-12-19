package kr.re.keti.sc.datacoreusertool.api.widgetdashboard.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * WidgetSessionVO class
 * @FileName WidgetSessionVO.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 4. 29.
 * @Author Elvin
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WidgetSessionVO {	
	/** Widget ID */
	private String widgetId;
	/** User ID */
	private String userId;
	/** Session ID */
	private String sessionId;
	/** Chart type */
	private String chartType;
	/** Data Type */
	private String dataType;
	/** Retrieve period */
	private int period;
	/** Multiple entities or not */
	private boolean isMultiEntities;
	/** Legend */
	private String legend;
	/** X Axis Unit */
	private int xAxisUnit;
	
	public WidgetSessionVO() {
		// Default x-axis unit
		xAxisUnit = 1;
	}
}