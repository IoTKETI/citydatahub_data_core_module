package kr.re.keti.sc.datamanager.datasetflow.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.datamanager.common.code.DataManagerCode;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ErrorCode;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ProvisionEventType;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ProvisionServerType;
import kr.re.keti.sc.datamanager.common.exception.ProvisionException;
import kr.re.keti.sc.datamanager.datasetflow.dao.DatasetFlowDAO;
import kr.re.keti.sc.datamanager.datasetflow.vo.DatasetFlowBaseVO;
import kr.re.keti.sc.datamanager.datasetflow.vo.DatasetFlowBaseVO.DatasetFlowServerDetailVO;
import kr.re.keti.sc.datamanager.datasetflow.vo.DatasetFlowProvisioningVO;
import kr.re.keti.sc.datamanager.datasetflow.vo.DatasetFlowVO;
import kr.re.keti.sc.datamanager.datasetflow.vo.DatasetFlowVO.TargetTypeVO;
import kr.re.keti.sc.datamanager.provisioning.service.ProvisioningSVC;
import kr.re.keti.sc.datamanager.provisioning.vo.ProvisionNotiVO;
import kr.re.keti.sc.datamanager.provisioning.vo.ProvisionResultVO;
import kr.re.keti.sc.datamanager.provisionserver.service.ProvisionServerSVC;
import kr.re.keti.sc.datamanager.provisionserver.vo.ProvisionServerBaseVO;
import lombok.extern.slf4j.Slf4j;

/**
 * <pre>
 * 데이터 셋 흐름 관리 서비스 클래스
 *  - 데이터 셋 흐름 정보를 생성/수정/삭제하고 기 등록된 Provisioning 대상 서버로 생성/수정/삭제 이벤트를 Provisioning 한다
 *  - 데이터 셋 흐름 정보 조회 기능을 제공한다
 * </pre>
 */
@Service
@Slf4j
public class DatasetFlowSVC {

    @Autowired
    private DatasetFlowDAO datasetFlowDAO;
    @Autowired
    private ProvisionServerSVC provisionServerSVC;
    @Autowired
	private ProvisioningSVC provisioningSVC;
    @Autowired
    private ObjectMapper objectMapper;
    
	/**
	 * 데이터 셋 흐름 생성
	 * @param datasetFlowVO 데이터 셋 흐름VO
	 * @param requestUri 요청 수신 URI
	 */
    public void createDatasetFlow(DatasetFlowVO datasetFlowVO, String requestUri) {

    	// 1. 수신 데이터 파싱
    	DatasetFlowBaseVO datasetFlowBaseVO = datasetFlowVOToDatasetFlowBaseVO(datasetFlowVO);
    	
    	// 2. Provisioning
    	ProvisionNotiVO provisionNotiVO = provisionDatasetFlow(datasetFlowBaseVO, ProvisionEventType.CREATED, requestUri);

    	// 3. 데이터 셋 흐름 생성
    	if(provisionNotiVO != null) {
    		datasetFlowBaseVO.setProvisioningRequestId(provisionNotiVO.getRequestId());
    		datasetFlowBaseVO.setProvisioningEventTime(provisionNotiVO.getEventTime());
    	}
        datasetFlowDAO.createDatasetFlow(datasetFlowBaseVO);
    }

    /**
	 * 데이터 셋 흐름 수정
	 * @param datasetFlowVO 데이터 셋 흐름VO
	 * @param requestUri 요청 수신 URI
	 */
	public int updateDatasetFlow(DatasetFlowVO datasetFlowVO, String requestUri) {

		// 1. 수신데이터 파싱
		DatasetFlowBaseVO datasetFlowBaseVO = datasetFlowVOToDatasetFlowBaseVO(datasetFlowVO);

		// 2. Provisioning
    	ProvisionNotiVO provisionNotiVO = provisionDatasetFlow(datasetFlowBaseVO, ProvisionEventType.UPDATED, requestUri);

		// 3. DatasetFlow 정보 업데이트
	    if(provisionNotiVO != null) {
    		datasetFlowBaseVO.setProvisioningRequestId(provisionNotiVO.getRequestId());
    		datasetFlowBaseVO.setProvisioningEventTime(provisionNotiVO.getEventTime());
    	}
	    return datasetFlowDAO.updateDatasetFlow(datasetFlowBaseVO);
    }

