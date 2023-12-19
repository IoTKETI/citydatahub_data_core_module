package kr.re.keti.sc.ingestinterface.externalplatformauthentication.dao;

import kr.re.keti.sc.ingestinterface.common.vo.PageRequest;
import kr.re.keti.sc.ingestinterface.externalplatformauthentication.vo.ExternalPlatformAuthenticationBaseVO;
import kr.re.keti.sc.ingestinterface.verificationhistory.vo.VerificationHistoryBaseVO;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * External platform authentication repository access class
 */
@Repository
public class ExternalPlatformAuthenticationDAO {

    @Autowired
    private SqlSessionTemplate sqlSession;


    public int createExternalPlatformAuthenticationBaseVO(ExternalPlatformAuthenticationBaseVO externalPlatformAuthenticationBaseVO) {
        return sqlSession.update("ingestinterface.externalplatformauthentication.insertExternalPlatformAuthenticationBase", externalPlatformAuthenticationBaseVO);
    }

    public int updateExternalPlatformAuthenticationBaseVO(ExternalPlatformAuthenticationBaseVO externalPlatformAuthenticationBaseVO) {
        return sqlSession.update("ingestinterface.externalplatformauthentication.updateExternalPlatformAuthenticationBase", externalPlatformAuthenticationBaseVO);
    }

    public int deleteExternalPlatformAuthenticationBaseVO(String id) {
        return sqlSession.update("ingestinterface.externalplatformauthentication.deleteExternalPlatformAuthenticationBase", id);
    }

    public ExternalPlatformAuthenticationBaseVO getExternalPlatformAuthenticationBaseVOById(String id) {
        return sqlSession.selectOne("ingestinterface.externalplatformauthentication.selectById", id);
    }


    public ExternalPlatformAuthenticationBaseVO getExternalPlatformAuthenticationBaseVOByClientId(String clientId) {
        return sqlSession.selectOne("ingestinterface.externalplatformauthentication.selectByClientId", clientId);
    }

    public List<ExternalPlatformAuthenticationBaseVO> getExternalPlatformAuthenticationBaseVOList(PageRequest pageRequest) {
        return sqlSession.selectList("ingestinterface.externalplatformauthentication.selectAll", pageRequest);
    }
    public Integer getExternalPlatformAuthenticationBaseVOListTotalCount() {
        return sqlSession.selectOne("ingestinterface.externalplatformauthentication.selectTotalCount");
    }
}
