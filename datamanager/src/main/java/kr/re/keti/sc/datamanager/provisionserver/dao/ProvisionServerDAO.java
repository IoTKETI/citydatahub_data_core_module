package kr.re.keti.sc.datamanager.provisionserver.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ProvisionServerType;
import kr.re.keti.sc.datamanager.provisionserver.vo.ProvisionServerBaseVO;

/**
 * Provision server repository access Class
 */
@Repository
public class ProvisionServerDAO {

    @Autowired
    private SqlSessionTemplate sqlSession;

    public int createProvisionServer(ProvisionServerBaseVO provisionServerBaseVO) {
        return sqlSession.update("datamanager.provisionserver.createProvisionServer", provisionServerBaseVO);
    }

    public int updateProvisionServer(ProvisionServerBaseVO provisionServerBaseVO) {
        return sqlSession.update("datamanager.provisionserver.updateProvisionServer", provisionServerBaseVO);
    }

    public int deleteProvisionServer(String id) {
        return sqlSession.update("datamanager.provisionserver.deleteProvisionServer", id);
    }

    public List<ProvisionServerBaseVO> getProvisionServerVOList(ProvisionServerBaseVO provisionServerBaseVO) {
        return sqlSession.selectList("datamanager.provisionserver.selectAll", provisionServerBaseVO);
    }

    public ProvisionServerBaseVO getProvisionServerVOById(String id) {
        return sqlSession.selectOne("datamanager.provisionserver.selectById", id);
    }

    public List<ProvisionServerBaseVO> getProvisionServerVOByType(ProvisionServerType type) {
        return sqlSession.selectList("datamanager.provisionserver.selectByType", type.getCode());
    }

    public Integer getProvisionServerTotalCount(ProvisionServerBaseVO provisionServerBaseVO) {
        return sqlSession.selectOne("datamanager.provisionserver.selectTotalCount", provisionServerBaseVO);
    }

    public void setSqlSession(SqlSessionTemplate sqlSession) {
        this.sqlSession = sqlSession;
    }
}
