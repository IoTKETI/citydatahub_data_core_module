package kr.re.keti.sc.datacoreusertool.api.widgetdashboard.vo;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * WidgetDashboardFileUIVO class
 * @FileName WidgetDashboardFileUIVO.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WidgetDashboardFileUIVO {
	/** File */
	private MultipartFile file;
	/** WidgetDashboardUI **/
	private String widgetDashboardUI;
}
