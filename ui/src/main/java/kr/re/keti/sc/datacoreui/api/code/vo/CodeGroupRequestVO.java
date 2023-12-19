package kr.re.keti.sc.datacoreui.api.code.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * This is the VO class used when requesting a code group.
 * @FileName CodeGroupRequestVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 22.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodeGroupRequestVO {
	/** Current page */
	private Integer currentPage;
	/** Page size */
	private Integer pageSize;
	/** Search value */
	private String searchValue;
}
