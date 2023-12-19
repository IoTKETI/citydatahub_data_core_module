package kr.re.keti.sc.dataservicebroker.acl.rule.dao;

import kr.re.keti.sc.dataservicebroker.acl.rule.vo.AclRuleVO;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AclRuleDAO {

    @Autowired
    private SqlSessionTemplate sqlSession;

    public int createAclRule(AclRuleVO aclRuleVO) {
        return sqlSession.update("dataservicebroker.acl.rule.createAclRule", aclRuleVO);
    }

    public int updateAclRule(AclRuleVO aclRuleVO) {
        return sqlSession.update("dataservicebroker.acl.rule.updateAclRule", aclRuleVO);
    }

    public int deleteAclRule(String id) {
        return sqlSession.update("dataservicebroker.acl.rule.deleteAclRule", id);
    }

    public List<AclRuleVO> getAclRuleVOList(AclRuleVO aclRuleVO) {
        return sqlSession.selectList("dataservicebroker.acl.rule.selectAll", aclRuleVO);
    }

    public AclRuleVO getAclRuleVOById(String id) {
        return sqlSession.selectOne("dataservicebroker.acl.rule.selectById", id);
    }

    public Integer getAclDatasetTotalCount(AclRuleVO aclRuleVO) {
        return sqlSession.selectOne("dataservicebroker.acl.rule.selectTotalCount", aclRuleVO);
    }


    public void setSqlSession(SqlSessionTemplate sqlSession) {
        this.sqlSession = sqlSession;
    }

}
