package kr.re.keti.sc.ingestinterface.common.vo;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.DefaultAttributeKey;

/**
 * Ngsi-ld entity common vo class
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
		return (String) super.get(IngestInterfaceCode.DefaultAttributeKey.ID.getCode());
	}
	public void setId(String id) {
		super.put(IngestInterfaceCode.DefaultAttributeKey.ID.getCode(), id);
	}

	public Date getCreatedAt() {
		return (Date) super.get(IngestInterfaceCode.DefaultAttributeKey.CREATED_AT.getCode());
	}
	public void setCreatedAt(Date createdAt) {
		super.put(IngestInterfaceCode.DefaultAttributeKey.CREATED_AT.getCode(), createdAt);
	}

	public Date getModifiedAt() {
		return (Date) super.get(IngestInterfaceCode.DefaultAttributeKey.MODIFIED_AT.getCode());
	}
	public void setModifiedAt(Date modifiedAt) {
		super.put(IngestInterfaceCode.DefaultAttributeKey.MODIFIED_AT.getCode(), modifiedAt);
	}
}
