package kr.re.keti.sc.datacoreui.api.menu.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import kr.re.keti.sc.datacoreui.common.code.Constants;
import lombok.Data;

/**
 * Menu base VO class
 * @FileName MenuBaseVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuBaseVO {
	/** Menu ID */
	private String id;
	/** Menu name */
	private String name;
	/** Menu URL */
	private String url;
	/** Parent menu ID */
	private String upMenuId;
	/** Menu sort order */
	private Integer sortOrder;
	/** Enabled (TRUE, FALSE) */
	private Boolean enabled;
	/** Menu level */
	private Integer level;
	/** Language code */
	private String langCd;
	/** Creation date */
	@JsonProperty("createdAt")
	@JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT, timezone = Constants.CONTENT_DATE_TIMEZONE)
	private Date createDatetime;
	/** Creator ID */
	private String creatorId;
	/** Modified date */
	@JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT, timezone = Constants.CONTENT_DATE_TIMEZONE)
	@JsonProperty("modifiedAt")
	private Date modifyDatetime;
	/** Modifier date */
	private String modifierId;
}
