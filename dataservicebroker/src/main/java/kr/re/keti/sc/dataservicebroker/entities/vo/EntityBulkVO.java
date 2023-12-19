package kr.re.keti.sc.dataservicebroker.entities.vo;

import java.util.List;

import kr.re.keti.sc.dataservicebroker.common.vo.entities.DynamicEntityDaoVO;
import lombok.Data;
@Data   
public class EntityBulkVO {
    private String tableName;
    private List<DynamicEntityDaoVO> entityDaoVOList;
    private List<String> tableColumns;
}
