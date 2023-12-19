package kr.re.keti.sc.dataservicebroker.common.vo;

import java.util.*;

import kr.re.keti.sc.dataservicebroker.util.ValidateUtil;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.DefaultAttributeKey;

@SuppressWarnings("serial")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonEntityVO extends LinkedHashMap<String, Object> implements Comparable<CommonEntityVO>, TermExpandable {

	@JsonIgnore
	private Date sortKey;

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

    public String getType() {
        return (String) super.get(DataServiceBrokerCode.DefaultAttributeKey.TYPE.getCode());
    }
    public void setType(String type) {
        super.put(DataServiceBrokerCode.DefaultAttributeKey.TYPE.getCode(), type);
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

    @Override
    public void expandTerm(Map<String, String> dataModelContextMap) {
        expandTerm(null, dataModelContextMap);
    }

    @Override
    public void expandTerm(Map<String, String> requestContextMap, Map<String, String> dataModelContextMap) {
        if (requestContextMap == null) requestContextMap = new HashMap<>();

        expandKey(requestContextMap, dataModelContextMap);
        expandValue(requestContextMap, dataModelContextMap);
    }

    void expandKey(Map<String, String> requestContextMap, Map<String, String> dataModelContextMap) {
        expandKey(this, requestContextMap, dataModelContextMap);
    }

    private void expandKey(Map<String, Object> entityVO, Map<String, String> requestContextMap, Map<String, String> dataModelContextMap) {

        List<String> targetKeys = new ArrayList<>();

        Iterator<Map.Entry<String, Object>> it = entityVO.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> next = it.next();
            String entryKey = next.getKey();
            Object entryValue = next.getValue();

            if (isReservedKey(entryKey)) {
                continue;
            }

            if (isArrayAttribute(entryValue)) {
                for (Map<String, Object> innerEntry : (List<Map<String, Object>>)entryValue) {
                    expandKey(innerEntry, requestContextMap, dataModelContextMap);
                }
            } else if (isAttribute(entryValue)) {
                expandKey((Map<String, Object>)entryValue, requestContextMap, dataModelContextMap);
            }

            String fullUriByRequest = requestContextMap.get(entryKey);
            String fullUriByDataModel = dataModelContextMap.get(entryKey);
            if (isExpansionTarget(fullUriByRequest, fullUriByDataModel)) {
                targetKeys.add(entryKey);
            }
        }

        for (String entryKey : targetKeys) {
            entityVO.put(dataModelContextMap.get(entryKey), entityVO.get(entryKey));
            entityVO.remove(entryKey);
        }
    }

    private boolean isObjectMember(Object entryValue) {
        if (entryValue instanceof Map) {
            if (!((Map<String, Object>)entryValue).containsKey(DataServiceBrokerCode.PropertyKey.TYPE.getCode())) {
                return true;
            }
        }
        return false;
    }

    private void expandValue(Map<String, String> requestContextMap, Map<String, String> dataModelContextMap) {
        String fullUriByRequest = requestContextMap.get(this.getType());
        String fullUriByDataModel = dataModelContextMap.get(this.getType());

        if (isExpansionTarget(fullUriByRequest, fullUriByDataModel)) {
            this.setType(fullUriByDataModel);
        }
    }

    protected boolean isReservedKey(String key) {
        if (DefaultAttributeKey.parseType(key) != null) {
            return true;
        } else if (DataServiceBrokerCode.AttributeResultType.parseType(key) != null) {
            return true;
        }
        return false;
    }

    protected boolean isExpansionTarget(String fullUriByRequest, String fullUriByDataModel) {

        // 요청 context 기반으로 full uri 파싱이 불가능한 경우
        if (ValidateUtil.isEmptyData(fullUriByRequest)) {
            return true;

        // 요청 context 기반으로 full uri 파싱은 가능하지만, dataModel의 full url와 다른 경우
        } else if (!ValidateUtil.isEmptyData(fullUriByRequest)
                && !ValidateUtil.isEmptyData(fullUriByDataModel)
                && !fullUriByRequest.equals(fullUriByDataModel)) {
            return true;
        }
        return false;
    }

    private boolean isArrayAttribute(Object entryValue) {
        if (entryValue instanceof List
                && !ValidateUtil.isEmptyData((List)entryValue)
                && ((List<?>) entryValue).get(0) instanceof Map
                && !isObjectMember(entryValue)) {
            return true;
        }
        return false;
    }

    private boolean isAttribute(Object entryValue) {
        if (entryValue instanceof Map && !isObjectMember(entryValue)) {
            return true;
        }
        return false;
    }
}
