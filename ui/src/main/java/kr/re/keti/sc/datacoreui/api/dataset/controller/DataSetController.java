package kr.re.keti.sc.datacoreui.api.dataset.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import kr.re.keti.sc.datacoreui.api.dataset.service.DataSetSVC;
import kr.re.keti.sc.datacoreui.api.dataset.vo.DataSetListResponseVO;
import kr.re.keti.sc.datacoreui.api.dataset.vo.DataSetResponseVO;
import kr.re.keti.sc.datacoreui.api.dataset.vo.DataSetRetrieveVO;
import kr.re.keti.sc.datacoreui.api.dataset.vo.DataSetVO;
import kr.re.keti.sc.datacoreui.common.code.DataCoreUiCode.ErrorCode;
import kr.re.keti.sc.datacoreui.common.exception.BadRequestException;
import kr.re.keti.sc.datacoreui.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Class for data set management API calls.
 * @FileName DataSetController.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 23.
 * @Author Elvin
 */
@Slf4j
@RestController
public class DataSetController {
	
	@Autowired
	private DataSetSVC dataSetSVC;
	
	/**
	 * Create data set
	 * @param dataSet		DataSetVO object
	 * @return				Result of data set creation.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@PostMapping(value="/datasets")
	public <T> ResponseEntity<T> createDataSet(HttpServletRequest request, HttpServletResponse response,
			@RequestBody DataSetVO dataSet) throws Exception {
		
		log.info("[UI API] createDataSet - dataSet: {}", dataSet);
		
		// 1. Check required values
		if(!checkMandatoryCreateValues(dataSet)) {
			throw new BadRequestException(ErrorCode.BAD_REQUEST, "Missing required value.");
		}
		
		// 2. Create Data Set
		ResponseEntity<T> reslt = dataSetSVC.createDataSet(dataSet, request);
		
		return reslt;
	}
	
	/**
	 * Update data set
	 * @param id			Data set ID
	 * @param dataSet		DataSetVO object (update data)
	 * @return				Result of update data set.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@PutMapping(value="/datasets/{id}")
	public <T> ResponseEntity<T> updateDataSet(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") String id,
			@RequestBody DataSetVO dataSet) throws Exception {
		
		log.info("[UI API] updateDataSet - id: {}, dataSet: {}", id, dataSet);
		
		// 1. Check required values
		if(!checkMandatoryUpdateValues(dataSet)) {
			throw new BadRequestException(ErrorCode.BAD_REQUEST, "Missing required value.");
		}
		
		// 2. Update Data Set
		ResponseEntity<T> reslt = dataSetSVC.updateDataSet(id, dataSet, request);
		
		return reslt;
	}
	
	/**
	 * Delete data set
	 * @param id			Data set ID
	 * @return				Result of delete data set.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@DeleteMapping(value="/datasets/{id}")
	public <T> ResponseEntity<T> deleteDataSet(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") String id) throws Exception {
		
		log.info("[UI API] deleteDataSet - id: {}", id);
		
		// 1. Delete Data Set
		ResponseEntity<T> reslt = dataSetSVC.deleteDataSet(id);
		
		return reslt;
	}
	
	/**
	 * Retrieve data set
	 * @param id			Data set ID
	 * @return				Data set information retrieved by data set ID.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@GetMapping(value="/datasets/{id}")
	@ResponseBody
	public ResponseEntity<DataSetResponseVO> getDataSet(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") String id) throws Exception {
		
		log.info("[UI API] getDataSet - id: {}", id);
		
		// 1. Retrieve Data Set
		ResponseEntity<DataSetResponseVO> dataSetVO = dataSetSVC.getDataSet(id);
		
		return dataSetVO;
	}
	
	/**
	 * Retrieve multiple data set
	 * @param dataSetRetrieveVO		DataSetRetrieveVO object
	 * @return						List of data set retrieved by DataSetRetrieveVO.
	 * @throws Exception			Throw an exception when an error occurs.
	 */
	@GetMapping(value="/datasets")
	@ResponseBody
	public ResponseEntity<DataSetListResponseVO> getDataSets(HttpServletRequest request, HttpServletResponse response,
			DataSetRetrieveVO dataSetRetrieveVO) throws Exception {
		
		log.info("[UI API] getDataSets - dataSetRetrieveVO: {}", dataSetRetrieveVO);
		
		// 1. Retrieve multiple Data Set
		ResponseEntity<DataSetListResponseVO> dataSetVOs = dataSetSVC.getDataSets(dataSetRetrieveVO);
		
		return dataSetVOs;
	}
	
	/**
	 * Request a data set provision
	 * @param id			Data set ID
	 * @return				Data set provisioning result.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@PostMapping(value="/datasets/{id}/provision")
	public <T> ResponseEntity<T> requestDataSetProvision(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") String id) throws Exception {
		
		log.info("[UI API] requestDataSetProvision - id: {}", id);
		
		// 1.Request a Data Set Provision
		ResponseEntity<T> reslt = dataSetSVC.requestDataSetProvision(id);
		
		return reslt;
	}

	/**
	 * Check required values for Data set creation
	 * @param 	dataSet
	 * @return	valid: true, invalid: false
	 */
	private boolean checkMandatoryCreateValues(DataSetVO dataSet) {
		if(dataSet == null) {
			return false;
		}
		
		if (ValidateUtil.isEmptyData(dataSet.getId())
				|| ValidateUtil.isEmptyData(dataSet.getName())
				|| ValidateUtil.isEmptyData(dataSet.getUpdateInterval())
				|| ValidateUtil.isEmptyData(dataSet.getCategory())
				|| ValidateUtil.isEmptyData(dataSet.getProviderOrganization())
				|| ValidateUtil.isEmptyData(dataSet.getProviderSystem())
				|| ValidateUtil.isEmptyData(dataSet.getIsProcessed())
				|| ValidateUtil.isEmptyData(dataSet.getOwnership())
				|| ValidateUtil.isEmptyData(dataSet.getLicense())
				|| ValidateUtil.isEmptyData(dataSet.getDatasetItems())
				|| ValidateUtil.isEmptyData(dataSet.getTargetRegions())
				|| ValidateUtil.isEmptyData(dataSet.getQualityCheckEnabled())) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Check required values for Data set modification
	 * @param	dataSet
	 * @return	valid: true, invalid: false
	 */
	private boolean checkMandatoryUpdateValues(DataSetVO dataSet) {
		if(dataSet == null) {
			return false;
		}
		
		if (ValidateUtil.isEmptyData(dataSet.getName())
				|| ValidateUtil.isEmptyData(dataSet.getUpdateInterval())
				|| ValidateUtil.isEmptyData(dataSet.getCategory())
				|| ValidateUtil.isEmptyData(dataSet.getProviderOrganization())
				|| ValidateUtil.isEmptyData(dataSet.getProviderSystem())
				|| ValidateUtil.isEmptyData(dataSet.getIsProcessed())
				|| ValidateUtil.isEmptyData(dataSet.getOwnership())
				|| ValidateUtil.isEmptyData(dataSet.getLicense())
				|| ValidateUtil.isEmptyData(dataSet.getDatasetItems())
				|| ValidateUtil.isEmptyData(dataSet.getTargetRegions())
				|| ValidateUtil.isEmptyData(dataSet.getQualityCheckEnabled())) {
			return false;
		}
		
		return true;
	}
}
