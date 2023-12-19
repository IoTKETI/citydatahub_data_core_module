package kr.re.keti.sc.dataservicebroker.entities.vo;

import java.util.List;

import lombok.Data;

@Data
public class AvailableEntityType {
	private String id;
	private String type;
	private List<String> typeList;
}