	/**
	 * 데이터 셋 흐름 삭제
	 * @param datasetFlowBaseVO 데이터 셋 흐름 기본 VO
	 * @param requestUri 요청 수신 URI
	 */
	public int deleteDatasetFlow(DatasetFlowBaseVO datasetFlowBaseVO, String requestUri) {

 		// 1. 삭제 Provisioning
    	provisionDatasetFlow(datasetFlowBaseVO, ProvisionEventType.DELETED, requestUri);

        // 2. DatasetFlow 삭제
        return datasetFlowDAO.deleteDatasetFlow(datasetFlowBaseVO);
        
    }

	/**
	 * 데이터 셋 흐름 Provisioning
	 * @param datasetFlowBaseVO 데이터 셋 흐름VO
	 * @param requestUri 요청 수신 URI
	 */
	public int provisioningDatasetFlow(DatasetFlowBaseVO datasetFlowBaseVO, String requestUri) {

		// 1. Provisioning
    	ProvisionNotiVO provisionNotiVO = provisionDatasetFlow(datasetFlowBaseVO, ProvisionEventType.UPDATED, requestUri);

		// 3. DatasetFlow 정보 업데이트
    	DatasetFlowBaseVO updateDatasetFlowBaseVO = new DatasetFlowBaseVO();
    	updateDatasetFlowBaseVO.setDatasetId(datasetFlowBaseVO.getDatasetId());
    	updateDatasetFlowBaseVO.setProvisioningRequestId(provisionNotiVO.getRequestId());
    	updateDatasetFlowBaseVO.setProvisioningEventTime(provisionNotiVO.getEventTime());
    	int result = datasetFlowDAO.updateDatasetFlowProvisioning(updateDatasetFlowBaseVO);
	    return result;
    }
	
	/**
	 * 데이터 셋 흐름 조회
	 * @return 데이터 셋 흐름 리스트
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
     * 데이터 셋 흐름 조회 by ID
     * @param datasetId 데이터 셋 아이디
     * @return DatasetFlowBaseVO 데이터 셋 흐름 기본 VO
     */
    public DatasetFlowBaseVO getDatasetFlowBaseVOById(String datasetId) {

    	DatasetFlowBaseVO retrieveDatasetFlowBaseVO = new DatasetFlowBaseVO();
    	retrieveDatasetFlowBaseVO.setDatasetId(datasetId);

    	return datasetFlowDAO.getDatasetFlowBaseVOById(retrieveDatasetFlowBaseVO);
    }

