package kr.re.keti.sc.datacoreui.api.dataset.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import kr.re.keti.sc.datacoreui.api.dataset.vo.DataSetListResponseVO;
import kr.re.keti.sc.datacoreui.api.dataset.vo.DataSetResponseVO;
import kr.re.keti.sc.datacoreui.api.dataset.vo.DataSetRetrieveVO;
import kr.re.keti.sc.datacoreui.api.dataset.vo.DataSetVO;
import kr.re.keti.sc.datacoreui.api.datasetflow.vo.DataSetIdVO;
import kr.re.keti.sc.datacoreui.common.code.Constants;
import kr.re.keti.sc.datacoreui.common.service.DataCoreRestSVC;
import kr.re.keti.sc.datacoreui.security.service.DataCoreUiSVC;
import kr.re.keti.sc.datacoreui.util.ValidateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A service class for data set management API calls.
 * @FileName DataSetSVC.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 23.
 * @Author Elvin
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DataSetSVC {
	@Value("${datamanager.url}")
	private String datasetUrl;
	
	private final static String DEFAULT_PATH_URL = "datasets";
	private final DataCoreRestSVC dataCoreRestSVC;

	@Autowired
	private DataCoreUiSVC dataCoreUiSVC;

	/**
	 * Create data set
	 * @param dataSetVO		DataSetVO
	 * @return				Result of data set creation.
	 */
	public <T> ResponseEntity<T> createDataSet(DataSetVO dataSetVO, HttpServletRequest request) {
		Object principal = dataCoreUiSVC.getPrincipal(request);
		if (principal != null) {
			dataSetVO.setCreatorId(principal.toString());
		}

		String dataSet = new Gson().toJson(dataSetVO);
		
		ResponseEntity<T> response = (ResponseEntity<T>) dataCoreRestSVC.post(datasetUrl, DEFAULT_PATH_URL, null, dataSet, null, Void.class);
		
		return response;
	}

	/**
	 * Update data set
	 * @param id			Data set ID
	 * @param dataSetVO		DataSetVO
	 * @return				Result of update data set.
	 */
	public <T> ResponseEntity<T> updateDataSet(String id, DataSetVO dataSetVO, HttpServletRequest request) {
		String pathUri = DEFAULT_PATH_URL + "/" + id;
		Object principal = dataCoreUiSVC.getPrincipal(request);
		if (principal != null) {
			dataSetVO.setModifierId(principal.toString());
		}
		String dataSet = new Gson().toJson(dataSetVO);
		
		ResponseEntity<T> response = (ResponseEntity<T>) dataCoreRestSVC.put(datasetUrl, pathUri, null, dataSet, null, Void.class);
		
		return response;
	}

	/**
	 * Delete data set
	 * @param id	Data set ID
	 * @return		Result of delete data set.
	 */
	public <T> ResponseEntity<T> deleteDataSet(String id) {
		String pathUri = DEFAULT_PATH_URL + "/" + id;
		
		ResponseEntity<T> response = (ResponseEntity<T>) dataCoreRestSVC.delete(datasetUrl, pathUri, null, null, null, Void.class);
		
		return response;
	}

	/**
	 * Retrieve data set
	 * @param id	Data set ID
	 * @return		List of data set retrieved by data set id.
	 */
	public ResponseEntity<DataSetResponseVO> getDataSet(String id) {
		String pathUri = DEFAULT_PATH_URL + "/" + id;
		Map<String, String> header = new HashMap<String, String>();
		header.put("Accept", "application/json");
		
		ResponseEntity<DataSetResponseVO> response = dataCoreRestSVC.get(datasetUrl, pathUri, header, null, null, DataSetResponseVO.class);
		
		return response;
	}

	/**
	 * Retrieve multiple data set
	 * @param dataSetRetrieveVO		DataSetRetrieveVO
	 * @return						List of data set retrieved by DataSetRetrieveVO.
	 */
	public ResponseEntity<DataSetListResponseVO> getDataSets(DataSetRetrieveVO dataSetRetrieveVO) {
		String pathUri = DEFAULT_PATH_URL;
		
		if(!ValidateUtil.isEmptyData(dataSetRetrieveVO.getSearchValue())) {
			pathUri += "/ui";
		}
		
		DataSetListResponseVO dataSetListResponseVO = new DataSetListResponseVO();
		Map<String, Object> param = createParams(dataSetRetrieveVO);
		Map<String, String> header = new HashMap<String, String>();
		header.put("Accept", "application/json");
		
		ResponseEntity<List<DataSetResponseVO>> response = dataCoreRestSVC.getList(datasetUrl, pathUri, header, null, param, new ParameterizedTypeReference<List<DataSetResponseVO>>() {});
		if(response != null) {
			dataSetListResponseVO.setDataSetResponseVO(response.getBody());
			if(response.getHeaders() != null) {
				List<String> totalCountHeader = (response.getHeaders().get(Constants.TOTAL_COUNT));
				if (totalCountHeader != null && totalCountHeader.size() > 0) {
					dataSetListResponseVO.setTotalCount(Integer.valueOf(totalCountHeader.get(0)));
				}
			}
		}
		
		return ResponseEntity.status(response.getStatusCode()).body(dataSetListResponseVO);
	}

	/**
	 * Request data set provision
	 * @param id	Data set ID
	 * @return		Data set Provisioning Results
	 */
	public <T> ResponseEntity<T> requestDataSetProvision(String id) {
		String pathUri = DEFAULT_PATH_URL + "/" + id + "/provisioning";
		
		ResponseEntity<T> response = (ResponseEntity<T>) dataCoreRestSVC.post(datasetUrl, pathUri, null, null, null, Void.class);
		
		return response;
	}

	/**
	 * Retrieve list of data set ID
	 * @param dataSetRetrieveVO		DataSetRetrieveVO
	 * @return						List of data set ID
	 */
	public ResponseEntity<List<DataSetIdVO>> getDataSetIds(DataSetRetrieveVO dataSetRetrieveVO) {
		List<DataSetIdVO> reslt = new ArrayList<DataSetIdVO>();
		String dataSetRetrieve = new Gson().toJson(dataSetRetrieveVO);
		Map<String, String> header = new HashMap<String, String>();
		header.put("Accept", "application/json");
		
		ResponseEntity<List<DataSetResponseVO>> response = dataCoreRestSVC.getList(datasetUrl, DEFAULT_PATH_URL, header, dataSetRetrieve, null, new ParameterizedTypeReference<List<DataSetResponseVO>>() {});
		
		if(response != null || response.getBody() != null || response.getBody().size() > 0) {
			for(DataSetResponseVO dataSetResponseVO : response.getBody()) {
				DataSetIdVO dataSetIdVO = new DataSetIdVO();
				dataSetIdVO.setId(dataSetResponseVO.getId());
				dataSetIdVO.setName(dataSetResponseVO.getName());
				reslt.add(dataSetIdVO);
			}
		}
		
		return ResponseEntity.status(response.getStatusCode()).body(reslt);
	}

	/**
	 * Create data set request param
	 * @param dataSetRetrieveVO		DataSetRetrieveVO
	 * @return						Http request parameter
	 */
	private Map<String, Object> createParams(DataSetRetrieveVO dataSetRetrieveVO) {
		Map<String, Object> param = new HashMap<String, Object>();
		
		if(dataSetRetrieveVO == null) {
			return null;
		}
		
		if(!ValidateUtil.isEmptyData(dataSetRetrieveVO.getName())) {
			param.put("name", dataSetRetrieveVO.getName().trim());
		}
		if(!ValidateUtil.isEmptyData(dataSetRetrieveVO.getUpdateInterval())) {
			param.put("updateInterval", dataSetRetrieveVO.getUpdateInterval().trim());
		}
		if(!ValidateUtil.isEmptyData(dataSetRetrieveVO.getCategory())) {
			param.put("category", dataSetRetrieveVO.getCategory().trim());
		}
		if(!ValidateUtil.isEmptyData(dataSetRetrieveVO.getProviderOrganization())) {
			param.put("providerOrganization", dataSetRetrieveVO.getProviderOrganization().trim());
		}
		if(!ValidateUtil.isEmptyData(dataSetRetrieveVO.getProviderSystem())) {
			param.put("providerSystem", dataSetRetrieveVO.getProviderSystem().trim());
		}
		if(!ValidateUtil.isEmptyData(dataSetRetrieveVO.getIsProcessed())) {
			param.put("isProcessed", dataSetRetrieveVO.getIsProcessed());
		}
		if(!ValidateUtil.isEmptyData(dataSetRetrieveVO.getOwnership())) {
			param.put("ownership", dataSetRetrieveVO.getOwnership().trim());
		}
		if(!ValidateUtil.isEmptyData(dataSetRetrieveVO.getLicense())) {
			param.put("license", dataSetRetrieveVO.getLicense().trim());
		}
		if(!ValidateUtil.isEmptyData(dataSetRetrieveVO.getDatasetItems())) {
			param.put("datasetItems", dataSetRetrieveVO.getDatasetItems().trim());
		}
		if(!ValidateUtil.isEmptyData(dataSetRetrieveVO.getTargetRegions())) {
			param.put("targetRegions", dataSetRetrieveVO.getTargetRegions().trim());
		}
		if(!ValidateUtil.isEmptyData(dataSetRetrieveVO.getDataStoreUri())) {
			param.put("dataStoreUri", dataSetRetrieveVO.getDataStoreUri());
		}
		if(!ValidateUtil.isEmptyData(dataSetRetrieveVO.getQualityCheckEnabled())) {
			param.put("qualityCheckEnabled", dataSetRetrieveVO.getQualityCheckEnabled());
		}
		if(!ValidateUtil.isEmptyData(dataSetRetrieveVO.getDataModelId())) {
			param.put("dataModelId", dataSetRetrieveVO.getDataModelId().trim());
		}
		if(!ValidateUtil.isEmptyData(dataSetRetrieveVO.getLimit())) {
			param.put("limit", dataSetRetrieveVO.getLimit());
		}
		if(!ValidateUtil.isEmptyData(dataSetRetrieveVO.getOffset())) {
			param.put("offset", dataSetRetrieveVO.getOffset());
		}
		if(!ValidateUtil.isEmptyData(dataSetRetrieveVO.getSearchValue())) {
			param.put("searchValue", dataSetRetrieveVO.getSearchValue());
		}
		
		return param;
	}
}
