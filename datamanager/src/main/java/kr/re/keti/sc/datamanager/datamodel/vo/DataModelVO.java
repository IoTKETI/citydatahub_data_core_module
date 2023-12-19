package kr.re.keti.sc.datamanager.datamodel.vo;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import kr.re.keti.sc.datamanager.common.code.Constants;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataModelVO {
	private List<String> context;
	private String id;
    private String type;
    private String typeUri;
    private String name;
    private String description;
    private List<String> indexAttributeNames;
    private List<Attribute> attributes;
    private String creatorId;
    @JsonProperty("createdAt")
    @JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
    private Date createdAt;
    private String modifierId;
    @JsonProperty("modifiedAt")
    @JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
    private Date modifiedAt;
}
