package kr.re.keti.sc.datamanager.acl.rule.vo;

import com.fasterxml.jackson.annotation.*;
import kr.re.keti.sc.datamanager.common.code.Constants;
import kr.re.keti.sc.datamanager.common.vo.PageRequest;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;
import java.util.List;

import static kr.re.keti.sc.datamanager.common.code.DataManagerCode.*;

/**
 * Access control policy rule domain class
 */
@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AclRuleVO extends PageRequest {

    private String id;
    private String userId;
    private String clientId;
    private String resourceId;
    private AclRuleResourceType resourceType;
    private AclRuleCondition condition;
    private List<AclRuleOperationType> operation;

    @JsonProperty("createdAt")
    @JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
    private Date createDatetime;
    private String creatorId;
    @JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
    @JsonProperty("modifiedAt")
    private Date modifyDatetime;
    private String modifierId;

    @JsonIgnore
    private String provisioningRequestId;
    @JsonIgnore
    private Date provisioningEventTime;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
