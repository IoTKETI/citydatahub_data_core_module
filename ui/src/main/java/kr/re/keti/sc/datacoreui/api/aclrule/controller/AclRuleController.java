package kr.re.keti.sc.datacoreui.api.aclrule.controller;

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
import org.springframework.web.bind.annotation.RestController;

import kr.re.keti.sc.datacoreui.api.aclrule.service.AclRuleSVC;
import kr.re.keti.sc.datacoreui.api.aclrule.vo.AclRuleListResponseVO;
import kr.re.keti.sc.datacoreui.api.aclrule.vo.AclRuleResponseVO;
import kr.re.keti.sc.datacoreui.api.aclrule.vo.AclRuleVO;
import kr.re.keti.sc.datacoreui.common.code.DataCoreUiCode.ErrorCode;
import kr.re.keti.sc.datacoreui.common.exception.BadRequestException;
import kr.re.keti.sc.datacoreui.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Manage the rules of the access control list.
 * @FileName AclRuleController.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 27.
 * @Author Elvin
 */
@Slf4j
@RestController
public class AclRuleController {

	@Autowired
	private AclRuleSVC aclRuleSVC;
	
	/**
	 * Create rules of access control list
	 * @param aclRuleVO		AclRuleVO
	 * @return				Result of rules of access control list creation.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@PostMapping(value="/acl/rules")
	public ResponseEntity<AclRuleResponseVO> createAclRules(HttpServletRequest request, HttpServletResponse response, @RequestBody AclRuleVO aclRuleVO) throws Exception {
		
		log.info("[UI API] createAclRules - userId: {}, clientId: {}, resourceId: {}, resourceType: {}, condition: {}",
				aclRuleVO.getUserId(), aclRuleVO.getClientId(), aclRuleVO.getResourceId(), aclRuleVO.getResourceType(), aclRuleVO.getCondition());
		
		// 1. Check required values
		if(!checkMandatoryAclRulesValues(aclRuleVO)) {
			throw new BadRequestException(ErrorCode.BAD_REQUEST, "Missing required value.");
		}
		
		// 2. Create rules of access control list
		ResponseEntity<AclRuleResponseVO> reslt = aclRuleSVC.createAclRules(request, aclRuleVO);
		
		return reslt;
	}
	
	/**
	 * Update rules of access control list
	 * @param id			ACL rule ID
	 * @param aclRuleVO		AclRuleVO
	 * @return				Result of update rules of access control list.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@PutMapping(value="/acl/rules/{id}")
	public <T> ResponseEntity<T> updateAclRules(HttpServletRequest request, HttpServletResponse response, 
			@PathVariable(value="id") String id, @RequestBody AclRuleVO aclRuleVO) throws Exception {
		
		log.info("[UI API] updateAclRules - id: {}", id);
		
		// 1. Update rules of access control list
		ResponseEntity<T> reslt = (ResponseEntity<T>) aclRuleSVC.updateAclRules(request, id, aclRuleVO);
		
		return reslt;
	}
	
	/**
	 * Delete rules of access control list
	 * @param id			ACL rule ID
	 * @return				Result of delete rules of access control list.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@DeleteMapping(value="/acl/rules/{id}")
	public <T> ResponseEntity<T> deleteAclRules(HttpServletRequest request, HttpServletResponse response,
			@PathVariable(value="id") String id) throws Exception {
		
		log.info("[UI API] deleteAclRules - id: {}", id);
		
		// 1. Delete rules of access control list
		ResponseEntity<T> reslt = (ResponseEntity<T>) aclRuleSVC.deleteAclRules(request, id);
		
		return reslt;
	}
	
	/**
	 * Retrieve multiple ACL rules
	 * @param aclRuleVO		AclRuleVO
	 * @return				List of ACL rules retrieved by AclRuleVO.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@GetMapping(value="/acl/rules")
	public ResponseEntity<AclRuleListResponseVO> getAclRules(HttpServletRequest request, HttpServletResponse response,
			AclRuleVO aclRuleVO) throws Exception {
		
		log.info("[UI API] getAclRules.");
		
		// 1. Retrieve List of ACL rules
		ResponseEntity<AclRuleListResponseVO> aclRuleResponseVO = aclRuleSVC.getAclRules(request, aclRuleVO);
		
		return aclRuleResponseVO;
	}
	
	/**
	 * Retrieve ACL rules by ID
	 * @param id			ACL rule ID
	 * @return				ACL rules retrieved by id.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@GetMapping(value="/acl/rules/{id}")
	public ResponseEntity<AclRuleResponseVO> getAclRulesById(HttpServletRequest request, HttpServletResponse response,
			@PathVariable(value="id") String id) throws Exception {
		
		log.info("[UI API] getAclRulesById - id: {}", id);
		
		// 1. Retrieve ACL rules
		ResponseEntity<AclRuleResponseVO> dataModelVO = aclRuleSVC.getAclRulesById(request, id);
		
		return dataModelVO;
	}
	
	/**
	 * Check the required values ​​for AclRules
	 * @param aclRuleVO		AclRuleVO
	 * @return				Valid: true, Invalid: false
	 */
	private boolean checkMandatoryAclRulesValues(AclRuleVO aclRuleVO) {
		if(aclRuleVO == null) {
			return false;
		}
		
		if(aclRuleVO.getUserId() != null && aclRuleVO.getClientId() != null) {
			if(ValidateUtil.isEmptyData(aclRuleVO.getCondition())) {
				return false;
			}
		}
		
		return true;
	}
}
