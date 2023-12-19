package kr.re.keti.sc.dataservicebroker.entities.dao;

import java.util.ArrayList;
import java.util.List;

import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityDaoVO;
import kr.re.keti.sc.dataservicebroker.common.vo.EntityAttrDaoVO;
import kr.re.keti.sc.dataservicebroker.common.vo.ProcessResultVO;
import kr.re.keti.sc.dataservicebroker.common.vo.QueryVO;

/**
 * Entity 공통 DAO 인터페이스
 *
 * @param <T>
 */
public interface EntityDAOInterface<T extends CommonEntityDaoVO> {

    /**
     * 벌크 CREATE 처리
     *
     * @param createList CREATE 대상 VO 리스트
     * @return List<ProcessResultVO> 처리 결과 리스트
     */
    public List<ProcessResultVO> bulkCreate(List<T> createList);

    /**
     * CREATE 처리
     *
     * @param entityDaoVO CREATE 대상 VO
     * @return ProcessResultVO 처리 결과VO
     */
    public ProcessResultVO create(T entityDaoVO);

    /**
     * 벌크 Append Entity Attributes 처리
     *
     * @param AppendAttrList Append Entity Attributes 대상 VO 리스트
     * @return List<ProcessResultVO> 처리 결과 리스트
     */
    public List<ProcessResultVO> bulkAppendAttr(List<T> AppendAttrList);

    /**
     * Append Entity Attributes 처리
     *
     * @param entityDaoVO Append Entity Attributes 대상 VO
     * @return ProcessResultVO 처리 결과VO
     */
    public ProcessResultVO appendAttr(T entityDaoVO);


    /**
     * 벌크 Append Entity (noOverwrite) Attributes 처리
     *
     * @param AppendAttrList Append Entity (noOverwrite) Attributes 대상 VO 리스트
     * @return List<ProcessResultVO> 처리 결과 리스트
     */
    public List<ProcessResultVO> bulkAppendNoOverwriteAttr(List<T> AppendAttrList);

    /**
     * Append Entity (noOverwrite) Attributes 처리
     *
     * @param entityDaoVO Append Entity (noOverwrite) Attributes 대상 VO
     * @return ProcessResultVO 처리 결과VO
     */
    public ProcessResultVO appendNoOverwriteAttr(T entityDaoVO);

    /**
     * 벌크 Update Entity Attributes 처리
     * @param updateAttrList Update Entity Attributes 대상 VO 리스트
     * @return List<ProcessResultVO> 처리 결과 리스트
     */
    public List<ProcessResultVO> bulkUpdateAttr(List<T> updateAttrList);

    /**
     * Update Entity Attributes 처리
     * @param entityDaoVO Update Entity Attributes 대상 VO
     * @return ProcessResultVO 처리 결과VO
     */
    public ProcessResultVO updateAttr(T entityDaoVO);

    /**
     * 벌크 Partial Attribute Update 처리
     * @param partialAttrUpdateList Partial Attribute Update 대상 VO 리스트
     * @return List<ProcessResultVO> 처리 결과 리스트
     */
    public List<ProcessResultVO> bulkPartialAttrUpdate(List<T> partialAttrUpdateList);

    /**
     * Partial Attribute Update 처리
     * @param entityDaoVO Partial Attribute Update 대상 VO
     * @return ProcessResultVO 처리 결과VO
     */
    public ProcessResultVO partialAttrUpdate(T entityDaoVO);

    
    /**
     * 벌크 Replace Entity Attributes 처리
     *
     * @param daoVOList Replace Entity Attributes 대상 VO 리스트
     * @return List<ProcessResultVO> 처리 결과 리스트
     */
    public List<ProcessResultVO> bulkReplaceEntity(List<T> daoVOList);

    /**
     * Replace Entity Attributes 처리
     *
     * @param entityDaoVO Replace Entity Attributes 대상 VO
     * @return ProcessResultVO 처리 결과VO
     */
    public ProcessResultVO replaceEntity(T entityDaoVO);

