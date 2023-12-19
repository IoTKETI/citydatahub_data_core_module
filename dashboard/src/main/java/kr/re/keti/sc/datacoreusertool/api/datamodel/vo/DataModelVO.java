package kr.re.keti.sc.datacoreusertool.api.datamodel.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * DataModel VO
 * @FileName DataModelVO.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 25.
 * @Author Elvin
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class DataModelVO {
	private List<String> context;
	private String id;
    private String type;
    private String typeUri;
    private String name;
    private String description;
    private List<String> indexAttributeNames;
    private List<Attribute> attributes;
}
