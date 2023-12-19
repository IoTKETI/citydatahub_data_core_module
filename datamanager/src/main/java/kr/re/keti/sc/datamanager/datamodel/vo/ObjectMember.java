package kr.re.keti.sc.datamanager.datamodel.vo;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import kr.re.keti.sc.datamanager.common.code.DataManagerCode.AttributeValueType;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ObjectMember {
    private String name;
    private String description;
    private Boolean isRequired;
    private BigDecimal greaterThan;
    private BigDecimal greaterThanOrEqualTo;
    private BigDecimal lessThanOrEqualTo;
    private BigDecimal lessThan;
    private String maxLength;
    private String minLength;
    private AttributeValueType valueType;
    private List<Object> valueEnum;
    private List<ObjectMember> objectMembers;
}
