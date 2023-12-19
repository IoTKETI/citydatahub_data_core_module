package kr.re.keti.sc.datacoreusertool.notification.vo;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import kr.re.keti.sc.datacoreusertool.api.dataservicebroker.vo.CommonEntityVO;
import kr.re.keti.sc.datacoreusertool.common.code.Constants;
import lombok.Data;

/**
 * NotificationVO class
 * @FileName NotificationVO.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationVO {
	private String id;
	private String widgetId;
	private String chartType;
	private String attributeId;
	private String subscriptionId;
	@JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
	private Date notifiedAt;
	private List<CommonEntityVO> data;
}
