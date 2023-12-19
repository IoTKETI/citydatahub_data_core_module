package kr.re.keti.sc.datacoreui;

import org.apache.catalina.connector.Connector;
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

import kr.re.keti.sc.datacoreui.common.code.Constants;

/**
 * Data core UI application
 * @FileName DataCoreUiApplication.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@SpringBootApplication(scanBasePackages = Constants.BASE_PACKAGE, exclude = {DataSourceAutoConfiguration.class})
@Configuration
@EnableWebMvc
@EnableDiscoveryClient
public class DataCoreUiApplication {

	/**
	 * Main function
	 * @param args
	 */
    public static void main(String[] args) {
        SpringApplication.run(DataCoreUiApplication.class, args);
    }

    /**
     * In case of coordinates=[8,40], GET paramater: coordinates=[xx,xx] <= Special character handling settings
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
     * DispatcherServlet
     * @return
     */
    @Bean
    DispatcherServlet dispatcherServlet () {
        DispatcherServlet ds = new DispatcherServlet();
        ds.setThrowExceptionIfNoHandlerFound(true);
        return ds;
    }
}
