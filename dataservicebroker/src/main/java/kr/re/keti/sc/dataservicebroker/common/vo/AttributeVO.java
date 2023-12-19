package kr.re.keti.sc.dataservicebroker.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.PropertyKey;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

@SuppressWarnings("serial")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttributeVO  extends HashMap<String, Object> {

    protected String type;
    @JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
    private Date observedAt;
    protected String unitCode;
    
    public String getType() {
        return (String) super.get(PropertyKey.TYPE.getCode());
    }

    public void setType(String type) {
        super.put(PropertyKey.TYPE.getCode(), type);
    }

    public Date getObservedAt() {
        return (Date) super.get(PropertyKey.OBSERVED_AT.getCode());
    }

    public void setObservedAt(Date observedAt) {
        super.put(PropertyKey.OBSERVED_AT.getCode(), observedAt);
    }

    public String getUnitCode() {
        return (String) super.get(PropertyKey.UNIT_CODE.getCode());
    }

    public void setUnitCode(String unitCode) {
        super.put(PropertyKey.UNIT_CODE.getCode(), unitCode);
    }
    
    public Date getCreatedAt() {
        return (Date) super.get(PropertyKey.CREATED_AT.getCode());
    }

    public void setCreatedAt(Date observedAt) {
        super.put(PropertyKey.CREATED_AT.getCode(), observedAt);
    }
    
    public Date getModifiedAt() {
        return (Date) super.get(PropertyKey.MODIFIED_AT.getCode());
    }

    public void setModifiedAt(Date observedAt) {
        super.put(PropertyKey.MODIFIED_AT.getCode(), observedAt);
    }
    
//    @SuppressWarnings("unchecked")
//    public List<String> getContext() {
//        return (List<String>) super.get(DataServiceBrokerCode.DefaultAttributeKey.CONTEXT.getCode());
//    }
//    public void setContext(List<String> context) {
//        super.put(DataServiceBrokerCode.DefaultAttributeKey.CONTEXT.getCode(), context);
//    }
}
