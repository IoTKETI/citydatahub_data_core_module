package kr.re.keti.sc.dataservicebroker.service.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ServiceExecutionResult;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceExecutorResponseVO {
	private String id;
	private ServiceExecutionResult status;
}
