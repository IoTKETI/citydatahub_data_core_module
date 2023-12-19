package kr.re.keti.sc.datacoreui.api.code.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import kr.re.keti.sc.datacoreui.api.code.dao.CodeDAO;
import kr.re.keti.sc.datacoreui.api.code.vo.CodeBaseVO;
import kr.re.keti.sc.datacoreui.api.code.vo.CodeGroupBaseVO;
import kr.re.keti.sc.datacoreui.api.code.vo.CodeGroupRequestVO;
import kr.re.keti.sc.datacoreui.api.code.vo.CodeGroupResponseVO;
import kr.re.keti.sc.datacoreui.api.code.vo.CodeGroupVO;
import kr.re.keti.sc.datacoreui.api.code.vo.CodeRequestVO;
import kr.re.keti.sc.datacoreui.api.code.vo.CodeResponseVO;
import kr.re.keti.sc.datacoreui.api.code.vo.CodeVO;
import lombok.extern.slf4j.Slf4j;

/**
 * UI code management service class.
 * @FileName CodeSVC.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 22.
 * @Author Elvin
 */
@Slf4j
@Service
public class CodeSVC {
	
	@Autowired
	private CodeDAO codeDAO;

	/**
	 * Create code group
	 * @param codeGroupBaseVO	CodeGroupBaseVO
	 * @return					Result of code group creation.
	 */
	public <T> ResponseEntity<T> createCodeGroup(CodeGroupBaseVO codeGroupBaseVO) {
		if(codeGroupBaseVO.getEnabled() == null) {
			codeGroupBaseVO.setEnabled(true);
		}
		
		try {
			codeDAO.createCodeGroup(codeGroupBaseVO);
		} catch(DuplicateKeyException e) {
			log.warn("Duplicate CodeGroup key. codeGroupId: {}", codeGroupBaseVO.getCodeGroupId(), e);
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		} catch(Exception e) {
			log.error("Fail to createCodeGroup.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
				
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
	
	/**
	 * Update code group
	 * @param 	codeGroupId			Code group ID
	 * @param 	codeGroupBaseVO		CodeGroupBaseVO
	 * @return						Result of update code group.
	 */
	public <T> ResponseEntity<T> updateCodeGroup(String codeGroupId, CodeGroupBaseVO codeGroupBaseVO) {
		codeGroupBaseVO.setCodeGroupId(codeGroupId);

		try {
			codeDAO.updateCodeGroup(codeGroupBaseVO);
		} catch(Exception e) {
			log.error("Fail to updateCodeGroup.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
	
	/**
	 * Delete code group
	 * @param 	codeGroupId		Code group ID
	 * @return					Result of delete code group.
	 */
	public <T> ResponseEntity<T> deleteCodeGroup(String codeGroupId) {
		try {
			codeDAO.deleteCodeGroup(codeGroupId);
		} catch(Exception e) {
			log.error("Fail to deleteCodeGroup.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
	
	/**
	 * Retrieve code group
	 * @param 	codeGroupId
	 * @return	Code group information
	 */
	public ResponseEntity<CodeGroupBaseVO> getCodeGroup(String codeGroupId) {
		CodeGroupBaseVO result = null;
		
		try {
			result = codeDAO.getCodeGroup(codeGroupId);
		} catch(Exception e) {
			log.error("Fail to getCodeGroup.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		if(result == null) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}
		
		return ResponseEntity.ok().body(result);
	}
	
	/**
	 * Retrieve multiple code group
	 * @param 	codeGroupRequestVO		CodeGroupRequestVO
	 * @return							List of code group information
	 */
	public ResponseEntity<CodeGroupResponseVO> getCodeGroups(CodeGroupRequestVO codeGroupRequestVO) {
		CodeGroupResponseVO codeGroupResponseVO = new CodeGroupResponseVO();
		List<CodeGroupVO> result = null;
		
		try {
			result = codeDAO.getCodeGroups(codeGroupRequestVO);
		} catch(Exception e) {
			log.error("Fail to getCodeGroups.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		if(result == null || result.size() < 1) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}
		
		codeGroupResponseVO.setTotalCount(result.get(0).getTotalCnt());
		codeGroupResponseVO.setCodeGroupVOs(result);
		
		return ResponseEntity.ok().body(codeGroupResponseVO);
	}

	/**
	 * Create code data
	 * @param	codeBaseVO	CodeBaseVO
	 * @return				Result of code creation.
	 */
	public <T> ResponseEntity<T> createCode(CodeBaseVO codeBaseVO) {
		if(codeBaseVO.getEnabled() == null) {
			codeBaseVO.setEnabled(true);
		}
		
		try {
			codeDAO.createCode(codeBaseVO);
		} catch(DuplicateKeyException e) {
			log.warn("Duplicate Code key. codeGroupId: {}, codeId: {}, langCd: {}", codeBaseVO.getCodeGroupId(), codeBaseVO.getCodeId(), codeBaseVO.getLangCd(), e);
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		} catch(DataIntegrityViolationException e) {
			log.warn("CodeGroup is not exist. codeGroupId: {}, codeId: {}, langCd: {}", codeBaseVO.getCodeGroupId(), codeBaseVO.getCodeId(), codeBaseVO.getLangCd(), e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		} catch(Exception e) {
			log.error("Fail to createCode.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
				
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	/**
	 * Update code data
	 * @param codeGroupId	Code group ID
	 * @param codeId		Code ID
	 * @param codeBaseVO	CodeBaseVO
	 * @return				Result of update code.
	 */
	public <T> ResponseEntity<T> updateCode(String codeGroupId, String codeId, String langCd, CodeBaseVO codeBaseVO) {
		codeBaseVO.setCodeGroupId(codeGroupId);
		codeBaseVO.setCodeId(codeId);
		codeBaseVO.setLangCd(langCd);

		try {
			codeDAO.updateCode(codeBaseVO);
		} catch(Exception e) {
			log.error("Fail to updateCode.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	/**
	 * Delete code data
	 * @param codeGroupId	Code group ID
	 * @param codeId		Code ID
	 * @return				Result of delete code.
	 */
	public <T> ResponseEntity<T> deleteCode(String codeGroupId, String codeId, String langCd) {
		CodeBaseVO codeBaseVO = new CodeBaseVO();
		codeBaseVO.setCodeGroupId(codeGroupId);
		codeBaseVO.setCodeId(codeId);
		codeBaseVO.setLangCd(langCd);
		
		try {
			codeDAO.deleteCode(codeBaseVO);
		} catch(Exception e) {
			log.error("Fail to deleteCode.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	/**
	 * Retrieve code data
	 * @param codeGroupId	Code group ID
	 * @param codeId		Code ID
	 * @return				Code information 
	 */
	public ResponseEntity<CodeBaseVO> getCode(String codeGroupId, String codeId, String langCd) {
		CodeBaseVO result = null;
		CodeBaseVO codeBaseVO = new CodeBaseVO();
		codeBaseVO.setCodeGroupId(codeGroupId);
		codeBaseVO.setCodeId(codeId);
		codeBaseVO.setLangCd(langCd);
		
		try {
			result = codeDAO.getCode(codeBaseVO);
		} catch(Exception e) {
			log.error("Fail to getCode.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		if(result == null) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}
		
		return ResponseEntity.ok().body(result);
	}

	/**
	 * Retrieve multiple code data
	 * @param codeRequestVO	CodeRequestVO
	 * @return				List of code information
	 */
	public ResponseEntity<CodeResponseVO> getCodes(CodeRequestVO codeRequestVO) {
		CodeResponseVO codeResponseVO = new CodeResponseVO();
		List<CodeVO> result = null;
		
		try {
			if(codeRequestVO.getCodeGroupId() != null && codeRequestVO.getSearchValue() != null) {
				// When searching codeGroupId and searchValue under simultaneous conditions, the codeGroupId is searched first.
				codeRequestVO.setSearchValue(null);
			}
			result = codeDAO.getCodes(codeRequestVO);
		} catch(Exception e) {
			log.error("Fail to getCodes.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		if(result == null || result.size() < 1) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}
		
		codeResponseVO.setTotalCount(result.get(0).getTotalCnt());
		codeResponseVO.setCodeVOs(result);
		
		return ResponseEntity.ok().body(codeResponseVO);
	}
}
