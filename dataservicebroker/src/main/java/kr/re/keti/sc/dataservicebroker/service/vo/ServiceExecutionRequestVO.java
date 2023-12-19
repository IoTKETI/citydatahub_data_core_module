package kr.re.keti.sc.dataservicebroker.service.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import kr.re.keti.sc.dataservicebroker.service.vo.ServiceRegistrationRequestVO.Attribute;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceExecutionRequestVO {
	private String id;
	private String serviceRegistrationId;
	private String type = "ServiceRequest";
	private List<ExecutionRequest> executions;

	@Data
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ExecutionRequest {
		private String id;
		private String name;
		private String type;
		private String entityId;
		private String entityType;
		private Attribute input;
	}
}
