package kr.re.keti.sc.datamanager.acl.rule.service;


import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.datamanager.acl.rule.dao.AclRuleDAO;
import kr.re.keti.sc.datamanager.acl.rule.vo.AclRuleVO;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode;
import kr.re.keti.sc.datamanager.common.exception.ProvisionException;
import kr.re.keti.sc.datamanager.provisioning.service.ProvisioningSVC;
import kr.re.keti.sc.datamanager.provisioning.vo.ProvisionNotiVO;
import kr.re.keti.sc.datamanager.provisioning.vo.ProvisionResultVO;
import kr.re.keti.sc.datamanager.provisionserver.service.ProvisionServerSVC;
import kr.re.keti.sc.datamanager.provisionserver.vo.ProvisionServerBaseVO;
import lombok.extern.slf4j.Slf4j;

/**
 * Access control policy rule service class
 */
@Service
@Slf4j
public class AclRuleSVC {

    @Autowired
    AclRuleDAO aclRuleDAO;
    @Autowired
    private ProvisionServerSVC provisionServerSVC;
    @Autowired
    private ProvisioningSVC provisioningSVC;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * create access control policy rule
     * @param aclRuleVO acl rule data
     * @return create result (1=success)
     */
    public int createAclRule(AclRuleVO aclRuleVO) {
        return aclRuleDAO.createAclRule(aclRuleVO);
    }

    /**
     * Update control policy rule
     * @param aclRuleVO acl rule data
     * @return update result (1=success, 0=fail)
     */
    public int updateAclRule(AclRuleVO aclRuleVO) {
        return aclRuleDAO.updateAclRule(aclRuleVO);
    }

    /**
     * Delete control policy rule
     * @param id acl rule id
     * @return delete result (1=success, 0=fail)
     */
    public int deleteAclRule(String id) {
        return aclRuleDAO.deleteAclRule(id);
    }

    /**
     * Retrieve acl rule count
     * @param aclRuleVO retrieve condition
     * @return retrieve acl rule count
     */
    public Integer getAclRuleTotalCount(AclRuleVO aclRuleVO) {
        Integer count = aclRuleDAO.getAclRuleTotalCount(aclRuleVO);
        return count;
    }

    /**
     * Retrieve acl rule data
     * @param aclRuleVO retrieve condition
     * @return retrieve acl rule list
     */
    public List<AclRuleVO> getAclRuleVOList(AclRuleVO aclRuleVO) {
        List<AclRuleVO> aclRuleVOS = aclRuleDAO.getAclRuleVOList(aclRuleVO);
        return aclRuleVOS;
    }

    /**
     * Retrieve acl rule data
     * @param id retrieve id
     * @return retrieve acl rule data
     */
    public AclRuleVO getAclRuleVOById(String id) {
        AclRuleVO resAclRuleVO = aclRuleDAO.getAclRuleVOById(id);
        return resAclRuleVO;
    }


    /**
     * Provisioning event (created, updated, deleted)
     * @param aclRuleVO acl rule data
     * @param provisionEventType provisioning event type
     * @param requestUri provisioning endpoint
     * @return ProvisionNotiVO provisioning data
     */
    public ProvisionNotiVO provisionAclRule(AclRuleVO aclRuleVO, DataManagerCode.ProvisionEventType provisionEventType, String requestUri) {

        // 1. create provisioning data
        String provisioningData = null;
        if (aclRuleVO != null) {
            try {
                provisioningData = objectMapper.writeValueAsString(aclRuleVO);
            } catch (JsonProcessingException e) {
                throw new ProvisionException(DataManagerCode.ErrorCode.PROVISIONING_ERROR, HttpStatus.BAD_REQUEST.value(), "Request message parsing error.", e);
            }
        }
        ProvisionNotiVO provisionNotiVO = new ProvisionNotiVO();
        provisionNotiVO.setRequestId(UUID.randomUUID().toString());
        provisionNotiVO.setEventTime(new Date());
        provisionNotiVO.setEventType(provisionEventType);
        provisionNotiVO.setTo(requestUri);
        provisionNotiVO.setData(provisioningData);

        // 2. provisioning
        // 2-1. dataServiceBroker
        provisionAclRule(provisionNotiVO, DataManagerCode.ProvisionServerType.DATA_SERVICE_BROKER);

        // 2-2. ingestInterface
        provisionAclRule(provisionNotiVO, DataManagerCode.ProvisionServerType.INGEST_INTERFACE);

        // 3. send kafka Event
        provisioningSVC.sendKafkaEvent(ProvisioningSVC.KafkaProvisioningType.ACL_RULE, provisionNotiVO);

        return provisionNotiVO;
    }


    /**
     * Provisioning event (created, updated, deleted)
     * @param provisionNotiVO provisioning send data
     * @param provisionServerType provisioning target server type
     */
    private void provisionAclRule(ProvisionNotiVO provisionNotiVO, DataManagerCode.ProvisionServerType provisionServerType) {

        // 1. retriege provisioning target server
        List<ProvisionServerBaseVO> provisionServerVOs = provisionServerSVC.getProvisionServerVOByType(provisionServerType);

        // 2. send provisioning
        List<ProvisionResultVO> provisionResultVOs = provisioningSVC.provisioning(provisionServerType,
                provisionServerVOs, provisionNotiVO, DataManagerCode.ProvisioningSubUri.ACL_RULE);

        log.info("Dataset Provisioning Result. {}", provisionResultVOs);

        // 3. set result
        boolean processResult = false;

        // not exists provisionig server
        if (provisionResultVOs == null || provisionResultVOs.size() == 0) {
            processResult = true;
        } else {

            // if even one server of the same type succeeds, it is judged as a success
            for (ProvisionResultVO provisionResultVO : provisionResultVOs) {
                if (provisionResultVO.getResult()) {
                    processResult = true;
                }
            }
        }

        // exception throw when all servers of the same type fail
        if (!processResult) {
            for (ProvisionResultVO provisionResultVO : provisionResultVOs) {
                if (!provisionResultVO.getResult()) {
                    throw provisionResultVO.getProvisionException();
                }
            }
        }
    }
}