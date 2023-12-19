package kr.re.keti.sc.ingestinterface.common.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode;

/**
 * Ngsi-ld GeoProperty VO class
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeoPropertyVO extends AttributeVO {

    public GeoPropertyVO() {
        this.setType(IngestInterfaceCode.AttributeType.GEO_PROPERTY.getCode());
    }

    public Object getValue() {
        return super.get("value");
    }

    public void setValue(Object value) {
        super.put("value", value);
    }

}
