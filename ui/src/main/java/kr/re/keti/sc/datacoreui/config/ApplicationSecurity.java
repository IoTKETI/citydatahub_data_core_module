package kr.re.keti.sc.datacoreui.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import kr.re.keti.sc.datacoreui.api.menu.service.MenuSVC;
import kr.re.keti.sc.datacoreui.common.component.Properties;
import kr.re.keti.sc.datacoreui.security.filter.JwtAuthenticationFilter;
import kr.re.keti.sc.datacoreui.security.filter.JwtAuthorizationFilter;
import kr.re.keti.sc.datacoreui.security.handler.DataCoreUIAuthenticationEntryPoint;
import kr.re.keti.sc.datacoreui.security.service.DataCoreUiSVC;

/**
 * Class for application security
 * @FileName ApplicationSecurity.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Configuration
@EnableOAuth2Sso
public class ApplicationSecurity extends WebSecurityConfigurerAdapter {
	
	@Autowired
	OAuth2ClientContext oauth2ClientContext;
	
	@Autowired
	private MenuSVC menuSVC;
	
	@Autowired
	private DataCoreUiSVC dataCoreUiSVC;
	
	@Autowired
	private Properties properties;
	
	/**
	 * Set up application http security configuration.
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		if (properties.getSpringSecurityEnabled()) {
			http.antMatcher("/**")
			.authorizeRequests()
			.antMatchers("/error**")
			.permitAll()
			.anyRequest()
			.authenticated()
			.and().csrf().disable()
			.addFilterAfter(new JwtAuthenticationFilter(authenticationEntryPoint(), dataCoreUiSVC), BasicAuthenticationFilter.class)
			.addFilterAfter(new JwtAuthorizationFilter(authenticationEntryPoint(), menuSVC, dataCoreUiSVC), JwtAuthenticationFilter.class);
		} else {
			http.csrf().disable().
			authorizeRequests()
			.anyRequest().permitAll();
		}
	}
	
	/**
	 * Set up application web security configuration.
	 */
	@Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(
        		"/accesstoken"
        		,"/logout");
    }
	
	/**
	 * Create bean for DataCoreUIAuthenticationEntryPoint class
	 * @return
	 */
	@Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new DataCoreUIAuthenticationEntryPoint();
    }
}
