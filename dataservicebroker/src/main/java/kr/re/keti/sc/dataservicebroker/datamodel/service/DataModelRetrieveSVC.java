package kr.re.keti.sc.dataservicebroker.datamodel.service;

import kr.re.keti.sc.dataservicebroker.datamodel.dao.DataModelDAO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelBaseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DataModelRetrieveSVC {
	
    private final DataModelDAO dataModelDAO;

    public DataModelRetrieveSVC(DataModelDAO dataModelDAO) {
        this.dataModelDAO = dataModelDAO;
    }

    public List<DataModelBaseVO> getDataModelBaseVOList() {
        return dataModelDAO.getDataModelBaseVOList();
    }

    public DataModelBaseVO getDataModelBaseVOById(String dataModelId) {
        DataModelBaseVO dataModelBaseVO = new DataModelBaseVO();
        dataModelBaseVO.setId(dataModelId);
        return dataModelDAO.getDataModelBaseVOById(dataModelBaseVO);
    }
}
