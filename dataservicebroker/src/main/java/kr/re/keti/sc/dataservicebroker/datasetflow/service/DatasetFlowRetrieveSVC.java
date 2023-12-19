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
     * 데이터 셋 흐름 목록 조회 (API 에서 사용하는 형태로 변환하여 반환)
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
     * 데이터 셋 흐름 목록 조회 (DB테이블 형태 VO 그대로 반환)
     * @return
     */
    public List<DatasetFlowBaseVO> getDatasetFlowBaseVOList() {
    	return datasetFlowDAO.getDatasetFlowBaseVOList();
    }

    /**
     * 데이터 셋 흐름 조회 By Id (API 에서 사용하는 형태로 변환하여 반환)
     * @param datasetId 데이터 셋 아이디
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
     * 데이터 셋 흐름 조회 By Id (DB테이블 형태 VO 그대로 반환)
     * @param datasetId 데이터 셋 아이디
     * @return
     */
    public DatasetFlowBaseVO getDatasetFlowBaseVOById(String datasetId) {

    	DatasetFlowBaseVO retrieveDatasetFlowBaseVO = new DatasetFlowBaseVO();
    	retrieveDatasetFlowBaseVO.setDatasetId(datasetId);

    	return datasetFlowDAO.getDatasetFlowBaseVOById(retrieveDatasetFlowBaseVO);
    }


    /**
     * DatasetFlow dao VO 를 DatasetFlow API VO 로 변환
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

