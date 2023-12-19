package kr.re.keti.sc.datamanager.common.vo;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 데이터허브 인증서버로 부터 획득한 JWT 토큰에서 획득 정보 VO 클래스
 */
public class AASUserVO {

    /**
     * "type": "userSystem",
     * "userId": "cityhub08",
     * "nickname": "cityhub08",
     * "email": "skycross@n2m.co.kr",
     * "role": "Marketplace_User",
     * "iat": 1625316127,
     * "exp": 1625319727,
     * "aud": "Ud6WGtFacxrAbbWLHMLO",
     * "iss": "urn:datahub:cityhub:security"
     */
    private String type;
    private String userId;
    private String nickname;
    private String email;
    private String role;
    private String clientId;
    private String issuer;
    private Date expiration;
    private Date issuedAt;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }

    public Date getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Date issuedAt) {
        this.issuedAt = issuedAt;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
