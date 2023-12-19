package kr.re.keti.sc.dataservicebroker.datamodel.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContextVO {
	@JsonProperty("@context")
	private Object context; // context는 string, Object, Array Object 모두 들어갈 수 있음
}
