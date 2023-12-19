package kr.re.keti.sc.datacoreui.api.verificationhistory.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import kr.re.keti.sc.datacoreui.api.verificationhistory.vo.VerificationHistoryCountVO;
import kr.re.keti.sc.datacoreui.api.verificationhistory.vo.VerificationHistoryListResponseVO;
import kr.re.keti.sc.datacoreui.api.verificationhistory.vo.VerificationHistoryListVO;
import kr.re.keti.sc.datacoreui.api.verificationhistory.vo.VerificationHistoryRequestVO;
import kr.re.keti.sc.datacoreui.api.verificationhistory.vo.VerificationHistoryResponseVO;
import kr.re.keti.sc.datacoreui.api.verificationhistory.vo.VerificationHistoryVO;
import kr.re.keti.sc.datacoreui.common.code.Constants;
import kr.re.keti.sc.datacoreui.common.service.DataCoreRestSVC;
import kr.re.keti.sc.datacoreui.util.ValidateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A service class for verification history management API calls.
 * @FileName VerificationHistorySVC.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class VerificationHistorySVC {
	@Value("${ingestinterface.url}")
	private String verificationHistoryUrl;
	
	private final static String DEFAULT_PATH_URL = "verificationHistory";
	private final DataCoreRestSVC dataCoreRestSVC;

	/**
	 * Retrieve list of verification history
	 * @param verificationHistoryRequestVO
	 * @return
	 */
	public ResponseEntity<VerificationHistoryListVO> getVerificationHistorys(
			VerificationHistoryRequestVO verificationHistoryRequestVO) {
		VerificationHistoryListVO verificationHistoryListVO = new VerificationHistoryListVO();
		Map<String, String> header = new HashMap<String, String>();
		header.put("Accept", "application/json");
		
		Map<String, Object> params = createParams(verificationHistoryRequestVO);
		
		ResponseEntity<List<VerificationHistoryVO>> verificationHistorys = dataCoreRestSVC.getList(verificationHistoryUrl, 
				DEFAULT_PATH_URL, header, null, params, new ParameterizedTypeReference<List<VerificationHistoryVO>>() {});
		
		verificationHistoryListVO.setVerificationHistoryVOs(verificationHistorys.getBody());
		if(verificationHistorys.getHeaders() != null) {
			List<String> totalCountHeader = (verificationHistorys.getHeaders().get(Constants.TOTAL_COUNT));
			if (totalCountHeader != null && totalCountHeader.size() > 0) {
				verificationHistoryListVO.setTotalCount(Integer.valueOf(totalCountHeader.get(0)));
			}
		}
		
		return ResponseEntity.status(verificationHistorys.getStatusCode()).body(verificationHistoryListVO);
	}
	
	/**
	 * Retrieve verification history
	 * @param seq
	 * @return
	 */
	public ResponseEntity<VerificationHistoryVO> getVerificationHistory(Long seq) {
		String pathUri = DEFAULT_PATH_URL + "/" + seq;
		Map<String, String> header = new HashMap<String, String>();
		header.put("Accept", "application/json");
		
		ResponseEntity<VerificationHistoryVO> verificationHistory = dataCoreRestSVC.get(verificationHistoryUrl, 
				pathUri, header, null, null, VerificationHistoryVO.class);
		
		return verificationHistory;
	}

	/**
	 * Retrieve count of verification history
	 * @param verificationHistoryRequestVO
	 * @return
	 */
	public ResponseEntity<VerificationHistoryCountVO> getVerificationHistoryCount(
			VerificationHistoryRequestVO verificationHistoryRequestVO) {
		String pathUri = DEFAULT_PATH_URL + "/count";
		Map<String, String> header = new HashMap<String, String>();
		header.put("Accept", "application/json");
		
		Map<String, Object> params = createParams(verificationHistoryRequestVO);
		
		ResponseEntity<VerificationHistoryCountVO> VerificationHistoryCount = dataCoreRestSVC.get(verificationHistoryUrl, 
				pathUri, header, null, params, VerificationHistoryCountVO.class);
		
		return VerificationHistoryCount;
	}

	/**
	 * Retrieve all verification history
	 * @param verificationHistoryRequest
	 * @return
	 */
	public ResponseEntity<VerificationHistoryListResponseVO> getVerificationHistoryAll(
			VerificationHistoryRequestVO verificationHistoryRequest) {
		
		VerificationHistoryListResponseVO verificationHistoryListResponseVO = new VerificationHistoryListResponseVO();
		VerificationHistoryResponseVO verificationHistoryResponse = new VerificationHistoryResponseVO();
		ResponseEntity<VerificationHistoryListVO> verificationHistorys = null;
		ResponseEntity<VerificationHistoryCountVO> verificationHistoryCount = null;
		
		try {
			verificationHistorys = getVerificationHistorys(verificationHistoryRequest);
			// When searching for success/failure counts, paging conditions are unnecessary, so null is set.
			verificationHistoryRequest.setLimit(null);
			verificationHistoryRequest.setOffset(null);
			verificationHistoryCount = getVerificationHistoryCount(verificationHistoryRequest);
			
			if(verificationHistorys != null && verificationHistorys.getBody() != null) {
				verificationHistoryResponse.setVerificationHistorys(verificationHistorys.getBody().getVerificationHistoryVOs());
			}
			if(verificationHistoryCount != null) {
				verificationHistoryResponse.setVerificationHistoryCount(verificationHistoryCount.getBody());
			}
			
			verificationHistoryListResponseVO.setVerificationHistoryResponseVO(verificationHistoryResponse);
			verificationHistoryListResponseVO.setTotalCount(verificationHistorys.getBody().getTotalCount());
		} catch (Exception e) {
			log.error("Fail to getVerificationHistoryAll()", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
		
		return ResponseEntity.status(verificationHistorys.getStatusCode()).body(verificationHistoryListResponseVO);
	}
	
	/**
	 * Create verification history request param
	 * @param verificationHistoryRequestVO
	 * @return
	 */
	private Map<String, Object> createParams(VerificationHistoryRequestVO verificationHistoryRequestVO) {
		Map<String, Object> params = new HashMap<String, Object>();
		
		params.put("startTime", verificationHistoryRequestVO.getStartTime());
		params.put("endTime", verificationHistoryRequestVO.getEndTime());
		if(!ValidateUtil.isEmptyData(verificationHistoryRequestVO.getDatasetId())) {
			params.put("datasetId", verificationHistoryRequestVO.getDatasetId().trim());
		}
		if(!ValidateUtil.isEmptyData(verificationHistoryRequestVO.getDataModelId())) {
			params.put("dataModelId", verificationHistoryRequestVO.getDataModelId().trim());
		}
		if(!ValidateUtil.isEmptyData(verificationHistoryRequestVO.getEntityId())) {
			params.put("entityId", verificationHistoryRequestVO.getEntityId().trim());
		}
		if(!ValidateUtil.isEmptyData(verificationHistoryRequestVO.getVerified())) {
			params.put("verified", verificationHistoryRequestVO.getVerified());
		}
		if(!ValidateUtil.isEmptyData(verificationHistoryRequestVO.getSmartSearchValue())) {
			params.put("smartSearchValue", verificationHistoryRequestVO.getSmartSearchValue());
		}
		if(!ValidateUtil.isEmptyData(verificationHistoryRequestVO.getLimit())) {
			params.put("limit", verificationHistoryRequestVO.getLimit());
		}
		if(!ValidateUtil.isEmptyData(verificationHistoryRequestVO.getOffset())) {
			params.put("offset", verificationHistoryRequestVO.getOffset());
		}
		
		return params;
	}
}
