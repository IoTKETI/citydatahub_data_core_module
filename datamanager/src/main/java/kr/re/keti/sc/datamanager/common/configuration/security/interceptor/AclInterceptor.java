package kr.re.keti.sc.datamanager.common.configuration.security.interceptor;

import kr.re.keti.sc.datamanager.common.code.Constants;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode;
import kr.re.keti.sc.datamanager.common.service.security.AASSVC;
import kr.re.keti.sc.datamanager.common.vo.AASUserDetailsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * Access control policy interceptor class
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

        // check acl skip
        if (securityAclUseYn.equals(DataManagerCode.UseYn.NO)) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AASUserDetailsVO aasUserDetailsVO = (AASUserDetailsVO) authentication.getPrincipal();
        String role = aasUserDetailsVO.getRole();
        if (adminUserRoleList.contains(role)) {
            request.setAttribute(Constants.ACL_ADMIN, true);

        } else {
            request.setAttribute(Constants.ACL_ADMIN, false);
            List<String> aclDatasetIds = aasSVC.getAclResourceIds(aasUserDetailsVO);
            request.setAttribute(Constants.ACL_DATASET_IDS, aclDatasetIds);
        }

        return true;
    }


}
