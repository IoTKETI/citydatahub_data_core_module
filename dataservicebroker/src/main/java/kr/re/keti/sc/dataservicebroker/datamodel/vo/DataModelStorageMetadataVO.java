package kr.re.keti.sc.dataservicebroker.datamodel.vo;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataModelStorageMetadataVO {
	private String rdbTableName;
	private String hiveTableName;
	private Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = new LinkedHashMap<>();
}
