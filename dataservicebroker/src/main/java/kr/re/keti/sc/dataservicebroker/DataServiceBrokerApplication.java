package kr.re.keti.sc.dataservicebroker;

import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.apache.catalina.connector.Connector;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication(scanBasePackages = Constants.BASE_PACKAGE, exclude = {DataSourceAutoConfiguration.class})
@Configuration
@EnableWebMvc
@EnableBatchProcessing
@EnableScheduling
@EnableDiscoveryClient
@Slf4j
public class DataServiceBrokerApplication {

	@Value("${server.timezone:Asia/Seoul}")
	private String timeZone;
	
	private String DEFAULT_TIMEZONE = "Asia/Seoul";

	@PostConstruct
	public void initApplication() {
		try {
			TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
		} catch(Exception e) {
			log.error("initApplication set timezone error.", e);
			TimeZone.setDefault(TimeZone.getTimeZone(DEFAULT_TIMEZONE));
		}
	}
	
    public static void main(String[] args) {
        SpringApplication.run(DataServiceBrokerApplication.class, args);
    }

    /**
     * coordinates=[8,40]와 같이 GET paramater에  coordinates=[xx,xx] <= 특수문자 처리 설정 추가
     * @return
     */
    @Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers(new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector) {
                connector.setProperty("relaxedQueryChars", "|{}[]");
            }
        });
        return factory;
    }

    @Bean
    DispatcherServlet dispatcherServlet () {
        DispatcherServlet ds = new DispatcherServlet();
        ds.setThrowExceptionIfNoHandlerFound(true);
        return ds;
    }
}
