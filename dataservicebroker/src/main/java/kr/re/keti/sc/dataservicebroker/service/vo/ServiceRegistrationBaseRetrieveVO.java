package kr.re.keti.sc.dataservicebroker.service.vo;

import lombok.Data;

@Data
public class ServiceRegistrationBaseRetrieveVO extends ServiceRegistrationBaseVO {
	private Integer limit;
	private Integer offset;
}
