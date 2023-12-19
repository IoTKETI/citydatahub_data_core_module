package kr.re.keti.sc.ingestinterface.verificationhistory.service;

import kr.re.keti.sc.ingestinterface.common.exception.BaseException;
import kr.re.keti.sc.ingestinterface.common.vo.CommonEntityFullVO;
import kr.re.keti.sc.ingestinterface.common.vo.EntityProcessVO;
import kr.re.keti.sc.ingestinterface.datamodel.vo.DataModelCacheVO;
import kr.re.keti.sc.ingestinterface.verificationhistory.dao.VerificationHistoryDAO;
import kr.re.keti.sc.ingestinterface.verificationhistory.vo.VerificationHistoryBaseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Verification history service class
 */
@Service
public class VerificationHistorySVC<T extends CommonEntityFullVO> {

    @Autowired
    private VerificationHistoryDAO verificationHistoryDAO;
    
    @Value("${verification.history.store.enabled:false}")
	private Boolean verificationHistoryStoreEnabled;


    public void insertVerificationHistories(List<VerificationHistoryBaseVO> verificationHistoryBaseVOs) {

        try {
            // TODO : 묶음 처리 필요
            verificationHistoryDAO.insertVerificationHistories(verificationHistoryBaseVOs);

        } catch (Exception e) {
            for (VerificationHistoryBaseVO verificationHistoryBaseVO : verificationHistoryBaseVOs) {
                verificationHistoryDAO.insertVerificationHistory(verificationHistoryBaseVO);
            }
        }
    }

    public List<VerificationHistoryBaseVO> getVerificationHistory(VerificationHistoryBaseVO verificationHistoryBaseVO) {
        return verificationHistoryDAO.getVerificationHistory(verificationHistoryBaseVO);
    }

    public VerificationHistoryBaseVO getVerificationHistoryBySeq(Integer seq) {
        return verificationHistoryDAO.getVerificationHistoryBySeq(seq);
    }

    public VerificationHistoryBaseVO getVerificationHistoryCount(VerificationHistoryBaseVO verificationHistoryBaseVO) {
        return verificationHistoryDAO.getVerificationHistoryCount(verificationHistoryBaseVO);
    }

    public Integer getVerificationHistoryTotalCount(VerificationHistoryBaseVO verificationHistoryBaseVO) {
        return verificationHistoryDAO.getVerificationHistoryTotalCount(verificationHistoryBaseVO);
    }


    /**
     * 데이터 품질 검사 이력 저장
     *
     * @param entityProcessVOList 에러 이력 저장 대상 VO
     */
    public void storeQualityHistory(List<EntityProcessVO<T>> entityProcessVOList) {

    	if(!verificationHistoryStoreEnabled) {
    		return;
    	}

        List<VerificationHistoryBaseVO> verificationHistoryBaseVOs = new ArrayList<>();

        //TODO 품질 이력 저장 로직 추가 xml도, 코드도 정리 필요
        for (EntityProcessVO<T> entityProcessVO : entityProcessVOList) {
            //품질 체크 false일 경우, PASS
            if (entityProcessVO.getDatasetBaseVO() != null && !entityProcessVO.getDatasetBaseVO().getQualityCheckEnabled()) {
                continue;
            }

            if (entityProcessVO.getProcessResultVO().isProcessResult()) {
                verificationHistoryBaseVOs.add(this.storeSuccessHistory(entityProcessVO));
            } else {
                verificationHistoryBaseVOs.add(this.storeErrorHistory(entityProcessVO));

            }
        }

        if (verificationHistoryBaseVOs != null && verificationHistoryBaseVOs.size() > 0) {
            insertVerificationHistories(verificationHistoryBaseVOs);
        }
    }

    /**
     * 에러 이력 저장
     *
     * @param entityProcessVO 에러 이력 저장 대상 VO
     */
    private VerificationHistoryBaseVO storeErrorHistory(EntityProcessVO<T> entityProcessVO) {

        VerificationHistoryBaseVO verificationHistoryBaseVO = new VerificationHistoryBaseVO();

        verificationHistoryBaseVO.setVerified(false);
        verificationHistoryBaseVO.setDatasetId(entityProcessVO.getDatasetId());
        verificationHistoryBaseVO.setTestTime(new Date());
        if (entityProcessVO.getEntityFullVO() != null) {
            if (entityProcessVO.getEntityFullVO().getId() != null) {
                verificationHistoryBaseVO.setEntityId(entityProcessVO.getEntityFullVO().getId());
            }
        }

        DataModelCacheVO dataModelCacheVO = entityProcessVO.getDataModelCacheVO();

        if (dataModelCacheVO != null) {
            verificationHistoryBaseVO.setDataModelId(dataModelCacheVO.getId());
        }

        BaseException exception = (BaseException) entityProcessVO.getProcessResultVO().getException();
        verificationHistoryBaseVO.setErrorCode(exception.getErrorCode());
        verificationHistoryBaseVO.setErrorCause(exception.getMessage());
        verificationHistoryBaseVO.setData(entityProcessVO.getContent());

        return verificationHistoryBaseVO;
    }

    /**
     * 성공 이력 저장
     *
     * @param entityProcessVO 에러 이력 저장 대상 VO
     */
    private VerificationHistoryBaseVO storeSuccessHistory(EntityProcessVO<T> entityProcessVO) {

        VerificationHistoryBaseVO verificationHistoryBaseVO = new VerificationHistoryBaseVO();
        verificationHistoryBaseVO.setVerified(true);
        verificationHistoryBaseVO.setDatasetId(entityProcessVO.getDatasetId());
        verificationHistoryBaseVO.setTestTime(new Date());

        DataModelCacheVO dataModelCacheVO = entityProcessVO.getDataModelCacheVO();
        if (dataModelCacheVO != null) {
            verificationHistoryBaseVO.setDataModelId(dataModelCacheVO.getId());
        }
        verificationHistoryBaseVO.setEntityId(entityProcessVO.getEntityFullVO().getId());

        return verificationHistoryBaseVO;
    }

}
