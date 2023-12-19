package kr.re.keti.sc.dataservicebroker.entities.vo;

import java.util.List;

import lombok.Data;

@Data
public class AvailableAttributeInformation {
	private String id;
	private String type;
	private String attributeName;
	private List<String> attributeTypes;
	private List<String> typeNames;
	private int attributeCount;
}
