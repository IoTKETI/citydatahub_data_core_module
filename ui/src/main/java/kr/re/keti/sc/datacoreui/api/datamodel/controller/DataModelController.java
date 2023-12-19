package kr.re.keti.sc.datacoreui.api.datamodel.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import kr.re.keti.sc.datacoreui.api.datamodel.service.DataModelSVC;
import kr.re.keti.sc.datacoreui.api.datamodel.vo.AttributeVO;
import kr.re.keti.sc.datacoreui.api.datamodel.vo.DataModelListResponseVO;
import kr.re.keti.sc.datacoreui.api.datamodel.vo.DataModelResponseVO;
import kr.re.keti.sc.datacoreui.api.datamodel.vo.DataModelRetrieveVO;
import kr.re.keti.sc.datacoreui.api.datamodel.vo.DataModelVO;
import kr.re.keti.sc.datacoreui.api.datamodel.vo.ObjectMemberVO;
import kr.re.keti.sc.datacoreui.common.code.DataCoreUiCode.ErrorCode;
import kr.re.keti.sc.datacoreui.common.exception.BadRequestException;
import kr.re.keti.sc.datacoreui.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Class for data model management API calls.
 * @FileName DataModelController.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 22.
 * @Author Elvin
 */
@Slf4j
@RestController
public class DataModelController {
	
	@Autowired
	private DataModelSVC dataModelSVC;
	
	/**
	 * Retrieve Data Model Context
	 * @param context		List of context
	 * @return				Data model retrieve result.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@PostMapping(value="/datamodels/context")
	public <T> ResponseEntity<T> getDataModelContext(HttpServletRequest request, HttpServletResponse response, 
			@RequestBody List<String> context) throws Exception {
		
		log.info("[UI API] getDataModelContext - dataModelContext: {}", context);
		
		// 1. Check required values
		if (ValidateUtil.isEmptyData(context)) {
			throw new BadRequestException(ErrorCode.BAD_REQUEST, "Missing required value.");
		}
		
		// 2. Retrieve Data Model Context
		ResponseEntity<T> reslt = (ResponseEntity<T>) dataModelSVC.getDataModelContext(context);
		
		return reslt;
	}
	
	/**
	 * Create data model
	 * @param dataModel		Data model VO
	 * @return				Result of data model creation.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@PostMapping(value="/datamodels")
	public <T> ResponseEntity<T> createDataModel(HttpServletRequest request, HttpServletResponse response, 
			@RequestBody DataModelVO dataModel) throws Exception {
		
		log.info("[UI API] createDataModel - dataModel: {}", dataModel);
		
		// 1. Check required values
		if (!checkMandatoryDataModelValues(dataModel)) {
			throw new BadRequestException(ErrorCode.BAD_REQUEST, "Missing required value.");
		}
		
		// 2. Create Data Model
		ResponseEntity<T> reslt = (ResponseEntity<T>) dataModelSVC.createDataModel(request, dataModel);
		
		return reslt;
	}
	
	/**
	 * Update data model
	 * @param dataModel		DataModelVO	(update data)
	 * @return				Result of update data model.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@PutMapping(value="/datamodels")
	public <T> ResponseEntity<T> updateDataModel(HttpServletRequest request, HttpServletResponse response, 
			@RequestBody DataModelVO dataModel) throws Exception {
		
		log.info("[UI API] updateDataModel - dataModel: {}", dataModel);
		
		// 1. Update DataModel
		ResponseEntity<T> reslt = (ResponseEntity<T>) dataModelSVC.updateDataModel(request, dataModel);
		
		return reslt;
	}
	
	/**
	 * Delete data model
	 * @param id			Data model ID
	 * @return				Result of delete data model.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@DeleteMapping(value="/datamodels")
	public <T> ResponseEntity<T> deleteDataModel(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value="id") String id) throws Exception {
		
		log.info("[UI API] deleteDataModel - id: {}", id);
		
		// 1. Delete DataModel
		ResponseEntity<T> reslt = (ResponseEntity<T>) dataModelSVC.deleteDataModel(id);
		
		return reslt;
	}
	
	/**
	 * Retrieve data model
	 * @param id			Data model ID
	 * @return				Data model retrieved by data model id.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@GetMapping(value="/datamodels")
	public ResponseEntity<DataModelResponseVO> getDataModel(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value="id") String id) throws Exception {
		
		log.info("[UI API] getDataModel - id: {}, typeUri: {}", id);
		
		// 1. Retrieve Data Model
		ResponseEntity<DataModelResponseVO> dataModelVO = dataModelSVC.getDataModel(id);
		
		return dataModelVO;
	}
	
	/**
	 * Retrieve data model (Model attribute details)
	 * @param id			Data model ID
	 * @param attrName		Attribute name
	 * @return				Data model attribute retrieved by data model ID and attribute name.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@GetMapping(value="/datamodels/attr")
	public ResponseEntity<DataModelResponseVO> getDataModelAttr(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value="id") String id,
			@RequestParam(value="attrName") String attrName) throws Exception {
		
		log.info("[UI API] getDataModelAttr - id: {}, attrName: {}", id, attrName);
		
		// 1. Retrieve Data Model
		ResponseEntity<DataModelResponseVO> dataModelVO = dataModelSVC.getDataModelAttr(id, attrName);
		
		return dataModelVO;
	}
	
	/**
	 * Retrieve multiple data models
	 * @param dataModelRetrieveVO	DataModelRetrieve VO
	 * @return						List of data model	retrieved by DataModelRetrieveVO.
	 * @throws Exception			Throw an exception when an error occurs.
	 */
	@GetMapping(value="/datamodels/list")
	@ResponseBody
	public ResponseEntity<DataModelListResponseVO> getDataModels(HttpServletRequest request, HttpServletResponse response,
			DataModelRetrieveVO dataModelRetrieveVO) throws Exception {
		
		log.info("[UI API] getDataModels - dataModelRetrieveVO: {}", dataModelRetrieveVO);
		
		// 1. Retrieve multiple Data Model
		ResponseEntity<DataModelListResponseVO> dataModelVOs = dataModelSVC.getDataModels(dataModelRetrieveVO);
		
		return dataModelVOs;
	}
	
