package kr.re.keti.sc.datacoreui.api.provisionserver.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import kr.re.keti.sc.datacoreui.api.provisionserver.vo.ProvisionServerListResponseVO;
import kr.re.keti.sc.datacoreui.api.provisionserver.vo.ProvisionServerResponseVO;
import kr.re.keti.sc.datacoreui.api.provisionserver.vo.ProvisionServerRetrieveVO;
import kr.re.keti.sc.datacoreui.api.provisionserver.vo.ProvisionServerVO;
import kr.re.keti.sc.datacoreui.common.code.Constants;
import kr.re.keti.sc.datacoreui.common.service.DataCoreRestSVC;
import kr.re.keti.sc.datacoreui.util.ValidateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A service class for provision server management API calls.
 * @FileName ProvisionServerSVC.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ProvisionServerSVC {
	@Value("${datamanager.url}")
	private String provisionServerUrl;
	
	private final static String DEFAULT_PATH_URL = "provision/servers";
	private final DataCoreRestSVC dataCoreRestSVC;

	/**
	 * Create provision server
	 * @param provisionServerVO		ProvisionServerVO
	 * @return						Result of provision server creation.
	 */
	public <T> ResponseEntity<T> createProvisionServer(ProvisionServerVO provisionServerVO) {
		String pathUri = DEFAULT_PATH_URL;
		String provisionServer = new Gson().toJson(provisionServerVO);
		
		ResponseEntity<T> response = (ResponseEntity<T>) dataCoreRestSVC.post(provisionServerUrl, pathUri, null, provisionServer, null, Void.class);
		
		return response;
	}

	/**
	 * Update provision server
	 * @param id					Provision server ID
	 * @param provisionServerVO		ProvisionServerVO (update data)
	 * @return						Result of update provision server.
	 */
	public <T> ResponseEntity<T> updateProvisionServer(String id, ProvisionServerVO provisionServerVO) {
		String pathUri = DEFAULT_PATH_URL + "/" + id;
		String provisionServer = new Gson().toJson(provisionServerVO);
		
		ResponseEntity<T> response = (ResponseEntity<T>) dataCoreRestSVC.patch(provisionServerUrl, pathUri, null, provisionServer, null, Void.class);
		
		return response;
	}

	/**
	 * Delete provision server
	 * @param id	Provision server ID
	 * @return		Result of delete provision server.
	 */
	public <T> ResponseEntity<T> deleteProvisionServer(String id) {
		String pathUri = DEFAULT_PATH_URL + "/" + id;
		
		ResponseEntity<T> response = (ResponseEntity<T>) dataCoreRestSVC.delete(provisionServerUrl, pathUri, null, null, null, Void.class);
		
		return response;
	}

	/**
	 * Retrieve provision server
	 * @param id	Provision server ID
	 * @return		Provision server information retrieved by provision server ID.
	 */
	public ResponseEntity<ProvisionServerResponseVO> getProvisionServer(String id) {
		String pathUri = DEFAULT_PATH_URL + "/" + id;
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", "application/json");
		
		ResponseEntity<ProvisionServerResponseVO> response = dataCoreRestSVC.get(provisionServerUrl, pathUri, headers, null, null, ProvisionServerResponseVO.class);
		
		return response;
	}

	/**
	 * Retrieve multiple provision server
	 * @param provisionServerRetrieve	ProvisionServerRetrieveVO (search condition)
	 * @return							List of provision server information retrieved by provision server ID.
	 */
	public ResponseEntity<ProvisionServerListResponseVO> getProvisionServers(ProvisionServerRetrieveVO provisionServerRetrieve) {
		ProvisionServerListResponseVO provisionServerListResponseVO = new ProvisionServerListResponseVO();
		Map<String, Object> param = createParams(provisionServerRetrieve);
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", "application/json");
		
		ResponseEntity<List<ProvisionServerResponseVO>> response = dataCoreRestSVC.getList(provisionServerUrl, DEFAULT_PATH_URL, headers, null, param, new ParameterizedTypeReference<List<ProvisionServerResponseVO>>() {});
		
		if(response != null) {
			provisionServerListResponseVO.setProvisionServerResponseVO(response.getBody());
			if(response.getHeaders() != null) {
				List<String> totalCountHeader = (response.getHeaders().get(Constants.TOTAL_COUNT));
				if (totalCountHeader != null && totalCountHeader.size() > 0) {
					provisionServerListResponseVO.setTotalCount(Integer.valueOf(totalCountHeader.get(0)));
				}
			}
		}
		
		return ResponseEntity.status(response.getStatusCode()).body(provisionServerListResponseVO);
	}

	/** 
	 * Create request parameter
	 * @param provisionServerRetrieve	ProvisionServerRetrieveVO
	 * @return							Http request parameter
	 */
	private Map<String, Object> createParams(ProvisionServerRetrieveVO provisionServerRetrieve) {
		Map<String, Object> param = new HashMap<String, Object>();
		if(provisionServerRetrieve == null) {
			return null;
		}
		
		if(!ValidateUtil.isEmptyData(provisionServerRetrieve.getType())) {
			param.put("type", provisionServerRetrieve.getType().trim());
		}
		if(!ValidateUtil.isEmptyData(provisionServerRetrieve.getProvisionProtocol())) {
			param.put("provisionProtocol", provisionServerRetrieve.getProvisionProtocol().trim());
		}
		if(!ValidateUtil.isEmptyData(provisionServerRetrieve.getEnabled())) {
			param.put("enabled", provisionServerRetrieve.getEnabled());
		}
		if(!ValidateUtil.isEmptyData(provisionServerRetrieve.getLimit())) {
			param.put("limit", provisionServerRetrieve.getLimit());
		}
		if(!ValidateUtil.isEmptyData(provisionServerRetrieve.getOffset())) {
			param.put("offset", provisionServerRetrieve.getOffset());
		}
			
		return param;
	}
}
