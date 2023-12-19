package kr.re.keti.sc.ingestinterface.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import kr.re.keti.sc.ingestinterface.common.code.Constants;

import java.util.Date;
import java.util.HashMap;

/**
 * Ngsi-ld spec attribute VO Class
 */
@SuppressWarnings("serial")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttributeVO  extends HashMap<String, Object> {

    protected String type;

    @JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
    private Date observedAt;
    
    protected String unitCode;

    public String getType() {
        return (String) super.get("type");
    }

    public void setType(String type) {
        super.put("type", type);
    }

    public Date getObservedAt() {
        return (Date) super.get("observedAt");
    }

    public void setObservedAt(Date observedAt) {
        super.put("observedAt", observedAt);
    }
    
    public String getUnitCode() {
        return (String) super.get("unitCode");
    }

    public void setUnitCode(String type) {
        super.put("unitCode", type);
    }

//    @SuppressWarnings("unchecked")
//    public List<String> getContext() {
//        return (List<String>) super.get(DataServiceBrokerCode.DefaultAttributeKey.CONTEXT.getCode());
//    }
//    public void setContext(List<String> context) {
//        super.put(DataServiceBrokerCode.DefaultAttributeKey.CONTEXT.getCode(), context);
//    }
}
