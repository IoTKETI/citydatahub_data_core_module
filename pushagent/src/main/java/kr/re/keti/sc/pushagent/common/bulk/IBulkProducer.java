package kr.re.keti.sc.pushagent.common.bulk;

import kr.re.keti.sc.pushagent.common.exception.CoreException;

/**
 * 벌크처리를 위한 Object 묶음 구현 인터페이스
 * @param <T> 벌크처리 대상 Object Type
 */
public interface IBulkProducer<T> {
	/**
	 * 특정 타입의 object를 produce
	 * @param object produce object
	 * @param doWait queue가 full 인 경우 대기여부
	 * @return boolean produce 결과
	 */
	public boolean produceData(T object, Boolean doWait) throws CoreException;
}