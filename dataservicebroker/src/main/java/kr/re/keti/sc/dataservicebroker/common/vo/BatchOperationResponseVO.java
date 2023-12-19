package kr.re.keti.sc.dataservicebroker.common.vo;


import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class BatchOperationResponseVO {
    private HttpStatus statusCode;
    private String responseBody;
}
