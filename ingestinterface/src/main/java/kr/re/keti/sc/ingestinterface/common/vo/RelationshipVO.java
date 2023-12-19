package kr.re.keti.sc.ingestinterface.common.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode;

/**
 * Ngsi-ld RelationshipVO VO class
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RelationshipVO extends AttributeVO {

    public RelationshipVO(){
        this.setType(IngestInterfaceCode.AttributeType.RELATIONSHIP.getCode());
    }

    public Object getObject() {
        return super.get("object");
    }

    public void setObject(Object object) {
        super.put("object", object);

    }


}
