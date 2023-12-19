package kr.re.keti.sc.datacoreusertool.api.datamodel.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import kr.re.keti.sc.datacoreusertool.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.datacoreusertool.common.code.DataServiceBrokerCode.AttributeType;
import lombok.Data;

/**
 * Attribute VO
 * @FileName Attribute.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 25.
 * @Author Elvin
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Attribute extends ObjectMember {
    private AttributeType attributeType;
    private Boolean hasObservedAt;
    private Boolean hasUnitCode;
    private List<Attribute> childAttributes;
    private DataServiceBrokerCode.AccessMode accessMode;
    private String attributeUri;

    /**
     * Get child attribute
     * @param childAttributeId	Child attribute ID
     * @return					Object member
     */
    public ObjectMember getChildAttribute(String childAttributeId) {
        for (ObjectMember objectMember : getObjectMembers()) {
            if (childAttributeId.equals(objectMember.getName())) {
                return objectMember;
            }
        }
        return null;
    }

    /**
     * If there is an attribute id in the child attribute, the corresponding attribute is returned.
     * @param hasAttributeId	Attribute ID to find
     * @return					Object member
     */
    public ObjectMember getHasAttribute(String hasAttributeId) {
        for (Attribute hasAttributes : getChildAttributes()) {
            if (hasAttributeId.equals(hasAttributes.getName())) {
                return hasAttributes;
            }
        }
        return null;
    }
}
