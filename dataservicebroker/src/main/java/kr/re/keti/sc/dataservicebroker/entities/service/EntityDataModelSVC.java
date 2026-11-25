package kr.re.keti.sc.dataservicebroker.entities.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.re.keti.sc.dataservicebroker.datamodel.DataModelManager;
import kr.re.keti.sc.dataservicebroker.entities.dao.EntityDataModelDAO;
import kr.re.keti.sc.dataservicebroker.entities.vo.EntityDataModelVO;

@Service
public class EntityDataModelSVC {

    @Autowired
    private EntityDataModelDAO entityDataModelDAO;

    public void createEntityDataModel(EntityDataModelVO entityDataModelVO) {
    	entityDataModelDAO.createEntityDataModel(entityDataModelVO);
    }

    /**
     * Entity별 데이터모델 매핑 원자적 upsert (신규 INSERT / 변경 시 UPDATE / 동일하면 무시)
     */
    public void upsertEntityDataModel(EntityDataModelVO entityDataModelVO) {
    	entityDataModelDAO.upsertEntityDataModel(entityDataModelVO);
    }

    public void updateEntityDataModel(EntityDataModelVO entityDataModelVO) {
    	entityDataModelDAO.updateEntityDataModel(entityDataModelVO);
    }
    
    public void deleteEntityDataModel(String id) {
    	entityDataModelDAO.deleteEntityDataModel(id);
    }

    public List<EntityDataModelVO> getEntityDataModelVOList() {
        return entityDataModelDAO.getEntityDataModelVOList();
    }

    public EntityDataModelVO getEntityDataModelVOById(String id) {
        return entityDataModelDAO.getEntityDataModelVOById(id);
    }
}

