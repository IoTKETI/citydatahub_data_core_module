package kr.re.keti.sc.datacoreui.api.aclrule.vo;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import kr.re.keti.sc.datacoreui.common.code.DataCoreUiCode;
import lombok.Data;

/**
 * AclRuleVO class
 * @FileName AclRuleVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 27.
 * @Author Elvin
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AclRuleVO {
	private String id;
    private String userId;
    private String clientId;
    private String resourceId;
    private DataCoreUiCode.AclRuleResourceType resourceType;
    private DataCoreUiCode.AclRuleCondition condition;
    private List<DataCoreUiCode.AclRuleOperationType> operation;
    
    private String creatorId;
    private String modifierId;
    
    private String provisioningRequestId;
    private Date provisioningEventTime;
}
