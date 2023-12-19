package kr.re.keti.sc.datacoreui.api.datamodel.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * Attribute VO class.
 * @FileName AttributeVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 22.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttributeVO {
	/** rootAttribute ID */
	private String name;
	/** rootAttribute description */
	private String description;
	/** rootAttribute type */
	private String attributeType;
	/** rootAttribute Uri */
	private String attributeUri;
	/** Whether to include the observedAt field of rootAttribute */
	private Boolean hasObservedAt;
	/** Whether the rootAttribute contains the unitCode field */
	private Boolean hasUnitCode;
	/** Required */
	private Boolean isRequired;
	/** accessMode */
	private String accessMode;
	/** > */
	private Double greaterThan;
	/** >= */
	private Double greaterThanOrEqualTo;
	/** <= */
	private Double lessThanOrEqualTo;
	/** < */
	private Double lessThan;
	/** value maximum length */
	private Integer maxLength;
	/** value minimum length */
	private Integer minLength;
	/** value type */
	private String valueType;
	/** allowable value enum */
	private Object[] valueEnum;
	/** ObjectMember List */
	private List<ObjectMemberVO> objectMembers;
	/** child Attributes */
	private List<AttributeVO> childAttributes;
}
