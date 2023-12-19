package kr.re.keti.sc.datacoreui.api.code.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import kr.re.keti.sc.datacoreui.common.code.Constants;
import lombok.Data;

/**
 * Code group base VO class.
 * @FileName CodeGroupBaseVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 22.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodeGroupBaseVO {
	/** Code group ID */
	private String codeGroupId;
	/** Code group name */
	private String codeGroupName;
	/** Code group description */
	private String description;
	/** Enabled (TRUE, FALSE) */
	private Boolean enabled;
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
