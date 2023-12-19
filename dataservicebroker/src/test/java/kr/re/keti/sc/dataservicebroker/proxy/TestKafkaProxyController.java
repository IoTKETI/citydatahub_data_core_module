package kr.re.keti.sc.dataservicebroker.proxy;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestKafkaProxyController {

	@Autowired
    private TestKafkaProducer producer;

    @PostMapping("/test/kafka")
    public void test(HttpServletResponse response, @RequestBody String requestBody) throws Exception {
        // HTTP 수신 Body를 그대로 KAFKA로 전송
        producer.send(requestBody.getBytes());
    }
}
