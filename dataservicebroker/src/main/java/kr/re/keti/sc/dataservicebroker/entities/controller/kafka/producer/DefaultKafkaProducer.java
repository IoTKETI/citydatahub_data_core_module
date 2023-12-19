package kr.re.keti.sc.dataservicebroker.entities.controller.kafka.producer;


import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class DefaultKafkaProducer implements IKafkaProducer {

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

		log.info("KafkaProduce topic=" + topic + ", Message=" + new String(message));

		producer.send(new ProducerRecord<>(topic, message));
	}

	@Override
	public void destroy() {
		producer.close();
	}
}
