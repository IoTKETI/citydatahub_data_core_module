package kr.re.keti.sc.datacoreusertool.api.dataservicebroker.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import kr.re.keti.sc.datacoreusertool.common.vo.ClientExceptionPayloadVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * CommonEntityResponseVO class
 * @FileName CommonEntityResponseVO.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonEntityResponseVO extends ClientExceptionPayloadVO {
	/** Total count */
	private Integer totalCount;
	/** Entity list */
	private CommonEntityVO commonEntityVO;
}
