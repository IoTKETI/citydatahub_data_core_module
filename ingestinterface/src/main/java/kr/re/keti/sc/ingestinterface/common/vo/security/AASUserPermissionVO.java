package kr.re.keti.sc.ingestinterface.common.vo.security;


import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.AclRuleOperationType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class AASUserPermissionVO {
    private boolean isSuperUser;
    private List<String> resourceIds;
    private List<AclRuleOperationType> operationTypes;

    @Builder
    public AASUserPermissionVO(
            boolean isSuperUser,
            List<String> resourceIds,
            List<AclRuleOperationType> operationTypes
    ) {
        this.isSuperUser = isSuperUser;
        this.resourceIds = resourceIds;
        this.operationTypes = operationTypes;
    }
}
