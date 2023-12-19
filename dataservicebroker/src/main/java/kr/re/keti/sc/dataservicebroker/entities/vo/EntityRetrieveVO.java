package kr.re.keti.sc.dataservicebroker.entities.vo;

import java.util.List;

import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityVO;
import lombok.Data;

@Data
public class EntityRetrieveVO {
	private Integer totalCount;
	private List<CommonEntityVO> entities;
}
