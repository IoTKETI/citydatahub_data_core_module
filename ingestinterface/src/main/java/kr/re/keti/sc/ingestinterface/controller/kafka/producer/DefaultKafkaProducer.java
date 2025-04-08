package kr.re.keti.sc.ingestinterface.controller.kafka.producer;


import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import lombok.extern.slf4j.Slf4j;

/**
 * kafka producer 구현 클래스
 */
@Slf4j
public class DefaultKafkaProducer implements IKafkaProducer {

	/** 카프카프로듀서명 */
	private String name;
	/** 카프카프로듀서 클라이언트 */
	private KafkaProducer<Integer, byte[]> producer;

	@Override
	public void init(String name, Properties kafkaProperties) {
		this.name = name;
		this.producer = new KafkaProducer<>(kafkaProperties);
	}

	@Override
	public void send(String topic, byte[] message) {

		int key = UUID.randomUUID().hashCode();
		log.info("KafkaProduce topic=" + topic + ", Key=" + key +", Message=" + new String(message));

		//데이터 처리 순서 보장을 위하여 Kafka Key 값 활용하여 하나의 Partition으로 전송
		producer.send(new ProducerRecord<>(topic, key, message));
	}

	@Override
	public void destroy() {
		producer.close();
	}
}
