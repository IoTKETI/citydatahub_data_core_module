package kr.re.keti.sc.datacoreui.api.code.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * This is the VO class used when responding to a code group.
 * @FileName CodeGroupResponseVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 22.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodeGroupResponseVO {
	/** Total count */
	private Integer totalCount;
	/** List of code groups */
	private List<CodeGroupVO> codeGroupVOs;
}
