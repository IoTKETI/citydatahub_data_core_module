package kr.re.keti.sc.dataservicebroker.common.bulk;

import kr.re.keti.sc.dataservicebroker.common.exception.CoreException;

/**
 * 벌크처리 채널 매니저 인터페이스
 *  - 다수의 벌크처리 채널을 등록하고 관리하기 위한 인터페이스
 * @param <T> 벌크처리 대상 Object Type
 */
public interface IBulkChannelManager<T> {

	/**
	 * 벌크채널매니저 초기화
	 */
	public void init();

	/**
	 * 벌크처리할 객체 라우팅 처리
	 *  - 벌크처리 대상 객체 처리
	 * @param message 벌크처리객체
	 * @throws CoreException 벌크처리 라우팅 시 발생하는 에러
	 */
	public void produceData(T message) throws CoreException;

	/**
	 * 벌크채널매니저 종료
	 */
	public void destroy();
}
