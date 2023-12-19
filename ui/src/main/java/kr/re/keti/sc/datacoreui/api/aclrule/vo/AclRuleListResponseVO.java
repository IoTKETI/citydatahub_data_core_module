package kr.re.keti.sc.datacoreui.api.aclrule.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

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
public class AclRuleListResponseVO {
	/** Total count */
	private Integer totalCount;
	/** Response of ACL Rule list */
	private List<AclRuleResponseVO> aclRuleResponseVOs;
}
