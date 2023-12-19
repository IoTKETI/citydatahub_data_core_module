package kr.re.keti.sc.ingestinterface.common.configuration.security.interceptor;

import kr.re.keti.sc.ingestinterface.common.code.Constants;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode;
import kr.re.keti.sc.ingestinterface.common.service.security.AASSVC;
import kr.re.keti.sc.ingestinterface.common.vo.security.AASUserDetailsVO;
import kr.re.keti.sc.ingestinterface.common.vo.security.AASUserPermissionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * 데이터셋 접근제어 인터셉터
 */
public class AclInterceptor extends HandlerInterceptorAdapter {


    @Value("${security.acl.useYn:N}")
    private String securityAclUseYn;
    @Value("#{'${security.headers.admin.value}'.split(',')}")
    private List<String> adminUserRoleList;

    @Autowired
    AASSVC aasSVC;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        //인증을 사용하지 않을 경우, SKIP
        if (securityAclUseYn.equals(IngestInterfaceCode.UseYn.NO)) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AASUserDetailsVO aasUserDetailsVO = (AASUserDetailsVO) authentication.getPrincipal();
        String role = aasUserDetailsVO.getRole();

        boolean isSuperUser = adminUserRoleList.contains(role);

        if(!isSuperUser) {
            aasSVC.validateUserRole(role);
        }

        AASUserPermissionVO permissionVO = AASUserPermissionVO.builder()
                .isSuperUser(isSuperUser)
                .resourceIds(aasUserDetailsVO.getResourceIds())
                .operationTypes(aasUserDetailsVO.getOperationTypes())
                .build();

        request.setAttribute(Constants.ACL_PERMISSION_KEY, permissionVO);

        return true;
    }


}
