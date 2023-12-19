package kr.re.keti.sc.datacoreusertool.api.dataservicebroker.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.ApiOperation;
import kr.re.keti.sc.datacoreusertool.api.dataservicebroker.service.DataServiceBrokerSVC;
import kr.re.keti.sc.datacoreusertool.api.dataservicebroker.vo.CommonEntityListResponseVO;
import kr.re.keti.sc.datacoreusertool.api.dataservicebroker.vo.CommonEntityVO;
import kr.re.keti.sc.datacoreusertool.api.dataservicebroker.vo.EntityRetrieveVO;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for DataServiceBroker API calls.
 * @FileName DataServiceBrokerController.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 25.
 * @Author Elvin
 */
@Slf4j
@Controller
public class DataServiceBrokerController {
	
	@Autowired
	private DataServiceBrokerSVC dataServiceBrokerSVC;
	

	/**
	 * Retrieve multiple Entity(latest)
	 * @param isMap					for map data or not
	 * @param entityRetrieveVO		EntityRetrieveVO
	 * @return						List of Entity retrieved by EntityRetrieveVO.
	 * @throws Exception			Throw an exception when an error occurs.
	 */
	@PostMapping(value="/entities")
	@ApiOperation(value = "Retrieve multiple Entity(latest)", notes = "Retrieve the latest value of entity.")
	public ResponseEntity<CommonEntityListResponseVO> getEntities(HttpServletRequest request, HttpServletResponse response, 
			@RequestParam(required=false) boolean isMap, @RequestBody EntityRetrieveVO entityRetrieveVO) throws Exception {
		
		// 1. Check required values
		if (entityRetrieveVO == null || 
				(entityRetrieveVO.getDataModelId() == null && entityRetrieveVO.getTypeUri() == null)) {
			CommonEntityListResponseVO result = new CommonEntityListResponseVO();
			result.setType(HttpStatus.BAD_REQUEST.name());
			result.setTitle("Missing required value.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
		}
		
		// 2. Retrieve multiple Entity(latest)
		ResponseEntity<CommonEntityListResponseVO> commonEntityListResponseVO = dataServiceBrokerSVC.getEntities(isMap, entityRetrieveVO, request, null);
		
		return commonEntityListResponseVO;
	}

	/**
	 * Retrieve multiple Entity(latest) by model
	 * @param entityRetrieveVOs		List of EntityRetrieveVO
	 * @return						List of multiple entity retrieved by EntityRetrieveVO list.
	 * @throws Exception			Throw an exception when an error occurs.
	 */
	@PostMapping(value="/entities/multimodel")
	@ApiOperation(value = "Retrieve multiple Entity(latest) by model", notes = "Retrieve the multiple entity(latest) by model.")
	public ResponseEntity<List<CommonEntityListResponseVO>> getEntities(HttpServletRequest request, HttpServletResponse response, 
			@RequestBody List<EntityRetrieveVO> entityRetrieveVOs) throws Exception {
		
		// 1. Check required values
		if (entityRetrieveVOs == null || entityRetrieveVOs.size() < 1) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
		for(EntityRetrieveVO entityRetrieveVO : entityRetrieveVOs) {
			if(entityRetrieveVO.getDataModelId() == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
			}
		}
		
		// 2. Retrieve multiple Entity(latest) by model
		ResponseEntity<List<CommonEntityListResponseVO>> commonEntityListResponseVO = dataServiceBrokerSVC.getEntitiesbyMultiModel(entityRetrieveVOs, request, null);
		
		return commonEntityListResponseVO;
	}

	/**
	 * Retrieve Entity(latest)
	 * @param id					Entity ID
	 * @param entityRetrieveVO		entityRetrieveVO object
	 * @return						Entity retrieved by entity ID and EntityRetrieveVO.
	 * @throws Exception			Throw an exception when an error occurs.
	 */
	@PostMapping(value="/entities/{id}")
	@ApiOperation(value = "Retrieve Entity(latest)", notes = "Retrieve the latest entity value.")
	public ResponseEntity<CommonEntityVO> getEntityById(HttpServletRequest request, HttpServletResponse response, @PathVariable String id, @RequestBody EntityRetrieveVO entityRetrieveVO) 
			throws Exception {
		
		// 1. Retrieve Entity(latest)
		ResponseEntity<CommonEntityVO> commonEntityVO = dataServiceBrokerSVC.getEntityById(id, entityRetrieveVO, request, null);
		
		return commonEntityVO;
	}
	
	/**
	 * Retrieve multiple Entity(historical)
	 * @param entityRetrieveVO		entityRetrieveVO object
	 * @return						List of entity retrieved by EntityRetrieveVO.
	 * @throws Exception			Throw an exception when an error occurs.
	 */
	@PostMapping(value="/temporal/entities")
	@ApiOperation(value = "Retrieve multiple Entity(historical)", notes = "Retrieve multiple entity history data.")
	public ResponseEntity<CommonEntityListResponseVO> getEntitiesHistory(HttpServletRequest request, HttpServletResponse response, @RequestBody EntityRetrieveVO entityRetrieveVO) 
			throws Exception {
		// 1. Check required values
		if (entityRetrieveVO == null || entityRetrieveVO.getDataModelId() == null) {
			CommonEntityListResponseVO result = new CommonEntityListResponseVO();
			result.setType(HttpStatus.BAD_REQUEST.name());
			result.setTitle("Missing required value.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
		}
		
		// 2. Retrieve multiple Entity(historical)
		ResponseEntity<CommonEntityListResponseVO> commonEntityListResponseVO = dataServiceBrokerSVC.getEntitiesHistory(entityRetrieveVO, request, null);
		
		return commonEntityListResponseVO;
	}
	
	/**
	 * Retrieve single Entity(historical)
	 * @param id					Entity ID
	 * @param entityRetrieveVO		entityRetrieveVO object
	 * @return						List of entity retrieved by EntityRetrieveVO.
	 * @throws Exception			Throw an exception when an error occurs.
	 */
	@PostMapping(value="/temporal/entities/{id}")
	@ApiOperation(value = "Retrieve single Entity(historical)", notes = "Retrieve single entity history data.")
	public ResponseEntity<CommonEntityListResponseVO> getEntityHistoryById(HttpServletRequest request, HttpServletResponse response, @PathVariable String id, @RequestBody EntityRetrieveVO entityRetrieveVO) 
			throws Exception {
		
		ResponseEntity<CommonEntityListResponseVO> commonEntityListVO = dataServiceBrokerSVC.getEntityHistoryById(id, entityRetrieveVO, request);
		
		return commonEntityListVO;
	}
	
	/**
	 * Retrieve list of entity ID 
	 * @param entityRetrieveVO		EntityRetrieveVO
	 * @return						List of entity ID
	 * @throws Exception			Throw an exception when an error occurs.
	 */
	@PostMapping(value="/entityIds")
	public ResponseEntity<List<String>> getEntityIds(HttpServletRequest request, HttpServletResponse response, 
			@RequestBody EntityRetrieveVO entityRetrieveVO) throws Exception {
		
		// 1. Check required values
		if (entityRetrieveVO == null || 
				(entityRetrieveVO.getDataModelId() == null && entityRetrieveVO.getTypeUri() == null)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
		
		// 2. Retrieve single Entity(historical)
		ResponseEntity<List<String>> entityIds = dataServiceBrokerSVC.getEntityIds(entityRetrieveVO, request);
		
		return entityIds;
	}
}
