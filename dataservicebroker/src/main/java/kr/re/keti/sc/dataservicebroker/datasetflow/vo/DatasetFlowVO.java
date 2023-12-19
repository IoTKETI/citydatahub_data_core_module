package kr.re.keti.sc.dataservicebroker.datasetflow.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.BigDataStorageType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.HistoryStoreType;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetFlowVO {
	private String datasetId;
	private String description;
	private HistoryStoreType historyStoreType;
	private List<BigDataStorageType> bigDataStorageTypes;
	private Boolean enabled;

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public HistoryStoreType getHistoryStoreType() {
		return historyStoreType;
	}
	public void setHistoryStoreType(HistoryStoreType historyStoreType) {
		this.historyStoreType = historyStoreType;
	}
	public List<BigDataStorageType> getBigDataStorageTypes() {
		return bigDataStorageTypes;
	}
	public void setBigDataStorageTypes(List<BigDataStorageType> bigDataStorageTypes) {
		this.bigDataStorageTypes = bigDataStorageTypes;
	}
	public Boolean getEnabled() {
		return enabled;
	}
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	public String getDatasetId() {
		return datasetId;
	}
	public void setDatasetId(String datasetId) {
		this.datasetId = datasetId;
	}
}
