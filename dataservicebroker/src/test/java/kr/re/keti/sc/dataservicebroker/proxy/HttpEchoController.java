package kr.re.keti.sc.dataservicebroker.proxy;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HttpEchoController {

	@PostMapping("/echo")
    public String echo(HttpServletResponse response, @RequestBody String requestBody) throws Exception {
        // HTTP 수신 Body를 그대로 반환
		return requestBody;
    }
}
