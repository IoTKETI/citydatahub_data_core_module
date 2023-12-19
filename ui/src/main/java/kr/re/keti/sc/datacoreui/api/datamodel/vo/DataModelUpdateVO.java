package kr.re.keti.sc.datacoreui.api.datamodel.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/** 
 * This is the VO class used when updating the data model.
 * @FileName DataModelUpdateVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 22.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataModelUpdateVO {
	/** DataModel name */
	private String name;
	/** DataModel @Context */
	private List<String> context;
	/** Entity description */
	private String description;
	/** Featured search Attribute Id list */
	private List<String> indexAttributeNames;
}
