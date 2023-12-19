package kr.re.keti.sc.dataservicebroker.entities.service;

import java.util.List;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.Operation;
import kr.re.keti.sc.dataservicebroker.common.exception.BaseException;
import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityDaoVO;
import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityFullVO;
import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityVO;
import kr.re.keti.sc.dataservicebroker.common.vo.EntityProcessVO;
import kr.re.keti.sc.dataservicebroker.common.vo.IngestMessageVO;
import kr.re.keti.sc.dataservicebroker.common.vo.QueryVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelCacheVO;

/**
 * Entity 공통 서비스 인터페이스
 * @param <T1> Entity 별 FullVO
 * @param <T2> Entity 별 DaoVO
 */
public interface EntitySVCInterface<T1 extends CommonEntityFullVO, T2 extends CommonEntityDaoVO> {

	/**
     * Entity 데이터 Operation 별 벌크 처리
     * @param requestMessageVOList 요청수신메시지VO리스트
     * @return 처리내역 및 결과포함VO리스트
     */
    public List<EntityProcessVO<T1, T2>> processBulk(List<IngestMessageVO> requestMessageVOList);

	/**
     * 수신받은 content 기반으로 생성된 FullVO 를 DB용 daoVO로 파싱
     * @param CommonEntityFullVO 수신받은 content 기반으로 생성된 FullVO
     * @param DataModelCacheVO Entity파싱에 사용될 Schema 정보
     * @return EntityDaoVO DB용 daoVO
     * @throws BaseException 파싱도중 발생 예외처리
     */
	public T2 fullVOToDaoVO(T1 fullVO, DataModelCacheVO dataModelCacheVO, Operation operation) throws BaseException;

	/**
	 * DB조회 결과인 daoVO 를 FullVO 규격으로 변환 (* representation)
	 * @param daoVO (EntityDaoVO)
	 * @return CommonEntityVO (EntityFullVO)
	 */
	public CommonEntityVO daoVOToFullRepresentationVO(T2 daoVO, DataModelCacheVO dataModelCacheVO, boolean includeSysAttrs, List<String> attrs);

	/**
     * DB조회 결과인 daoVO 를 SimplifiedRepresentationVO 로 변환 (* options=KeyValues)
     * @param daoVO (EntityDaoVO)
     * @return CommonEntityVO (EntitySimpleVO)
     */
	public CommonEntityVO daoVOToSimpleRepresentationVO(T2 daoVO, DataModelCacheVO dataModelCacheVO, List<String> attrs);

	/**
     * DB조회 결과인 daoVO 를 temporal 기본 규격으로 변환
     * @param daoVOList (EntityDaoVO List)
     * @return List<CommonEntityVO> (EntityTemporalFullVO List)
     */
	public List<CommonEntityVO> daoVOToTemporalFullRepresentationVO(List<T2> daoVOList, DataModelCacheVO dataModelCacheVO, Integer lastN, String accept);



	/**
	 * DB조회 결과인 daoVO 를 temporalValues 규격으로 변환
	 * @param commonEntityVOList
	 * @return List<CommonEntityVO> (EntityTemporalFullVO List)
	 */
	public List<CommonEntityVO> daoVOToTemporalTemporalValuesRepresentationVO(List<CommonEntityVO> commonEntityVOList);


	/**
	 * DB조회 결과인 daoVO 를 NormalizedHistory 규격으로 변환
	 * @param entityDaoVOList (EntityDaoVO List)
	 * @param dataModelCacheVO
	 * @return List<CommonEntityVO> (EntityTemporalFullVO List)
	 */
	public List<CommonEntityVO> daoVOToTemporalNormalizedHistoryRepresentationVO(List<T2> entityDaoVOList, DataModelCacheVO dataModelCacheVO, String accept);



	/**
     * 요청수신 VO -> 실제 서비스 처리 VO 파싱
     * @param requestMessageVOList 요청 수신 VO
     * @return List<OffStreetParkingProcessVO> 서비스 처리 VO 리스트
     */
	public List<EntityProcessVO<T1, T2>> requestMessageVOToProcessVO(List<IngestMessageVO> requestMessageVOList);

	/**
     * 파라미터 유효성 및 권한 체크
     * @param processVOList 서비스 처리 VO 리스트
     */
	public void processValidate(List<EntityProcessVO<T1, T2>> processVOList);

	/**
     * Operation 별 묶음 처리
     * @param entityProcessVOList 처리VO리스트
     */
	public void processOperation(List<EntityProcessVO<T1, T2>> entityProcessVOList);

	/**
     * Operation 정상처리 항목 이력 저장
     * @param entityProcessVOList 처리VO리스트
     */
	public void storeEntityStatusHistory(List<EntityProcessVO<T1, T2>> entityProcessVOList);

	/**
     * Entity 최종데이터 다건 조회
     * @param queryVO 요청 파라미터
	 * @param accept
     * @return Entity 최종데이터리스트
     */
	public List<CommonEntityVO> selectAll(QueryVO queryVO, String accept);
	
	/**
	 * Entity 최종데이터 건수 조회
	 * @param queryVO 요청 파라미터
	 * @return entity 최종데이터 건수
	 */
	public Integer selectCount(QueryVO queryVO);


	/**
     * Entity 최종데이터 단건 조회
     * @param queryVO 요청 파라미터
     * @param accept
     * @return
     */
	public CommonEntityVO selectById(QueryVO queryVO, String accept, Boolean useForCreateOperation);

	/**
     * 이력데이터 단건 조회
     * @param queryVO 요청 파라미터
     * @return Entity Operation 처리 이력 데이터
     */
	public CommonEntityVO selectTemporalById(QueryVO queryVO, String accept);

    /**
     * 이력데이터 리스트 조회
     * @param queryVO 요청 파라미터
     * @return Entity Operation 처리 이력데이터 리스트
     */
	public List<CommonEntityVO> selectTemporal(QueryVO queryVO, String accept);
	
	/**
	 * 이력데이터 건수 조회
	 * @param queryVO 요청 파라미터
	 * @return entity 이력 건수
	 */
	public Integer selectTemporalCount(QueryVO queryVO);

	
}