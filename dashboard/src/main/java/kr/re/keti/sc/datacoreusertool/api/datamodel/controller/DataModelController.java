package kr.re.keti.sc.datacoreusertool.api.datamodel.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import kr.re.keti.sc.datacoreusertool.api.datamodel.service.DataModelSVC;
import kr.re.keti.sc.datacoreusertool.api.datamodel.vo.DataModelVO;
import kr.re.keti.sc.datacoreusertool.api.datamodel.vo.UiTreeVO;
import kr.re.keti.sc.datacoreusertool.common.code.Constants;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller to get DataModel information.
 * @FileName DataModelController.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 25.
 * @Author Elvin
 */
@Slf4j
@Controller
public class DataModelController {

	@Autowired
	private DataModelSVC dataModelSVC;
	
	/**
	 * Retrieve data model information.
	 * @param id			Data model ID
	 * @return				Data model information (DataModelVO) retrieved by data model ID.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@GetMapping(value="/datamodels")
	@ApiOperation(value = "Retrieve DataModel", notes = "Retrieve DataModel")
	public ResponseEntity<DataModelVO> getDataModel(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value="id", defaultValue = "") String id,
			@RequestParam(value="type", defaultValue = "") String type) throws Exception {

		// 1. Check parameters
		if (id.isEmpty() && type.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}

		if (id.isEmpty()) {
			return dataModelSVC.getDataModelByEntityType(type);
		} else {
			return dataModelSVC.getDataModelbyId(id);
		}
	}
	
	/**
	 * Retrieve multi data model ID
	 * @return				All data model information.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@GetMapping(value="/datamodelIds")
	@ApiOperation(value = "Retrieve multi data model ID", notes = "Retrieve multi data model ID")
	@ResponseBody
	public ResponseEntity<List<String>> getDataModels(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		// 1. Retrieve multi data model ID
		ResponseEntity<List<String>> dataModelVOs = dataModelSVC.getDataModels();
		
		return dataModelVOs;
	}
	
	/**
	 * Retrieve type uri of data model
	 * @return				All data model type uri
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@GetMapping(value="/datamodel/typeuri")
	public ResponseEntity<List<String>> getDataModelTypeUri(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		// 1. Retrieve multi data model type uri
		ResponseEntity<List<String>> dataModelVOs = dataModelSVC.getDataModelTypeUri();
		
		return dataModelVOs;
	}
	
	/**
	 * Retrieve attribute, objectMember
	 * @param id			DataModel ID
	 * @param typeUri		Data model type uri
	 * @return				List of data model attribute retrieved by data model ID.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@GetMapping(value="datamodels/attrs")
	@ApiOperation(value = "Retrieve attribute", notes = "Retrieve attribute list")
	public ResponseEntity<List<String>> getDataModelAttrs(HttpServletRequest request, HttpServletResponse response, 
			@RequestParam(value="id", required=false) String id, @RequestParam(value="typeUri", required=false) String typeUri)
			throws Exception {
		
		// 1. Retrieve data model attributes
		ResponseEntity<List<String>> attrInfo = dataModelSVC.getDataModelAttrs(id, typeUri, Constants.ALL_LEVEL_ATTR);
		
		return attrInfo;
	}
	
	/**
	 * Retrieve top-level attribute
	 * @param id			Data model ID	
	 * @param typeUri		Data model type uri
	 * @return				The value of the top-level attribute in the data model.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@GetMapping(value="datamodels/topattrs")
	@ApiOperation(value = "Retrieve top-level attribute", notes = "Retrieve top-level attribute list")
	public ResponseEntity<List<String>> getDataModelTopAttrs(HttpServletRequest request, HttpServletResponse response, 
			@RequestParam(value="id", required=false) String id, @RequestParam(value="typeUri", required=false) String typeUri)
			throws Exception {
		
		// 1. Retrieve top-level attribute
		ResponseEntity<List<String>> attrInfo = dataModelSVC.getDataModelAttrs(id, typeUri, Constants.TOP_LEVEL_ATTR);
		
		return attrInfo;
	}
	
	/**
	 * Attribute/ObjectMember information of data model is composed of tree structure
	 * @param id			Data model ID
	 * @param typeUri		Data model type uri
	 * @return				Attribute in the tree structure
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@GetMapping(value="datamodels/attrstree")
	@ApiOperation(value = "Retrieve Attribute/ObjectMember tree structure", notes = "Attribute/ObjectMember information of data model is composed of tree structure.")
	public ResponseEntity<List<UiTreeVO>> getAttrsTree(HttpServletRequest request, HttpServletResponse response, 
			@RequestParam(value="id", required=false) String id, @RequestParam(value="typeUri", required=false) String typeUri)
			throws Exception {
	
		// 1. Attribute/ObjectMember information of data model is composed of tree structure
		ResponseEntity<List<UiTreeVO>> uiTreeVO = dataModelSVC.getAttrsTree(id, typeUri);
		
		return uiTreeVO;
	}
}
