package kr.re.keti.sc.ingestinterface.acl.rule.dao;

import kr.re.keti.sc.ingestinterface.acl.rule.vo.AclRuleVO;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Access control policy repository access class
 */
@Repository
public class AclRuleDAO {

    @Autowired
    private SqlSessionTemplate sqlSession;

    public int createAclRule(AclRuleVO aclRuleVO) {
        return sqlSession.update("ingestinterface.acl.rule.createAclRule", aclRuleVO);
    }

    public int updateAclRule(AclRuleVO aclRuleVO) {
        return sqlSession.update("ingestinterface.acl.rule.updateAclRule", aclRuleVO);
    }

    public int deleteAclRule(String id) {
        return sqlSession.update("ingestinterface.acl.rule.deleteAclRule", id);
    }

    public List<AclRuleVO> getAclRuleVOList(AclRuleVO aclRuleVO) {
        return sqlSession.selectList("ingestinterface.acl.rule.selectAll", aclRuleVO);
    }

    public AclRuleVO getAclRuleVOById(String id) {
        return sqlSession.selectOne("ingestinterface.acl.rule.selectById", id);
    }

    public Integer getAclRuleTotalCount(AclRuleVO aclRuleVO) {
        return sqlSession.selectOne("ingestinterface.acl.rule.selectTotalCount", aclRuleVO);
    }

    public String createUuid() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public void setSqlSession(SqlSessionTemplate sqlSession) {
        this.sqlSession = sqlSession;
    }

}
