package kr.re.keti.sc.datacoreui.api.code.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.re.keti.sc.datacoreui.common.component.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.re.keti.sc.datacoreui.api.code.service.CodeSVC;
import kr.re.keti.sc.datacoreui.api.code.vo.CodeBaseVO;
import kr.re.keti.sc.datacoreui.api.code.vo.CodeGroupBaseVO;
import kr.re.keti.sc.datacoreui.api.code.vo.CodeGroupRequestVO;
import kr.re.keti.sc.datacoreui.api.code.vo.CodeGroupResponseVO;
import kr.re.keti.sc.datacoreui.api.code.vo.CodeRequestVO;
import kr.re.keti.sc.datacoreui.api.code.vo.CodeResponseVO;
import kr.re.keti.sc.datacoreui.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * UI code management class.
 * @FileName CodeController.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 22.
 * @Author Elvin
 */
@Slf4j
@RestController
public class CodeController {
	
	@Autowired
	private CodeSVC codeSVC;

	@Autowired
	private Properties properties;

	/**
	 * Create code group
	 * @param codeGroupBaseVO 	CodeGroupBase VO
	 * @return					Result of code group creation.
	 * @throws Exception		Throw an exception when an error occurs.
	 */
	@PostMapping(value="/codegroup")
	public <T> ResponseEntity<T> createCodeGroup(HttpServletRequest request, HttpServletResponse response,
			@RequestBody CodeGroupBaseVO codeGroupBaseVO) throws Exception {
		
		log.info("[UI API] createCodeGroup - codeGroupBaseVO: {}", codeGroupBaseVO);
		
		// 1. Check required values
		if(!checkMandatoryCodeGroupValues(codeGroupBaseVO)) {
			return new ResponseEntity<>((T)"Missing required value.", HttpStatus.BAD_REQUEST);
		}
		
		// 2. Create code group
		ResponseEntity<T> reslt = codeSVC.createCodeGroup(codeGroupBaseVO);
		
		return reslt;
	}
	
	/**
	 * Update code group
	 * @param codeGroupId    	Code group ID
	 * @param codeGroupBasVO 	CodeGroupBasVO VO (update data)
	 * @return					Code group update result.
	 * @throws Exception		Throw an exception when an error occurs.
	 */
	@PatchMapping(value="/codegroup/{codeGroupId}")
	public <T> ResponseEntity<T> updateCodeGroup(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String codeGroupId,
			@RequestBody CodeGroupBaseVO codeGroupBaseVO) throws Exception {
		log.info("[UI API] updateCodeGroup - codeGroupId: {}, codeGroupBaseVO: {}", codeGroupId, codeGroupBaseVO);
		
		// 1. Update code group
		ResponseEntity<T> reslt = codeSVC.updateCodeGroup(codeGroupId, codeGroupBaseVO);
		
		return reslt;
	}
	
	/**
	 * Delete code group
	 * @param codeGroupId   Code group ID
	 * @return				Code group delete result.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@DeleteMapping(value="/codegroup/{codeGroupId}")
	public <T> ResponseEntity<T> deleteCodeGroup(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String codeGroupId) throws Exception {
		log.info("[UI API] deleteCodeGroup - codeGroupId: {}", codeGroupId);
		
		// 1. Delete code group
		ResponseEntity<T> reslt = codeSVC.deleteCodeGroup(codeGroupId);
		
		return reslt;
	}
	
	/**
	 * Retrieve code group
	 * @param codeGroupId   Code group ID
	 * @return				Code group information retrieved by code group ID.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@GetMapping(value="/codegroup/{codeGroupId}")
	public ResponseEntity<CodeGroupBaseVO> getCodeGroup(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String codeGroupId) throws Exception {
		log.info("[UI API] getCodeGroup - codeGroupId: {}", codeGroupId);
		
		// 1. Retrieve code group
		ResponseEntity<CodeGroupBaseVO> reslt = codeSVC.getCodeGroup(codeGroupId);
		
		return reslt;
	}
	
	/**
	 * Retrieve multiple code group
	 * @param codeGroupRequestVO  	CodeGourpRequestVO object
	 * @return						List of code group retrieved by CodeGroupRequestVO.
	 * @throws Exception			Throw an exception when an error occurs.
	 */
	@GetMapping(value="/codegroup")
	public ResponseEntity<CodeGroupResponseVO> getCodeGroups(HttpServletRequest request, HttpServletResponse response,
			CodeGroupRequestVO codeGroupRequestVO) throws Exception {
		log.info("[UI API] getCodeGroups - codeGroupRequestVO: {}", codeGroupRequestVO);
		
		// 1. Check required values
		if(codeGroupRequestVO == null
				|| codeGroupRequestVO.getCurrentPage() == null || codeGroupRequestVO.getCurrentPage() < 1
				|| codeGroupRequestVO.getPageSize() == null || codeGroupRequestVO.getPageSize() < 1) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		// 2. Retrieve multiple code group
		ResponseEntity<CodeGroupResponseVO> reslt = codeSVC.getCodeGroups(codeGroupRequestVO);
		
		return reslt;
	}
	
