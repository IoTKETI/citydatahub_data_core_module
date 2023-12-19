package kr.re.keti.sc.dataservicebroker.jsonldcontext.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class JsonldContextCacheVO {
    private String url;
    private Map<String, String> contextFlatMap;
    private Date expireDatetime;
}
