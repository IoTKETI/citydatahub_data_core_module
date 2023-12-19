package kr.re.keti.sc.datacoreusertool.api.dataservicebroker.vo;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import kr.re.keti.sc.datacoreusertool.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.datacoreusertool.common.code.DataServiceBrokerCode.DefaultAttributeKey;

/**
 * CommonEntityVO class
 * @FileName CommonEntityVO.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@SuppressWarnings("serial")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonEntityVO extends HashMap<String, Object> {

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	@SuppressWarnings("unchecked")
	public List<String> getContext() {
		return (List<String>) super.get(DefaultAttributeKey.CONTEXT.getCode());
	}
	public void setContext(List<String> context) {
		super.put(DefaultAttributeKey.CONTEXT.getCode(), context);
	}

	public String getId() {
		return (String) super.get(DataServiceBrokerCode.DefaultAttributeKey.ID.getCode());
	}
	public void setId(String id) {
		super.put(DataServiceBrokerCode.DefaultAttributeKey.ID.getCode(), id);
	}
	
	public String getType() {
		return (String) super.get(DataServiceBrokerCode.DefaultAttributeKey.TYPE.getCode());
	}
	public void setType(String type) {
		super.put(DataServiceBrokerCode.DefaultAttributeKey.TYPE.getCode(), type);
	}

	public String getDatasetId() {
		return (String) super.get(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode());
	}
	public void setDatasetId(String datasetId) {
		super.put(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode(), datasetId);
	}

	public String getCreatedAt() {
		return (String) super.get(DataServiceBrokerCode.DefaultAttributeKey.CREATED_AT.getCode());
	}
	public void setCreatedAt(Date createdAt) {
		super.put(DataServiceBrokerCode.DefaultAttributeKey.CREATED_AT.getCode(), createdAt);
	}

	public String getModifiedAt() {
		return (String) super.get(DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode());
	}
	
	public void setModifiedAt(Date modifiedAt) {
		super.put(DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode(), modifiedAt);
	}
}
