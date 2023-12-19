package kr.re.keti.sc.datacoreui.api.datasetflow.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * Data set ID VO class
 * @FileName DataSetIdVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 23.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataSetIdVO {
	/** Data set ID */
	private String id;
	/** Data set name */
	private String name;
}
