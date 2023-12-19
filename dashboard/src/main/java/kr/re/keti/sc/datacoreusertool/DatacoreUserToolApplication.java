package kr.re.keti.sc.datacoreusertool;

import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import kr.re.keti.sc.datacoreusertool.common.code.Constants;
import lombok.extern.slf4j.Slf4j;

/**
 * DatacoreUserTool application base class
 * @FileName DatacoreUserToolApplication.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@SpringBootApplication(scanBasePackages = Constants.BASE_PACKAGE, exclude = {DataSourceAutoConfiguration.class})
@Configuration
@EnableWebMvc
@Slf4j
public class DatacoreUserToolApplication {
	
	@Value("${server.timezone:Asia/Seoul}")
	private String timeZone;
	
	private String DEFAULT_TIMEZONE = "Asia/Seoul";

	/**
	 * Init application
	 */
	@PostConstruct
	public void initApplication() {
		try {
			TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
		} catch(Exception e) {
			log.error("initApplication set timezone error.", e);
			TimeZone.setDefault(TimeZone.getTimeZone(DEFAULT_TIMEZONE));
		}
	}

	/**
	 * Main
	 * @param args	args
	 */
    public static void main(String ... args) {
        SpringApplication.run(DatacoreUserToolApplication.class, args);
    }

    /**
     * ServerEndpointExporter Bean object
     * @return	ServerEndpointExporter object
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    /**
     * GET paramater like coordinates=[8,40] coordinates=[xx,xx] <= Add special character handling settings
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
    
    /**
     * DispatcherServlet Bean object
     * @return	DispatcherServlet object
     */
    @Bean
    DispatcherServlet dispatcherServlet () {
        DispatcherServlet ds = new DispatcherServlet();
        ds.setThrowExceptionIfNoHandlerFound(true);
        return ds;
    }
}
