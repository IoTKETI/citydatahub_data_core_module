package kr.re.keti.sc.datacoreui.api.aclrule.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import kr.re.keti.sc.datacoreui.common.code.Constants;
import lombok.Data;

/**
 * AclRuleResponseVO class
 * @FileName AclRuleResponseVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 27.
 * @Author Elvin
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AclRuleResponseVO extends AclRuleVO {
	@JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
	private Date createdAt;
	@JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
    private Date modifiedAt;
	@JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
	private Date provisioningEventTime;
}
