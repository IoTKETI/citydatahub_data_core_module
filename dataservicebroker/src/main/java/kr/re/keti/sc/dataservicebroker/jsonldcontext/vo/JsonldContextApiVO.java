package kr.re.keti.sc.dataservicebroker.jsonldcontext.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import lombok.Data;

import java.util.Date;

@Data
public class JsonldContextApiVO {
    private String url;
    private DataServiceBrokerCode.JsonldContextKind kind;
    @JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
    private Date timestamp;
}