	/**
	 * Create code
	 * @param codeBasVO 	CodeBasVO object
	 * @return				Result of code group creation.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@PostMapping(value="/code")
	public <T> ResponseEntity<T> createCode(HttpServletRequest request, HttpServletResponse response,
			@RequestBody CodeBaseVO codeBaseVO) throws Exception {
		
		log.info("[UI API] createCode - codeBaseVO: {}", codeBaseVO);
		
		// 1. Check required values
		if(!checkMandatoryCodeValues(codeBaseVO)) {
			return new ResponseEntity<>((T)"Missing required value.", HttpStatus.BAD_REQUEST);
		}
		
		if (codeBaseVO.getLangCd() == null) {
			codeBaseVO.setLangCd(properties.getLangCd());
		}
		
		// 2. Create code
		ResponseEntity<T> reslt = codeSVC.createCode(codeBaseVO);
		
		return reslt;
	}
	
	/**
	 * Update code
	 * @param codeGroupId   Code group ID
	 * @param codeId        Code ID
	 * @param codeBaseVO    CodeBaseVO Object (update data)
	 * @return				Code group update result.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@PatchMapping(value="/code/{codeGroupId}/{codeId}")
	public <T> ResponseEntity<T> updateCode(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String codeGroupId,
			@PathVariable String codeId,
			@RequestParam(value="langCd", required=false) String langCd,
			@RequestBody CodeBaseVO codeBaseVO) throws Exception {
		log.info("[UI API] updateCode - codeGroupId: {}, codeId: {}, langCd: {}, codeGroupBasVO: {}", codeGroupId, codeId, langCd, codeBaseVO);
		
		if (langCd == null) {
			langCd = properties.getLangCd();
		}
		
		// 1. Update code
		ResponseEntity<T> reslt = codeSVC.updateCode(codeGroupId, codeId, langCd, codeBaseVO);
		
		return reslt;
	}
	
	/**
	 * Delete code
	 * @param codeGroupId   Code group ID
	 * @param codeId        Code ID
	 * @return				Code group delete result.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@DeleteMapping(value="/code/{codeGroupId}/{codeId}")
	public <T> ResponseEntity<T> deleteCode(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String codeGroupId,
			@PathVariable String codeId,
			@RequestParam(value="langCd", required=false) String langCd) throws Exception {
		log.info("[UI API] deleteCode - codeGroupId: {}, codeId: {}, langCd: {}", codeGroupId, codeId, langCd);
		
		if (langCd == null) {
			langCd = properties.getLangCd();
		}
		
		// 1. Delete code
		ResponseEntity<T> reslt = codeSVC.deleteCode(codeGroupId, codeId, langCd);
		
		return reslt;
	}

	/**
	 * Retrieve code
	 * @param codeGroupId   Code group ID
	 * @param codeId        Code ID
	 * @return				Code retrieved by code group ID and code ID.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@GetMapping(value="/code/{codeGroupId}/{codeId}")
	public ResponseEntity<CodeBaseVO> getCode(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String codeGroupId,
			@PathVariable String codeId,
			@RequestParam(value="langCd", required=false) String langCd) throws Exception {
		log.info("[UI API] getCode - codeGroupId: {}, codeId: {}, langCd: {}", codeGroupId, codeId, langCd);
		
		if (langCd == null) {
			langCd = properties.getLangCd();
		}
		
		// 1. Retrieve code
		ResponseEntity<CodeBaseVO> reslt = codeSVC.getCode(codeGroupId, codeId, langCd);
		
		return reslt;
	}
	
	/**
	 * Retrieve multiple code
	 * @param codeRequestVO    	codeRequestVO object
	 * @return					List of code retrieved by CodeRequestVO.
	 * @throws Exception		Throw an exception when an error occurs.
	 */
	@GetMapping(value="/code")
	public ResponseEntity<CodeResponseVO> getCodes(HttpServletRequest request, HttpServletResponse response,
			CodeRequestVO codeRequestVO) throws Exception {
		log.info("[UI API] getCodes - codeRequestVO: {}", codeRequestVO);
		
		// 1. Check required values
		if(codeRequestVO == null
				|| codeRequestVO.getCurrentPage() == null || codeRequestVO.getCurrentPage() < 1
				|| codeRequestVO.getPageSize() == null || codeRequestVO.getPageSize() < 1) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		//TODO: client must fill in langCd if necessary
		if (codeRequestVO.getLangCd() == null) {
			codeRequestVO.setLangCd(properties.getLangCd());
		}
		
		// 2. Retrieve multiple code
		ResponseEntity<CodeResponseVO> reslt = codeSVC.getCodes(codeRequestVO);
		
		return reslt;
	}

	/**
	 * Required value check for code group
	 * @param codeGroupBasVO	Code group base VO
	 * @return					valid: true, invalid: false
	 */
	private boolean checkMandatoryCodeGroupValues(CodeGroupBaseVO codeGroupBaseVO) {
		if(codeGroupBaseVO == null) {
			return false;
		}
			
		if(ValidateUtil.isEmptyData(codeGroupBaseVO.getCodeGroupId())
				|| ValidateUtil.isEmptyData(codeGroupBaseVO.getCodeGroupName())) {
			return false;
		}
		
		return true;
	}
	
	/** 
	 * Required value check for code
	 * @param codeBasVO Code base VO
	 * @return			valid: true, invalid: false
	 */
	private boolean checkMandatoryCodeValues(CodeBaseVO codeBaseVO) {
		if(codeBaseVO == null) {
			return false;
		}
		
		if(ValidateUtil.isEmptyData(codeBaseVO.getCodeGroupId())
				|| ValidateUtil.isEmptyData(codeBaseVO.getCodeId())
//				|| ValidateUtil.isEmptyData(codeBaseVO.getLangCd())
				|| ValidateUtil.isEmptyData(codeBaseVO.getCodeName())
				|| ValidateUtil.isEmptyData(codeBaseVO.getSortOrder())) {
			return false;
		}
		
		return true;
	}
}
