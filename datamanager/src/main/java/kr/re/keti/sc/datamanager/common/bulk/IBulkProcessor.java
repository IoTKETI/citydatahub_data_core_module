package kr.re.keti.sc.datamanager.common.bulk;

import java.util.List;

/**
 * 벌크로 묶인 데이터 처리 구현 인터페이스
 * @param <T> 벌크처리 대상 Object Type
 */
public interface IBulkProcessor<T> {	

	/**
	 * 벌크묶음데이터 벌크처리
	 * @param objects 벌크묶음데이터
	 */
	public Object processBulk(List<T> objects);

	/**
	 * 단건 처리
	 * @param object
	 */
	public Object processSingle(T object);
}
