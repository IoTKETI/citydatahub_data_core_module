package kr.re.keti.sc.dataservicebroker;

import org.apache.catalina.connector.Connector;
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

// @SpringBootApplication(
//   scanBasePackages = "kr.re.keti.sc.dataservicebroker.proxy",
//   exclude = { DataSourceAutoConfiguration.class }
// )
// @Configuration
@EnableWebMvc
public class TestApplication {

  public static void main(String[] args) {
    // application-test.yml 을 로딩하기 위한 설정
    System.setProperty("spring.profiles.active", "test");
    SpringApplication.run(TestApplication.class);
  }

  /**
   * coordinates=[8,40]와 같이 GET paramater에  coordinates=[xx,xx] <= 특수문자 처리 설정 추가
   * @return
   */
  @Bean
  public ConfigurableServletWebServerFactory webServerFactory() {
    TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
    factory.addConnectorCustomizers(
      new TomcatConnectorCustomizer() {
        @Override
        public void customize(Connector connector) {
          connector.setProperty("relaxedQueryChars", "|{}[]");
        }
      }
    );
    return factory;
  }

  @Bean
  DispatcherServlet dispatcherServlet() {
    DispatcherServlet ds = new DispatcherServlet();
    ds.setThrowExceptionIfNoHandlerFound(true);
    return ds;
  }
}
