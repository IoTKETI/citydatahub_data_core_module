package kr.re.keti.sc.datacoreui.api.datasetflow.controller;

import java.util.List;

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
import kr.re.keti.sc.datacoreui.api.dataset.vo.DataSetRetrieveVO;
import kr.re.keti.sc.datacoreui.api.datasetflow.service.DataSetFlowSVC;
import kr.re.keti.sc.datacoreui.api.datasetflow.vo.DataSetFlowResponseVO;
import kr.re.keti.sc.datacoreui.api.datasetflow.vo.DataSetFlowVO;
import kr.re.keti.sc.datacoreui.api.datasetflow.vo.DataSetIdVO;
import kr.re.keti.sc.datacoreui.api.datasetflow.vo.TargetTypeVO;
import kr.re.keti.sc.datacoreui.common.code.DataCoreUiCode.ErrorCode;
import kr.re.keti.sc.datacoreui.common.exception.BadRequestException;
import kr.re.keti.sc.datacoreui.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Class for data set flow management API calls.
 * @FileName DataSetFlowController.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 23.
 * @Author Elvin
 */
@Slf4j
@RestController
public class DataSetFlowController {

	@Autowired
	private DataSetFlowSVC dataSetFlowSVC;
	
	@Autowired
	private DataSetSVC dataSetSVC;
	
	/**
	 * Create Data set flow
	 * @param datasetId		Data set ID
	 * @param dataSetFlow	DataSetFlowVO object
	 * @return				Result of data set flow creation.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@PostMapping(value="/datasets/{datasetId}/flow")
	public <T> ResponseEntity<T> createDataSetFlow(HttpServletRequest request, HttpServletResponse response, 
			@PathVariable("datasetId") String datasetId,
			@RequestBody DataSetFlowVO dataSetFlow) throws Exception {
		
		log.info("[UI API] createDataSetFlow - datasetId: {}, datasetFlowId: {}, dataSetFlow: {}", datasetId, dataSetFlow);
		
		// 1. Check required values
		if(!checkMandatoryDataSetFlowValues(dataSetFlow)) {
			throw new BadRequestException(ErrorCode.BAD_REQUEST, "Missing required value.");
		}
		
		// 2. Create Data Set Flow
		ResponseEntity<T> reslt = dataSetFlowSVC.createDataSetFlow(datasetId, dataSetFlow);
		
		return reslt;
	}
	
	/**
	 * Update Data set flow
	 * @param datasetId		Data set ID
	 * @param dataSetFlow	Data set flow (update data)
	 * @return				Result of update data set flow.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@PutMapping(value="/datasets/{datasetId}/flow")
	public <T> ResponseEntity<T> updateDataSetFlow(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("datasetId") String datasetId,
			@RequestBody DataSetFlowVO dataSetFlow) throws Exception {
		
		log.info("[UI API] updateDataSetFlow - datasetId: {}, DataSetFlowVO: {}", datasetId, dataSetFlow);
		
		// 1. Update DataSetFlow
		ResponseEntity<T> reslt = dataSetFlowSVC.updateDataSetFlow(datasetId, dataSetFlow);
		
		return reslt;
	}
	
	/**
	 * Delete Data set flow
	 * @param datasetId		Data set ID
	 * @return				Result of delete data set flow.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@DeleteMapping(value="/datasets/{datasetId}/flow")
	public <T> ResponseEntity<T> deleteDataSetFlow(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("datasetId") String datasetId) throws Exception {
		
		log.info("[UI API] deleteDataSetFlow - datasetId: {}", datasetId);
		
		// 1. Delete DataSetFlow
		ResponseEntity<T> reslt = dataSetFlowSVC.deleteDataSetFlow(datasetId);
		
		return reslt;
	}
	
	/**
	 * Retrieve Data set flow
	 * @param datasetId		Data set ID
	 * @return				Data set flow retrieved by data set id.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@GetMapping(value="/datasets/{datasetId}/flow")
	public ResponseEntity<DataSetFlowResponseVO> getDataSetFlow(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("datasetId") String datasetId) throws Exception {
		
		log.info("[UI API] getDataSetFlow - datasetId: {}", datasetId);
		
		// 1. Retrieve DataSetFlow
		ResponseEntity<DataSetFlowResponseVO> dataSetFlow = dataSetFlowSVC.getDataSetFlow(datasetId);
		
		return dataSetFlow;
	}
	
	/**
	 * Request data set flow provisioning
	 * @param datasetId		Data set ID
	 * @return				Result of provisioning for data set flow.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@PostMapping(value="/datasets/{datasetId}/flow/provision")
	public <T> ResponseEntity<T> provisionDataSetFlow(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("datasetId") String datasetId) throws Exception {
		
		log.info("[UI API] getDataSetFlows - datasetId: {}", datasetId);
		
		// 1. Request DataSetFlow Provisioning
		ResponseEntity<T> reslt = dataSetFlowSVC.provisionDataSetFlow(datasetId);
		
		return reslt;
	}
	
	/**
	 * Retrieve Data set ID list
	 * @param dataSetRetrieveVO		DataSetRetrieveVO object
	 * @return						List of Data set ID
	 * @throws Exception			Throw an exception when an error occurs.
	 */
	@GetMapping(value="/dataset/ids")
	@ResponseBody
	public ResponseEntity<List<DataSetIdVO>> getDataSetIds(HttpServletRequest request, HttpServletResponse response,
			DataSetRetrieveVO dataSetRetrieveVO) throws Exception {
		
		log.info("[UI API] getDataSetIds");
		
		// 1. Retrieve multiple Data Set
		ResponseEntity<List<DataSetIdVO>> dataSetVOs = dataSetSVC.getDataSetIds(dataSetRetrieveVO);
		
		return dataSetVOs;
	}
	
	/**
	 * Check the required values ​​for data set flow
	 * @param 	dataSetFlow
	 * @return	valid: true, invalid: false
	 */
	private boolean checkMandatoryDataSetFlowValues(DataSetFlowVO dataSetFlow) {
		if(dataSetFlow == null) {
			return false;
		}
		
		if(ValidateUtil.isEmptyData(dataSetFlow.getEnabled())
				|| ValidateUtil.isEmptyData(dataSetFlow.getTargetTypes())) {
					return false;
				}
		
		if(!checkMandatoryTargetTypeValues(dataSetFlow.getTargetTypes())) {
			return false;
		}
		
		return true;
	}

	/**
	 * Check the required values ​​for target type
	 * @param 	targetTypes
	 * @return	valid: true, invalid: false
	 */
	private boolean checkMandatoryTargetTypeValues(List<TargetTypeVO> targetTypes) {
		for(TargetTypeVO targetType : targetTypes) {
			if(ValidateUtil.isEmptyData(targetType.getType())) {
				return false;
			}
		}
		
		return true;
	}
}
