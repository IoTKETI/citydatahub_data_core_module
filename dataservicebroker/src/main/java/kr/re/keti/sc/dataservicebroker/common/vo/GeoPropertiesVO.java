package kr.re.keti.sc.dataservicebroker.common.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeoPropertiesVO extends AttributeVO {

    public GeoPropertiesVO() {
        this.setType(DataServiceBrokerCode.AttributeType.GEO_PROPERTY.getCode());
    }

    public Object getValue() {
        return super.get("values");
    }

    public void setValue(Object value) {
        super.put("values", value);
    }

}
