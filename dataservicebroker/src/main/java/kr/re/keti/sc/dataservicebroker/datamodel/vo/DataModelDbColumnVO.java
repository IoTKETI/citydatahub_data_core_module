package kr.re.keti.sc.dataservicebroker.datamodel.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.DbColumnType;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataModelDbColumnVO {
	private List<String> hierarchyAttributeIds;
	private String daoAttributeId;
	private String columnName;
	private DbColumnType columnType;
	private String maxLength;
	private Boolean isNotNull;
}
