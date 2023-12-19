package kr.re.keti.sc.ingestinterface.common.configuration.security.filter;

import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode;
import kr.re.keti.sc.ingestinterface.common.configuration.CachedBodyHttpServletRequest;
import kr.re.keti.sc.ingestinterface.common.configuration.security.AASTokenUtil;
import kr.re.keti.sc.ingestinterface.common.configuration.security.JwtTokenUtil;
import kr.re.keti.sc.ingestinterface.common.service.security.AASSVC;
import kr.re.keti.sc.ingestinterface.common.vo.security.AASUserDetailsVO;
import kr.re.keti.sc.ingestinterface.common.vo.security.AASUserVO;
import kr.re.keti.sc.ingestinterface.datamodel.DataModelManager;
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
import java.util.List;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;
    @Autowired
    private AASSVC aASSVC;

    @Value("${security.external.platform.useYn:N}")
    private String securityExternalPlatformUseYn;
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

        //0. 인증 기능 쓰지 않을 경우, 리턴 처리
        if (securityExternalPlatformUseYn.equals(IngestInterfaceCode.UseYn.NO.getCode())
                && securityAclUseYn.equals(IngestInterfaceCode.UseYn.NO.getCode())) {
            chain.doFilter(request, response);
            return;
        }

        // request 객체를 한 번이상 읽기 위해 처리 filter에서 데이터 파싱 후, datasetId를 추출하기 위해 필요
        CachedBodyHttpServletRequest cachedBodyHttpServletRequest = new CachedBodyHttpServletRequest(request);

        //1. 커스텀헤더(슈퍼유저)가 있는 경우 처리
        if (cachedBodyHttpServletRequest.getHeader(adminUserRoleKey) != null) {
            String adminRole = cachedBodyHttpServletRequest.getHeader(adminUserRoleKey);
            if (adminUserRoleList.contains(adminRole)) {
                //어드민 권한으로 처리
                grantAdminAuthority(cachedBodyHttpServletRequest, response, chain, IngestInterfaceCode.AclRuleResourceType.DATASET);
                return;
            }
        }

        final String header = cachedBodyHttpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);

        //2. 인증 토큰 없은 경우
        /*
            CUD의 경우 dataset이 있는 경우에만 접근제어 적용
            R의 경우 token으로 허용되는 dataset값들만 조회 (접근제어 적용 항상)
            - /datamodels, /subscriptions, /csourceSubscriptions, csourceRegistrations, /datasetset/{datasetId}/flow, /provision/servers는 이번에는 적용 X
            - /entityOperations, /entities에 CUD의 경우 dataset이 있는 경우에만 접근제어 적용
            - /entities, /temporal/entities R의 경우 token으로 허용되는 dataset값들만 조회 (접근제어 적용 항상)
         */
        if (header == null
                || (!header.startsWith("Bearer ") && !header.startsWith("bearer "))) {

            // 스프링 시큐리티 룰에 따라 처리, permit url은 skip 처리되고 권한이 필요한 url access denied 예외 발생됨
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
            //로그인의 경우 토큰의 role 필드가 "XXX" 또는 "XXXX"이면 접근제어 Ignore
            if (adminUserRoleList.contains(role)) {
                grantAdminAuthority(cachedBodyHttpServletRequest, response, chain, IngestInterfaceCode.AclRuleResourceType.DATASET);
            } else {
                SecurityContextHolder.getContext().setAuthentication(makeUsernamePasswordAuthenticationToken(aasUserVO, IngestInterfaceCode.AclRuleResourceType.DATASET));
            }

        } catch (Exception e) {
            resolver.resolveException(cachedBodyHttpServletRequest, response, null, new AccessDeniedException("Unable to get JWT Token"));
            return;
        }


        chain.doFilter(cachedBodyHttpServletRequest, response);
    }


    private UsernamePasswordAuthenticationToken makeUsernamePasswordAuthenticationToken(AASUserVO aasUserVO, IngestInterfaceCode.AclRuleResourceType aclRuleResourceType) {

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
     * 어드민 권한 부여
     */
    private void grantAdminAuthority(CachedBodyHttpServletRequest cachedBodyHttpServletRequest,
                                     HttpServletResponse response,
                                     FilterChain chain,
                                     IngestInterfaceCode.AclRuleResourceType aclRuleResourceType) throws ServletException, IOException {

        AASUserVO aasUserVO = new AASUserVO();
        aasUserVO.setUserId(adminUserRoleList.get(0));
        aasUserVO.setRole(adminUserRoleList.get(0));
        aasUserVO.setClientId(adminUserRoleList.get(0));
        aasUserVO.setIssuer(adminUserRoleList.get(0));

        SecurityContextHolder.getContext().setAuthentication(makeUsernamePasswordAuthenticationToken(aasUserVO, aclRuleResourceType));
        chain.doFilter(cachedBodyHttpServletRequest, response);

    }


}
