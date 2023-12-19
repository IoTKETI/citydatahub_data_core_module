package kr.re.keti.sc.ingestinterface.verificationhistory.dao;

import kr.re.keti.sc.ingestinterface.verificationhistory.vo.VerificationHistoryBaseVO;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Verification history repository access class
 */
@Repository
public class VerificationHistoryDAO {

    @Autowired
    private SqlSessionTemplate sqlSession;


    public int insertVerificationHistories(List<VerificationHistoryBaseVO> verificationHistoryBaseVOs) {

        int result = sqlSession.insert("ingestinterface.verificationhistory.insertVerificationHistories", verificationHistoryBaseVOs);
        return result;
    }

    public int insertVerificationHistory(VerificationHistoryBaseVO verificationHistoryBaseVO) {

        int result = sqlSession.insert("ingestinterface.verificationhistory.insertVerificationHistory", verificationHistoryBaseVO);
        return result;
    }

    public List<VerificationHistoryBaseVO> getVerificationHistory(VerificationHistoryBaseVO verificationHistoryBaseVO) {
        return sqlSession.selectList("ingestinterface.verificationhistory.selectAll", verificationHistoryBaseVO);
    }


    public VerificationHistoryBaseVO getVerificationHistoryBySeq(Integer seq) {
        return sqlSession.selectOne("ingestinterface.verificationhistory.selectBySeq", seq);
    }


    public VerificationHistoryBaseVO getVerificationHistoryCount(VerificationHistoryBaseVO verificationHistoryBaseVO) {
        return sqlSession.selectOne("ingestinterface.verificationhistory.selectCount", verificationHistoryBaseVO);
    }


    public Integer getVerificationHistoryTotalCount(VerificationHistoryBaseVO verificationHistoryBaseVO) {
        return sqlSession.selectOne("ingestinterface.verificationhistory.selectTotalCount", verificationHistoryBaseVO);
    }

}
