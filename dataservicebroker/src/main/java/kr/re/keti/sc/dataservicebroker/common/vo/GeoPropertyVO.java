package kr.re.keti.sc.dataservicebroker.common.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeoPropertyVO extends AttributeVO {

    public GeoPropertyVO() {
        this.setType(DataServiceBrokerCode.AttributeType.GEO_PROPERTY.getCode());
    }

    public Object getValue() {
        return super.get("value");
    }

    public void setValue(Object value) {
        super.put("value", value);
    }

}
