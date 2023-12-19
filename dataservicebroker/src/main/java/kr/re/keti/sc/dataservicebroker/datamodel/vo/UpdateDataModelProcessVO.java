package kr.re.keti.sc.dataservicebroker.datamodel.vo;

public class UpdateDataModelProcessVO {
	
	public enum AttributeUpdateProcessType {
    	NEW_ATTRIBUTE,
    	EXISTS_ATTRIBUTE,
    	REMOVE_ATTRIBUTE;
    }

	private AttributeUpdateProcessType attributeUpdateProcessType;
	private String attributeName;
	private Attribute beforeAttribute;
	private Attribute afterAttribute;

	public AttributeUpdateProcessType getAttributeUpdateProcessType() {
		return attributeUpdateProcessType;
	}
	public void setAttributeUpdateProcessType(AttributeUpdateProcessType attributeUpdateProcessType) {
		this.attributeUpdateProcessType = attributeUpdateProcessType;
	}
	public String getAttributeName() {
		return attributeName;
	}
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	public Attribute getBeforeAttribute() {
		return beforeAttribute;
	}
	public void setBeforeAttribute(Attribute beforeAttribute) {
		this.beforeAttribute = beforeAttribute;
	}
	public Attribute getAfterAttribute() {
		return afterAttribute;
	}
	public void setAfterAttribute(Attribute afterAttribute) {
		this.afterAttribute = afterAttribute;
	}
}
