package kr.re.keti.sc.dataservicebroker.entities.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AvailableAttributeDetail {
	private String id;
	private String type;
	private String attributeName;
	private List<String> typeNames;
	private List<String> attributeTypes;
}
