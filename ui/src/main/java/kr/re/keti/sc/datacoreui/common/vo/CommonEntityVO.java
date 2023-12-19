package kr.re.keti.sc.datacoreui.common.vo;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import kr.re.keti.sc.datacoreui.common.code.DataCoreUiCode;
import kr.re.keti.sc.datacoreui.common.code.DataCoreUiCode.DefaultAttributeKey;

/**
 * Common entity VO class
 * @FileName CommonEntityVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
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
		return (String) super.get(DataCoreUiCode.DefaultAttributeKey.ID.getCode());
	}
	public void setId(String id) {
		super.put(DataCoreUiCode.DefaultAttributeKey.ID.getCode(), id);
	}

	public Date getCreatedAt() {
		return (Date) super.get(DataCoreUiCode.DefaultAttributeKey.CREATED_AT.getCode());
	}
	public void setCreatedAt(Date createdAt) {
		super.put(DataCoreUiCode.DefaultAttributeKey.CREATED_AT.getCode(), createdAt);
	}

	public Date getModifiedAt() {
		return (Date) super.get(DataCoreUiCode.DefaultAttributeKey.MODIFIED_AT.getCode());
	}
	public void setModifiedAt(Date modifiedAt) {
		super.put(DataCoreUiCode.DefaultAttributeKey.MODIFIED_AT.getCode(), modifiedAt);
	}
}
