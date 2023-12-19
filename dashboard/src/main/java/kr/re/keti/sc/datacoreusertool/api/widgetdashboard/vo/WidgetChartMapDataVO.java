package kr.re.keti.sc.datacoreusertool.api.widgetdashboard.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import kr.re.keti.sc.datacoreusertool.api.dataservicebroker.vo.CommonEntityListResponseVO;
import lombok.Data;

/**
 * WidgetChartMapDataVO class
 * @FileName WidgetChartMapDataVO.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WidgetChartMapDataVO {
	private String id;
	private String widgetId;
	private String chartType;
	private String dataType;
	private String attributeId;
	private List<CommonEntityListResponseVO> data;
}
