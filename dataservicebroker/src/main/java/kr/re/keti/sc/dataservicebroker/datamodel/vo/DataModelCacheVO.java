package kr.re.keti.sc.dataservicebroker.datamodel.vo;

import java.util.List;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.BigDataStorageType;
import lombok.Data;

@Data
public class DataModelCacheVO {

	private DataModelVO dataModelVO;
	private DataModelStorageMetadataVO dataModelStorageMetadataVO;
	private List<BigDataStorageType> createdStorageTypes;

	public Attribute getRootAttribute(String rootAttributeName) {
		if(rootAttributeName == null || dataModelVO == null) {
			return null;
		}
		
		for(Attribute rootAttribute : dataModelVO.getAttributes()) {
			if(rootAttributeName.equals(rootAttribute.getName())) {
				return rootAttribute;
			}
		}
		return null;
	}
}
