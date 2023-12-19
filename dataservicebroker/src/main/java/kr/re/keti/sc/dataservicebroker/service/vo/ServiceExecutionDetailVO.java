package kr.re.keti.sc.dataservicebroker.service.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ServiceExecutionStatus;
import lombok.Data;

@Data
public class ServiceExecutionDetailVO {
	private String id;
	private String serviceId;
	private String executionId;
	private String name;
	private String entityId;
	private String entityType;
	private String type;
	private String input;
	private ServiceExecutionStatus status;
	@JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
    private Date createDatetime;
    private String creatorId;
    @JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
	private Date modifyDatetime;
    private String modifierId;
}
