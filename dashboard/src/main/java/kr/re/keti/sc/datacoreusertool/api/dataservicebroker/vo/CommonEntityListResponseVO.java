package kr.re.keti.sc.datacoreusertool.api.dataservicebroker.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import kr.re.keti.sc.datacoreusertool.common.vo.ClientExceptionPayloadVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * CommonEntityListResponseVO class
 * @FileName CommonEntityListResponseVO.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonEntityListResponseVO extends ClientExceptionPayloadVO {
	/** Total count */
	private Integer totalCount;
	/** Attribute name(Label)*/
	private List<String> attrsLabel;
	/** Entity list */
	private List<CommonEntityVO> commonEntityVOs;
	/** Widget ID */
	private String widgetId;
	/** Attribute ID */
	private String attributeId;
	/** Chart Type */
	private String chartType;
}
