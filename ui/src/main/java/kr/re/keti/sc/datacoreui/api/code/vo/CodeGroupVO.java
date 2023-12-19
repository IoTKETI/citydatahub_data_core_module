package kr.re.keti.sc.datacoreui.api.code.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Code group VO class.
 * @FileName CodeGroupVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 22.
 * @Author Elvin
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodeGroupVO extends CodeGroupBaseVO {
	/** Total count */
	@JsonIgnore
	private Integer totalCnt;
}
