
package kr.re.keti.sc.datamanager.common.service.security;

import kr.re.keti.sc.datamanager.common.code.Constants;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode;
import kr.re.keti.sc.datamanager.common.exception.ngsild.NgsiLdResourceNotFoundException;
import kr.re.keti.sc.datamanager.common.vo.AASUserDetailsVO;
import kr.re.keti.sc.datamanager.dataset.vo.DatasetBaseVO;
import kr.re.keti.sc.datamanager.util.ValidateUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Authorization and Authentication service class
 */
@Service
public class AASSVC {

    @Value("#{'${security.headers.admin.value}'.split(',')}")
    private List<String> adminUserRoleList;

    public List<String> getAclResourceIds(AASUserDetailsVO aasUserDetailsVO) {
        List<String> resourceIds = aasUserDetailsVO.getResourceIds();
        String role = aasUserDetailsVO.getRole();
        if (role == null) {
            throw new AccessDeniedException("empty role");
        }
        if (adminUserRoleList.contains(role)) {
            return null;
        }
        if (resourceIds == null || resourceIds.size() == 0) {
            return null;
//            throw new AccessDeniedException("empty access dataset");
        }
        return resourceIds;
    }


    /**
     * 조회 가능한 데이터셋 id 추가
     * @param request HttpServletRequest
     * @param datasetBaseVO DatasetBaseVO
     */
    public void addAclDatasetIds(HttpServletRequest request, DatasetBaseVO datasetBaseVO) {

        Object isAdmin = request.getAttribute(Constants.ACL_ADMIN);
        if (isAdmin != null && (Boolean) isAdmin) {
            datasetBaseVO.setIsAdminUser(true);
            return;
        } else {
            datasetBaseVO.setIsAdminUser(false);
        }

        List<String> aclDatasetIds = (List<String>) request.getAttribute(Constants.ACL_DATASET_IDS);
        if (ValidateUtil.isEmptyData(aclDatasetIds)) {
            throw new NgsiLdResourceNotFoundException(DataManagerCode.ErrorCode.NOT_EXIST_ID, "no dataset access available");
        }
        datasetBaseVO.setAclDatasetIds(aclDatasetIds);
    }

    /**
     * 데이터셋 권한 체크
     * @param request HttpServletRequest
     * @param datasetId datasetId
     */
    public void checkPermission(HttpServletRequest request, String datasetId) {

        if(checkAdmin(request)){
            return;
        }

        List<String> aclDatasetIds = (List<String>) request.getAttribute(Constants.ACL_DATASET_IDS);
        if (!ValidateUtil.isEmptyData(aclDatasetIds)) {
            for (String aclDatasetId : aclDatasetIds) {

                if (aclDatasetId.equalsIgnoreCase(datasetId)) {
                    return;
                }
            }
            throw new NgsiLdResourceNotFoundException(DataManagerCode.ErrorCode.NOT_EXIST_ID, "no dataset access available");
        }

    }


    /**
     * 관리자 여부 체크
     * @param request HttpServletRequest
     * @return
     */
    public boolean checkAdmin(HttpServletRequest request) {

        Object isAdmin = request.getAttribute(Constants.ACL_ADMIN);
        if (isAdmin != null && (Boolean) isAdmin) {
            return true;
        }else {

            return false;
        }
    }

    /**
     * CUD operation 에 대한 접근제어
     * @param request HttpServletRequest
     * @param datasetId datasetId
     */
    public void checkCUDAccessRule(HttpServletRequest request, String datasetId) {

        Object aclAdmin = request.getAttribute(Constants.ACL_ADMIN);
        if (aclAdmin == null) {
            //admin 권한일 경우 skip
            return;
        }

        if (datasetId == null) {
            // 데이터 셋이 없는 경우, skip
            return;
        }

        boolean isAdmin = (boolean) aclAdmin;
        if (!isAdmin) {
            List<String> aclDatasetList = (List<String>) request.getAttribute(Constants.ACL_DATASET_IDS);
            if (aclDatasetList == null) {
                throw new NgsiLdResourceNotFoundException(DataManagerCode.ErrorCode.NOT_EXIST_ID, "no dataset access available");
            }
            for (String aclDataset : aclDatasetList) {
                if (datasetId.equalsIgnoreCase(aclDataset)) {
                    return;
                }
            }
            throw new NgsiLdResourceNotFoundException(DataManagerCode.ErrorCode.NOT_EXIST_ID, "no dataset access available");
        }
    }


//
//    public void addUserInfo(HttpServletRequest request, AclRuleVO requestAclRuleVO) {
//
//        AbstractAuthenticationToken token = (AbstractAuthenticationToken) request.getUserPrincipal();
//        AASUserDetailsVO aasUserDetailsVO = (AASUserDetailsVO) token.getPrincipal();
//
//        if (aasUserDetailsVO.getRole().equals(superUserRoleValue)) {
//            return;
//        }
//
//        if (aasUserDetailsVO.getDatasetIds() == null || aasUserDetailsVO.getDatasetIds().size() == 0) {
//            throw new AccessDeniedException("can't access dataset");
//
//        }
//        requestAclRuleVO.setUserId(aasUserDetailsVO.getUserId());
//        requestAclRuleVO.setClientId(aasUserDetailsVO.getClientId());
//    }
}
