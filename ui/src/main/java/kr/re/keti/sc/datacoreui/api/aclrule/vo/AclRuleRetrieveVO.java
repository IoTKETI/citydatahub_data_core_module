package kr.re.keti.sc.datacoreui.api.aclrule.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * AclRuleRetrieveVO class
 * @FileName AclRuleRetrieveVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 27.
 * @Author Elvin
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AclRuleRetrieveVO extends AclRuleVO {
	private Integer offset;
	private Integer limit;
}
