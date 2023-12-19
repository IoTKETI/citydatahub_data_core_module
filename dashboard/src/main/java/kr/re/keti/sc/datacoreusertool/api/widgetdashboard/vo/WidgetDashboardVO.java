package kr.re.keti.sc.datacoreusertool.api.widgetdashboard.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * WidgetDashboardVO class
 * @FileName WidgetDashboardVO.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WidgetDashboardVO {
	/** Widget ID */
	private String widgetId;
	/** User ID */
	private String userId;
	/** Dashboard ID */
	private String dashboardId;
	/** Chart type */
	private String chartType;
	/** Chart order */
	private Integer chartOrder;
	/** Chart size */
	private String chartSize;
	/** Data Type */
	private String dataType;
	/** Chart title */
	private String chartTitle;
	/** Chart X-AXIS title */
	private String chartXName;
	/** Chart Y-AXIS title */
	private String chartYName;
	/** Y-AXIS range */
	private String yAxisRange;
	/** Update interval */
	private Integer updateInterval;
	/** Real time update? */
	private Boolean realtimeUpdateEnabled;
	/** Chart attribute */
	private String chartAttribute;
	/** Search Condition */
	private String searchCondition;
	/** Map search condition ID */
	private String mapSearchConditionId;
	/** File */
	private byte[] file;
	/** Extention1 */
	private String extention1;
	/** Extention2 */
	private String extention2;
	/** Create date */
	private String createDatetime;
	/** Update date */
	private String modifyDatetime;
}