package kr.re.keti.sc.datacoreusertool.api.map.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * MapSearchConditionBaseResponseVO class
 * @FileName MapSearchConditionBaseResponseVO.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MapSearchConditionBaseResponseVO {
	/** Map search condition ID */
	private String mapSearchConditionId;
	/** Map search condition type(LATEST/HISTORY) */
	private String mapSearchConditionType;
	/** Title of map search condition */
	private String mapSearchConditionName;
	/** Search condition */
	private String searchCondition;
	/** subscription condition */
	private String subscriptionCondition;
}
