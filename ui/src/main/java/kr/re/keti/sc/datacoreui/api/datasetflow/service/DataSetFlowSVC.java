package kr.re.keti.sc.datacoreui.api.datasetflow.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import kr.re.keti.sc.datacoreui.api.datasetflow.vo.DataSetFlowResponseVO;
import kr.re.keti.sc.datacoreui.api.datasetflow.vo.DataSetFlowVO;
import kr.re.keti.sc.datacoreui.common.service.DataCoreRestSVC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A service class for data set flow management API calls.
 * @FileName DataSetFlowSVC.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 23.
 * @Author Elvin
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DataSetFlowSVC {
	@Value("${datamanager.url}")
	private String datasetflowUrl;
	
	private final static String DEFAULT_PATH_URL = "datasets";
	private final DataCoreRestSVC dataCoreRestSVC;

	/**
	 * Create data set flow
	 * @param datasetId			Data set ID
	 * @param dataSetFlowVO		DataSetFlowVO
	 * @return					Result of data set flow creation.
	 */
	public <T> ResponseEntity<T> createDataSetFlow(String datasetId, DataSetFlowVO dataSetFlowVO) {
		String pathUri = DEFAULT_PATH_URL + "/" + datasetId + "/flow";
		String dataSetFlow = new Gson().toJson(dataSetFlowVO);
		
		ResponseEntity<T> response = (ResponseEntity<T>) dataCoreRestSVC.post(datasetflowUrl, pathUri, null, dataSetFlow, null, Void.class);
		
		return response;
	}

	/**
	 * Update data set flow
	 * @param datasetId			Data set ID
	 * @param dataSetFlowVO		DataSetFlowVO
	 * @return					Result of update data set flow.
	 */
	public <T> ResponseEntity<T> updateDataSetFlow(String datasetId, DataSetFlowVO dataSetFlowVO) {
		String pathUri = DEFAULT_PATH_URL + "/" + datasetId + "/flow";
		String dataSetFlow = new Gson().toJson(dataSetFlowVO);
		
		ResponseEntity<T> response = (ResponseEntity<T>) dataCoreRestSVC.put(datasetflowUrl, pathUri, null, dataSetFlow, null, Void.class);
		
		return response;
	}

	/**
	 * Delete data set flow
	 * @param datasetId		Data set ID
	 * @return				Result of delete data set flow.
	 */
	public <T> ResponseEntity<T> deleteDataSetFlow(String datasetId) {
		String pathUri = DEFAULT_PATH_URL + "/" + datasetId + "/flow";
		
		ResponseEntity<T> response = (ResponseEntity<T>) dataCoreRestSVC.delete(datasetflowUrl, pathUri, null, null, null, Void.class);
		
		return response;
	}

	/**
	 *  Retrieve data set flow
	 * @param datasetId		Data set ID
	 * @return				Data set flow information retrieved by data set id.
	 */
	public ResponseEntity<DataSetFlowResponseVO> getDataSetFlow(String datasetId) {
		String pathUri = DEFAULT_PATH_URL + "/" + datasetId + "/flow";
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", "application/json");
		
		ResponseEntity<DataSetFlowResponseVO> response = dataCoreRestSVC.get(datasetflowUrl, pathUri, headers, null, null, DataSetFlowResponseVO.class);
		
		return response;
	}

	/**
	 * Provision data set flow
	 * @param datasetId		Data set ID
	 * @return				Data set flow Provisioning Results
	 */
	public <T> ResponseEntity<T> provisionDataSetFlow(String datasetId) {
		String pathUri = DEFAULT_PATH_URL + "/" + datasetId + "/flow/provisioning";
		
		ResponseEntity<T> response = (ResponseEntity<T>) dataCoreRestSVC.post(datasetflowUrl, pathUri, null, null, null, Void.class);
		
		return response;
	}
}
