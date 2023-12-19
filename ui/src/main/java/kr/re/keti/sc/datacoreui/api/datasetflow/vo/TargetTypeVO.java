package kr.re.keti.sc.datacoreui.api.datasetflow.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * Target type VO class
 * @FileName TargetTypeVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 23.
 * @Author Elvin
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TargetTypeVO {
	/** Provision server type */
	private String type;
	/** Big data storage type */
	private List<String> bigDataStorageTypes;
}
