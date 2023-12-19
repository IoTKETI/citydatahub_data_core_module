package kr.re.keti.sc.datamanager.datasetflow.vo;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import kr.re.keti.sc.datamanager.common.code.Constants;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode.BigDataStorageType;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode.HistoryStoreType;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ProvisionServerType;

public class DatasetFlowBaseVO {
	private String datasetId;
	private String description;
	private HistoryStoreType historyStoreType;
	private Boolean enabled = true;
	@JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
	private Date createDatetime;
	private String creatorId;
	@JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
	private Date modifyDatetime;
	private String modifierId;
	private String provisioningRequestId;
	private Date provisioningEventTime;
	private List<DatasetFlowServerDetailVO> datasetFlowServerDetailVOs; 

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
	public Date getCreateDatetime() {
		return createDatetime;
	}
	public void setCreateDatetime(Date createDatetime) {
		this.createDatetime = createDatetime;
	}
	public String getCreatorId() {
		return creatorId;
	}
	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}
	public Date getModifyDatetime() {
		return modifyDatetime;
	}
	public void setModifyDatetime(Date modifyDatetime) {
		this.modifyDatetime = modifyDatetime;
	}
	public String getModifierId() {
		return modifierId;
	}
	public void setModifierId(String modifierId) {
		this.modifierId = modifierId;
	}
	public List<DatasetFlowServerDetailVO> getDatasetFlowServerDetailVOs() {
		return datasetFlowServerDetailVOs;
	}
	public void setDatasetFlowServerDetailVOs(List<DatasetFlowServerDetailVO> datasetFlowServerDetailVOs) {
		this.datasetFlowServerDetailVOs = datasetFlowServerDetailVOs;
	}
	
	public String getProvisioningRequestId() {
		return provisioningRequestId;
	}
	public void setProvisioningRequestId(String provisioningRequestId) {
		this.provisioningRequestId = provisioningRequestId;
	}
	public Date getProvisioningEventTime() {
		return provisioningEventTime;
	}
	public void setProvisioningEventTime(Date provisioningEventTime) {
		this.provisioningEventTime = provisioningEventTime;
	}

	public static class DatasetFlowServerDetailVO {
		private String datasetId;
		private ProvisionServerType provisionServerType;
		private List<BigDataStorageType> bigDataStorageTypes;

		public String getDatasetId() {
			return datasetId;
		}
		public void setDatasetId(String datasetId) {
			this.datasetId = datasetId;
		}
		public ProvisionServerType getProvisionServerType() {
			return provisionServerType;
		}
		public void setProvisionServerType(ProvisionServerType provisionServerType) {
			this.provisionServerType = provisionServerType;
		}
		public List<BigDataStorageType> getBigDataStorageTypes() {
			return bigDataStorageTypes;
		}
		public void setBigDataStorageTypes(List<BigDataStorageType> bigDataStorageTypes) {
			this.bigDataStorageTypes = bigDataStorageTypes;
		}
	}
}
