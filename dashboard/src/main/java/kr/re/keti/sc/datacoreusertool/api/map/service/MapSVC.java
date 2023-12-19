package kr.re.keti.sc.datacoreusertool.api.map.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import kr.re.keti.sc.datacoreusertool.api.map.dao.MapDAO;
import kr.re.keti.sc.datacoreusertool.api.map.vo.MapSearchConditionBaseIdResponseVO;
import kr.re.keti.sc.datacoreusertool.api.map.vo.MapSearchConditionBaseResponseVO;
import kr.re.keti.sc.datacoreusertool.api.map.vo.MapSearchConditionBaseVO;
import kr.re.keti.sc.datacoreusertool.common.vo.ClientExceptionPayloadVO;
import kr.re.keti.sc.datacoreusertool.security.service.UserToolSecuritySVC;
import kr.re.keti.sc.datacoreusertool.util.ValidateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for management of map search condition.
 * @FileName MapSVC.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class MapSVC {
	
	@Autowired
	private MapDAO mapDAO;
	
	@Autowired
	private UserToolSecuritySVC userToolSecuritySVC;

	/**
	 * Create map search conditions
	 * @param mapSearchBaseVO	MapSearchConditionBaseVO
	 * @return					Result of create map search condition.
	 */
	public ResponseEntity<MapSearchConditionBaseVO> createMapSearchCondition(HttpServletRequest request,
			MapSearchConditionBaseVO mapSearchBaseVO) {
		
		String userId = userToolSecuritySVC.getUserId(request).getBody();
		
		if(ValidateUtil.isEmptyData(userId)) {
			log.warn("Authentication failure. userId: {}", userId);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		String mapSearchConditionId = UUID.randomUUID().toString();
		mapSearchBaseVO.setMapSearchConditionId(mapSearchConditionId);
		mapSearchBaseVO.setUserId(userId);
		
		try {
			mapDAO.createMapSearchCondition(mapSearchBaseVO);
		} catch(DuplicateKeyException e) {
			log.warn("Duplicate map search condition. mapSearchConditionId: {}", mapSearchBaseVO.getMapSearchConditionId(), e);
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		} catch(Exception e) {
			log.error("Fail to createMapSearchCondition.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		return ResponseEntity.status(HttpStatus.CREATED).body(mapSearchBaseVO);
	}

	/**
	 * Update map search conditions
	 * @param mapSearchBaseVO	MapSearchConditionBaseVO
	 * @return					Result of update map search condition.
	 */
	public ResponseEntity<MapSearchConditionBaseVO> updateMapSearchCondition(HttpServletRequest request,
			MapSearchConditionBaseVO mapSearchBaseVO) {
		
		String userId = userToolSecuritySVC.getUserId(request).getBody();
		
		if(ValidateUtil.isEmptyData(userId)) {
			log.warn("Authentication failure. userId: {}", userId);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		mapSearchBaseVO.setUserId(userId);
		
		try {
			if(mapDAO.updateMapSearchCondition(mapSearchBaseVO) <= 0) {
				log.error("Fail to update DB for widget data.");
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			}
		} catch(Exception e) {
			log.error("Fail to updateWidget.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} 
		
		return ResponseEntity.ok().body(mapSearchBaseVO);
	}

	/**
	 * Delete map search conditions
	 * @param mapSearchConditionId	ID of map search condition
	 * @return						Result of delete map search condition.
	 */
	public ResponseEntity<ClientExceptionPayloadVO> deleteMapSearchCondition(HttpServletRequest request,
			String mapSearchConditionId) {
		
		String userId = userToolSecuritySVC.getUserId(request).getBody();
		
		if(ValidateUtil.isEmptyData(userId)) {
			log.warn("Authentication failure. userId: {}", userId);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		MapSearchConditionBaseVO mapSearchBaseVO = new MapSearchConditionBaseVO();
		mapSearchBaseVO.setMapSearchConditionId(mapSearchConditionId);
		mapSearchBaseVO.setUserId(userId);
		
		try {
			mapDAO.deleteMapSearchCondition(mapSearchBaseVO);
		} catch(Exception e) {
			log.error("Fail to deleteMapSearchCondition.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	/**
	 * Retrieve map search condition
	 * @param mapSearchConditionId	ID of map search condition
	 * @return						Map search condition retrieved by ID.
	 */
	public ResponseEntity<MapSearchConditionBaseResponseVO> getMapSearchCondition(String userId, String mapSearchConditionId) {
		
		MapSearchConditionBaseResponseVO mapSearchBaseResponseVO = null;
		
		if(ValidateUtil.isEmptyData(userId)) {
			log.warn("Authentication failure. userId: {}", userId);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		MapSearchConditionBaseVO mapSearchBaseVO = new MapSearchConditionBaseVO();
		mapSearchBaseVO.setMapSearchConditionId(mapSearchConditionId);
		mapSearchBaseVO.setUserId(userId);
		
		try {
			mapSearchBaseResponseVO = mapDAO.getMapSearchCondition(mapSearchBaseVO);
		} catch(Exception e) {
			log.error("Fail to getMapSearchCondition.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		if(mapSearchBaseResponseVO == null) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}
		
		return ResponseEntity.ok().body(mapSearchBaseResponseVO);
	}

	/**
	 * Retrieve list of map search condition
	 * @param mapSearchConditionType	Type of map search condition
	 * @return							List of Map search condition retrieved by Type.
	 */
	public ResponseEntity<List<MapSearchConditionBaseIdResponseVO>> getMapSearchConditions(HttpServletRequest request,
			String mapSearchConditionType) {
		
		List<MapSearchConditionBaseIdResponseVO> mapSearchBaseIdResponseVOs = new ArrayList<MapSearchConditionBaseIdResponseVO>();
		
		String userId = userToolSecuritySVC.getUserId(request).getBody();
		
		if(ValidateUtil.isEmptyData(userId)) {
			log.warn("Authentication failure. userId: {}", userId);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		MapSearchConditionBaseVO mapSearchBaseVO = new MapSearchConditionBaseVO();
		mapSearchBaseVO.setMapSearchConditionType(mapSearchConditionType);
		mapSearchBaseVO.setUserId(userId);
		
		try {
			mapSearchBaseIdResponseVOs = mapDAO.getMapSearchConditions(mapSearchBaseVO);
		} catch(Exception e) {
			log.error("Fail to getMapSearchCondition.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		return ResponseEntity.ok().body(mapSearchBaseIdResponseVOs);
	}

}
