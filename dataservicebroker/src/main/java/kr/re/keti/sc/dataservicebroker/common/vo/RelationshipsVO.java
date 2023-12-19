package kr.re.keti.sc.dataservicebroker.common.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RelationshipsVO extends AttributeVO {

    public RelationshipsVO(){
        this.setType(DataServiceBrokerCode.AttributeType.RELATIONSHIP.getCode());
    }

    public Object getObjects() {
        return super.get("objects");
    }

    public void setObjects(Object object) {
        super.put("objects", object);

    }


}
