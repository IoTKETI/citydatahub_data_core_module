package kr.re.keti.sc.datacoreusertool.api.subscription.vo;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * SubscriptionUIVO class
 * @FileName SubscriptionUIVO.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubscriptionUIVO {
	/** DataModel */
	private String typeUri;
	/** Entity ID */
	private String id;
	/** Attributes */
	private List<String> attrs;
	/** Expires Date */
	private Date expires;
	/** Endpoint URI */
	private String endpointUri;
	/** Widget ID */
	private String widgetId;
	/** Dashboard ID */
	private String dashboardId;
}
