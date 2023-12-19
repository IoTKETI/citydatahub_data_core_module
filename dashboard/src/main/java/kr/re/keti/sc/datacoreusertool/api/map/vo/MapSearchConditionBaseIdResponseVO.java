package kr.re.keti.sc.datacoreusertool.api.map.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * MapSearchConditionBaseIdResponseVO class
 * @FileName MapSearchConditionBaseIdResponseVO.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MapSearchConditionBaseIdResponseVO {
	/** Map search condition ID */
	private String mapSearchConditionId;
	/** Title of map search condition */
	private String mapSearchConditionName;
}
