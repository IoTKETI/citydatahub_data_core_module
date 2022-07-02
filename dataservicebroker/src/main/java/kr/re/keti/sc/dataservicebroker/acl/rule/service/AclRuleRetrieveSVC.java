package kr.re.keti.sc.dataservicebroker.acl.rule.service;

import kr.re.keti.sc.dataservicebroker.acl.rule.dao.AclRuleDAO;
import kr.re.keti.sc.dataservicebroker.acl.rule.vo.AclRuleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AclRuleRetrieveSVC {

    private AclRuleDAO aclRuleDAO;

    public AclRuleRetrieveSVC(AclRuleDAO aclRuleDAO) {
        this.aclRuleDAO = aclRuleDAO;
    }

    public List<AclRuleVO> getAclRuleVOList(AclRuleVO aclRuleVO) {
        return aclRuleDAO.getAclRuleVOList(aclRuleVO);
    }

    public AclRuleVO getAclRuleVOById(String id) {
        return aclRuleDAO.getAclRuleVOById(id);
    }

}
