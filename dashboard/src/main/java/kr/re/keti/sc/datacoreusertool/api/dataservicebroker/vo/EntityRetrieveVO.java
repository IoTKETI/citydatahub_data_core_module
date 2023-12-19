package kr.re.keti.sc.datacoreusertool.api.dataservicebroker.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * EntityRetrieveVO class
 * @FileName EntityRetrieveVO.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntityRetrieveVO {
	// DataModel ID
	private String dataModelId;
	// DataModel Type
	private String type;
	// DataModel TypeUri
	private String typeUri;
	// Search value
	private String searchValue;
	// Attributes
	private List<String> attrs;
	// EntityId
	private String id;
	
	// Options
	private String options;
	
	// Query
	private List<QVO> q;

	// GeoQuery (coordinates)
	private String coordinates;
	
	// Temporal Query
	private String timerel;
	private String timeproperty;
	private String time;
	private String endtime;
	
	// page limit
	private Integer limit;
	// page offset
    private Integer offset;
    
    // Display attribute
    private String displayAttribute;
}
