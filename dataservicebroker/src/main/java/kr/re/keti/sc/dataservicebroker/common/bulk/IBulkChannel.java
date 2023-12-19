package kr.re.keti.sc.dataservicebroker.common.bulk;

import org.slf4j.Logger;

/**
 * 벌크처리를 위한 채널 구현 인터페이스
 * @param <T> 벌크처리 대상 Object Type
 */
public interface IBulkChannel<T> extends IBulkProducer<T> {
	/**
	 * 벌크채널 초기화
	 */
	public void init();

	/**
	 * 벌크 채널 기동 
	 */
	public void start();

	/**
	 * 벌크 채널 중지
	 */
	public void stop();

	/**
	 * 벌크 채널 종료
	 */
	public void destroy();

	/**
	 *  남은 데이터 클리어
	 * @param processRemainingData 잔여데이터 처리 여부 후 클리어 여부
	 */
	public void clear(boolean processRemainingData);

	/**
	 *  남은 데이터 클리어
	 * @param processRemainingData 잔여데이터 처리 여부 후 클리어 여부
	 * @param logger 잔여데이터 처리 시 사용할 logger
	 */
	public void clear(boolean processRemainingData, Logger logger);
}