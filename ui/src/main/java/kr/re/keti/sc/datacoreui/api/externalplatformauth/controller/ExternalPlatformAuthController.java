package kr.re.keti.sc.datacoreui.api.externalplatformauth.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import kr.re.keti.sc.datacoreui.api.externalplatformauth.service.ExternalPlatformAuthSVC;
import kr.re.keti.sc.datacoreui.api.externalplatformauth.vo.ExternalPlatformAuthListResponseVO;
import kr.re.keti.sc.datacoreui.api.externalplatformauth.vo.ExternalPlatformAuthResponseVO;
import kr.re.keti.sc.datacoreui.api.externalplatformauth.vo.ExternalPlatformAuthRetrieveVO;
import kr.re.keti.sc.datacoreui.api.externalplatformauth.vo.ExternalPlatformAuthVO;
import kr.re.keti.sc.datacoreui.common.code.DataCoreUiCode.ErrorCode;
import kr.re.keti.sc.datacoreui.common.exception.BadRequestException;
import kr.re.keti.sc.datacoreui.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Class for external platform auth management API calls.
 * @FileName ExternalPlatformAuthController.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 23.
 * @Author Elvin
 */
@Slf4j
@RestController
public class ExternalPlatformAuthController {
	
	@Autowired
	private ExternalPlatformAuthSVC externalPlatformAuthSVC;

	/**
	 * Create external platform authentication
	 * @param externalPlatformAuth	ExternalPlatformAuthVO object
	 * @return				Result of external platform authentication creation.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@PostMapping(value="/externalplatform/authentication")
	public <T> ResponseEntity<T> createExternalPlatformAuth(HttpServletRequest request, HttpServletResponse response,
			@RequestBody ExternalPlatformAuthVO externalPlatformAuth) throws Exception {
		
		log.info("[UI API] createExternalPlatformAuth - externalPlatformAuth: {}", externalPlatformAuth);
		
		// 1. Check required values
		if(!checkMandatoryExternalPlatformAuthValues(externalPlatformAuth)) {
			throw new BadRequestException(ErrorCode.BAD_REQUEST, "Missing required value.");
		}
		
		// 2. Create external platform authentication
		ResponseEntity<T> reslt = externalPlatformAuthSVC.createExternalPlatformAuth(externalPlatformAuth);
		
		return reslt;
	}

	/**
	 * Update external platform authentication
	 * @param id					External platform ID 
	 * @param externalPlatformAuth	ExternalPlatformAuthVO object
	 * @return						Result of update external platform authentication.
	 * @throws Exception			Throw an exception when an error occurs.
	 */
	@PatchMapping(value="/externalplatform/authentication/{id}")
	public <T> ResponseEntity<T> updateExternalPlatformAuth(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") String id,
			@RequestBody ExternalPlatformAuthVO externalPlatformAuth) throws Exception {
		
		log.info("[UI API] updateExternalPlatformAuth - id: {}, externalPlatformAuth: {}", id, externalPlatformAuth);
		
		// 1. Update external platform authentication
		ResponseEntity<T> reslt = externalPlatformAuthSVC.updateExternalPlatformAuth(id, externalPlatformAuth);
		
		return reslt;
	}
	
	/**
	 * Delete external platform authentication
	 * @param id				External platform ID
	 * @return					Result of delete external platform authentication.
	 * @throws Exception		Throw an exception when an error occurs.
	 */
	@DeleteMapping(value="/externalplatform/authentication/{id}")
	public <T> ResponseEntity<T> deleteExternalPlatformAuth(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") String id) throws Exception {
		
		log.info("[UI API] deleteExternalPlatformAuth - id: {}", id);
		
		// 1. Update external platform authentication
		ResponseEntity<T> reslt = externalPlatformAuthSVC.deleteExternalPlatformAuth(id);
		
		return reslt;
	}
	
	/**
	 * Retrieve external platform authentication
	 * @param id				External platform ID
	 * @return					External platform authentication retrieved by external platform id.
	 * @throws Exception		Throw an exception when an error occurs.
	 */
	@GetMapping(value="/externalplatform/authentication/{id}")
	public ResponseEntity<ExternalPlatformAuthResponseVO> getExternalPlatformAuth(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") String id) throws Exception {
		
		log.info("[UI API] getExternalPlatformAuth - id: {}", id);
		
		// 1. Retrieve external platform authentication
		ResponseEntity<ExternalPlatformAuthResponseVO> externalPlatformAuth = externalPlatformAuthSVC.getExternalPlatformAuth(id);
		
		return externalPlatformAuth;
	}
	
	/**
	 * Retrieve multiple external platform authentication
	 * @param externalPlatformAuthRetrieve	ExternalPlatformAuthRetrieveVO object 
	 * @return								List of External platform authentication.
	 * @throws Exception					Throw an exception when an error occurs.
	 */
	@GetMapping(value="/externalplatform/authentication")
	public ResponseEntity<ExternalPlatformAuthListResponseVO> getExternalPlatformAuths(HttpServletRequest request, HttpServletResponse response,
			ExternalPlatformAuthRetrieveVO externalPlatformAuthRetrieve) throws Exception {
		
		log.info("[UI API] getExternalPlatformAuths - externalPlatformAuthRetrieve: {}", externalPlatformAuthRetrieve);
		
		// 1. Retrieve multiple external platform authentication
		ResponseEntity<ExternalPlatformAuthListResponseVO> provisionServers = externalPlatformAuthSVC.getExternalPlatformAuths(externalPlatformAuthRetrieve);
		
		return provisionServers;
	}
	
	/**
	 * Check the required value for external platform authentication
	 * @param 	externalPlatformAuth
	 * @return	valid: true, invalid: false
	 */
	private boolean checkMandatoryExternalPlatformAuthValues(ExternalPlatformAuthVO externalPlatformAuth) {
		if(externalPlatformAuth == null) {
			return false;
		}
		
		if(ValidateUtil.isEmptyData(externalPlatformAuth.getId())
				|| ValidateUtil.isEmptyData(externalPlatformAuth.getName())
				|| ValidateUtil.isEmptyData(externalPlatformAuth.getReceptionIps())
				|| ValidateUtil.isEmptyData(externalPlatformAuth.getReceptionDatasetIds())
				|| ValidateUtil.isEmptyData(externalPlatformAuth.getReceptionClientIds())) {
			return false;
		}
		
		return true;
	}
}
