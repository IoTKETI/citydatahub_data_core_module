package kr.re.keti.sc.datamanager.provisionserver.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ProvisionServerType;
import kr.re.keti.sc.datamanager.provisionserver.dao.ProvisionServerDAO;
import kr.re.keti.sc.datamanager.provisionserver.vo.ProvisionServerBaseVO;

/**
 * Provision server serviec class
 */
@Service
public class ProvisionServerSVC {

    @Autowired
    private ProvisionServerDAO provisionServerDAO;

    public int createProvisionServer(ProvisionServerBaseVO provisionServerBaseVO) {
        return provisionServerDAO.createProvisionServer(provisionServerBaseVO);
    }

    public int updateProvisionServer(ProvisionServerBaseVO provisionServerBaseVO) {
        return provisionServerDAO.updateProvisionServer(provisionServerBaseVO);
    }

    public int deleteProvisionServer(String id) {
        return provisionServerDAO.deleteProvisionServer(id);
    }

    public List<ProvisionServerBaseVO> getProvisionServerVOList(ProvisionServerBaseVO provisionServerBaseVO) {
    	List<ProvisionServerBaseVO> provisionServerBaseVOs = provisionServerDAO.getProvisionServerVOList(provisionServerBaseVO);
    	if(provisionServerBaseVOs == null || provisionServerBaseVOs.size() == 0) {
    		return null;
    	}
        return provisionServerBaseVOs;
    }

    public ProvisionServerBaseVO getProvisionServerVOById(String id) {
        return provisionServerDAO.getProvisionServerVOById(id);
    }

    public List<ProvisionServerBaseVO> getProvisionServerVOByType(ProvisionServerType type) {
        return provisionServerDAO.getProvisionServerVOByType(type);
    }

    public Integer getProvisionServerTotalCount(ProvisionServerBaseVO provisionServerBaseVO) {
        return provisionServerDAO.getProvisionServerTotalCount(provisionServerBaseVO);
    }
}

