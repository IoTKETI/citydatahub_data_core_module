package kr.re.keti.sc.pushagent;


import kr.re.keti.sc.pushagent.common.code.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication(scanBasePackages = Constants.BASE_PACKAGE, exclude = {DataSourceAutoConfiguration.class})
@Configuration
@Slf4j
public class PushAgentApplication {


    public static void main(String[] args) {
        SpringApplication.run(PushAgentApplication.class, args);
    }

}
