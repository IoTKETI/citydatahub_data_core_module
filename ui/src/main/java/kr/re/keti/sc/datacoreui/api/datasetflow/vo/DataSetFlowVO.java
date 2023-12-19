package kr.re.keti.sc.datacoreui.api.datasetflow.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * Data set flow VO class
 * @FileName DataSetFlowVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 23.
 * @Author Elvin
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataSetFlowVO {
	/** Entity history store type */
	private String historyStoreType;
	/** Enabled */
	private Boolean enabled;
	/** Description of data set flow */
	private String description;
	/** List of provision module of Data set flow */
	private List<TargetTypeVO> targetTypes;
}
