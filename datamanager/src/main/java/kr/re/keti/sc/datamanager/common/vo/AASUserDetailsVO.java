package kr.re.keti.sc.datamanager.common.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 데이터허브 인증서버로 부터 획득한 JWT 토큰에서 획득 정보 VO 클래스
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AASUserDetailsVO extends kr.re.keti.sc.datamanager.common.vo.AASUserVO implements UserDetails {


    private String username;
    private List<String> resourceIds;
    private DataManagerCode.AclRuleResourceType resourceType;

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(List<String> resourceIds) {
        this.resourceIds = resourceIds;
    }

    public DataManagerCode.AclRuleResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(DataManagerCode.AclRuleResourceType resourceType) {
        this.resourceType = resourceType;
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
