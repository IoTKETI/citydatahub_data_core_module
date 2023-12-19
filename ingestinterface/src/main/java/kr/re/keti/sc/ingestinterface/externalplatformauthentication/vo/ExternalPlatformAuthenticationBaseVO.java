package kr.re.keti.sc.ingestinterface.externalplatformauthentication.vo;

import com.fasterxml.jackson.annotation.*;
import kr.re.keti.sc.ingestinterface.common.code.Constants;
import kr.re.keti.sc.ingestinterface.common.vo.PageRequest;

import java.util.Date;
import java.util.List;

/**
 * External platform authentication DB VO class
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalPlatformAuthenticationBaseVO extends PageRequest {

    private String id;
    private String name;
    private String description;
    private List<String> receptionIps;
    private List<String> receptionDatasetIds;
    private String dataInstancePrefix;
    private List<String> receptionClientIds;

    @JsonProperty("createdAt")
    @JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
    private Date createDatetime;

    @JsonIgnore
    private String creatorId;

    @JsonProperty("modifiedAt")
    @JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
    private String modifyDatetime;
    private String modifierId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getReceptionIps() {
        return receptionIps;
    }

    public void setReceptionIps(List<String> receptionIps) {
        this.receptionIps = receptionIps;
    }

    public List<String> getReceptionDatasetIds() {
        return receptionDatasetIds;
    }

    public void setReceptionDatasetIds(List<String> receptionDatasetIds) {
        this.receptionDatasetIds = receptionDatasetIds;
    }

    public String getDataInstancePrefix() {
        return dataInstancePrefix;
    }

    public void setDataInstancePrefix(String dataInstancePrefix) {
        this.dataInstancePrefix = dataInstancePrefix;
    }

    public List<String> getReceptionClientIds() {
        return receptionClientIds;
    }

    public void setReceptionClientIds(List<String> receptionClientIds) {
        this.receptionClientIds = receptionClientIds;
    }

    public Date getCreateDatetime() {
        return createDatetime;
    }

    public void setCreateDatetime(Date createDatetime) {
        this.createDatetime = createDatetime;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getModifyDatetime() {
        return modifyDatetime;
    }

    public void setModifyDatetime(String modifyDatetime) {
        this.modifyDatetime = modifyDatetime;
    }

    public String getModifierId() {
        return modifierId;
    }

    public void setModifierId(String modifierId) {
        this.modifierId = modifierId;
    }
}
