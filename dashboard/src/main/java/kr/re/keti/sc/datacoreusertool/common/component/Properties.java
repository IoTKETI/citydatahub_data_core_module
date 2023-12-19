package kr.re.keti.sc.datacoreusertool.common.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Properties component class
 * @FileName Properties.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
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
	
	@Value("${spring.security.enabled}") 
	private Boolean springSecurityEnabled;
	
	@Value("${entity.history.limit}")
	private Integer entityHistoryLimit;
	
	@Value("${entity.history.days}")
	private Integer entityHistoryDays;
	
	@Value("${chart.time.format}")
	private String chartTimeFormat;
	
	@Value("${google.api.key}")
	private String googleApiKey;
	
	@Value("${language.code}")
	private String languageCode;
}