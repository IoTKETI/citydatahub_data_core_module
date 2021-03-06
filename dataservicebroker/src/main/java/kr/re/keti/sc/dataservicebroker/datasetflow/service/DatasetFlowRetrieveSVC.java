package kr.re.keti.sc.dataservicebroker.datasetflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.BigDataStorageType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.StorageType;
import kr.re.keti.sc.dataservicebroker.common.exception.BadRequestException;
import kr.re.keti.sc.dataservicebroker.common.exception.InternalServerErrorException;
import kr.re.keti.sc.dataservicebroker.datamodel.DataModelManager;
import kr.re.keti.sc.dataservicebroker.datamodel.service.DataModelSVC;
import kr.re.keti.sc.dataservicebroker.datamodel.service.hive.HiveTableSVC;
import kr.re.keti.sc.dataservicebroker.datamodel.sqlprovider.BigdataTableSqlProvider;
import kr.re.keti.sc.dataservicebroker.datamodel.sqlprovider.RdbTableSqlProvider;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelBaseVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelCacheVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelVO;
import kr.re.keti.sc.dataservicebroker.dataset.service.DatasetSVC;
import kr.re.keti.sc.dataservicebroker.dataset.vo.DatasetBaseVO;
import kr.re.keti.sc.dataservicebroker.datasetflow.dao.DatasetFlowDAO;
import kr.re.keti.sc.dataservicebroker.datasetflow.vo.DatasetFlowBaseVO;
import kr.re.keti.sc.dataservicebroker.datasetflow.vo.DatasetFlowProvisioningVO;
import kr.re.keti.sc.dataservicebroker.datasetflow.vo.DatasetFlowVO;
import kr.re.keti.sc.dataservicebroker.datasetflow.vo.RetrieveDatasetFlowBaseVO;
import kr.re.keti.sc.dataservicebroker.entities.controller.kafka.consumer.KafkaConsumerManager;
import kr.re.keti.sc.dataservicebroker.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DatasetFlowRetrieveSVC {

    private DatasetFlowDAO datasetFlowDAO;

	public DatasetFlowRetrieveSVC(DatasetFlowDAO datasetFlowDAO) {
		this.datasetFlowDAO = datasetFlowDAO;
	}

	/**
     * ????????? ??? ?????? ?????? ?????? (API ?????? ???????????? ????????? ???????????? ??????)
     * @return
     */
    public List<DatasetFlowVO> getDatasetFlowVOList() {
    	
    	List<DatasetFlowBaseVO> datasetFlowBaseVOs = datasetFlowDAO.getDatasetFlowBaseVOList();

    	List<DatasetFlowVO> datasetFlowVOs = null;
    	if(datasetFlowBaseVOs != null && datasetFlowBaseVOs.size() > 0) {
    		datasetFlowVOs = new ArrayList<>(datasetFlowBaseVOs.size());
    		for(DatasetFlowBaseVO datasetFlowBaseVO : datasetFlowBaseVOs) {
    			DatasetFlowVO datasetFlowVO = datasetFlowBaseVOToDatasetFlowVO(datasetFlowBaseVO);
    			datasetFlowVOs.add(datasetFlowVO);
    		}
    	}

        return datasetFlowVOs;
    }

    /**
     * ????????? ??? ?????? ?????? ?????? (DB????????? ?????? VO ????????? ??????)
     * @return
     */
    public List<DatasetFlowBaseVO> getDatasetFlowBaseVOList() {
    	return datasetFlowDAO.getDatasetFlowBaseVOList();
    }

    /**
     * ????????? ??? ?????? ?????? By Id (API ?????? ???????????? ????????? ???????????? ??????)
     * @param datasetId ????????? ??? ?????????
     * @return
     */
    public DatasetFlowVO getDatasetFlowVOById(String datasetId) {

    	DatasetFlowBaseVO retrieveDatasetFlowBaseVO = new DatasetFlowBaseVO();
    	retrieveDatasetFlowBaseVO.setDatasetId(datasetId);

    	DatasetFlowBaseVO datasetFlowBaseVO = datasetFlowDAO.getDatasetFlowBaseVOById(retrieveDatasetFlowBaseVO);

    	DatasetFlowVO datasetFlowVO = null;
    	if(datasetFlowBaseVO != null) {
    		datasetFlowVO = datasetFlowBaseVOToDatasetFlowVO(datasetFlowBaseVO);
    	}
        return datasetFlowVO;
    }

    /**
     * ????????? ??? ?????? ?????? By Id (DB????????? ?????? VO ????????? ??????)
     * @param datasetId ????????? ??? ?????????
     * @return
     */
    public DatasetFlowBaseVO getDatasetFlowBaseVOById(String datasetId) {

    	DatasetFlowBaseVO retrieveDatasetFlowBaseVO = new DatasetFlowBaseVO();
    	retrieveDatasetFlowBaseVO.setDatasetId(datasetId);

    	return datasetFlowDAO.getDatasetFlowBaseVOById(retrieveDatasetFlowBaseVO);
    }


    /**
     * DatasetFlow dao VO ??? DatasetFlow API VO ??? ??????
     * @param datasetFlowBaseVO
     * @return
     */
    private DatasetFlowVO datasetFlowBaseVOToDatasetFlowVO(DatasetFlowBaseVO datasetFlowBaseVO) {

    	DatasetFlowVO datasetFlowVO = new DatasetFlowVO();
    	datasetFlowVO.setDatasetId(datasetFlowBaseVO.getDatasetId());
    	datasetFlowVO.setHistoryStoreType(datasetFlowBaseVO.getHistoryStoreType());
    	datasetFlowVO.setBigDataStorageTypes(datasetFlowBaseVO.getBigDataStorageTypes());
    	datasetFlowVO.setDescription(datasetFlowBaseVO.getDescription());
    	datasetFlowVO.setEnabled(datasetFlowBaseVO.getEnabled());

		return datasetFlowVO;
	}
}

