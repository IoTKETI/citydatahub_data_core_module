package kr.re.keti.sc.datacoreusertool.notification.vo;

import lombok.Data;

/**
 * WidgetWebSocketRegistVO class
 * @FileName WidgetWebSocketRegistVO.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Data
public class WidgetWebSocketRegistVO {
	private String dashboardId;
	private String widgetId;
	private String userId;
	private String method;
}
