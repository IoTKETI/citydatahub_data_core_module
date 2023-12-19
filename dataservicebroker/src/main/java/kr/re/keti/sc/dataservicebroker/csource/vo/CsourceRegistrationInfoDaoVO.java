package kr.re.keti.sc.dataservicebroker.csource.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CsourceRegistrationInfoDaoVO {

	private String csourceRegistrationInfoId;
	private String csourceRegistrationBaseId;
	private List<String> properties;
	private List<String> relationships;
	private List<CsourceRegistrationEntityDaoVO> entities;
}
