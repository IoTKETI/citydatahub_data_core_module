package kr.re.keti.sc.ingestinterface.externalplatformauthentication.service;

import kr.re.keti.sc.ingestinterface.common.vo.PageRequest;
import kr.re.keti.sc.ingestinterface.externalplatformauthentication.dao.ExternalPlatformAuthenticationDAO;
import kr.re.keti.sc.ingestinterface.externalplatformauthentication.vo.ExternalPlatformAuthenticationBaseVO;
import kr.re.keti.sc.ingestinterface.verificationhistory.vo.VerificationHistoryBaseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * External platform authentication Service class
 */
@Service
public class ExternalPlatformAuthenticationSVC {


    @Autowired
    ExternalPlatformAuthenticationDAO externalPlatformAuthenticationDAO;

    public int createExternalPlatformAuthenticationBaseVO(ExternalPlatformAuthenticationBaseVO externalPlatformAuthenticationBaseVO) {
        return externalPlatformAuthenticationDAO.createExternalPlatformAuthenticationBaseVO(externalPlatformAuthenticationBaseVO);
    }

    public int updateExternalPlatformAuthenticationBaseVO(ExternalPlatformAuthenticationBaseVO externalPlatformAuthenticationBaseVO) {
        return externalPlatformAuthenticationDAO.updateExternalPlatformAuthenticationBaseVO(externalPlatformAuthenticationBaseVO);
    }

    public int deleteExternalPlatformAuthenticationBaseVO(String id) {

        return externalPlatformAuthenticationDAO.deleteExternalPlatformAuthenticationBaseVO(id);
    }

    public ExternalPlatformAuthenticationBaseVO getExternalPlatformAuthenticationBaseVOById(String id) {

        return externalPlatformAuthenticationDAO.getExternalPlatformAuthenticationBaseVOById(id);
    }

    public ExternalPlatformAuthenticationBaseVO getExternalPlatformAuthenticationBaseVOByClientId(String clientId) {

        return externalPlatformAuthenticationDAO.getExternalPlatformAuthenticationBaseVOByClientId(clientId);
    }


    public List<ExternalPlatformAuthenticationBaseVO> getExternalPlatformAuthenticationBaseVOList(PageRequest pageRequest) {
        return externalPlatformAuthenticationDAO.getExternalPlatformAuthenticationBaseVOList(pageRequest);
    }


    public Integer getExternalPlatformAuthenticationBaseVOListTotalCount() {
        return externalPlatformAuthenticationDAO.getExternalPlatformAuthenticationBaseVOListTotalCount();
    }



}
