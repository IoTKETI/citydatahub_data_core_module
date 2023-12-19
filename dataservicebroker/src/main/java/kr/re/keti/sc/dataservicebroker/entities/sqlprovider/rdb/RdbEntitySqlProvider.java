package kr.re.keti.sc.dataservicebroker.entities.sqlprovider.rdb;

import java.util.List;

import kr.re.keti.sc.dataservicebroker.common.vo.DbConditionVO;
import kr.re.keti.sc.dataservicebroker.common.vo.entities.DynamicEntityDaoVO;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.SelectProvider; // Mapper 인터페이스로 만든다
import org.apache.ibatis.annotations.UpdateProvider;

import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityDaoVO;

public interface RdbEntitySqlProvider {

    @UpdateProvider(method = "executeDdl")
    void executeDdl(String ddl);

    @InsertProvider(method = "create")
    void create(CommonEntityDaoVO entityDaoVO);

    @UpdateProvider(method = "replaceAttr")
    int replaceAttr(CommonEntityDaoVO entityDaoVO);

    @UpdateProvider(method = "replaceAttrHBase")
    int replaceAttrHBase(CommonEntityDaoVO entityDaoVO);

    @UpdateProvider(method = "appendAttr")
    int appendAttr(CommonEntityDaoVO entityDaoVO);

    @UpdateProvider(method = "appendNoOverwriteAttr")
    int appendNoOverwriteAttr(CommonEntityDaoVO entityDaoVO);

    @UpdateProvider(method = "updateAttr")
    int updateAttr(CommonEntityDaoVO entityDaoVO);

    @UpdateProvider(method = "partialAttrUpdate")
    int partialAttrUpdate(CommonEntityDaoVO entityDaoVO);

    @DeleteProvider(method = "delete")
    int delete(CommonEntityDaoVO entityDaoVO);

    @DeleteProvider(method = "deleteHist")
    int deleteHist(CommonEntityDaoVO entityDaoVO);

    @DeleteProvider(method = "deleteFullHist")
    int deleteFullHist(CommonEntityDaoVO entityDaoVO);

    @UpdateProvider(method = "deleteAttr")
    int deleteAttr(CommonEntityDaoVO entityDaoVO);

    @UpdateProvider(method = "createHist")
    int createHist(CommonEntityDaoVO entityDaoVO);

    @UpdateProvider(method = "createFullHist")
    int createFullHist(CommonEntityDaoVO entityDaoVO);

    @SelectProvider(method = "selectOne")
    DynamicEntityDaoVO selectOne(DbConditionVO dbConditionVO);

    @SelectProvider(method = "selectList")
    List<DynamicEntityDaoVO> selectList(DbConditionVO dbConditionVO);

    @SelectProvider(method = "selectHistList")
    List<DynamicEntityDaoVO> selectHistList(DbConditionVO dbConditionVO);
    
    @SelectProvider(method = "selectCount")
    int selectCount(DbConditionVO dbConditionVO);
    
    @SelectProvider(method = "selectHistCount")
    int selectHistCount(DbConditionVO dbConditionVO);

}