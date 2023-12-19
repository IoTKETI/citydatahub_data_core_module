package kr.re.keti.sc.pushagent.notification.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import kr.re.keti.sc.pushagent.common.code.DataServiceBrokerCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

@SuppressWarnings("serial")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonEntityVO extends LinkedHashMap<String, Object> implements Comparable<CommonEntityVO> {

	@JsonIgnore
	private Date sortKey;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
    }

    @SuppressWarnings("unchecked")
    public List<String> getContext() {
        return (List<String>) super.get(DataServiceBrokerCode.DefaultAttributeKey.CONTEXT.getCode());
    }

    public void setContext(List<String> context) {
        super.put(DataServiceBrokerCode.DefaultAttributeKey.CONTEXT.getCode(), context);
    }

    public String getId() {
        return (String) super.get(DataServiceBrokerCode.DefaultAttributeKey.ID.getCode());
    }

    public void setId(String id) {
        super.put(DataServiceBrokerCode.DefaultAttributeKey.ID.getCode(), id);
    }

    public String getDatasetId() {
        return (String) super.get(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode());
    }

    public void setDatasetId(String datasetId) {
        super.put(DataServiceBrokerCode.DefaultAttributeKey.DATASET_ID.getCode(), datasetId);
    }

    public Date getCreatedAt() {
        return (Date) super.get(DataServiceBrokerCode.DefaultAttributeKey.CREATED_AT.getCode());
    }

    public void setCreatedAt(Date createdAt) {
        super.put(DataServiceBrokerCode.DefaultAttributeKey.CREATED_AT.getCode(), createdAt);
    }

    public Date getModifiedAt() {
        return (Date) super.get(DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode());
    }

    public void setModifiedAt(Date modifiedAt) {
        super.put(DataServiceBrokerCode.DefaultAttributeKey.MODIFIED_AT.getCode(), modifiedAt);
        setSortKey(modifiedAt);
    }

    public Date getSortKey() {
		return sortKey;
	}

	public void setSortKey(Date sortKey) {
		this.sortKey = sortKey;
	}

	/**
     * @param commonEntityVO 수정시간 기준 내림 차순 정렬
     * @return
     */
    @Override
    public int compareTo(CommonEntityVO commonEntityVO) {
    	if(commonEntityVO.getSortKey() != null && getSortKey() != null) {
    		return commonEntityVO.getSortKey().compareTo(getSortKey());
    	}
    	
    	if(commonEntityVO.getModifiedAt() != null && getModifiedAt() != null) {
    		return commonEntityVO.getModifiedAt().compareTo(getModifiedAt());
    	}

    	return 0;
    }
}