    /**
     * 데이터 셋 흐름 조회 by ID
     * @param datasetId 데이터 셋 아이디
     * @return DatasetFlowVO 데이터 셋 흐름 VO
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
     * 데이터 셋 흐름 Provisioning
     * @param datasetFlowBaseVO 데이터 셋 흐름 기본 VO
     * @param provisionEventType Provisioning 이벤트 유형
     * @param requestUri 요청 수신 URI
     * @throws ProvisionException Provisioning 시 발생 Exception
     */
	public ProvisionNotiVO provisionDatasetFlow(DatasetFlowBaseVO datasetFlowBaseVO, 
			ProvisionEventType provisionEventType, String requestUri) throws ProvisionException {

		// 1. 데이터 셋 흐름 서버 상세 정보 조회
		List<DatasetFlowServerDetailVO> datasetFlowServerDetailVOs = datasetFlowBaseVO.getDatasetFlowServerDetailVOs();
		if(datasetFlowServerDetailVOs == null || datasetFlowServerDetailVOs.size() == 0) {
			return null;
		}

		// 2. create provisioning vo
		ProvisionNotiVO provisionNotiVO = new ProvisionNotiVO();
		provisionNotiVO.setRequestId(UUID.randomUUID().toString());
		provisionNotiVO.setEventTime(new Date());
		provisionNotiVO.setEventType(provisionEventType);
		provisionNotiVO.setTo(requestUri);

		for(DatasetFlowServerDetailVO datasetFlowServerDetailVO : datasetFlowServerDetailVOs) {

			// 2-1. set provisioning data
			String provisioningData = null;
			if(provisionEventType != ProvisionEventType.DELETED) {
				DatasetFlowProvisioningVO provisioningVO = new DatasetFlowProvisioningVO();
				provisioningVO.setDatasetId(datasetFlowBaseVO.getDatasetId());
				provisioningVO.setDescription(datasetFlowBaseVO.getDescription());
				provisioningVO.setHistoryStoreType(datasetFlowBaseVO.getHistoryStoreType());
				provisioningVO.setEnabled(datasetFlowBaseVO.getEnabled());
				provisioningVO.setBigDataStorageTypes(datasetFlowServerDetailVO.getBigDataStorageTypes());
				
				try {
					provisioningData = objectMapper.writeValueAsString(provisioningVO);
					provisionNotiVO.setData(provisioningData);
				} catch (JsonProcessingException e) {
					throw new ProvisionException(ErrorCode.PROVISIONING_ERROR, 
							HttpStatus.BAD_REQUEST.value(), "Request message parsing error.", e);
				}
			}

			// 3. Provisioning
			provisionDatasetFlow(provisionNotiVO, datasetFlowServerDetailVO.getProvisionServerType());
		}
		
		return provisionNotiVO;
    }
	
