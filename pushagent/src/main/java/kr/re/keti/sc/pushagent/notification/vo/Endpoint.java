package kr.re.keti.sc.pushagent.notification.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Endpoint {
    private String uri;
    private String accept;
    private List<KeyValuePair> receiverInfo;
    private List<KeyValuePair> notifierInfo;
}
