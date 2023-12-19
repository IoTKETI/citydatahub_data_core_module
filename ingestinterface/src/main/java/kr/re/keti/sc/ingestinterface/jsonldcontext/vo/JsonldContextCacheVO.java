package kr.re.keti.sc.ingestinterface.jsonldcontext.vo;

import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * Jsonld context cache VO class
 */
@Data
public class JsonldContextCacheVO {
    private String url;
    private Map<String, String> contextFlatMap;
    private Date expireDatetime;
}
