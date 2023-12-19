package kr.re.keti.sc.ingestinterface.verificationhistory.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import kr.re.keti.sc.ingestinterface.common.vo.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Verification history DB VO class
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public class VerificationHistoryBaseVO extends PageRequest {

    Long seq;
    Date testTime;
    String startTime;
    String endTime;

    String datasetId;
    String dataModelId;
    String entityId;

    Boolean verified;
    String errorCode;
    String errorCause;
    String data;

    String smartSearchValue;
    Integer successCount;
    Integer failureCount;
}
