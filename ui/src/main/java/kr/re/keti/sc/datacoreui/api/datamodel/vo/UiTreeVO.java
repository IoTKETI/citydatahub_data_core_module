package kr.re.keti.sc.datacoreui.api.datamodel.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * This is a VO class for tree expression in UI.
 * @FileName UiTreeVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 22.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UiTreeVO {
	/** Tree ID */
	private String id;
	/** Tree label */
	private String label;
	/** Child Tree */
	private List<UiTreeVO> children;
}
