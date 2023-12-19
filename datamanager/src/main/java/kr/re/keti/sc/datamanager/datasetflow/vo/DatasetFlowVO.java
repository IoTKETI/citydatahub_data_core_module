package kr.re.keti.sc.datamanager.datasetflow.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import kr.re.keti.sc.datamanager.common.code.DataManagerCode.BigDataStorageType;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode.HistoryStoreType;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ProvisionServerType;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetFlowVO {
	private String datasetId;
	private String description;
	private HistoryStoreType historyStoreType;
	private Boolean enabled;
	private List<TargetTypeVO> targetTypes;

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
	public List<TargetTypeVO> getTargetTypes() {
		return targetTypes;
	}
	public void setTargetTypes(List<TargetTypeVO> targetTypes) {
		this.targetTypes = targetTypes;
	}
	public String getDatasetId() {
		return datasetId;
	}
	public void setDatasetId(String datasetId) {
		this.datasetId = datasetId;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class TargetTypeVO {
		private ProvisionServerType type;
		private List<BigDataStorageType> bigDataStorageTypes;
		
		public ProvisionServerType getType() {
			return type;
		}
		public void setType(ProvisionServerType type) {
			this.type = type;
		}
		public List<BigDataStorageType> getBigDataStorageTypes() {
			return bigDataStorageTypes;
		}
		public void setBigDataStorageTypes(List<BigDataStorageType> bigDataStorageTypes) {
			this.bigDataStorageTypes = bigDataStorageTypes;
		}
	}
}
