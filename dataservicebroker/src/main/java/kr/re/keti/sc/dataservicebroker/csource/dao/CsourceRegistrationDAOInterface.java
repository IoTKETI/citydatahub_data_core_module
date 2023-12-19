package kr.re.keti.sc.dataservicebroker.csource.dao;

import kr.re.keti.sc.dataservicebroker.common.vo.QueryVO;
import kr.re.keti.sc.dataservicebroker.csource.vo.CsourceRegistrationBaseDaoVO;
import kr.re.keti.sc.dataservicebroker.csource.vo.CsourceRegistrationEntityDaoVO;
import kr.re.keti.sc.dataservicebroker.csource.vo.CsourceRegistrationInfoDaoVO;

import java.util.List;

public interface CsourceRegistrationDAOInterface {

    //
    public Integer createCsourceRegistrationBase(CsourceRegistrationBaseDaoVO csourceRegistrationBaseDaoVO);

    public Integer createCsourceRegistrationInfo
            (List<CsourceRegistrationInfoDaoVO> csourceRegistrationInfoDaoVOs);

    public Integer createCsourceRegistrationEntity
            (List<CsourceRegistrationEntityDaoVO> csourceRegistrationEntityDaoVOs);


    public Integer updateCsourceRegistrationBase(CsourceRegistrationBaseDaoVO csourceRegistrationBaseDaoVO);
//
//    public Integer updateCsourceRegistrationInfo(List<CsourceRegistrationInfoDaoVO> csourceRegistrationInfoDaoVOs);
//
//    public Integer updateCsourceRegistrationEntity(List<CsourceRegistrationEntityDaoVO> csourceRegistrationEntityDaoVOs);


    public Integer deleteCsourceRegistrationEntity(String csourceRegistrationBaseId);

    public Integer deleteCsourceRegistrationInfo(String csourceRegistrationBaseId);

    public Integer deleteCsourceRegistrationBase(String csourceRegistrationBaseId);


    public List<CsourceRegistrationBaseDaoVO> retrieveCsourceRegistration(String registrationId);

    public List<CsourceRegistrationBaseDaoVO> queryCsourceRegistration(QueryVO queryVO);

    public Integer queryCsourceRegistrationCount(QueryVO queryVO);

    public List<CsourceRegistrationBaseDaoVO> queryCsourceRegistrationByEntityId(String entityId);
}
