package kr.re.keti.sc.dataservicebroker.common.vo;


import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class AASUserPermissionVO {
    private boolean isSuperUser;
    private List<String> resourceIds;
    private List<DataServiceBrokerCode.AclRuleOperationType> operationTypes;

    @Builder
    public AASUserPermissionVO(
            boolean isSuperUser,
            List<String> resourceIds,
            List<DataServiceBrokerCode.AclRuleOperationType> operationTypes
    ) {
        this.isSuperUser = isSuperUser;
        this.resourceIds = resourceIds;
        this.operationTypes = operationTypes;
    }
}
