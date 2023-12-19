package kr.re.keti.sc.dataservicebroker.service.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceRegistrationRequestVO {
	private String id;
	private String type = "ServiceRegistration";
	private String name; 
	private String description;
	private List<RegistrationInfo> information;

	@Data
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class RegistrationInfo {
		private List<Service> services;
	}

	@Data
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Service {
		private String id;
		private String idPattern;
		private String type;
		private String name;
		private String endpoint;
		private Attribute input;
		private Attribute output;
	}

	@Data
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Attribute {
		private String type;
		private List<AttributeValue> attribs;
	}
	
	@Data
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class AttributeValue {
		private String attribname;
		private String datatype;
		private Object range;
		private Object value;
	}
}
