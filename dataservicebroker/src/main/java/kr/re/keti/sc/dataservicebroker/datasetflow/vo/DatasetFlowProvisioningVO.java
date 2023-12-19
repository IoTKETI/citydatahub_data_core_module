package kr.re.keti.sc.dataservicebroker.datasetflow.vo;

import java.util.List;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.BigDataStorageType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.HistoryStoreType;

public class DatasetFlowProvisioningVO {
	private String datasetId;
	private String description;
	private HistoryStoreType historyStoreType;
	private Boolean enabled;
	private List<BigDataStorageType> bigDataStorageTypes;

	public String getDatasetId() {
		return datasetId;
	}
	public void setDatasetId(String datasetId) {
		this.datasetId = datasetId;
	}
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
	public Boolean getEnabled() {
		return enabled;
	}
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	public List<BigDataStorageType> getBigDataStorageTypes() {
		return bigDataStorageTypes;
	}
	public void setBigDataStorageTypes(List<BigDataStorageType> bigDataStorageTypes) {
		this.bigDataStorageTypes = bigDataStorageTypes;
	}
}
