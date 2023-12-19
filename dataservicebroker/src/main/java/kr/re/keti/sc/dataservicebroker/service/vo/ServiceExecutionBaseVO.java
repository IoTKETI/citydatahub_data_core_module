package kr.re.keti.sc.dataservicebroker.service.vo;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import lombok.Data;

@Data
public class ServiceExecutionBaseVO {
	private String id;
	private String serviceId;
	private String type;
	@JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
    private Date createDatetime;
    private String creatorId;
    @JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
	private Date modifyDatetime;
    private String modifierId;

    private List<ServiceExecutionDetailVO> serviceExecutionDetailVOs;
}
