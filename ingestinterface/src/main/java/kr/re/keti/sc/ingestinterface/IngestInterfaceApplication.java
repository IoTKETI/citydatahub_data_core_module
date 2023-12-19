package kr.re.keti.sc.ingestinterface;

import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.AbstractHttp11Protocol;
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
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import kr.re.keti.sc.ingestinterface.common.code.Constants;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication(scanBasePackages = Constants.BASE_PACKAGE, exclude = {DataSourceAutoConfiguration.class})
@Configuration
@EnableWebMvc
@EnableDiscoveryClient
@Slf4j
public class IngestInterfaceApplication {

    @Value("${server.http.port}")
    private String httpPort;

    @Value("${embedded_tomcat.keep-alive.maxKeepAliveRequests}")
    private Integer maxKeepAliveRequests;

    @Value("${embedded_tomcat.keep-alive.keepAliveTimeout}")
    private Integer keepAliveTimeout;

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
        SpringApplication.run(IngestInterfaceApplication.class, args);
    }

    /**
     * coordinates=[8,40]와 같이 GET paramater에  coordinates=[xx,xx] 특수문자 처리 설정 추가
     *
     * @return
     */
    @Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addAdditionalTomcatConnectors(createStandardConnector());

        factory.addConnectorCustomizers(new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector) {
                commonEmbeddedTomcatSetting(connector);
            }
        });


        return factory;
    }

    @Bean
    DispatcherServlet dispatcherServlet() {
        DispatcherServlet ds = new DispatcherServlet();
        ds.setThrowExceptionIfNoHandlerFound(true);
        return ds;
    }


    /**
     * HTTP용 포트 추가
     * @return
     */
    private Connector createStandardConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setPort(Integer.parseInt(httpPort)); // 포트 설정(https랑 달라야함)
        commonEmbeddedTomcatSetting(connector);

        return connector;
    }

    private Connector commonEmbeddedTomcatSetting(Connector connector) {

        // 파라미터 특수 문자 입력이 필터링 안되게 처리
        connector.setProperty("relaxedQueryChars", "|{}[]");
        ProtocolHandler protocolHandler = connector.getProtocolHandler();
        if (protocolHandler instanceof AbstractHttp11Protocol) {
            applyProperties((AbstractHttp11Protocol) protocolHandler);
        }

        return connector;
    }

    /**
     * keep-alive 정보 설정
     * (참고 : https://bcho.tistory.com/788)
     *
     * @param protocolHandler
     */
    private void applyProperties(AbstractHttp11Protocol protocolHandler) {

        //protocolHandler.setConnectionTimeout(3000);    // Connection의 타임아웃
        protocolHandler.setMaxKeepAliveRequests(maxKeepAliveRequests);   //Kepp Alive를 사용하지 않기 위해서 값을 1로 줌
        protocolHandler.setKeepAliveTimeout(keepAliveTimeout);
    }
}
