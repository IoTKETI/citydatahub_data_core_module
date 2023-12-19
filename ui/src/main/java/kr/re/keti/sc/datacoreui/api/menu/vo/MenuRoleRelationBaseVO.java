package kr.re.keti.sc.datacoreui.api.menu.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import kr.re.keti.sc.datacoreui.common.code.Constants;
import lombok.Data;

/**
 * Menu role relation base VO class
 * @FileName MenuRoleRelationBaseVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuRoleRelationBaseVO {
	/** Menu ID */
	private String menuId;
	/** Menu role ID */
	private String menuRoleId;
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
	/** Modifier ID */
	private String modifierId;
}
