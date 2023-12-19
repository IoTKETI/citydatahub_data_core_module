package kr.re.keti.sc.datamanager.dataset.vo;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import kr.re.keti.sc.datamanager.common.code.Constants;
import kr.re.keti.sc.datamanager.common.vo.PageRequest;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetBaseVO extends PageRequest {

    private String id;
    private String name;
    private String description;
    private String updateInterval;
    private String category;
    private String providerOrganization;
    private String providerSystem;
    private String isProcessed;
    private String ownership;
    private List<String> keyword;
    private String license;
    private String providingApiUri;
    private String restrictions;
    private String datasetExtension;
    private String datasetItems;
    private String targetRegions;
    private List<String> sourceDatasetIds;
    private List<String> dataStoreUri;
    private Boolean qualityCheckEnabled;
    private String dataIdentifierType;
    private String dataModelId;
    private Boolean enabled = true;
    @JsonProperty("createdAt")
    @JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
    private Date createDatetime;
    private String creatorId;
    @JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
    @JsonProperty("modifiedAt")
    private Date modifyDatetime;
    private String modifierId;

    private Integer storageRetention;
    private Long topicRetention;

    private String provisioningRequestId;
	private Date provisioningEventTime;

    // Search param for UI (Admin, Dashboard)
    private String searchValue;

    private List<String> aclDatasetIds;
    private Boolean isAdminUser;
}
