package kr.re.keti.sc.datamanager.common.configuration.security;

import kr.re.keti.sc.datamanager.acl.rule.service.AclRuleSVC;
import kr.re.keti.sc.datamanager.acl.rule.vo.AclRuleVO;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode;
import kr.re.keti.sc.datamanager.common.configuration.CachedBodyHttpServletRequest;
import kr.re.keti.sc.datamanager.common.exception.ngsild.NgsiLdResourceNotFoundException;
import kr.re.keti.sc.datamanager.common.vo.AASUserDetailsVO;
import kr.re.keti.sc.datamanager.common.vo.AASUserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {


    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;
    @Autowired
    private AclRuleSVC aclRuleSVC;

    @Value("${security.acl.useYn:N}")
    private String securityAclUseYn;
    @Value("${security.publicKey}")
    private String publicKey;
    @Value("${security.headers.admin.key:x-user-role}")
    private String adminUserRoleKey;
    @Value("#{'${security.headers.admin.value}'.split(',')}")
    private List<String> adminUserRoleList;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        //0. ?????? ?????? ?????? ?????? ??????, ?????? ??????
        if (securityAclUseYn.equals(DataManagerCode.UseYn.NO.getCode())) {
            chain.doFilter(request, response);
            return;
        }

        // request ????????? ??? ????????? ?????? ?????? ?????? filter?????? ????????? ?????? ???, datasetId??? ???????????? ?????? ??????
        CachedBodyHttpServletRequest cachedBodyHttpServletRequest = new CachedBodyHttpServletRequest(request);

        //1. ???????????????(????????????)??? ?????? ?????? ??????
        if (cachedBodyHttpServletRequest.getHeader(adminUserRoleKey) != null) {
            String adminRole = cachedBodyHttpServletRequest.getHeader(adminUserRoleKey);
            if (adminUserRoleList.contains(adminRole)) {
                //????????? ???????????? ??????
                grantAdminAuthority(cachedBodyHttpServletRequest, response, chain, DataManagerCode.AclRuleResourceType.DATASET);
                return;
            }
        }

        final String header = cachedBodyHttpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);

        //2. ?????? ?????? ?????? ??????
        /*
            CUD??? ?????? dataset??? ?????? ???????????? ???????????? ??????
            R??? ?????? token?????? ???????????? dataset????????? ?????? (???????????? ?????? ??????)
            - /datamodels, /subscriptions, /csourceSubscriptions, csourceRegistrations, /datasetset/{datasetId}/flow, /provision/servers??? ???????????? ?????? X
            - /entityOperations, /entities??? CUD??? ?????? dataset??? ?????? ???????????? ???????????? ??????
            - /entities, /temporal/entities R??? ?????? token?????? ???????????? dataset????????? ?????? (???????????? ?????? ??????)
         */
        if (header == null || !header.startsWith("Bearer ")) {
            //
            String method = cachedBodyHttpServletRequest.getMethod();
            if (method.equalsIgnoreCase("GET")) {
                // ????????? ???????????? ?????? ?????? ??????, permit url??? skip ???????????? ????????? ????????? url access denied ?????? ?????????
                chain.doFilter(cachedBodyHttpServletRequest, response);
            } else {
                //????????? ???????????? ??????
                grantAdminAuthority(cachedBodyHttpServletRequest, response, chain, DataManagerCode.AclRuleResourceType.DATASET);
            }
            return;
        }


        // Get jwt token and validate
        final String token = header.split(" ")[1].trim();
        if (!kr.re.keti.sc.datamanager.common.configuration.security.JwtTokenUtil.validate(publicKey, token)) {
            resolver.resolveException(cachedBodyHttpServletRequest, response, null, new AccessDeniedException("invalid JWT Token"));
            return;
        }

        try {
            AASUserVO aasUserVO = AASTokenUtil.getAllClaimsFromToken(publicKey, token);
            String role = aasUserVO.getRole();
            //???????????? ?????? ????????? role ????????? "XXX" ?????? "XXXX"?????? ???????????? Ignore
            if (adminUserRoleList.contains(role)) {
                grantAdminAuthority(cachedBodyHttpServletRequest, response, chain, DataManagerCode.AclRuleResourceType.DATASET);
            } else {
                SecurityContextHolder.getContext().setAuthentication(makeUsernamePasswordAuthenticationToken(aasUserVO, DataManagerCode.AclRuleResourceType.DATASET));
            }

        } catch (Exception e) {
            resolver.resolveException(cachedBodyHttpServletRequest, response, null, new AccessDeniedException("Unable to get JWT Token"));
            return;
        }


        chain.doFilter(cachedBodyHttpServletRequest, response);
    }


    private UsernamePasswordAuthenticationToken makeUsernamePasswordAuthenticationToken(AASUserVO aasUserVO, DataManagerCode.AclRuleResourceType aclRuleResourceType) {


        AASUserDetailsVO aasUserDetailsVO = new AASUserDetailsVO();
        BeanUtils.copyProperties(aasUserVO, aasUserDetailsVO);

        AclRuleVO reqAclRuleVO = new AclRuleVO();
        reqAclRuleVO.setUserId(aasUserDetailsVO.getUserId());
        reqAclRuleVO.setClientId(aasUserDetailsVO.getClientId());
        reqAclRuleVO.setResourceType(aclRuleResourceType);
        List<AclRuleVO> aclRuleVOS = aclRuleSVC.getAclRuleVOList(reqAclRuleVO);

        if (aclRuleVOS == null) {
            throw new NgsiLdResourceNotFoundException(DataManagerCode.ErrorCode.NOT_EXIST_ID, "no dataset access available");
        }

        // ingest-interface, data-service-broker??? ????????? ?????????.
        List<String> resourceIds = null;
        for (AclRuleVO aclRuleVO : aclRuleVOS) {
            if (resourceIds == null) {
                resourceIds = new ArrayList<>();
            }
            if (aclRuleVO.getResourceId() != null) {
                resourceIds.add(aclRuleVO.getResourceId());
            }
        }

        aasUserDetailsVO.setUsername(aasUserVO.getUserId());
        if (resourceIds != null) {
            aasUserDetailsVO.setResourceIds(resourceIds);
        }

        UserDetails userDetails = aasUserDetailsVO;
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        return usernamePasswordAuthenticationToken;
    }

    /**
     * ????????? ?????? ??????
     */
    private void grantAdminAuthority(CachedBodyHttpServletRequest cachedBodyHttpServletRequest,
                                     HttpServletResponse response,
                                     FilterChain chain,
                                     DataManagerCode.AclRuleResourceType aclRuleResourceType) throws ServletException, IOException {

        AASUserVO aasUserVO = new AASUserVO();
        aasUserVO.setUserId(adminUserRoleList.get(0));
        aasUserVO.setRole(adminUserRoleList.get(0));
        aasUserVO.setClientId(adminUserRoleList.get(0));
        aasUserVO.setIssuer(adminUserRoleList.get(0));

        SecurityContextHolder.getContext().setAuthentication(makeUsernamePasswordAuthenticationToken(aasUserVO, aclRuleResourceType));
        chain.doFilter(cachedBodyHttpServletRequest, response);

    }


}
