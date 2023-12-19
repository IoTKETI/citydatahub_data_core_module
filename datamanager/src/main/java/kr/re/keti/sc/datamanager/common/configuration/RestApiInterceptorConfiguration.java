package kr.re.keti.sc.datamanager.common.configuration;

import kr.re.keti.sc.datamanager.common.code.DataManagerCode;
import kr.re.keti.sc.datamanager.common.configuration.security.interceptor.AclInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * REST API 에 대한 인터셉터 설정
 */
@Configuration
public class RestApiInterceptorConfiguration implements WebMvcConfigurer {


    @Value("${security.acl.useYn:N}")
    private String securityAclUseYn;

//    @Autowired
//    AASSVC aasSVC ;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        /*
            - /datamodels, /subscriptions, /csourceSubscriptions, csourceRegistrations, /datasetset/{datasetId}/flow, /provision/servers는 이번에는 적용 X
            - /entityOperations, /entities에 CUD의 경우 dataset이 있는 경우에만 접근제어 적용
            - /entities, /temporal/entities R의 경우 token으로 허용되는 dataset값들만 조회 (접근제어 적용 항상)
         */
        //0. 인증 기능 쓸 경우, 인증 인터셉터 적용
        if (securityAclUseYn.equals(DataManagerCode.UseYn.YES.getCode())) {
            registry.addInterceptor(authenticationInterceptor()).addPathPatterns("/datasets/**")
            .excludePathPatterns("/datasets/**/flow");
        }
    }


    @Bean
    public AclInterceptor authenticationInterceptor() {
        return new AclInterceptor();
    }


}