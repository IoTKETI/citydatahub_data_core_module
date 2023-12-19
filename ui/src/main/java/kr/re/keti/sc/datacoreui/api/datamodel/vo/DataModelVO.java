package kr.re.keti.sc.datacoreui.api.datamodel.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * Data model VO class.
 * @FileName DataModelVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 22.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataModelVO {
	/** DataModel @Context */
	private List<String> context;
	/** DataModel ID */
	private String id;
	/** DataModel type */
	private String type;
	/** DataModel type Uri */
	private String typeUri;
	/** DataModel name */
	private String name;
	/** Entity description */
	private String description;
	/** Featured search Attribute Id list */
	private List<String> indexAttributeNames;
	/** Entity Attribute */
	private List<AttributeVO> attributes;

	private String creatorId;
	private String modifierId;
}
