package kr.re.keti.sc.datamanager.dataset.service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ErrorCode;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ProvisionEventType;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ProvisionServerType;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ProvisioningSubUri;
import kr.re.keti.sc.datamanager.common.exception.ProvisionException;
import kr.re.keti.sc.datamanager.dataset.dao.DatasetDAO;
import kr.re.keti.sc.datamanager.dataset.vo.DatasetBaseVO;
import kr.re.keti.sc.datamanager.datasetflow.dao.DatasetFlowDAO;
import kr.re.keti.sc.datamanager.datasetflow.vo.DatasetFlowBaseVO;
import kr.re.keti.sc.datamanager.provisioning.service.ProvisioningSVC;
import kr.re.keti.sc.datamanager.provisioning.service.ProvisioningSVC.KafkaProvisioningType;
import kr.re.keti.sc.datamanager.provisioning.vo.ProvisionNotiVO;
import kr.re.keti.sc.datamanager.provisioning.vo.ProvisionResultVO;
import kr.re.keti.sc.datamanager.provisionserver.service.ProvisionServerSVC;
import kr.re.keti.sc.datamanager.provisionserver.vo.ProvisionServerBaseVO;
import lombok.extern.slf4j.Slf4j;

/**
 * <pre>
 * 데이터 셋 관리 서비스 클래스
 *  - 데이터 셋 정보를 생성/수정/삭제하고 기 등록된 Provisioning 대상 서버로 생성/수정/삭제 이벤트를 Provisioning 한다
 *  - 데이터 셋 정보 조회 기능을 제공한다
 * </pre>
 */
@Service
@Slf4j
public class DatasetSVC {

    @Autowired
    private DatasetDAO datasetDAO;
    @Autowired
    private DatasetFlowDAO datasetFlowDAO;
    @Autowired
    private ProvisionServerSVC provisionServerSVC;
    @Autowired
    private ProvisioningSVC provisioningSVC;
    @Autowired
    private ObjectMapper objectMapper;

    public int createDatasetBaseVO(DatasetBaseVO datasetBaseVO) {
        return datasetDAO.createDatasetBaseVO(datasetBaseVO);
    }

	public int updateDatasetBaseVO(DatasetBaseVO datasetBaseVO) {
		return datasetDAO.updateDatasetBaseVO(datasetBaseVO);
    }

	public int updateDatasetProvisioning(DatasetBaseVO datasetBaseVO) {
		return datasetDAO.updateDatasetProvisioning(datasetBaseVO);
    }

    public int deleteDatasetBaseVO(String id) {
    	return datasetDAO.deleteDatasetBaseVO(id);
    }

    public List<DatasetFlowBaseVO> getEnabledDatasetByDatasetId(String id) {
    	return datasetFlowDAO.getEnabledDatasetByDatasetId(id);
    }

    public List<DatasetBaseVO> getDatasetVOList(DatasetBaseVO datasetBaseVO) {
        return datasetDAO.getDatasetVOList(datasetBaseVO);
    }

	public Integer getDatasetVOListTotalCount(DatasetBaseVO datasetBaseVO) {
		return datasetDAO.getDatasetVOListTotalCount(datasetBaseVO);
	}

	public List<DatasetBaseVO> getDatasetVOListForUI(DatasetBaseVO datasetBaseVO) {
		return datasetDAO.getDatasetVOListForUI(datasetBaseVO);
	}

	public Integer getDatasetVOListTotalCountForUI(DatasetBaseVO datasetBaseVO) {
		return datasetDAO.getDatasetVOListTotalCountForUI(datasetBaseVO);
	}

    public DatasetBaseVO getDatasetVOById(String id) {
        return datasetDAO.getDatasetVOById(id);
    }

    /**
     * 데이터 셋 정보 Provisioning
     * @param datasetBaseVO 데이터셋기본VO
     * @param provisionEventType Provision이벤트유형
     * @param requestUri 요청URI
     */
    public ProvisionNotiVO provisionDataset(DatasetBaseVO datasetBaseVO, ProvisionEventType provisionEventType, String requestUri) {

    	// 1. Provisioning 전송 데이터 생성
		String provisioningData = null;
		if(datasetBaseVO != null) {
			try {
				provisioningData = objectMapper.writeValueAsString(datasetBaseVO);
			} catch (JsonProcessingException e) {
				throw new ProvisionException(ErrorCode.PROVISIONING_ERROR, HttpStatus.BAD_REQUEST.value(), "Request message parsing error.", e);
			}
		}
		ProvisionNotiVO provisionNotiVO = new ProvisionNotiVO();
		provisionNotiVO.setRequestId(UUID.randomUUID().toString());
		provisionNotiVO.setEventTime(new Date());
		provisionNotiVO.setEventType(provisionEventType);
		provisionNotiVO.setTo(requestUri);
		provisionNotiVO.setData(provisioningData);

		// 2. Provisioning
    	// 2-1. DataServiceBroker
		provisionDataset(provisionNotiVO, ProvisionServerType.DATA_SERVICE_BROKER);
    	
    	// 2-2. IngestInterface
    	provisionDataset(provisionNotiVO, ProvisionServerType.INGEST_INTERFACE);

    	// 3. Kafka Event 전송
    	provisioningSVC.sendKafkaEvent(KafkaProvisioningType.DATASET, provisionNotiVO);
    	
    	return provisionNotiVO;
	}

    /**
     * 데이터 셋 정보 Provisioning
     * @param provisionNotiVO Provisioning 전송 데이터 VO
     * @param provisionServerType Provisioning 서버유형
     */
    private void provisionDataset(ProvisionNotiVO provisionNotiVO, ProvisionServerType provisionServerType) {
		
		// 1. Provisioning 대상 서버 조회
		List<ProvisionServerBaseVO> provisionServerVOs = provisionServerSVC.getProvisionServerVOByType(provisionServerType);
		
		// 2. Provisioning 처리
		List<ProvisionResultVO> provisionResultVOs = provisioningSVC.provisioning(provisionServerType, 
				provisionServerVOs, provisionNotiVO, ProvisioningSubUri.DATASET);
		
		log.info("Dataset Provisioning Result. {}", provisionResultVOs);

		// 3. 결과 처리
		boolean processResult = false;
		
		// Provisionig 대상 미 존재하므로 성공 처리
		if(provisionResultVOs == null || provisionResultVOs.size() == 0) {
			processResult = true;
		} else {
			
			// 동일한 타입군 서버 중 1대라도 Provisioning 성공 시 성공으로 판단
			for(ProvisionResultVO provisionResultVO : provisionResultVOs) {
				if(provisionResultVO.getResult()) {
					processResult = true;
				}
			}
		}

		// 동일한 타입군 서버 전체 실패 시 첫번 째 Exception throw
		// TODO: 에러내역 전파에서 DataCore UI 에서 사용자에게 정보 전달
		if(!processResult) {
			for(ProvisionResultVO provisionResultVO : provisionResultVOs) {
				if(!provisionResultVO.getResult()) {
					throw provisionResultVO.getProvisionException();
				}
			}
		}
	}
}

