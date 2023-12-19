package kr.re.keti.sc.dataservicebroker.jsonldcontext.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class JsonldContextBaseVO {
    private String url;
    @JsonIgnore
    private String payload;
    @JsonIgnore
    private String refinedPayload;
    private DataServiceBrokerCode.JsonldContextKind kind;
    @JsonIgnore
    private Date expireDatetime;
    @JsonIgnore
    private Date createDatetime;
    @JsonIgnore
    private Date modifyDatetime;
}
