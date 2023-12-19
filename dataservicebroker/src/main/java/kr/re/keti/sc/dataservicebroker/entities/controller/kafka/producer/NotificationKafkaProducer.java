package kr.re.keti.sc.dataservicebroker.entities.controller.kafka.producer;

import java.util.Properties;

public class NotificationKafkaProducer extends DefaultKafkaProducer {

	@Override
	public void init(String name, Properties kafkaProperties) {

		super.init(name, kafkaProperties);
	}
}