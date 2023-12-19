package kr.re.keti.sc.datamanager.datasetflow.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import kr.re.keti.sc.datamanager.datasetflow.vo.DatasetFlowBaseVO;
import kr.re.keti.sc.datamanager.datasetflow.vo.DatasetFlowBaseVO.DatasetFlowServerDetailVO;

/**
 * Dataset flow repository access Class
 */
@Repository
public class DatasetFlowDAO {

    @Autowired
    private SqlSessionTemplate sqlSession;

    @Transactional
    public int createDatasetFlow(DatasetFlowBaseVO datasetFlowBaseVO) {
    	
    	int createResult = sqlSession.update("datamanager.datasetflow.createDatasetFlowBase", datasetFlowBaseVO);
    	if(createResult > 0) {
    		List<DatasetFlowServerDetailVO> datasetFlowServerDetailVOs = datasetFlowBaseVO.getDatasetFlowServerDetailVOs();
    		for(DatasetFlowServerDetailVO datasetFlowServerDetailVO : datasetFlowServerDetailVOs) {
    			sqlSession.update("datamanager.datasetflow.createDatasetFlowServerDetail", datasetFlowServerDetailVO);
    		}
    	}
        return createResult;
    }

    @Transactional
    public int updateDatasetFlow(DatasetFlowBaseVO datasetFlowBaseVO) {
    	// 1. updatesetFlowBase 정보 수정
    	int createResult = sqlSession.update("datamanager.datasetflow.updateDatasetFlowBase", datasetFlowBaseVO);
    	if(createResult > 0) {
    		// 2. 기존 datasetFlowServerDetail 정보 삭제
    		sqlSession.update("datamanager.datasetflow.deleteDatasetFlowServerDetail", datasetFlowBaseVO);

    		// 3. 입력받은 datasetFlowServerDetail 정보 생성
    		List<DatasetFlowServerDetailVO> datasetFlowServerDetailVOs = datasetFlowBaseVO.getDatasetFlowServerDetailVOs();
    		for(DatasetFlowServerDetailVO datasetFlowServerDetailVO : datasetFlowServerDetailVOs) {
    			sqlSession.update("datamanager.datasetflow.createDatasetFlowServerDetail", datasetFlowServerDetailVO);
    		}
    	}
        return createResult;
    }

    public int updateDatasetFlowProvisioning(DatasetFlowBaseVO datasetFlowBaseVO) {
    	return sqlSession.update("datamanager.datasetflow.updateDatasetFlowProvisioning", datasetFlowBaseVO);
    }

    @Transactional
    public int deleteDatasetFlow(DatasetFlowBaseVO datasetFlowBaseVO) {
    	sqlSession.update("datamanager.datasetflow.deleteDatasetFlowServerDetail", datasetFlowBaseVO);
        return sqlSession.update("datamanager.datasetflow.deleteDatasetFlowBase", datasetFlowBaseVO);
    }

    public List<DatasetFlowBaseVO> getDatasetFlowBaseVOList() {
        return sqlSession.selectList("datamanager.datasetflow.selectAll");
    }

    public DatasetFlowBaseVO getDatasetFlowBaseVOById(DatasetFlowBaseVO datasetFlowBaseVO) {
        return sqlSession.selectOne("datamanager.datasetflow.selectById", datasetFlowBaseVO);
    }

    public List<DatasetFlowBaseVO> getEnabledDatasetByDatasetId(String selectByDatasetId) {
        return sqlSession.selectList("datamanager.datasetflow.selectEnabledDatasetByDatasetId", selectByDatasetId);
    }

    public void setSqlSession(SqlSessionTemplate sqlSession) {
        this.sqlSession = sqlSession;
    }
}
