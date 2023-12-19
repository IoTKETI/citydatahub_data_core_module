package kr.re.keti.sc.ingestinterface.common.configuration.security;

import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode;
import kr.re.keti.sc.ingestinterface.common.configuration.security.filter.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {


    @Autowired
    private JwtRequestFilter jwtRequestFilter;
    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Value("${security.external.platform.useYn:N}")
    private String securityExternalPlatformUseYn;
    @Value("${security.acl.useYn:N}")
    private String securityAclUseYn;



    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {

        //접근제어 또는 외부플랫폼인증 적용 시, 처리
        if (securityAclUseYn.equals(IngestInterfaceCode.UseYn.YES.getCode())
            || securityExternalPlatformUseYn.equals(IngestInterfaceCode.UseYn.YES.getCode())) {

            // Enable CORS and disable CSRF
            httpSecurity = httpSecurity.cors().and().csrf().disable();

            // Set session management to stateless
            httpSecurity = httpSecurity
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and();

            // Set permissions on endpoints
            httpSecurity.authorizeRequests()
                    // Our public endpoints
                    .antMatchers("/subscriptions/**").permitAll()
                    .antMatchers("/authenticate").permitAll()
                    .antMatchers("/datamodels").permitAll()
                    .antMatchers("/csourceSubscriptions/**").permitAll()
                    .antMatchers("/csourceRegistrations/**").permitAll()
                    .antMatchers("/datasets/**/flow").permitAll()
                    .antMatchers("/provision/**").permitAll()
                    .antMatchers("/provision/servers/**").permitAll()
                    .antMatchers("/externalplatform/**").permitAll()
                    .antMatchers("/verificationHistory/**").permitAll()
                    .anyRequest().authenticated()
                    .and()
                    .exceptionHandling()
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint);

            // Add JWT token filter
            httpSecurity.addFilterBefore(
                    jwtRequestFilter,
                    UsernamePasswordAuthenticationFilter.class
            );

        } else {
            httpSecurity.csrf().disable()
                    .authorizeRequests().antMatchers("/**").permitAll();
        }

    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

}
