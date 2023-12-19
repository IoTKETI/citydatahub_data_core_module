package kr.re.keti.sc.dataservicebroker.common.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AASUserDetailsVO extends kr.re.keti.sc.dataservicebroker.common.vo.AASUserVO implements UserDetails {

    private String username;
    private List<String> resourceIds;
    private DataServiceBrokerCode.AclRuleResourceType resourceType;
    private List<DataServiceBrokerCode.AclRuleOperationType> operationTypes;

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(List<String> resourceIds) {
        this.resourceIds = resourceIds;
    }

    public DataServiceBrokerCode.AclRuleResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(DataServiceBrokerCode.AclRuleResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public List<DataServiceBrokerCode.AclRuleOperationType> getOperationTypes() {
        return operationTypes;
    }

    public void setOperationTypes(List<DataServiceBrokerCode.AclRuleOperationType> operationTypes) {
        this.operationTypes = operationTypes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
