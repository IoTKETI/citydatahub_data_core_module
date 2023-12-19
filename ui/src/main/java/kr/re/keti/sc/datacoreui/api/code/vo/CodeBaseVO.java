package kr.re.keti.sc.datacoreui.api.code.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import kr.re.keti.sc.datacoreui.common.code.Constants;
import lombok.Data;

/**
 * Code base VO class.
 * @FileName CodeBaseVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 22.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodeBaseVO {
	/** Code group ID */
	private String codeGroupId;
	/** Code ID */
	private String codeId;
	/** Language code */
	private String langCd;
	/** Code name */
	private String codeName;
	/** Sort order */
	private Integer sortOrder;
	/** Enabled (TRUE, FALSE) */
	private Boolean enabled;
	/** Code Description */
	private String description;
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
