package kr.re.keti.sc.datacoreui.api.aclrule.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import kr.re.keti.sc.datacoreui.api.aclrule.vo.AclRuleListResponseVO;
import kr.re.keti.sc.datacoreui.api.aclrule.vo.AclRuleResponseVO;
import kr.re.keti.sc.datacoreui.api.aclrule.vo.AclRuleVO;
import kr.re.keti.sc.datacoreui.common.code.Constants;
import kr.re.keti.sc.datacoreui.common.service.DataCoreRestSVC;
import kr.re.keti.sc.datacoreui.security.service.DataCoreUiSVC;
import kr.re.keti.sc.datacoreui.util.ValidateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Manage the rules of the access control list.
 * @FileName AclRuleSVC.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 27.
 * @Author Elvin
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AclRuleSVC {
	
	@Value("${datamanager.url}")
	private String datamanagerUrl;
	
	@Autowired
	private DataCoreUiSVC dataCoreUiSVC;
	
	@Autowired
    private ObjectMapper objectMapper;
	
	private final static String DEFAULT_PATH_URL = "acl/rules";
	private final DataCoreRestSVC dataCoreRestSVC;
	
	/**
	 * Create rules of access control list
	 * @param aclRuleVO		AclRuleVO
	 * @return				Result of rules of access control list creation.
	 */
	public ResponseEntity<AclRuleResponseVO> createAclRules(HttpServletRequest request, AclRuleVO aclRuleVO) {
		// 1. Set the creator ID
		Object principal = dataCoreUiSVC.getPrincipal(request);
		if (principal != null) {
			aclRuleVO.setCreatorId(principal.toString());
		}
		
		// 2. Create ACL
		String aclRule = new Gson().toJson(aclRuleVO);
		ResponseEntity<String> response = dataCoreRestSVC.post(datamanagerUrl, DEFAULT_PATH_URL, null, aclRule, null, String.class);
		
		// 3. Responds to the ACL creation result
		AclRuleResponseVO aclRuleResponse = null;
		try {
			if(!ValidateUtil.isEmptyData(response.getBody())) {
				aclRuleResponse = objectMapper.readValue(response.getBody(), AclRuleResponseVO.class);
			}
		} catch (Exception e) {
			log.warn("AclRuleResponse parsing error.", e);
		}
		
		return ResponseEntity.ok(aclRuleResponse);
	}

	/**
	 * Update rules of access control list
	 * @param id			ACL rule ID
	 * @param aclRuleVO		AclRuleVO
	 * @return				Result of update rules of access control list.
	 */
	public <T> ResponseEntity<T> updateAclRules(HttpServletRequest request, String id, AclRuleVO aclRuleVO) {
		// 1. Set the Modifier ID
		Object principal = dataCoreUiSVC.getPrincipal(request);
		if (principal != null) {
			aclRuleVO.setModifierId(principal.toString());
		}
				
		String pathUri = DEFAULT_PATH_URL + "/" + id;
		String aclRule = new Gson().toJson(aclRuleVO);
		
		// 2. Update ACL
		ResponseEntity<T> response = (ResponseEntity<T>) dataCoreRestSVC.put(datamanagerUrl, pathUri, null, aclRule, null, Void.class);
		
		return response;
	}

	/**
	 * Delete rules of access control list
	 * @param id	ACL rule ID
	 * @return		Result of delete rules of access control list.
	 */
	public <T> ResponseEntity<T> deleteAclRules(HttpServletRequest request, String id) {
		String pathUri = DEFAULT_PATH_URL + "/" + id;
		
		// 1. Delete ACL
		ResponseEntity<T> response = (ResponseEntity<T>) dataCoreRestSVC.delete(datamanagerUrl, pathUri, null, null, null, Void.class);
		
		return response;
	}

	/**
	 * Retrieve multiple ACL rules
	 * @param aclRuleVO		AclRuleVO
	 * @return				List of ACL rules retrieved by AclRuleVO.
	 */
	public ResponseEntity<AclRuleListResponseVO> getAclRules(HttpServletRequest request, AclRuleVO aclRuleVO) {
		Map<String, String> headers = new HashMap<String, String>();
		Map<String, Object> params = new HashMap<String, Object>();
		headers.put("Accept", "application/json");
		
		aclRuleVOtoParams(aclRuleVO, params);
		
		ResponseEntity<String> response = dataCoreRestSVC.get(datamanagerUrl, DEFAULT_PATH_URL, headers, null, params, String.class);
		
		AclRuleListResponseVO aclRuleResponseList = new AclRuleListResponseVO();
		try {
			if(!ValidateUtil.isEmptyData(response.getBody())) {
				aclRuleResponseList.setAclRuleResponseVOs(objectMapper.readValue(response.getBody(), new TypeReference<List<AclRuleResponseVO>>() {}));
			} else {
				return ResponseEntity.noContent().build();
			}
			
			if(response.getHeaders() != null) {
				List<String> totalCountHeader = (response.getHeaders().get(Constants.TOTAL_COUNT));
				if (totalCountHeader != null && totalCountHeader.size() > 0) {
					aclRuleResponseList.setTotalCount(Integer.valueOf(totalCountHeader.get(0)));
				}
			}
		} catch (Exception e) {
			log.warn("AclRuleResponse parsing error.", e);
		}
		
		return ResponseEntity.ok(aclRuleResponseList);
	}

	/**
	 * Retrieve ACL rules by ID
	 * @param id	ACL rule ID
	 * @return		ACL rules retrieved by id.
	 */
	public ResponseEntity<AclRuleResponseVO> getAclRulesById(HttpServletRequest request, String id) {
		String pathUri = DEFAULT_PATH_URL + "/" + id;
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", "application/json");
		
		ResponseEntity<String> response = dataCoreRestSVC.get(datamanagerUrl, pathUri, headers, null, null, String.class);
		
		AclRuleResponseVO aclRuleResponse = null;
		try {
			if(!ValidateUtil.isEmptyData(response.getBody())) {
				aclRuleResponse = objectMapper.readValue(response.getBody(), AclRuleResponseVO.class);
			} else {
				return ResponseEntity.noContent().build();
			}
		} catch (Exception e) {
			log.warn("AclRuleResponse parsing error.", e);
		}
		
		return ResponseEntity.ok(aclRuleResponse);
	}
	
	/**
	 * AclRuleVO to Params
	 * @param aclRuleVO		AclRuleVO
	 * @param params		Http request params
	 */
	private void aclRuleVOtoParams(AclRuleVO aclRuleVO, Map<String, Object> params) {		
		if(!ValidateUtil.isEmptyData(aclRuleVO.getId())) {
			params.put("id", aclRuleVO.getId());
		}
		if(!ValidateUtil.isEmptyData(aclRuleVO.getUserId())) {
			params.put("userId", aclRuleVO.getUserId());
		}
		if(!ValidateUtil.isEmptyData(aclRuleVO.getClientId())) {
			params.put("clientId", aclRuleVO.getClientId());
		}
		if(!ValidateUtil.isEmptyData(aclRuleVO.getResourceId())) {
			params.put("resourceId", aclRuleVO.getResourceId());
		}
		if(!ValidateUtil.isEmptyData(aclRuleVO.getResourceType())) {
			params.put("resourceType", aclRuleVO.getResourceType());
		}
		if(!ValidateUtil.isEmptyData(aclRuleVO.getCondition())) {
			params.put("condition", aclRuleVO.getCondition());
		}
		if(!ValidateUtil.isEmptyData(aclRuleVO.getOperation())) {
			params.put("operation", aclRuleVO.getOperation());
		}
		if(!ValidateUtil.isEmptyData(aclRuleVO.getProvisioningRequestId())) {
			params.put("provisioningRequestId", aclRuleVO.getProvisioningRequestId());
		}
		if(!ValidateUtil.isEmptyData(aclRuleVO.getCreatorId())) {
			params.put("creatorId", aclRuleVO.getCreatorId());
		}
		if(!ValidateUtil.isEmptyData(aclRuleVO.getModifierId())) {
			params.put("modifierId", aclRuleVO.getModifierId());
		}
	}
}
