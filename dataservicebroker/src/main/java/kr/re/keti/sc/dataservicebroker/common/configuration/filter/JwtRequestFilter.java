package kr.re.keti.sc.dataservicebroker.common.configuration.filter;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.re.keti.sc.dataservicebroker.common.service.security.AASSVC;
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

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.configuration.CachedBodyHttpServletRequest;
import kr.re.keti.sc.dataservicebroker.common.configuration.security.AASTokenUtil;
import kr.re.keti.sc.dataservicebroker.common.configuration.security.JwtTokenUtil;
import kr.re.keti.sc.dataservicebroker.common.vo.AASUserDetailsVO;
import kr.re.keti.sc.dataservicebroker.common.vo.AASUserVO;
import kr.re.keti.sc.dataservicebroker.datamodel.DataModelManager;
import lombok.extern.slf4j.Slf4j;


@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;
    @Autowired
    private AASSVC aASSVC;

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
        if (securityAclUseYn.equals(DataServiceBrokerCode.UseYn.NO.getCode())) {
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
                grantAdminAuthority(cachedBodyHttpServletRequest, response, chain, DataServiceBrokerCode.AclRuleResourceType.DATASET);
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
        if (header == null
                || (!header.startsWith("Bearer ") && !header.startsWith("bearer "))) {
            chain.doFilter(cachedBodyHttpServletRequest, response);
            return;
        }

        // Get jwt token and validate
        final String token = header.split(" ")[1].trim();
        if (!JwtTokenUtil.validate(publicKey, token)) {
            resolver.resolveException(cachedBodyHttpServletRequest, response, null, new AccessDeniedException("invalid JWT Token"));

            return;
        }

        try {
            AASUserVO aasUserVO = AASTokenUtil.getAllClaimsFromToken(publicKey, token);
            String role = aasUserVO.getRole();
            //???????????? ?????? ????????? role ????????? "XXX" ?????? "XXXX"?????? ???????????? Ignore
            if (adminUserRoleList.contains(role)) {
                grantAdminAuthority(cachedBodyHttpServletRequest, response, chain, DataServiceBrokerCode.AclRuleResourceType.DATASET);
            } else {
                SecurityContextHolder.getContext().setAuthentication(makeUsernamePasswordAuthenticationToken(aasUserVO, DataServiceBrokerCode.AclRuleResourceType.DATASET));
            }

        } catch (Exception e) {
            resolver.resolveException(cachedBodyHttpServletRequest, response, null, new AccessDeniedException("Unable to get JWT Token"));
            return;
        }


        chain.doFilter(cachedBodyHttpServletRequest, response);
    }


    private UsernamePasswordAuthenticationToken makeUsernamePasswordAuthenticationToken(AASUserVO aasUserVO, DataServiceBrokerCode.AclRuleResourceType aclRuleResourceType) {

        AASUserDetailsVO aasUserDetailsVO = aASSVC.createAASUserByAclRule(
                aasUserVO,
                aclRuleResourceType
        );

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
                                     DataServiceBrokerCode.AclRuleResourceType aclRuleResourceType) throws ServletException, IOException {

        AASUserVO aasUserVO = new AASUserVO();
        aasUserVO.setUserId(adminUserRoleList.get(0));
        aasUserVO.setRole(adminUserRoleList.get(0));
        aasUserVO.setClientId(adminUserRoleList.get(0));
        aasUserVO.setIssuer(adminUserRoleList.get(0));

        SecurityContextHolder.getContext().setAuthentication(makeUsernamePasswordAuthenticationToken(aasUserVO, aclRuleResourceType));
        chain.doFilter(cachedBodyHttpServletRequest, response);

    }


}
