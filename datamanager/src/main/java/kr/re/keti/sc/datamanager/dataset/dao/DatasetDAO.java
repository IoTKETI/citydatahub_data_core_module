package kr.re.keti.sc.datamanager.dataset.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.re.keti.sc.datamanager.datamodel.vo.DataModelBaseVO;
import kr.re.keti.sc.datamanager.dataset.vo.DatasetBaseVO;

/**
 * Dataset repository access Class
 */
@Repository
public class DatasetDAO {

    @Autowired
    private SqlSessionTemplate sqlSession;

    public int createDatasetBaseVO(DatasetBaseVO datasetBaseVO) {
        return sqlSession.update("datacore.dataset.createDatasetBaseVO", datasetBaseVO);
    }

    public int updateDatasetBaseVO(DatasetBaseVO datasetBaseVO) {
        return sqlSession.update("datacore.dataset.updateDatasetBaseVO", datasetBaseVO);
    }

    public int updateDatasetProvisioning(DatasetBaseVO datasetBaseVO) {
        return sqlSession.update("datacore.dataset.updateDatasetProvisioning", datasetBaseVO);
    }

    public int deleteDatasetBaseVO(String id) {
        return sqlSession.update("datacore.dataset.deleteDatasetBaseVO", id);
    }

    public List<DatasetBaseVO> getDatasetVOList(DatasetBaseVO datasetBaseVO) {
        return sqlSession.selectList("datacore.dataset.selectAll", datasetBaseVO);
    }

    public Integer getDatasetVOListTotalCount(DatasetBaseVO datasetBaseVO) {
        return sqlSession.selectOne("datacore.dataset.selectTotalCount", datasetBaseVO);
    }

    public DatasetBaseVO getDatasetVOById(String id) {
        return sqlSession.selectOne("datacore.dataset.selectById", id);
    }

    public DatasetBaseVO getEnabledDatasetByDataModelBaseVO(DatasetBaseVO datasetBaseVO) {
        return sqlSession.selectOne("datacore.dataset.selectEnabledDatasetByDataModelBaseVO", datasetBaseVO);
    }

    public List<DatasetBaseVO> getDatasetVOListForUI(DatasetBaseVO datasetBaseVO) {
        return sqlSession.selectList("datacore.dataset.selectAllForUI", datasetBaseVO);
    }

    public Integer getDatasetVOListTotalCountForUI(DatasetBaseVO datasetBaseVO) {
        return sqlSession.selectOne("datacore.dataset.selectTotalCountForUI", datasetBaseVO);
    }

    public void setSqlSession(SqlSessionTemplate sqlSession) {
        this.sqlSession = sqlSession;
    }
}
