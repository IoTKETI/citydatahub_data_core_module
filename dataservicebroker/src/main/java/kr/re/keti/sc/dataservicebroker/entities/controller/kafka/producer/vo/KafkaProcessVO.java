package kr.re.keti.sc.dataservicebroker.entities.controller.kafka.producer.vo;

import lombok.Data;

@Data
public class KafkaProcessVO {
	private String topic;
	private NotificationReqeustVO notificationReqeustVO;
}
