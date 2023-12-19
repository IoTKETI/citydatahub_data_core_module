package kr.re.keti.sc.ingestinterface.common.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode;

/**
 * Ngsi-ld entity full representation vo class
 */
@SuppressWarnings("serial")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonEntityFullVO extends CommonEntityVO {

	public String getType() {
		return (String) super.get(IngestInterfaceCode.DefaultAttributeKey.TYPE.getCode());
	}
	public void setType(String type) {
		super.put(IngestInterfaceCode.DefaultAttributeKey.TYPE.getCode(), type);
	}
}
