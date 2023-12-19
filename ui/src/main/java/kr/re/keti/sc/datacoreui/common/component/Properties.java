package kr.re.keti.sc.datacoreui.common.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Properties component class
 * @FileName Properties.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Data
@Component
public class Properties {
	
	@Value("${cityhub.client.clientId}") 
	private String clientId;
	
	@Value("${cityhub.client.clientSecret}") 
	private String clientSecret;
	
	@Value("${cityhub.client.accessTokenUri}") 
	private String accessTokenUri;
	
	@Value("${cityhub.client.userAuthorizationUri}") 
	private String userAuthorizationUri;
	
	@Value("${cityhub.client.publicKeyUri}")
	private String publicKeyUri;
	
	@Value("${cityhub.client.userInfoUri}")
	private String userInfoUri;
	
	@Value("${cityhub.client.logoutUri}")
	private String logoutUri;
	
	@Value("${cityhub.client.redirectUri}") 
	private String redirectUri;
	
	@Value("${spring.security.enabled:Y}") 
	private Boolean springSecurityEnabled;
	
	@Value("${language.code:en}") 
	private String langCd;
}
