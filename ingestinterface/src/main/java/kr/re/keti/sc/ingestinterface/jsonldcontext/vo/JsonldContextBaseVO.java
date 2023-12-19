package kr.re.keti.sc.ingestinterface.jsonldcontext.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode;
import lombok.Data;

import java.util.Date;

/**
 * Jsonld context DB VO class
 */
@Data
public class JsonldContextBaseVO {
    private String url;
    @JsonIgnore
    private String payload;
    @JsonIgnore
    private String refinedPayload;
    private IngestInterfaceCode.JsonldContextKind kind;
    @JsonIgnore
    private Date expireDatetime;
    @JsonIgnore
    private Date createDatetime;
    @JsonIgnore
    private Date modifyDatetime;
}
