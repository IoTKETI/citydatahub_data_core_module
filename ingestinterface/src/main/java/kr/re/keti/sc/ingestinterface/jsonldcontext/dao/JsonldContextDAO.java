package kr.re.keti.sc.ingestinterface.jsonldcontext.dao;

import kr.re.keti.sc.ingestinterface.jsonldcontext.vo.JsonldContextBaseVO;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Jsonld context repository access class
 */
@Repository
public class JsonldContextDAO {

    @Autowired
    private SqlSessionTemplate sqlSession;

    public int upsertJsonldContext(JsonldContextBaseVO jsonldContextBaseVO) {
        return sqlSession.update("ingestinterface.jsonldcontext.upsertJsonldContext", jsonldContextBaseVO);
    }

    public List<JsonldContextBaseVO> getJsonldContextBaseVOList(JsonldContextBaseVO jsonldContextBaseVO) {
        return sqlSession.selectList("ingestinterface.jsonldcontext.selectAll", jsonldContextBaseVO);
    }

    public JsonldContextBaseVO getJsonldContextBaseVOById(JsonldContextBaseVO jsonldContextBaseVO) {
        return sqlSession.selectOne("ingestinterface.jsonldcontext.selectById", jsonldContextBaseVO);
    }

     public void setSqlSession(SqlSessionTemplate sqlSession) {
        this.sqlSession = sqlSession;
    }
}
