package kr.re.keti.sc.datamanager.common.configuration.security;

import kr.re.keti.sc.datamanager.common.code.DataManagerCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
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
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {


    @Autowired
    private JwtRequestFilter jwtRequestFilter;
    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;


    @Value("${security.acl.useYn:N}")
    private String securityAclUseYn;

    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {

        if (securityAclUseYn.equals(DataManagerCode.UseYn.YES.getCode())) {
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
