package kr.re.keti.sc.ingestinterface.dataset.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.re.keti.sc.ingestinterface.dataset.vo.DatasetBaseVO;

/**
 * Dataset repository access class
 */
@Repository
public class DatasetDAO {

    @Autowired
    private SqlSessionTemplate sqlSession;

    public int createDataset(DatasetBaseVO datasetBaseVO) {
        return sqlSession.update("dataservicebroker.dataset.createDataset", datasetBaseVO);
    }
    public int updateDataset(DatasetBaseVO datasetBaseVO) {
        return sqlSession.update("dataservicebroker.dataset.updateDataset", datasetBaseVO);
    }

    public int deleteDataset(String id) {
        return sqlSession.update("dataservicebroker.dataset.deleteDataset", id);
    }

    public List<DatasetBaseVO> getDatasetVOList() {
        return sqlSession.selectList("dataservicebroker.dataset.selectAll");
    }

    public DatasetBaseVO getDatasetVOById(String id) {
        return sqlSession.selectOne("dataservicebroker.dataset.selectById", id);
    }


    public void setSqlSession(SqlSessionTemplate sqlSession) {
        this.sqlSession = sqlSession;
    }
}
