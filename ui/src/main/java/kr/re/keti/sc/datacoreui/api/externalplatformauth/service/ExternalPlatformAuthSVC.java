package kr.re.keti.sc.datacoreui.api.externalplatformauth.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import kr.re.keti.sc.datacoreui.api.externalplatformauth.vo.ExternalPlatformAuthListResponseVO;
import kr.re.keti.sc.datacoreui.api.externalplatformauth.vo.ExternalPlatformAuthResponseVO;
import kr.re.keti.sc.datacoreui.api.externalplatformauth.vo.ExternalPlatformAuthRetrieveVO;
import kr.re.keti.sc.datacoreui.api.externalplatformauth.vo.ExternalPlatformAuthVO;
import kr.re.keti.sc.datacoreui.common.code.Constants;
import kr.re.keti.sc.datacoreui.common.service.DataCoreRestSVC;
import kr.re.keti.sc.datacoreui.util.ValidateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A service class for external platform auth management API calls.
 * @FileName ExternalPlatformAuthSVC.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 23.
 * @Author Elvin
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ExternalPlatformAuthSVC {
	@Value("${ingestinterface.url}")
	private String externalPlatformAuthUrl;
	
	private final static String DEFAULT_PATH_URL = "/externalplatform/authentication";
	private final DataCoreRestSVC dataCoreRestSVC;
	
	/**
	 * Create external platform auth
	 * @param externalPlatformAuthVO	ExternalPlatformAuthVO
	 * @return							Result of external platform authenticate creation.
	 */
	public  <T> ResponseEntity<T> createExternalPlatformAuth(ExternalPlatformAuthVO externalPlatformAuthVO) {
		String externalPlatformAuth = new Gson().toJson(externalPlatformAuthVO);
		
		ResponseEntity<T> response = (ResponseEntity<T>) dataCoreRestSVC.post(externalPlatformAuthUrl, DEFAULT_PATH_URL, null, externalPlatformAuth, null, Void.class);
		
		return response;
	}

	/**
	 * Update external platform auth
	 * @param id						External platform authentication ID
	 * @param externalPlatformAuthVO	ExternalPlatformAuthVO
	 * @return							Result of update external platform authenticate.
	 */
	public <T> ResponseEntity<T> updateExternalPlatformAuth(String id, ExternalPlatformAuthVO externalPlatformAuthVO) {
		String pathUri = DEFAULT_PATH_URL + "/" + id;
		String externalPlatformAuth = new Gson().toJson(externalPlatformAuthVO);
		
		ResponseEntity<T> response = (ResponseEntity<T>) dataCoreRestSVC.patch(externalPlatformAuthUrl, pathUri, null, externalPlatformAuth, null, Void.class);
		
		return response;
	}

	/**
	 * Delete external platform auth
	 * @param id	External platform authentication ID
	 * @return		Result of delete external platform authenticate.
	 */
	public <T> ResponseEntity<T> deleteExternalPlatformAuth(String id) {
		String pathUri = DEFAULT_PATH_URL + "/" + id;
		
		ResponseEntity<T> response = (ResponseEntity<T>) dataCoreRestSVC.delete(externalPlatformAuthUrl, pathUri, null, null, null, Void.class);
		
		return response;
	}

	/**
	 *  Retrieve external platform auth
	 * @param id	External platform authentication ID
	 * @return		External platform authentication retrieve by attribute external platform authentication ID.
	 */
	public ResponseEntity<ExternalPlatformAuthResponseVO> getExternalPlatformAuth(String id) {
		String pathUri = DEFAULT_PATH_URL + "/" + id;
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", "application/json");
		
		ResponseEntity<ExternalPlatformAuthResponseVO> response = dataCoreRestSVC.get(externalPlatformAuthUrl, pathUri, headers, null, null, ExternalPlatformAuthResponseVO.class);
		
		return response;
	}

	/**
	 * Retrieve multiple external platform auth
	 * @param externalPlatformAuthRetrieve		ExternalPlatformAuthRetrieveVO
	 * @return									List of external platform authentication retrieve by ExternalPlatformAuthRetrieveVO.
	 */
	public ResponseEntity<ExternalPlatformAuthListResponseVO> getExternalPlatformAuths(
			ExternalPlatformAuthRetrieveVO externalPlatformAuthRetrieve) {
		ExternalPlatformAuthListResponseVO externalPlatformAuthListResponseVO = new ExternalPlatformAuthListResponseVO();
		Map<String, Object> param = createParams(externalPlatformAuthRetrieve);
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", "application/json");
		
		ResponseEntity<List<ExternalPlatformAuthResponseVO>> response = dataCoreRestSVC.getList(externalPlatformAuthUrl, DEFAULT_PATH_URL, headers, null, param, new ParameterizedTypeReference<List<ExternalPlatformAuthResponseVO>>() {});
		
		if(response != null) {
			externalPlatformAuthListResponseVO.setExternalPlatformAuthResponseVO(response.getBody());
			if(response.getHeaders() != null) {
				List<String> totalCountHeader = (response.getHeaders().get(Constants.TOTAL_COUNT));
				if (totalCountHeader != null && totalCountHeader.size() > 0) {
					externalPlatformAuthListResponseVO.setTotalCount(Integer.valueOf(totalCountHeader.get(0)));
				}
			}
		}
		return ResponseEntity.status(response.getStatusCode()).body(externalPlatformAuthListResponseVO);
	}

	/**
	 * Create external platform auth request param
	 * @param externalPlatformAuthRetrieve		ExternalPlatformAuthRetrieveVO
	 * @return									Http request parameter
	 */
	private Map<String, Object> createParams(ExternalPlatformAuthRetrieveVO externalPlatformAuthRetrieve) {
		Map<String, Object> param = new HashMap<String, Object>();
		
		if(externalPlatformAuthRetrieve == null) {
			return null;
		}
		
		if(!ValidateUtil.isEmptyData(externalPlatformAuthRetrieve.getLimit())) {
			param.put("limit", externalPlatformAuthRetrieve.getLimit());
		}
		if(!ValidateUtil.isEmptyData(externalPlatformAuthRetrieve.getOffset())) {
			param.put("offset", externalPlatformAuthRetrieve.getOffset());
		}
		
		return param;
	}
}
