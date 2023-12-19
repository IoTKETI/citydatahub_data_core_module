package kr.re.keti.sc.datacoreui.api.dataset.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * This is the VO class used for data set retrieve.
 * @FileName DataSetRetrieveVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 23.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataSetRetrieveVO {
	/** Data set name */
	private String name;
	/** Update interval */
	private String updateInterval;
	/** Category(code) */
	private String category;
	/** Provider Organization */
	private String providerOrganization;
	/** Provider System */
	private String providerSystem;
	/** Whether data is processed */
	private String isProcessed;
	/** Ownership */
	private String ownership;
	/** License(code) */
	private String license;
	/** Data set items */
	private String datasetItems;
	/** Target regions */
	private String targetRegions;
	/** Data store uri */
	private List<String> dataStoreUri;
	/** Whether quality is verified */
	private Boolean qualityCheckEnabled;
	/** Data model ID */
	private String dataModelId;
	/** Historical data retention period (Day) */
	private Integer storageRetention;
	/** Topic retention period (ms) */
	private Long topicRetention;
	/** limit */
	private Integer limit;
	/** offset */
	private Integer offset;
	
	/** Search value(ID or Name) */
	private String searchValue;
}
