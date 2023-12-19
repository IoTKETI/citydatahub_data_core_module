package kr.re.keti.sc.dataservicebroker.jsonldcontext.dao;

import kr.re.keti.sc.dataservicebroker.datasetflow.vo.DatasetFlowBaseVO;
import kr.re.keti.sc.dataservicebroker.datasetflow.vo.RetrieveDatasetFlowBaseVO;
import kr.re.keti.sc.dataservicebroker.jsonldcontext.vo.JsonldContextBaseVO;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JsonldContextDAO {

    @Autowired
    private SqlSessionTemplate sqlSession;

    public int upsertJsonldContext(JsonldContextBaseVO jsonldContextBaseVO) {
        return sqlSession.update("dataservicebroker.jsonldcontext.upsertJsonldContext", jsonldContextBaseVO);
    }

    public List<JsonldContextBaseVO> getJsonldContextBaseVOList(JsonldContextBaseVO jsonldContextBaseVO) {
        return sqlSession.selectList("dataservicebroker.jsonldcontext.selectAll", jsonldContextBaseVO);
    }

    public JsonldContextBaseVO getJsonldContextBaseVOById(JsonldContextBaseVO jsonldContextBaseVO) {
        return sqlSession.selectOne("dataservicebroker.jsonldcontext.selectById", jsonldContextBaseVO);
    }

     public void setSqlSession(SqlSessionTemplate sqlSession) {
        this.sqlSession = sqlSession;
    }
}
