package kr.re.keti.sc.dataservicebroker.csource.vo;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.vo.GeoPropertyVO;
import kr.re.keti.sc.dataservicebroker.common.vo.TimeIntervalVO;
import kr.re.keti.sc.dataservicebroker.util.TimeIntervalValueNullFilter;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CsourceRegistrationVO {

	@JsonProperty("@context")
    private List<String> context;
    private String id;
    private String type = DataServiceBrokerCode.JsonLdType.CSOURCE_REGISTRATION.getCode();
    private String name;
    private String description;
    private List<Information> information;
    private GeoPropertyVO location;
    private GeoPropertyVO observationSpace;
    private GeoPropertyVO operationSpace;
    private Date expiresAt;
    private List<String> supportedAggregationMethod;
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = TimeIntervalValueNullFilter.class)
    private TimeIntervalVO observationInterval;
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = TimeIntervalValueNullFilter.class)
    private TimeIntervalVO managementInterval;
    private String endpoint;
    private Object scope; // spec: string or string[]

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Information {
    	private List<EntityInfo> entities;
    	private List<String> propertyNames;
    	private List<String> relationshipNames;
    }
    
    // ETSI GS CIM 009 - 5.2.8 EntityInfo
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EntityInfo {
        private String id;
        private String idPattern;
        private String type;
    }

    // ETSI GS CIM 009 - 5.2.13 GeoQuery
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeoQuery {
        private String geometry;
        private Object coordinates;
        private String georel;
        private String geoproperty;
    }
}
