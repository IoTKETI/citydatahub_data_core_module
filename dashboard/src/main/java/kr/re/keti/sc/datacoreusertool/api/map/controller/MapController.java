package kr.re.keti.sc.datacoreusertool.api.map.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import kr.re.keti.sc.datacoreusertool.api.map.service.MapSVC;
import kr.re.keti.sc.datacoreusertool.api.map.vo.MapSearchConditionBaseIdResponseVO;
import kr.re.keti.sc.datacoreusertool.api.map.vo.MapSearchConditionBaseResponseVO;
import kr.re.keti.sc.datacoreusertool.api.map.vo.MapSearchConditionBaseVO;
import kr.re.keti.sc.datacoreusertool.common.vo.ClientExceptionPayloadVO;
import kr.re.keti.sc.datacoreusertool.security.service.UserToolSecuritySVC;
import kr.re.keti.sc.datacoreusertool.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Class for management of map search condition.
 * @FileName MapController.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Slf4j
@Controller
public class MapController {
	
	@Autowired
	private MapSVC mapSVC;
	
	@Autowired
	private UserToolSecuritySVC userToolSecuritySVC;
	
	/**
	 * Create map search conditions
	 * @param mapSearchBaseVO	MapSearchConditionBaseVO
	 * @return					Result of create map search condition.
	 * @throws Exception		Throw an exception when an error occurs.
	 */
	@PostMapping(value="map")
	public ResponseEntity<MapSearchConditionBaseVO> createMapSearchCondition(HttpServletRequest request, HttpServletResponse response, 
			@RequestBody MapSearchConditionBaseVO mapSearchBaseVO) throws Exception {
		
		// 1. Validate required values
		if(ValidateUtil.isEmptyData(mapSearchBaseVO.getMapSearchConditionType())
				|| ValidateUtil.isEmptyData(mapSearchBaseVO.getMapSearchConditionName())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
		
		// 2. Create map search conditions
		ResponseEntity<MapSearchConditionBaseVO> result = mapSVC.createMapSearchCondition(request, mapSearchBaseVO);
		
		return result;
	}

	/**
	 * Update map search conditions
	 * @param mapSearchBaseVO	MapSearchConditionBaseVO (update data)
	 * @return					Result of update map search condition.
	 * @throws Exception		Throw an exception when an error occurs.
	 */
	@PutMapping(value="map")
	public ResponseEntity<MapSearchConditionBaseVO> updateMapSearchCondition(HttpServletRequest request, HttpServletResponse response, 
			@RequestBody MapSearchConditionBaseVO mapSearchBaseVO) throws Exception {
		
		// 1. Validate required values
		if(ValidateUtil.isEmptyData(mapSearchBaseVO.getMapSearchConditionType())
				|| ValidateUtil.isEmptyData(mapSearchBaseVO.getMapSearchConditionName())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
		
		// 2. update map search conditions
		ResponseEntity<MapSearchConditionBaseVO> result = mapSVC.updateMapSearchCondition(request, mapSearchBaseVO);
		
		return result;
	}
	
	/**
	 * Delete map search conditions
	 * @param mapSearchConditionId		ID of map search condition
	 * @return							Result of delete map search condition.
	 * @throws Exception				Throw an exception when an error occurs.
	 */
	@DeleteMapping(value="map/{mapSearchConditionId}")
	public ResponseEntity<ClientExceptionPayloadVO> deleteMapSearchCondition(HttpServletRequest request, HttpServletResponse response, 
			@PathVariable("mapSearchConditionId") String mapSearchConditionId) throws Exception {
		
		// 1. delete map search conditions
		ResponseEntity<ClientExceptionPayloadVO> result = mapSVC.deleteMapSearchCondition(request, mapSearchConditionId);
		
		return result;
	}
	
	/**
	 * Retrieve map search condition
	 * @param mapSearchConditionId		ID of map search condition
	 * @return							Map search condition retrieved by ID.
	 * @throws Exception				Throw an exception when an error occurs.
	 */
	@GetMapping(value="map")
	public ResponseEntity<MapSearchConditionBaseResponseVO> getMapSearchCondition(HttpServletRequest request, HttpServletResponse response, 
			@RequestParam String mapSearchConditionId) throws Exception {
		
		// 1. retrieve userId
		String userId = userToolSecuritySVC.getUserId(request).getBody();
		
		// 2. retrieve map search conditions
		ResponseEntity<MapSearchConditionBaseResponseVO> result = mapSVC.getMapSearchCondition(userId, mapSearchConditionId);
		
		return result;
	}
	
	/**
	 * Retrieve list of map search condition
	 * @return					List of Map search condition retrieved by Type.
	 * @throws Exception		Throw an exception when an error occurs.
	 */
	@GetMapping(value="map/ids")
	public ResponseEntity<List<MapSearchConditionBaseIdResponseVO>> getMapSearchConditions(HttpServletRequest request, HttpServletResponse response,
			@RequestParam String mapSearchConditionType) throws Exception {
		
		// 1. retrieve all map search conditions
		ResponseEntity<List<MapSearchConditionBaseIdResponseVO>> result = mapSVC.getMapSearchConditions(request, mapSearchConditionType);
		
		return result;
	}
}
