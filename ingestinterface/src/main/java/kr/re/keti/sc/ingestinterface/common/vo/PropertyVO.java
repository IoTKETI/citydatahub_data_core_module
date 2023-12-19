package kr.re.keti.sc.ingestinterface.common.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode;

/**
 * Ngsi-ld PropertyVO VO class
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PropertyVO extends AttributeVO {

    public PropertyVO() {
        this.setType(IngestInterfaceCode.AttributeType.PROPERTY.getCode());
    }

    public Object getValue() {
        return super.get("value");
    }

    public void setValue(Object value) {
        super.put("value", value);

    }


}
