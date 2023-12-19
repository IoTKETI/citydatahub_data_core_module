package kr.re.keti.sc.datacoreusertool.api.map.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * MapSearchConditionBaseVO class
 * @FileName MapSearchConditionBaseVO.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MapSearchConditionBaseVO {
	/** Map search condition ID */
	private String mapSearchConditionId;
	/** Map search condition type(LATEST/HISTORY) */
	private String mapSearchConditionType;
	/** User ID */
	private String userId;
	/** Title of map search condition */
	private String mapSearchConditionName;
	/** Search condition */
	private String searchCondition;
	/** Subscription condition */
	private String subscriptionCondition;
	/** Create date */
	private String createDatetime;
	/** Update date */
	private String modifyDatetime;
}
