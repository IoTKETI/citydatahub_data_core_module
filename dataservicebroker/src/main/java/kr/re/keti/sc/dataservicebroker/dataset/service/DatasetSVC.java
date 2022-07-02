package kr.re.keti.sc.dataservicebroker.dataset.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kr.re.keti.sc.dataservicebroker.datasetflow.service.DatasetFlowRetrieveSVC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.dataservicebroker.common.exception.BadRequestException;
import kr.re.keti.sc.dataservicebroker.datamodel.DataModelManager;
import kr.re.keti.sc.dataservicebroker.dataset.dao.DatasetDAO;
import kr.re.keti.sc.dataservicebroker.dataset.vo.DatasetBaseVO;
import kr.re.keti.sc.dataservicebroker.datasetflow.service.DatasetFlowSVC;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DatasetSVC {

    private final DatasetDAO datasetDAO;
    private final DatasetFlowRetrieveSVC datasetFlowRetrieveSVC;
    private final DataModelManager dataModelManager;
    private final ObjectMapper objectMapper;

    /** dataset request uri pattern */
    private final Pattern URI_PATTERN_DATASET = Pattern.compile("/datasets/(?<datasetId>.+)");

	public DatasetSVC(
			DatasetDAO datasetDAO,
			DatasetFlowRetrieveSVC datasetFlowRetrieveSVC,
			DataModelManager dataModelManager,
			ObjectMapper objectMapper
	) {
		this.datasetDAO = datasetDAO;
		this.datasetFlowRetrieveSVC = datasetFlowRetrieveSVC;
		this.dataModelManager = dataModelManager;
		this.objectMapper = objectMapper;
	}

	/**
     * 데이터 셋 정보 생성
     * @param requestBody 요청 Body
     * @param requestId Provisioning Request Id
     * @param eventTime Provisioning Request Time
     */
    public void createDataset(String requestBody, String requestId, Date eventTime) {
    	// 1. 수신 데이터 파싱
    	DatasetBaseVO datasetBaseVO = null;
		try {
			datasetBaseVO = objectMapper.readValue(requestBody, DatasetBaseVO.class);
		} catch (IOException e) {
			throw new BadRequestException(ErrorCode.INVALID_PARAMETER,
                    "Invalid Parameter. body=" + requestBody);
		}

		// 2. 기 존재여부 확인
		DatasetBaseVO retrieveDatasetBaseVO = datasetDAO.getDatasetVOById(datasetBaseVO.getId());
		if(retrieveDatasetBaseVO != null) {
			// 3. 이중화되어 있는 다른 IngestInterface 인스턴스에서 DB 입력 했는지 체크
	 		if(alreadyProcessByOtherInstance(requestId, eventTime, retrieveDatasetBaseVO)) {
	         	// 다른 Instance에서 DB 업데이트 이미 처리했으므로 캐쉬만 로딩하고 성공 처리
	 			dataModelManager.putDatasetCache(retrieveDatasetBaseVO);
	            return;
	        } else {
	        	// 이미 존재하므로 업데이트 처리
	        	updateDataset("/datasets/"+datasetBaseVO.getId(), requestBody, requestId, eventTime);
	        	return;
	        }
		}

    	// 4. 데이터셋 정보 저장
 		datasetBaseVO.setProvisioningRequestId(requestId);
 		datasetBaseVO.setProvisioningEventTime(eventTime);
        datasetDAO.createDataset(datasetBaseVO);
        
        // 5. 데이터셋 캐쉬 로딩
        dataModelManager.putDatasetCache(datasetBaseVO);

    }

    /**
     * 데이터 셋 정보 수정
     * @param to 데이터 셋 정보 수정 요청 수신 url
     * @param requestBody 요청 Body
     * @param requestId Provisioning Request Id
     * @param eventTime Provisioning Request Time
     */
    public void updateDataset(String to, String requestBody, String requestId, Date eventTime) {
    	// 1. Request URI에서 식별자 추출
		Matcher matcherForUpdate = URI_PATTERN_DATASET.matcher(to);

    	if(matcherForUpdate.find()) {
			String datasetId = matcherForUpdate.group("datasetId");

			// 2. 수신 데이터 파싱
	    	DatasetBaseVO datasetBaseVO = null;
			try {
				datasetBaseVO = objectMapper.readValue(requestBody, DatasetBaseVO.class);
			} catch (IOException e) {
				throw new BadRequestException(ErrorCode.INVALID_PARAMETER,
	                    "Invalid Parameter. body=" + requestBody);
			}
			datasetBaseVO.setId(datasetId);

			// 3. 기 존재여부 확인
			DatasetBaseVO retrieveDatasetBaseVO = datasetDAO.getDatasetVOById(datasetId);
			if(retrieveDatasetBaseVO == null) {
				log.info("Create(Upsert) Dataset. requestURI={}", to);
				createDataset(requestBody, requestId, eventTime);
				return;
			}

			// 4. 이중화되어 있는 다른 IngestInterface 인스턴스에서 DB 입력 했는지 체크
	 		if(alreadyProcessByOtherInstance(requestId, eventTime, retrieveDatasetBaseVO)) {
	         	// 다른 Instance에서 DB 업데이트 이미 처리했으므로 캐쉬만 로딩하고 성공 처리
	 			dataModelManager.putDatasetCache(retrieveDatasetBaseVO);
	            return;
	        }

	 		// 데이터 모델이 변경되는 경우 유효성 체크
	 		String currentDataModelId = retrieveDatasetBaseVO.getDataModelId();
	        String updateDataModelId = datasetBaseVO.getDataModelId();
	        if(currentDataModelId != null && updateDataModelId != null && !currentDataModelId.equals(updateDataModelId)) {
	        	// 데이터모델은 데이터셋 흐름이 생성되어 있지 않은 경우만 수정이 가능
	        	if(datasetFlowRetrieveSVC.getDatasetFlowBaseVOById(datasetBaseVO.getId()) != null) {
	            	throw new BadRequestException(ErrorCode.INVALID_PARAMETER, 
	            			"Cannot change dataModel. Using in datasetFlow."
	            			+ "datasetId=" + datasetBaseVO.getId() + ", using dataModelId=" + currentDataModelId);
	            }
	        }

			// 5. Dataset 업데이트
	 		datasetBaseVO.setProvisioningRequestId(requestId);
	 		datasetBaseVO.setProvisioningEventTime(eventTime);
	        int result = datasetDAO.updateDataset(datasetBaseVO);

	        if(result == 0) {
	        	throw new BadRequestException(ErrorCode.NOT_EXIST_ID,
	                    "Not Exists. datasetId=" + datasetId);
	        }

	        // 6. set dataset Cache
	        dataModelManager.putDatasetCache(datasetBaseVO);

	    // 404
    	} else {
 			throw new BadRequestException(ErrorCode.NOT_EXIST_ID);
 		}
    }

    /**
     * 데이터 셋 정보 삭제
     * @param to 데이터 셋 정보 수정 요청 수신 url
     * @param requestBody 요청 Body
     */
    public void deleteDataset(String to, String requestBody) {
    	// 1. Request URI에서 식별자 추출
		Matcher matcherForUpdate = URI_PATTERN_DATASET.matcher(to);

    	if(matcherForUpdate.find()) {
			String datasetId = matcherForUpdate.group("datasetId");

			DatasetBaseVO retrieveDatasetBaseVO = datasetDAO.getDatasetVOById(datasetId);

			if(retrieveDatasetBaseVO != null) {
				// 2. Dataset 삭제
		        int result = datasetDAO.deleteDataset(datasetId);
	
		        if(result == 0) {
		        	throw new BadRequestException(ErrorCode.NOT_EXIST_ID,
		                    "Not Exists. datasetId=" + datasetId);
		        }
			}
	        
	        // 3. 데이터셋 캐쉬 삭제
	        dataModelManager.removeDatasetCache(datasetId);

	    // 404
    	} else {
 			throw new BadRequestException(ErrorCode.NOT_EXIST_ID);
 		}
    }

    /**
     * 이중화되어 있는 다른 IngestInterface 인스턴스에서 DB 입력 했는지 체크
     * @param requestId Provisioning Request Id
     * @param eventTime Provisioning Request Time
     * @param retrieveDatasetBaseVO DB에서 조회한 데이터 셋 정보 VO
     * @return
     */
    private boolean alreadyProcessByOtherInstance(String requestId, Date eventTime, DatasetBaseVO retrieveDatasetBaseVO) {
		// 이중화되어 있는 다른 IngestInterface 인스턴스에서 DB 입력 했는지 체크
    	if(requestId.equals(retrieveDatasetBaseVO.getProvisioningRequestId())
    			&& eventTime.getTime() >= retrieveDatasetBaseVO.getProvisioningEventTime().getTime()) {
    		return true;
    	}
    	return false;
	}



}
