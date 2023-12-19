package kr.re.keti.sc.dataservicebroker.entities.vo;

import java.util.List;

import lombok.Data;

@Data
public class AvailableEntityTypeInformation {
	private String id;
	private String type;
	private String typeName;
	private int entityCount;
	private List<AvailableAttributeDetail> attributeDetails;
}