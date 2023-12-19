package kr.re.keti.sc.dataservicebroker.datasetflow.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.re.keti.sc.dataservicebroker.datasetflow.vo.DatasetFlowBaseVO;
import kr.re.keti.sc.dataservicebroker.datasetflow.vo.RetrieveDatasetFlowBaseVO;

@Repository
public class DatasetFlowDAO {

    @Autowired
    private SqlSessionTemplate sqlSession;

    public int createDatasetFlow(DatasetFlowBaseVO datasetFlowBaseVO) {
        return sqlSession.update("dataservicebroker.datasetflow.createDatasetFlowBase", datasetFlowBaseVO);
    }

    public int updateDatasetFlow(DatasetFlowBaseVO datasetFlowBaseVO) {
        return sqlSession.update("dataservicebroker.datasetflow.updateDatasetFlowBase", datasetFlowBaseVO);
    }

    public int deleteDatasetFlow(DatasetFlowBaseVO datasetFlowBaseVO) {
        return sqlSession.update("dataservicebroker.datasetflow.deleteDatasetFlowBase", datasetFlowBaseVO);
    }

    public List<DatasetFlowBaseVO> getDatasetFlowBaseVOList() {
        return sqlSession.selectList("dataservicebroker.datasetflow.selectAll");
    }

    public DatasetFlowBaseVO getDatasetFlowBaseVOById(DatasetFlowBaseVO datasetFlowBaseVO) {
        return sqlSession.selectOne("dataservicebroker.datasetflow.selectById", datasetFlowBaseVO);
    }

    public List<DatasetFlowBaseVO> getDatasetFlowBaseVOByDataModel (RetrieveDatasetFlowBaseVO retrieveDatasetFlowBaseVO) {
        return sqlSession.selectList("dataservicebroker.datasetflow.selectByDataModel", retrieveDatasetFlowBaseVO);
    }

    public void setSqlSession(SqlSessionTemplate sqlSession) {
        this.sqlSession = sqlSession;
    }
}
