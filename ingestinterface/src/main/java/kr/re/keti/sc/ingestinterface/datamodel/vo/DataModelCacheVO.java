package kr.re.keti.sc.ingestinterface.datamodel.vo;

/**
 * Data model cache VO class
 */
public class DataModelCacheVO extends DataModelVO {

	public Attribute getRootAttribute(String rootAttributeName) {
		for(Attribute rootAttribute : getAttributes()) {
			if(rootAttributeName.equals(rootAttribute.getName())) {
				return rootAttribute;
			}
		}
		return null;
	}
}
