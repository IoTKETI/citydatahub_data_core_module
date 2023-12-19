package kr.re.keti.sc.datacoreui.api.datamodel.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * This is the VO class used when responding to the data model.
 * @FileName DataModelResponseVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 22.
 * @Author Elvin
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataModelResponseVO {
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
	/** Featured Search Attribute Id list */
	private List<String> indexAttributeNames;
	/** Entity Attribute Information */
	private List<AttributeVO> attributes;
	/** Creator ID */
	private String creatorId;
	/** Creation date */
	private String createdAt;
	/** Modifier ID */
	private String modifierId;
	/** Modified date */
	private String modifiedAt;
	/** UI Tree */
	private List<UiTreeVO> treeStructure;
}
