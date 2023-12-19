package kr.re.keti.sc.dataservicebroker.common.vo;

import java.util.List;
import java.util.Map;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.Operation;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelDbColumnVO;

@SuppressWarnings("serial")
public class CommonEntityDaoVO extends CommonEntityVO {

	private String entityType;
	private String dbTableName;
	private List<String> tableColumns;
	private Map<String, DataModelDbColumnVO> dbColumnInfoVOMap;

	public Operation getOperation() {
		return (Operation) super.get(DataServiceBrokerCode.DefaultAttributeKey.OPERATION.getCode());
	}
	public void setOperation(Operation operation) {
		super.put(DataServiceBrokerCode.DefaultAttributeKey.OPERATION.getCode(), operation);
	}

	public String getAttrId() {
		return (String) super.get(DataServiceBrokerCode.DefaultAttributeKey.ATTR_ID.getCode());
	}
	public void setAttrId(String attrId) {
		super.put(DataServiceBrokerCode.DefaultAttributeKey.ATTR_ID.getCode(), attrId);
	}

	public String getEntityType() {
		return entityType;
	}
	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}
	public String getDbTableName() {
		return dbTableName;
	}
	public void setDbTableName(String tableName) {
		this.dbTableName = tableName;
	}
	public Map<String, DataModelDbColumnVO> getDbColumnInfoVOMap() {
		return dbColumnInfoVOMap;
	}
	public void setDbColumnInfoVOMap(Map<String, DataModelDbColumnVO> dbColumnInfoVOMap) {
		this.dbColumnInfoVOMap = dbColumnInfoVOMap;
	}
	public List<String> getTableColumns() {
		return tableColumns;
	}
	public void setTableColumns(List<String> tableColumns) {
		this.tableColumns = tableColumns;
	}
}
