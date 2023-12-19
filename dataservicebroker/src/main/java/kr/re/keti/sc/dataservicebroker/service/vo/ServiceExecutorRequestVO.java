package kr.re.keti.sc.dataservicebroker.service.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import kr.re.keti.sc.dataservicebroker.service.vo.ServiceRegistrationRequestVO.AttributeValue;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceExecutorRequestVO {
	private String id;
	private String name;
	private String entityId;
	private String entityType;
	private List<AttributeValue> attribs;
}