	/**
     * 데이터 셋 흐름 정보 Provisioning
     * @param provisionNotiVO Provisining 전송 VO
     * @param provisionServerType Provisioning 대상 서버 유형
     */
	private void provisionDatasetFlow(ProvisionNotiVO provisionNotiVO, ProvisionServerType provisionServerType) {
		
		// 1. Provisioning 대상 서버 조회
		List<ProvisionServerBaseVO> provisionServerVOs = provisionServerSVC.getProvisionServerVOByType(provisionServerType);
		
		// 2. Provisioning 처리
		List<ProvisionResultVO> provisionResultVOs = provisioningSVC.provisioning(provisionServerType, 
				provisionServerVOs, provisionNotiVO, DataManagerCode.ProvisioningSubUri.DATASET_FLOW);
		
		log.info("DatasetFlow Provisioning Result. {}", provisionResultVOs);

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
	
//	/**
//	 * 데이터 셋 흐름 수정 시 Provisioning
//	 *  - 이전 데이터와 비교하여 CREATE, UPDATE, DELETE 여부를 판단하여 Provisioning 전송
//	 * @param datasetFlowBaseVO 데이터 셋 흐름 기본 VO
//	 * @param beforeDatasetFlowBaseVO 업데이트 이전 데이터 셋 흐름 기본 VO
//	 * @param requestUri 요청 수신 URI
//	 * @throws ProvisionException Provisioning 처리 시 발생한 Exception 
//	 */
//	public ProvisionNotiVO provisionDatasetFlowForUpdate(DatasetFlowBaseVO datasetFlowBaseVO, 
//			DatasetFlowBaseVO beforeDatasetFlowBaseVO, String requestUri) throws ProvisionException {
//
//		// 1. Provisioning Data Parsing
//		ProvisionNotiVO provisionNotiVO = new ProvisionNotiVO();
//		provisionNotiVO.setRequestId(UUID.randomUUID().toString());
//		provisionNotiVO.setEventTime(new Date());
//		provisionNotiVO.setEventType(ProvisionEventType.UPDATED);
//		provisionNotiVO.setTo(requestUri);
//
//		// 2. ServerType별 CREATE, UPDATE, DELETE 여부 판단
//		Map<ProvisionServerType, ProvisionEventType> operationByServerTypeMap = getOperationByServerType(
//				beforeDatasetFlowBaseVO.getDatasetFlowServerDetailVOs(), datasetFlowBaseVO.getDatasetFlowServerDetailVOs());
//
//		// 3. ServerType별 처리
//		for(Map.Entry<ProvisionServerType, ProvisionEventType> entry : operationByServerTypeMap.entrySet()) {
//
//			ProvisionServerType provisionServerType = entry.getKey();
//			ProvisionEventType provisionEventType = entry.getValue();
//			
//			provisionNotiVO.setEventType(provisionEventType);
//			// 3-1. set provisioning data
//			String provisioningData = null;
//			if(provisionEventType != ProvisionEventType.DELETED) {
//				DatasetFlowProvisioningVO provisioningVO = new DatasetFlowProvisioningVO();
//				provisioningVO.setDatasetId(datasetFlowBaseVO.getDatasetId());
//				provisioningVO.setDescription(datasetFlowBaseVO.getDescription());
//				provisioningVO.setHistoryStoreType(datasetFlowBaseVO.getHistoryStoreType());
//				provisioningVO.setEnabled(datasetFlowBaseVO.getEnabled());
//				for(DatasetFlowServerDetailVO detailVO : datasetFlowBaseVO.getDatasetFlowServerDetailVOs()) {
//					if(detailVO.getProvisionServerType() == provisionServerType) {
//						provisioningVO.setBigDataStorageTypes(detailVO.getBigDataStorageTypes());
//					}
//				}
//
//				try {
//					provisioningData = objectMapper.writeValueAsString(provisioningVO);
//					provisionNotiVO.setData(provisioningData);
//				} catch (JsonProcessingException e) {
//					throw new ProvisionException(ErrorCode.PROVISIONING_ERROR, HttpStatus.BAD_REQUEST.value(), "Request message parsing error.", e);
//				}
//			}
//
//			// 4. Provisioning
//			provisionDatasetFlow(provisionNotiVO, provisionServerType);
//		}
//		
//		return provisionNotiVO;
//    }
//
//	/**
//	 * Provisioning 대상 ServerType별 CREATE, UPDATE, DELETE 여부 판단
//	 * @param beforeDatasetFlowServerDetailVOs 업데이트 이전 데이터 셋 흐름 상세 VO
//	 * @param datasetFlowServerDetailVOs 업데이트할 데이터 셋 흐름 상세 VO
//	 * @return Map<ProvisionServerType, ProvisionEventType> Provisioning 대상 서버 유형 별 처리 이벤트 유형 Map
//	 */
//    private Map<ProvisionServerType, ProvisionEventType> getOperationByServerType(
//    		List<DatasetFlowServerDetailVO> beforeDatasetFlowServerDetailVOs, List<DatasetFlowServerDetailVO> datasetFlowServerDetailVOs) {
//    	
//    	Map<ProvisionServerType, ProvisionEventType> operationMap = new HashMap<>();
//		// 1. update 이전 데이터 셋 흐름 서버 상세 정보 조회
//    	if(beforeDatasetFlowServerDetailVOs != null) {
//    		for(DatasetFlowServerDetailVO datasetFlowServerDetailVO : beforeDatasetFlowServerDetailVOs) {
//    			// 기존에 존재하던 ServerType 리스트업 (일단 DELETE로 세팅)
//    			operationMap.put(datasetFlowServerDetailVO.getProvisionServerType(), ProvisionEventType.DELETED);
//    		}    		
//    	}
//
//    	if(beforeDatasetFlowServerDetailVOs != null) {
//    		// 2. update 이후 데이터 셋 흐름 서버 상세 정보 조회
//    		for(DatasetFlowServerDetailVO datasetFlowServerDetailVO : datasetFlowServerDetailVOs) {
//
//    			ProvisionServerType serverType = datasetFlowServerDetailVO.getProvisionServerType();
//
//    			// 동일한 서버타입에 대해 업데이트가 발생한 경우 : UPDATE
//    			if(operationMap.containsKey(serverType)) {
//    				operationMap.put(serverType, ProvisionEventType.UPDATED);
//
//    			// before 서버타입에 없던 서버타입이 추가된 경우 : CREATE
//    			} else if(!operationMap.containsKey(serverType)) {
//    				operationMap.put(serverType, ProvisionEventType.CREATED);
//    			}
//    		}
//    	}
//
//		return operationMap;
//	}

	/**
     * DatasetFlow API 요청 파라미터를 DB입력 VO 로 변환
     * @param datasetFlowVO
     * @return
     */
    private DatasetFlowBaseVO datasetFlowVOToDatasetFlowBaseVO(DatasetFlowVO datasetFlowVO) {
    	
    	DatasetFlowBaseVO datasetFlowBaseVO = new DatasetFlowBaseVO();
    	datasetFlowBaseVO.setDatasetId(datasetFlowVO.getDatasetId());
    	datasetFlowBaseVO.setHistoryStoreType(datasetFlowVO.getHistoryStoreType());
    	datasetFlowBaseVO.setEnabled(datasetFlowVO.getEnabled());
    	datasetFlowBaseVO.setDescription(datasetFlowVO.getDescription());

    	List<TargetTypeVO> targetTypeVOs = datasetFlowVO.getTargetTypes();
    	if(targetTypeVOs != null) {
    		for(TargetTypeVO targetTypeVO : targetTypeVOs) {
    			DatasetFlowServerDetailVO datasetFlowServerDetailVO = new DatasetFlowServerDetailVO();
    	    	datasetFlowServerDetailVO.setDatasetId(datasetFlowVO.getDatasetId());
    	    	datasetFlowServerDetailVO.setProvisionServerType(targetTypeVO.getType());
    	    	datasetFlowServerDetailVO.setBigDataStorageTypes(targetTypeVO.getBigDataStorageTypes());

    	    	if(datasetFlowBaseVO.getDatasetFlowServerDetailVOs() == null) {
    	    		datasetFlowBaseVO.setDatasetFlowServerDetailVOs(new ArrayList<>(targetTypeVOs.size()));
    	    	}
    	    	datasetFlowBaseVO.getDatasetFlowServerDetailVOs().add(datasetFlowServerDetailVO);
    		}
    	}
    	
		return datasetFlowBaseVO;
	}

    /**
     * DatasetFlow dao VO 를 DatasetFlow API VO 로 변환
     * @param datasetFlowBaseVO
     * @return
     */
    private DatasetFlowVO datasetFlowBaseVOToDatasetFlowVO(DatasetFlowBaseVO datasetFlowBaseVO) {

    	DatasetFlowVO datasetFlowVO = new DatasetFlowVO();
    	datasetFlowVO.setDatasetId(datasetFlowBaseVO.getDatasetId());
    	datasetFlowVO.setDescription(datasetFlowBaseVO.getDescription());
    	datasetFlowVO.setHistoryStoreType(datasetFlowBaseVO.getHistoryStoreType());
    	datasetFlowVO.setEnabled(datasetFlowBaseVO.getEnabled());

    	List<DatasetFlowServerDetailVO> datasetFlowServerDetailVOs = datasetFlowBaseVO.getDatasetFlowServerDetailVOs();
    	if(datasetFlowServerDetailVOs != null) {
    		datasetFlowVO.setTargetTypes(new ArrayList<>(datasetFlowServerDetailVOs.size()));
    		for(DatasetFlowServerDetailVO datasetFlowServerDetailVO : datasetFlowServerDetailVOs) {
    			TargetTypeVO targetTypeVO = new TargetTypeVO();
    			targetTypeVO.setType(datasetFlowServerDetailVO.getProvisionServerType());
    			targetTypeVO.setBigDataStorageTypes(datasetFlowServerDetailVO.getBigDataStorageTypes());
    			datasetFlowVO.getTargetTypes().add(targetTypeVO);
    		}
    	}
    	
		return datasetFlowVO;
	}
}

