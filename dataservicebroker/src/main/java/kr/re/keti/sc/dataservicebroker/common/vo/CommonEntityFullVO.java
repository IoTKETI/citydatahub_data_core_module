package kr.re.keti.sc.dataservicebroker.common.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;

@SuppressWarnings("serial")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonEntityFullVO extends CommonEntityVO {

	public String getType() {
		return (String) super.get(DataServiceBrokerCode.DefaultAttributeKey.TYPE.getCode());
	}
	public void setType(String type) {
		super.put(DataServiceBrokerCode.DefaultAttributeKey.TYPE.getCode(), type);
	}
}
