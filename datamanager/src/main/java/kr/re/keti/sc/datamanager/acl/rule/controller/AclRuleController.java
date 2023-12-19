package kr.re.keti.sc.datamanager.acl.rule.controller;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.datamanager.acl.rule.service.AclRuleSVC;
import kr.re.keti.sc.datamanager.acl.rule.vo.AclRuleVO;
import kr.re.keti.sc.datamanager.common.code.Constants;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode;
import kr.re.keti.sc.datamanager.common.configuration.security.AASTokenUtil;
import kr.re.keti.sc.datamanager.common.exception.BadRequestException;
import kr.re.keti.sc.datamanager.common.service.security.AASSVC;
import kr.re.keti.sc.datamanager.provisioning.vo.ProvisionNotiVO;
import lombok.extern.slf4j.Slf4j;

/**
 * Access Control Policy HTTP Controller Class
 */
@RestController
@Slf4j
public class AclRuleController {

    @Autowired
    private AclRuleSVC aclRuleSVC;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AASSVC aasSVC;

    @Value("${security.useYn:N}")
    private String securityUseYn;

    /**
     * Retrieve access control policy rule List
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param accept http accept header
     * @param requestAclRuleVO retrieve condition
     * @throws Exception
     */
    @GetMapping(value = "/acl/rules")
    public @ResponseBody
    void getAclRules(HttpServletRequest request,
                     HttpServletResponse response,
                     @RequestHeader(HttpHeaders.ACCEPT) String accept,
                     AclRuleVO requestAclRuleVO) throws Exception {

        // 1. check authorization
        if (securityUseYn.equals(DataManagerCode.UseYn.YES.getCode())) {
            if (!aasSVC.checkAdmin(request)) {
                throw new AccessDeniedException("access denied");
            }
        }

        // 2. get total count
        Integer totalCount = aclRuleSVC.getAclRuleTotalCount(requestAclRuleVO);

        // 3. get acl list
        List<AclRuleVO> aclRuleVOList = aclRuleSVC.getAclRuleVOList(requestAclRuleVO);

        // 4. set response
        response.addHeader(Constants.TOTAL_COUNT, Integer.toString(totalCount));
        response.getWriter().print(objectMapper.writeValueAsString(aclRuleVOList));

    }

    /**
     * Retrieve access control policy rule
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param accept http accept header
     * @param id retrieve id
     * @throws Exception
     */
    @GetMapping(value = "/acl/rules/{id}")
    public @ResponseBody
    void getAclRulesById(HttpServletRequest request,
                         HttpServletResponse response,
                         @RequestHeader(HttpHeaders.ACCEPT) String accept,
                         @PathVariable String id) throws Exception {

        // 1. check authorization
        if (securityUseYn.equals(DataManagerCode.UseYn.YES.getCode())) {
            if (!aasSVC.checkAdmin(request)) {
                throw new AccessDeniedException("access denied");
            }
        }

        // 2. get acl rule
        AclRuleVO aclRuleVO = aclRuleSVC.getAclRuleVOById(id);

        // 3. check result
        if (aclRuleVO == null) {
            throw new BadRequestException(DataManagerCode.ErrorCode.NOT_EXIST_ID, "Not Exists. id=" + id);
        }
        // 4. response
        response.getWriter().print(objectMapper.writeValueAsString(aclRuleVO));
    }


    /**
     * Create access control policy rule
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param requestBody create access control policy rule data
     * @throws Exception create error
     */
    @PostMapping(value = "/acl/rules")
    public void createAclRules(HttpServletRequest request,
                               HttpServletResponse response,
                               @RequestBody String requestBody) throws Exception {

        log.info("acl-rules CREATE request msg='{}'", requestBody);

        // 1. check authorization
        if (securityUseYn.equals(DataManagerCode.UseYn.YES.getCode())) {
            if (!aasSVC.checkAdmin(request)) {
                throw new AccessDeniedException("access denied");
            }
        }

        // 2. validate parameter
        AclRuleVO requestAclRuleVO = objectMapper.readValue(requestBody, AclRuleVO.class);
        requestAclRuleVO.setId(AASTokenUtil.createUuid());

        // 3. check authorization
        if (securityUseYn.equals(DataManagerCode.UseYn.YES.getCode())) {
            if (!aasSVC.checkAdmin(request)) {
                throw new AccessDeniedException("access denied");
            }
        }
        // 4. get acl rule list
        List<AclRuleVO> aclRuleVOS = aclRuleSVC.getAclRuleVOList(requestAclRuleVO);

        // 5. check already exists
        if (aclRuleVOS != null && aclRuleVOS.size() > 0) {
            throw new BadRequestException(DataManagerCode.ErrorCode.ALREADY_EXISTS,
                    "Already Exists. id=" + aclRuleVOS.get(0).getId() + ", userId=" + aclRuleVOS.get(0).getUserId() + ", clientId=" + aclRuleVOS.get(0).getClientId());
        }

        // 6. provisioning create event
        ProvisionNotiVO provisionNotiVO = aclRuleSVC.provisionAclRule(requestAclRuleVO, DataManagerCode.ProvisionEventType.CREATED, request.getRequestURI());

        // 7. create acl rule
        requestAclRuleVO.setProvisioningRequestId(provisionNotiVO.getRequestId());
        requestAclRuleVO.setProvisioningEventTime(provisionNotiVO.getEventTime());
        aclRuleSVC.createAclRule(requestAclRuleVO);

        // 8. set response
        response.setStatus(HttpStatus.CREATED.value());
        AclRuleVO responseVO = new AclRuleVO();
        responseVO.setId(requestAclRuleVO.getId());
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(Constants.CHARSET_ENCODING);
        response.getWriter().print(objectMapper.writeValueAsString(responseVO));
    }

