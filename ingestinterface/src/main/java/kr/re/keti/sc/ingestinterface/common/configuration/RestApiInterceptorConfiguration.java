package kr.re.keti.sc.ingestinterface.common.configuration;

import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode;
import kr.re.keti.sc.ingestinterface.common.configuration.security.interceptor.AclInterceptor;
import kr.re.keti.sc.ingestinterface.common.configuration.security.interceptor.ExternelPlatformInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * 인증을 위한 인터셉터 적용여부 설정
 */
@Configuration
public class RestApiInterceptorConfiguration implements WebMvcConfigurer {

    @Value("${security.external.platform.useYn:N}")
    private String securityExternalPlatformAclUseYn;
    @Value("${security.acl.useYn:N}")
    private String securityAclUseYn;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        //0. 플랫폼 인증 기능 쓸 경우, externelPlatformInterceptor 인터셉터 적용
        if (securityExternalPlatformAclUseYn.equals(IngestInterfaceCode.UseYn.YES.getCode())) {
            registry.addInterceptor(externelPlatformInterceptor()).addPathPatterns("/entityOperations/**");
        }

        // 접근제어 기능 사용할 경우, authenticationInterceptor 인터셉터 적용
        if (securityAclUseYn.equals(IngestInterfaceCode.UseYn.YES.getCode())) {
            registry.addInterceptor(authenticationInterceptor()).addPathPatterns("/entities/**", "/temporal/entities/**", "/entityOperations/**");
        }
    }

    @Bean
    public ExternelPlatformInterceptor externelPlatformInterceptor() {
        return new ExternelPlatformInterceptor();
    }

    @Bean
    public AclInterceptor authenticationInterceptor() {
        return new AclInterceptor();
    }



}