	/**
	 * Request data model provision
	 * @param id			Data model ID
	 * @return				Data model provisioning result.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@PostMapping(value="/datamodels/{id}/provision")
	public <T> ResponseEntity<T> requestDataModelProvision(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") String id) throws Exception {
		
		log.info("[UI API] requestDataModelProvision - id: {}", id);
		
		// 1. Request DataModel provision
		ResponseEntity<T> reslt = (ResponseEntity<T>) dataModelSVC.requestDataModelProvision(id);
						
		return reslt;
	}
	
	/**
	 * Retrieve data model ID
	 * @return				List of data model ID
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@GetMapping(value="/datamodels/id")
	public ResponseEntity<List<String>> getDataModelsId(HttpServletRequest request, HttpServletResponse response) 
			throws Exception {
		log.info("[UI API] getDataModelsId()");
		
		// 1. Retrieve DataModel ID
		ResponseEntity<List<String>> idList = (ResponseEntity<List<String>>) dataModelSVC.getDataModelsId();
		
		return idList;
	}
	
	/**
	 * Check the required values ​​for data model creation
	 * @param dataModel		DataModel VO
	 * @return				valid: true, invalid: false
	 */
	private boolean checkMandatoryDataModelValues(DataModelVO dataModel) {
		if(dataModel == null) {
			return false;
		}
		
		if(ValidateUtil.isEmptyData(dataModel.getId())
				|| ValidateUtil.isEmptyData(dataModel.getContext())
				|| dataModel.getAttributes() == null
				|| dataModel.getAttributes().size() < 1) {
					return false;
				}
		
		if(!checkMandatoryAttibuteValues(dataModel.getAttributes())) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Check the required values ​​for list of attribute
	 * @param 	attributes
	 * @return	valid: true, invalid: false
	 */
	private boolean checkMandatoryAttibuteValues(List<AttributeVO> attributes) {
		for (AttributeVO attribute : attributes) {
			boolean isValid = checkMandatoryAttibuteValue(attribute);
			
			if (!isValid) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Check the required values ​​for attribute
	 * @param 	attribute
	 * @return	valid: true, invalid: false
	 */
	private boolean checkMandatoryAttibuteValue(AttributeVO attribute) {
		if (ValidateUtil.isEmptyData(attribute.getName())
				|| ValidateUtil.isEmptyData(attribute.getAttributeType())
				|| ValidateUtil.isEmptyData(attribute.getValueType())) {
			return false;
		} else {
			if ("Object".equals(attribute.getValueType()) || "ArrayObject".equals(attribute.getValueType())) {
				if (attribute.getObjectMembers() == null || attribute.getObjectMembers().size() < 1) {
					return false;
				} else {
					for (ObjectMemberVO objectMember : attribute.getObjectMembers()) {
						if (ValidateUtil.isEmptyData(objectMember.getName())
								|| ValidateUtil.isEmptyData(objectMember.getValueType())) {
							return false;
						}
					}
				}
			}
		}
		
		return true;
	}
}
