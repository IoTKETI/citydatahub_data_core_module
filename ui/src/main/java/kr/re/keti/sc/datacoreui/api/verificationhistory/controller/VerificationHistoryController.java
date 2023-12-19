package kr.re.keti.sc.datacoreui.api.verificationhistory.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import kr.re.keti.sc.datacoreui.api.verificationhistory.service.VerificationHistorySVC;
import kr.re.keti.sc.datacoreui.api.verificationhistory.vo.VerificationHistoryCountVO;
import kr.re.keti.sc.datacoreui.api.verificationhistory.vo.VerificationHistoryListResponseVO;
import kr.re.keti.sc.datacoreui.api.verificationhistory.vo.VerificationHistoryListVO;
import kr.re.keti.sc.datacoreui.api.verificationhistory.vo.VerificationHistoryRequestVO;
import kr.re.keti.sc.datacoreui.api.verificationhistory.vo.VerificationHistoryVO;
import kr.re.keti.sc.datacoreui.common.code.DataCoreUiCode.ErrorCode;
import kr.re.keti.sc.datacoreui.common.exception.BadRequestException;
import kr.re.keti.sc.datacoreui.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Class for verification history management API calls.
 * @FileName VerificationHistoryController.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Slf4j
@RestController
public class VerificationHistoryController {
	
	@Autowired
	private VerificationHistorySVC verificationHistorySVC;
	
	/**
	 * Multiple inquiry of quality verification history
	 * @param verificationHistoryRequest	Quality verification history inquiry object
	 * @return								List of quality verification history retrieved by VerificationHistoryRequestVO.
	 * @throws Exception					Throw an exception when an error occurs.
	 */
	@GetMapping(value="/verificationHistory")
	public ResponseEntity<VerificationHistoryListVO> getVerificationHistorys(HttpServletRequest request, HttpServletResponse response,
			VerificationHistoryRequestVO verificationHistoryRequest) throws Exception {
		
		log.info("[UI API] getVerificationHistorys - verificationHistoryRequest: {}", verificationHistoryRequest);
		
		// 1. Check required values
		if(!checkMandatorVerificationHistoryValues(verificationHistoryRequest)) {
			throw new BadRequestException(ErrorCode.BAD_REQUEST, "Missing required value.");
		}
		
		// 2. Multiple inquiry of quality verification history
		ResponseEntity<VerificationHistoryListVO> verificationHistorys = verificationHistorySVC.getVerificationHistorys(verificationHistoryRequest);
		
		return verificationHistorys;
	}
	
	/**
	 * Single case inquiry of quality verification history
	 * @param seq			Quality inspection history identification serial number.
	 * @return				Quality verification history retrieved by sequence of verification history.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@GetMapping(value="/verificationHistory/{seq}")
	public ResponseEntity<VerificationHistoryVO> getVerificationHistory(HttpServletRequest request, HttpServletResponse response,
			@PathVariable Long seq) throws Exception {
		
		log.info("[UI API] getVerificationHistory - seq: {}", seq);
		
		// 1. Single case inquiry of quality verification history
		ResponseEntity<VerificationHistoryVO> verificationHistory = verificationHistorySVC.getVerificationHistory(seq);
		
		return verificationHistory;
	}
	
	/**
	 * Inquire the number of cases by quality verification history result
	 * @param verificationHistoryRequest	Quality verification history object
	 * @return								Count of quality verification history.
	 * @throws Exception					Throw an exception when an error occurs.
	 */
	@GetMapping(value="/verificationHistory/count")
	public ResponseEntity<VerificationHistoryCountVO> getVerificationHistoryCount(HttpServletRequest request, HttpServletResponse response,
			VerificationHistoryRequestVO verificationHistoryRequest) throws Exception {
		
		log.info("[UI API] getVerificationHistoryCount - verificationHistoryRequest: {}", verificationHistoryRequest);
		
		// 1. Check required values
		if(!checkMandatorVerificationHistoryValues(verificationHistoryRequest)) {
			throw new BadRequestException(ErrorCode.BAD_REQUEST, "Missing required value.");
		}
		
		// 2. Inquire the number of cases by quality verification history result
		ResponseEntity<VerificationHistoryCountVO> verificationHistoryCount = verificationHistorySVC.getVerificationHistoryCount(verificationHistoryRequest);
		
		return verificationHistoryCount;
	}
	
	/**
	 * Inquiry of quality verification history and number of cases by result
	 * @param verificationHistoryRequest	Quality verification history object
	 * @return								All quality verification history retrieved by VerificationHistoryRequestVO.
	 * @throws Exception					Throw an exception when an error occurs.
	 */
	@GetMapping(value="/verificationHistory/all")
	public ResponseEntity<VerificationHistoryListResponseVO> getVerificationHistoryAll(HttpServletRequest request, HttpServletResponse response,
			VerificationHistoryRequestVO verificationHistoryRequest) throws Exception {
		log.info("[UI API] getVerificationHistoryAll - verificationHistoryRequest: {}", verificationHistoryRequest);
		
		// 1. Check required values
		if(!checkMandatorVerificationHistoryValues(verificationHistoryRequest)) {
			throw new BadRequestException(ErrorCode.BAD_REQUEST, "Missing required value.");
		}
		
		// 2. Inquiry of quality verification history and number of cases by result
		ResponseEntity<VerificationHistoryListResponseVO> verificationHistoryListResponse = verificationHistorySVC.getVerificationHistoryAll(verificationHistoryRequest);
		
		return verificationHistoryListResponse;
	}

	/**
	 * Quality verification history inquiry condition Required value check
	 * @param verificationHistoryRequest	Quality verification history object
	 * @return	valid: true, invalid: false
	 */
	private boolean checkMandatorVerificationHistoryValues(VerificationHistoryRequestVO verificationHistoryRequest) {
		
		if(ValidateUtil.isEmptyData(verificationHistoryRequest.getStartTime())
				|| ValidateUtil.isEmptyData(verificationHistoryRequest.getEndTime())) {
			return false;
		}
		
		return true;
	}
	
}
