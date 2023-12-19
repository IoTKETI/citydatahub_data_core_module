package kr.re.keti.sc.dataservicebroker.common.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PropertiesVO extends AttributeVO {

    public PropertiesVO() {
        this.setType(DataServiceBrokerCode.AttributeType.PROPERTY.getCode());
    }

    public Object getValue() {
        return super.get("values");
    }

    public void setValue(Object value) {
        super.put("values", value);

    }


}
