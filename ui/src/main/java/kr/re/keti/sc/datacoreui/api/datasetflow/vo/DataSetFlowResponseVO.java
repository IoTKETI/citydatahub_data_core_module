package kr.re.keti.sc.datacoreui.api.datasetflow.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * This is the VO class used when responding to the data set flow.
 * @FileName DataSetFlowResponseVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 23.
 * @Author Elvin
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataSetFlowResponseVO extends DataSetFlowVO {
	/** Data set ID */
	private String datasetId;
	/** Creator ID */
	private String creatorId;
	/** Creation date */
	private String createdAt;
	/** Modifier ID */
	private String modifierId;
	/** Modified date */
	private String modifiedAt;
}
