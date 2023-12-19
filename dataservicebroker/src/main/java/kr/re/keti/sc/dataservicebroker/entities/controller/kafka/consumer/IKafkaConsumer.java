package kr.re.keti.sc.dataservicebroker.entities.controller.kafka.consumer;

import java.util.Properties;

public interface IKafkaConsumer {

	/**
	 * 카프카 컨슈머 초기화
	 * @param name 카프카컨슈머명
	 * @param kafkaProperties 카프카컨슈머 설정
	 * @param topic 카프카컨슈머 토픽
	 * @param consumerThreadCount 카프카컨슈머 쓰레드 수
	 * @param pollDurationMillis 카프카컨슈머 폴링 주기
	 */
	public void init(String name, Properties kafkaProperties, String topic, Integer consumerThreadCount, Integer pollDurationMillis);

	/**
	 * 카프카 컨슈머 시작
	 */
	public void start();

	/**
	 * 카프카 컨슈머 중지
	 */
	public void stop();

	/**
	 * 카프카 컨슈머 종료
	 */
	public void destroy();
}
