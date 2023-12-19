package kr.re.keti.sc.dataservicebroker.common.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RelationshipVO extends AttributeVO {

    public RelationshipVO(){
        this.setType(DataServiceBrokerCode.AttributeType.RELATIONSHIP.getCode());
    }

    public Object getObject() {
        return super.get("object");
    }

    public void setObject(Object object) {
        super.put("object", object);

    }


}
