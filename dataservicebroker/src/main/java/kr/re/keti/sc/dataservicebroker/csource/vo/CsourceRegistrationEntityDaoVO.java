package kr.re.keti.sc.dataservicebroker.csource.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CsourceRegistrationEntityDaoVO {

    private String csourceRegistrationBaseId;
    private String csourceRegistrationInfoId;
    private String entityId;
    private String entityIdPattern;
    private String entityType;
}
