package kr.re.keti.sc.ingestinterface.common.vo;

import java.util.Date;
import java.util.List;

import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.Operation;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.OperationOption;
import kr.re.keti.sc.ingestinterface.datamodel.vo.DataModelCacheVO;
import kr.re.keti.sc.ingestinterface.dataset.vo.DatasetBaseVO;

/**
 * Entity Operation Processing VO Class
 * @param <T>
 */
public class EntityProcessVO<T extends CommonEntityFullVO> {
	/** entityType */
	private String entityType;
	/** Operation */
	private Operation operation;
	/** 수신받은 entity 정보 */
	private String content;
	/** 수신받은 데이터 VO */
	private T entityFullVO;
	/** Entity 파싱 시 사용될 DataModel 정보 */
	private DataModelCacheVO dataModelCacheVO;
	/** 처리결과 VO */
	private ProcessResultVO processResultVO = new ProcessResultVO();
	/** 처리Options */
	private List<OperationOption> operationOptions;
	/** datasetId */
	private String datasetId;
	/** 수신시간 */
	private Date ingestTime;

	private DatasetBaseVO datasetBaseVO;


	public String getEntityType() {
		return entityType;
	}
	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}
	public Operation getOperation() {
		return operation;
	}
	public void setOperation(Operation operation) {
		this.operation = operation;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public T getEntityFullVO() {
		return entityFullVO;
	}
	public void setEntityFullVO(T entityFullVO) {
		this.entityFullVO = entityFullVO;
	}
	public DataModelCacheVO getDataModelCacheVO() {
		return dataModelCacheVO;
	}
	public void setDataModelCacheVO(DataModelCacheVO dataModelCacheVO) {
		this.dataModelCacheVO = dataModelCacheVO;
	}
	public ProcessResultVO getProcessResultVO() {
		return processResultVO;
	}
	public void setProcessResultVO(ProcessResultVO processResultVO) {
		this.processResultVO = processResultVO;
	}
	public List<OperationOption> getOperationOptions() {
		return operationOptions;
	}
	public void setOperationOptions(List<OperationOption> operationOptions) {
		this.operationOptions = operationOptions;
	}
	public String getDatasetId() {
		return datasetId;
	}
	public void setDatasetId(String datasetId) {
		this.datasetId = datasetId;
	}
	public Date getIngestTime() {
		return ingestTime;
	}
	public void setIngestTime(Date ingestTime) {
		this.ingestTime = ingestTime;
	}

	public DatasetBaseVO getDatasetBaseVO() {
		return datasetBaseVO;
	}

	public void setDatasetBaseVO(DatasetBaseVO datasetBaseVO) {
		this.datasetBaseVO = datasetBaseVO;
	}
}
