package kr.re.keti.sc.ingestinterface.common.service.security;

import kr.re.keti.sc.ingestinterface.acl.rule.vo.AclRuleVO;
import kr.re.keti.sc.ingestinterface.common.code.Constants;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.AclRuleCondition;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.AclRuleOperationType;
import kr.re.keti.sc.ingestinterface.common.vo.security.AASUserDetailsVO;
import kr.re.keti.sc.ingestinterface.common.vo.security.AASUserPermissionVO;
import kr.re.keti.sc.ingestinterface.common.vo.security.AASUserVO;
import kr.re.keti.sc.ingestinterface.datamodel.DataModelManager;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.Operation;

/**
 * Authorization and Authentication service class
 */
@Service
public class AASSVC {

    @Autowired
    private DataModelManager dataModelManager;

    @Value("#{'${security.headers.admin.value}'.split(',')}")
    private List<String> adminUserRoleList;

    public void validateUserRole(String role) {
        if (role == null) {
            throw new AccessDeniedException("empty role");
        }
    }

    /**
     * 접근제어에 사용되는 resouceId 리스트 가져오기
     *
     * @param userId
     * @param clientId
     * @return
     */
    public AASUserDetailsVO createAASUserByAclRule(AASUserVO aasUserVO, IngestInterfaceCode.AclRuleResourceType aclRuleResourceType) {

        String userId = aasUserVO.getUserId();
        String clientId = aasUserVO.getClientId();

        List<String> datasetIds = new ArrayList<>();
        List<AclRuleOperationType> operationTypes = new ArrayList<>();

        for (AclRuleVO aclRuleVO : dataModelManager.getAclRuleCaches()) {

            if (aclRuleVO.getResourceType() != aclRuleResourceType){
                continue;
            }

            if(aclRuleVO.getCondition() == AclRuleCondition.AND) {
                if (userId.equals(aclRuleVO.getUserId()) && clientId.equals(aclRuleVO.getClientId())) {
                    datasetIds.add(aclRuleVO.getResourceId());
                    operationTypes.addAll(aclRuleVO.getOperation());
                }
            } else if(aclRuleVO.getCondition() == AclRuleCondition.OR) {
                if (clientId.equals(aclRuleVO.getClientId())) {
                    datasetIds.add(aclRuleVO.getResourceId());
                    operationTypes.addAll(aclRuleVO.getOperation());
                }
                if (userId.equals(aclRuleVO.getUserId()) ) {
                    datasetIds.add(aclRuleVO.getResourceId());
                    operationTypes.addAll(aclRuleVO.getOperation());
                }

                // default OR 로 처리
            } else {
                if (clientId.equals(aclRuleVO.getClientId())) {
                    datasetIds.add(aclRuleVO.getResourceId());
                    operationTypes.addAll(aclRuleVO.getOperation());
                }
                if (userId.equals(aclRuleVO.getUserId()) ) {
                    datasetIds.add(aclRuleVO.getResourceId());
                    operationTypes.addAll(aclRuleVO.getOperation());
                }
            }
        }

        // 중복 제거
        datasetIds = datasetIds.stream().distinct().collect(Collectors.toList());
        operationTypes = operationTypes.stream().distinct().collect(Collectors.toList());

        AASUserDetailsVO aasUserDetailsVO = new AASUserDetailsVO();
        BeanUtils.copyProperties(aasUserVO, aasUserDetailsVO);
        aasUserDetailsVO.setResourceIds(datasetIds);
        aasUserDetailsVO.setOperationTypes(operationTypes);
        aasUserDetailsVO.setResourceType(aclRuleResourceType);

        return aasUserDetailsVO;
    }



    /**
     * CUD operation 에 대한 접근제어
     * @param request
     * @param datasetId
     */
    public void checkCUDAccessRule(HttpServletRequest request, String datasetId, Operation operation) {

        AASUserPermissionVO aasUserPermissionVO = (AASUserPermissionVO)request.getAttribute(Constants.ACL_PERMISSION_KEY);

        // 인증기능을 사용하지 않는 경우
        if(aasUserPermissionVO == null) {
            return;
        }

        // create, update, delete 의 경우 데이터 셋이 없는 경우, skip
        if (datasetId == null) {
            return;
        }

        if(aasUserPermissionVO.isSuperUser()) {
            return;
        }

        if(aasUserPermissionVO.getOperationTypes() == null
                || !aasUserPermissionVO.getOperationTypes().contains(operationToAclRuleOperation(operation))) {
            throw new AccessDeniedException("Access denied. Not allow '" + operation.getCode() + "' operation");
        }

        List<String> aclDatasetList = aasUserPermissionVO.getResourceIds();
        if (aclDatasetList == null) {
            throw new AccessDeniedException("Access denied. No dataset access available");
        }
        for (String aclDataset : aclDatasetList) {
            if (datasetId.equalsIgnoreCase(aclDataset)) {
                return;
            }
        }
        throw new AccessDeniedException("Access denied. No dataset access available");
    }

    private AclRuleOperationType operationToAclRuleOperation(Operation operation) {
        switch(operation) {
            case CREATE_ENTITY:
                return AclRuleOperationType.CREATE;
            case UPDATE_ENTITY_ATTRIBUTES:
            case APPEND_ENTITY_ATTRIBUTES:
            case PARTIAL_ATTRIBUTE_UPDATE:
            case REPLACE_ENTITY_ATTRIBUTES:
            case CREATE_ENTITY_OR_APPEND_ENTITY_ATTRIBUTES:
            case CREATE_ENTITY_OR_REPLACE_ENTITY_ATTRIBUTES:
            case DELETE_ENTITY_ATTRIBUTES:
                return AclRuleOperationType.UPDATE;
            case DELETE_ENTITY:
                return AclRuleOperationType.DELETE;
            case RETRIEVE:
                return AclRuleOperationType.RETRIEVE;
            default:
                return null;
        }
    }
}
