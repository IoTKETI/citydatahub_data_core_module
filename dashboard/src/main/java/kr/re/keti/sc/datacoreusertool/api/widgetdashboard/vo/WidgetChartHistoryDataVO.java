package kr.re.keti.sc.datacoreusertool.api.widgetdashboard.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import kr.re.keti.sc.datacoreusertool.api.dataservicebroker.vo.CommonEntityVO;
import lombok.Data;

/**
 * WidgetChartHistoryDataVO class
 * @FileName WidgetChartHistoryDataVO.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WidgetChartHistoryDataVO {
	private Integer totalCount;
	private String id;
	private String widgetId;
	private String chartType;
	private String dataType;
	private String attributeId;
	private List<CommonEntityVO> data;
	private List<String> entityIds;
	private List<String> legendvalues;
}
