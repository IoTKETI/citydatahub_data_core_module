package kr.re.keti.sc.ingestinterface.dataset.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.ErrorCode;
import kr.re.keti.sc.ingestinterface.common.exception.BadRequestException;
import kr.re.keti.sc.ingestinterface.datamodel.DataModelManager;
import kr.re.keti.sc.ingestinterface.dataset.dao.DatasetDAO;
import kr.re.keti.sc.ingestinterface.dataset.vo.DatasetBaseVO;
import lombok.extern.slf4j.Slf4j;

/**
 * Dataset Service class
 */
@Service
@Slf4j
public class DatasetSVC {

    @Autowired
    private DatasetDAO datasetDAO;
    @Autowired
    private DataModelManager dataModelManager;
    @Autowired
    private ObjectMapper objectMapper;
    /** dataset request uri pattern */
    private final Pattern URI_PATTERN_DATASET = Pattern.compile("/datasets/(?<datasetId>.+)");

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
	        	// 이미 존재하므로 업데이트
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

    public void deleteDataset(String to, String requestBody) {
    	// 1. Request URI에서 식별자 추출
		Matcher matcherForDelete = URI_PATTERN_DATASET.matcher(to);

    	if(matcherForDelete.find()) {
			String datasetId = matcherForDelete.group("datasetId");

			// 2. Dataset 조회
			DatasetBaseVO retrieveDatasetBaseVO = datasetDAO.getDatasetVOById(datasetId);

			if(retrieveDatasetBaseVO != null) {
				// 3. Dataset 삭제
		        int result = datasetDAO.deleteDataset(datasetId);

		        if(result == 0) {
		        	throw new BadRequestException(ErrorCode.NOT_EXIST_ID,
		                    "Not Exists. datasetId=" + datasetId);
		        }
			}

			// 4. 데이터셋 캐쉬 로딩
	        dataModelManager.removeDatasetCache(datasetId);

	    // 404
    	} else {
 			throw new BadRequestException(ErrorCode.NOT_EXIST_ID);
 		}
    }

    private boolean alreadyProcessByOtherInstance(String requestId, Date eventTime, DatasetBaseVO retrieveDatasetBaseVO) {
		// 이중화되어 있는 다른 IngestInterface 인스턴스에서 DB 입력 했는지 체크
    	if(requestId.equals(retrieveDatasetBaseVO.getProvisioningRequestId())
    			&& eventTime.getTime() >= retrieveDatasetBaseVO.getProvisioningEventTime().getTime()) {
    		return true;
    	}
    	return false;
	}

    public List<DatasetBaseVO> getDatasetVOList() {
        return datasetDAO.getDatasetVOList();
    }

    public DatasetBaseVO getDatasetVOById(String id) {
        return datasetDAO.getDatasetVOById(id);
    }

}

