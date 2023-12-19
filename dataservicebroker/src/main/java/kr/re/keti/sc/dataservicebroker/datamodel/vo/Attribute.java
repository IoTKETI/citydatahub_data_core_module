package kr.re.keti.sc.dataservicebroker.datamodel.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.AttributeType;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Attribute extends ObjectMember {
    private AttributeType attributeType;
    private Boolean hasObservedAt;
    private Boolean hasUnitCode;
    private List<Attribute> childAttributes;
    private DataServiceBrokerCode.AccessMode accessMode;
	private String attributeUri;

	public ObjectMember getChildAttribute(String childAttributeId) {
        for (ObjectMember objectMember : getObjectMembers()) {
            if (childAttributeId.equals(objectMember.getName())) {
                return objectMember;
            }
        }
        return null;
    }

    public ObjectMember getHasAttribute(String hasAttributeId) {
        for (Attribute hasAttributes : getChildAttributes()) {
            if (hasAttributeId.equals(hasAttributes.getName())) {
                return hasAttributes;
            }
        }
        return null;
    }
}
