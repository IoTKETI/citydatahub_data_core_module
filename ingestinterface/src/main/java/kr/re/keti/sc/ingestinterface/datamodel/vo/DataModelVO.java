package kr.re.keti.sc.ingestinterface.datamodel.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * Data model API VO class
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataModelVO {
	private String id;
	private String type;
	private String typeUri;
	private String name;
	private List<String> context;
	private String description;
	private List<String> indexAttributeNames;
	private List<Attribute> attributes;
}
