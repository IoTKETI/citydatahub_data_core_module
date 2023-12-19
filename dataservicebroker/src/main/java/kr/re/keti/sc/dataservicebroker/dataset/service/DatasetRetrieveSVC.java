package kr.re.keti.sc.dataservicebroker.dataset.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.dataservicebroker.common.exception.BadRequestException;
import kr.re.keti.sc.dataservicebroker.datamodel.DataModelManager;
import kr.re.keti.sc.dataservicebroker.dataset.dao.DatasetDAO;
import kr.re.keti.sc.dataservicebroker.dataset.vo.DatasetBaseVO;
import kr.re.keti.sc.dataservicebroker.datasetflow.service.DatasetFlowSVC;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class DatasetRetrieveSVC {

    private final DatasetDAO datasetDAO;

	public DatasetRetrieveSVC(DatasetDAO datasetDAO) {
		this.datasetDAO = datasetDAO;
	}

	public List<DatasetBaseVO> getDatasetVOList() {
        return datasetDAO.getDatasetVOList();
    }

    public DatasetBaseVO getDatasetVOById(String id) {
        return datasetDAO.getDatasetVOById(id);
    }
}
