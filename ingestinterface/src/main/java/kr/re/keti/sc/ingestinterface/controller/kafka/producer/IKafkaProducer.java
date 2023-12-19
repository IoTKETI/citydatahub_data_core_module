package kr.re.keti.sc.ingestinterface.controller.kafka.producer;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kafka procuder interface
 */
public interface IKafkaProducer {

	/**
	 * 카프카프로듀서 초기화
	 * @param name 카프카프로듀서명
	 * @param kafkaProperties 카프카프로듀서 설정
	 */
	public void init(String name, Properties kafkaProperties);

	/**
	 * 카프카 프로듀스 데이터 전송
	 * @param topic 전송대상topic
	 * @param sendData 전송데이터
	 */
	public void send(String topic, byte[] sendData);

	/**
	 * 카프카프로듀서 종료
	 */
	public void destroy();
}
