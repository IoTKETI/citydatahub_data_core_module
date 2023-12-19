package kr.re.keti.sc.datamanager.acl.rule.dao;

import kr.re.keti.sc.datamanager.acl.rule.vo.AclRuleVO;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Access control policy rule repository access Class
 */
@Repository
public class AclRuleDAO {

    @Autowired
    private SqlSessionTemplate sqlSession;

    public int createAclRule(AclRuleVO aclRuleVO) {
        return sqlSession.update("datamanager.acl.rule.createAclRule", aclRuleVO);
    }

    public int updateAclRule(AclRuleVO aclRuleVO) {
        return sqlSession.update("datamanager.acl.rule.updateAclRule", aclRuleVO);
    }

    public int deleteAclRule(String id) {
        return sqlSession.update("datamanager.acl.rule.deleteAclRule", id);
    }

    public List<AclRuleVO> getAclRuleVOList(AclRuleVO aclRuleVO) {
        return sqlSession.selectList("datamanager.acl.rule.selectAll", aclRuleVO);
    }

    public AclRuleVO getAclRuleVOById(String id) {
        return sqlSession.selectOne("datamanager.acl.rule.selectById", id);
    }

    public Integer getAclRuleTotalCount(AclRuleVO aclRuleVO) {
        return sqlSession.selectOne("datamanager.acl.rule.selectTotalCount", aclRuleVO);
    }

    public void setSqlSession(SqlSessionTemplate sqlSession) {
        this.sqlSession = sqlSession;
    }

}
