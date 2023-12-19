package kr.re.keti.sc.ingestinterface.ingest.service;

import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode;
import kr.re.keti.sc.ingestinterface.common.exception.BaseException;
import kr.re.keti.sc.ingestinterface.common.vo.CommonEntityFullVO;
import kr.re.keti.sc.ingestinterface.common.vo.EntityProcessVO;
import kr.re.keti.sc.ingestinterface.common.vo.RequestMessageVO;
import kr.re.keti.sc.ingestinterface.datamodel.vo.DataModelCacheVO;

import java.util.List;

/**
 * Entity 공통 서비스 인터페이스
 * @param <T> Entity 별 FullVO
 */
public interface IngestProcessorInterface<T extends CommonEntityFullVO> {

	/**
	 * Entity 데이터 Operation 별 벌크 처리
	 * @param requestMessageVOList 요청수신메시지VO리스트
	 * @return 처리내역 및 결과포함VO리스트
	 */
	public List<EntityProcessVO<T>> processBulk(List<RequestMessageVO> requestMessageVOList, IngestInterfaceCode.Operation operation);

	/**
     * 수신받은 Content 를 T1 객체로 Deserialize 처리
     * @param content 수신받은 content
     * @return T1 객체
     */
    public T deserializeContent(String content) throws BaseException;

	/**
	 * 수신받은 content 기반으로 생성된 FullVO 를 DB용 daoVO로 파싱
	 * @param fullVO 수신받은 content 기반으로 생성된 FullVO
	 * @param dataModelCacheVO Entity파싱에 사용될 Schema 정보
	 * @param operation entityOperation
	 * @throws BaseException 파싱도중 발생 예외처리
	 */
	public void verification(T fullVO, DataModelCacheVO dataModelCacheVO, IngestInterfaceCode.Operation operation) throws BaseException;


	/**
	 * 요청수신 VO를 실제 서비스 처리 VO로 파싱
	 * @param requestMessageVOList 요청 수신 VO
	 * @return List EntityProcessVO 서비스 처리 VO 리스트
	 */
	public List<EntityProcessVO<T>> requestMessageVOToProcessVO(List<RequestMessageVO> requestMessageVOList);


}