    /**
     * Update access control policy rule
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param requestBody update access control policy rule data
     * @param id update acl rule id
     * @throws Exception update error
     */
    @PutMapping(value = "/acl/rules/{id}")
    public void updateAclRules(HttpServletRequest request,
                               HttpServletResponse response,
                               @RequestBody String requestBody,
                               @PathVariable String id) throws Exception {

        log.info("Update acl-rules. requestUri={}, requestBody={}", request.getRequestURI(), requestBody);

        // 1. check authorization
        if (securityUseYn.equals(DataManagerCode.UseYn.YES.getCode())) {
            if (!aasSVC.checkAdmin(request)) {
                throw new AccessDeniedException("access denied");
            }
        }

        // 2. check exists
        AclRuleVO retrieveAclRuleVO = aclRuleSVC.getAclRuleVOById(id);
        if (retrieveAclRuleVO == null) {
            throw new BadRequestException(DataManagerCode.ErrorCode.NOT_EXISTS_DATAMODEL,
                    "Not Exists. id=" + id);
        }
        AclRuleVO requestAclRuleVO = objectMapper.readValue(requestBody, AclRuleVO.class);
        requestAclRuleVO.setId(retrieveAclRuleVO.getId());
        // 3. check duplicate
        List<AclRuleVO> duplicatedAclRuleVOS = aclRuleSVC.getAclRuleVOList(requestAclRuleVO);
        if (duplicatedAclRuleVOS != null && duplicatedAclRuleVOS.size() > 0) {

            for(AclRuleVO duplicatedAclRuleVO : duplicatedAclRuleVOS) {
                if(duplicatedAclRuleVO.getId().equals(id)) {
                    continue;
                }
                throw new BadRequestException(DataManagerCode.ErrorCode.ALREADY_EXISTS,
                        "Exists. id=" + duplicatedAclRuleVO.getClientId()
                                + " , userId=" + duplicatedAclRuleVO.getUserId()
                                + " , clientId=" + duplicatedAclRuleVO.getClientId()
                                + " , resourceId=" + duplicatedAclRuleVO.getResourceId()
                );
            }
        }

        // 4.  provisioning update event
        ProvisionNotiVO provisionNotiVO = aclRuleSVC.provisionAclRule(requestAclRuleVO, DataManagerCode.ProvisionEventType.UPDATED, request.getRequestURI());

        // 5. update acl rule data
        requestAclRuleVO.setProvisioningRequestId(provisionNotiVO.getRequestId());
        requestAclRuleVO.setProvisioningEventTime(provisionNotiVO.getEventTime());
        aclRuleSVC.updateAclRule(requestAclRuleVO);

        response.setStatus(HttpStatus.NO_CONTENT.value());
    }


    /**
     *
     * Delete access control policy rule
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param id delete acl rule id
     * @throws Exception delete error
     */
    @DeleteMapping(value = "/acl/rules/{id}")
    public void deleteAclRules(HttpServletRequest request,
                               HttpServletResponse response,
                               @PathVariable String id) throws Exception {

        log.info("Delete acl-rules. requestUri={}", request.getRequestURI());

        // 1. check authorization
        if (securityUseYn.equals(DataManagerCode.UseYn.YES.getCode())) {
            if (!aasSVC.checkAdmin(request)) {
                throw new AccessDeniedException("access denied");
            }
        }
        // 2. check exists
        AclRuleVO retrieveAclRuleVO = aclRuleSVC.getAclRuleVOById(id);
        if (retrieveAclRuleVO == null) {
            throw new BadRequestException(DataManagerCode.ErrorCode.NOT_EXIST_ID,
                    "Not Exists. id=" + id);
        }

        // 3. provisioning delete event
        aclRuleSVC.provisionAclRule(retrieveAclRuleVO, DataManagerCode.ProvisionEventType.DELETED, request.getRequestURI());

        // 4. delete acl rule data
        aclRuleSVC.deleteAclRule(id);

        response.setStatus(HttpStatus.NO_CONTENT.value());
    }
}
