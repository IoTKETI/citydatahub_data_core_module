package kr.re.keti.sc.dataservicebroker.csource.vo;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.AttributeValueType;
import lombok.Data;

/**
 * 5.2.9 CsourceRegistration
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CsourceRegistrationBaseDaoVO {

    @JsonProperty("@context")
    private List<String> context;
    private String id;
    private String type = DataServiceBrokerCode.JsonLdType.CSOURCE_REGISTRATION.getCode();
    private String name;
    private String description;
    private List<CsourceRegistrationInfoDaoVO> information;
    private Date observationIntervalStart;
    private Date observationIntervalEnd;
    private Date managementIntervalStart;
    private Date managementIntervalEnd;
    private Object location;
    private Object observationSpace;
    private Object operationSpace;
    private Date expires;
    private String endpoint;
    private List<String> supportedAggregationMethod;
    private List<String> scope;
    private AttributeValueType scopeDataType; // scope 은 ARRAY_STRING or STRING 이 올 수 있음
}
