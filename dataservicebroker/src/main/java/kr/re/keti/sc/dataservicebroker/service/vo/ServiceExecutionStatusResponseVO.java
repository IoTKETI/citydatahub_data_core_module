package kr.re.keti.sc.dataservicebroker.service.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ServiceExecutionStatus;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceExecutionStatusResponseVO {
	private String id;
	private List<ServiceExecutionStatusVO> executions;
	
	@Data
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ServiceExecutionStatusVO {
		private String id;
		private ServiceExecutionStatus status;
	}
}