    /**
     * FULL UPSERT 벌크 처리
     * - Replace Entity Attributes 벌크 처리 후 result 가 0 인 항목들만 Create 수행
     *
     * @param fullUpsertList FULL UPSERT 대상 VO 리스트
     * @return List<ProcessResultVO> 처리결과 리스트
     */
    public List<ProcessResultVO> bulkFullUpsert(List<T> fullUpsertList);

    /**
     * FULL UPSERT 처리
     * - Replace Entity Attributes 결과가 0 인 경우 Create 처리
     *
     * @param entityDaoVO FULL UPSERT 대상 VO
     * @return ProcessResultVO 처리결과VO
     */
    public ProcessResultVO fullUpsert(T entityDaoVO);

    /**
     * 벌크 PARTIAL UPSERT 처리
     *
     * @param partialUpsertList PARTIAL UPSERT 대상 VO 리스트
     * @return List<ProcessResultVO> 처리 결과 리스트
     */
    public List<ProcessResultVO> bulkPartialUpsert(List<T> partialUpsertList);

    /**
     * PARTIAL UPSERT 처리
     *
     * @param entityDaoVO PARTIAL UPSERT 대상 VO
     * @return ProcessResultVO 처리 결과VO
     */
    public ProcessResultVO partialUpsert(T entityDaoVO);

    /**
     * 벌크 DELETE 처리
     *
     * @param deleteList DELETE 대상 VO 리스트
     * @return List<ProcessResultVO> 처리 결과 리스트
     */
    public List<ProcessResultVO> bulkDelete(List<T> deleteList);

    /**
     * 벌크 이력 저장 처리
     *
     * @param histList 이력 VO 리스트
     * @return List<ProcessResultVO> 처리 결과 리스트
     */
    public ArrayList<Integer> bulkCreateHist(List<T> histList);

    /**
     * 벌크 이력 저장 처리 (Entity 의 전체 Attibute 값을 조회하여 저장)
     *
     * @param histList 이력 VO 리스트
     * @return List<ProcessResultVO> 처리 결과 리스트
     */
    public ArrayList<Integer> bulkCreateFullHist(List<T> histList);

    /**
     * DELETE 처리
     *
     * @param entityDaoVO DELETE 대상 VO
     * @return ProcessResultVO 처리 결과VO
     */
    public ProcessResultVO delete(T id);

    /**
     * Entity의 Attribute 삭제
     * @param entityDaoVO DELETE ATTRIBUTES 대상 VO
     * @return ProcessResultVO 처리 결과
     */
    public ProcessResultVO deleteAttr(T entityDaoVO);

    /**
     * 최종 데이터 조회 by ID
     * @param queryVO(id)
     * * @param useForCreateOperation Create 로직 처리를 위해 사용 여부
     * @return 조회결과VO
     */
    public T selectById(QueryVO queryVO, Boolean useForCreateOperation);

    /**
     * 최종 데이터 목록 조회
     * @param queryVO
     * @return 조회결과VO리스트
     */
    public List<T> selectAll(QueryVO queryVO);

    /**
     * 이력 데이터(parital) 조회 by ID
     * @param queryVO
     * @return 조회결과VO리스트
     */
    public List<T> selectHistById(QueryVO queryVO);

    /**
     * 이력 데이터 목록(parital) 조회
     * @param queryVO
     * @return 조회결과VO리스트
     */
    public List<T> selectAllHist(QueryVO queryVO);

    /**
     * Entity의 Attribute 삭제
     * @param entityAttrDaoVO 처리VO
     * @return ProcessResultVO 처리 결과
     */
    public ProcessResultVO deleteAttribute(EntityAttrDaoVO entityAttrDaoVO);
    
    /**
     * 최종 데이터 건수 조회
     * @param queryVO
     * @return 최종 데이터 건수
     */
    public Integer selectCount(QueryVO queryVO);

    /**
     * 이력 데이터 건수 조회
     * @param queryVO
     * @return 이력 데이터 건수
     */
    public Integer selectHistCount(QueryVO queryVO);
    /**
     * 빅데이터 테이블 refresh
     * @param tableName
     */
    public void refreshTable(String tableName);
}
