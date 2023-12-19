package kr.re.keti.sc.datacoreusertool.api.datamodel.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * Tree structure VO for UI
 * @FileName UiTreeVO.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 25.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UiTreeVO {
	/** Tree ID */
	private String id;
	/** Tree label */
	private String label;
	/** Full ID */
	private String fullId;
	/** Searchable */
	private boolean searchable;
	/** Graphable */
	private boolean graphable;
	/** Value type */
	private String valueType;
	/** Child tree */
	private List<UiTreeVO> child;
}
