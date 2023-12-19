package kr.re.keti.sc.pushagent.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.re.keti.sc.pushagent.common.bulk.IBulkChannelManager;
import kr.re.keti.sc.pushagent.common.code.DataServiceBrokerCode.UseYn;
import kr.re.keti.sc.pushagent.notification.vo.NotificationReqeustVO;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Properties;


@Component
public class KafkaConsumerManager {

	@Value("${kafka.url}")
	public String kafkaUrl; // 카프카 접속 URL
	@Value("${kafka.message.version}")
	private String kafkaMessageVersion; // 카프카 메시지 버전
	@Value("${kafka.security.yn:N}")
	private String kafkaSecurityYn; // 카프카 security 사용 여부 (Default N)
	@Value("${kafka.security.protocol}")
	private String kafkaSecurityProcol; // 카프카 security 옵션
	@Value("${kafka.sasl.mechanism}")
	private String kafkaSaslMechanism; // 카프카 security 옵션
	@Value("${kafka.username}")
	private String kafkaUsername; // 카프카 유저명
	@Value("${kafka.password}")
	private String kafkaPassword; // 카프카 유저 패스워드

	@Value("${kafka.notification.topic:NOTIFICATION_REQUEST}")
	private String consumerTopic; // 컨슈머 쓰레드 수 (Default 1)

	@Value("${kafka.notification.consumer.threadcount:1}")
	private int consumerThreadCount; // 컨슈머 쓰레드 수 (Default 1)

	@Value("${kafka.notification.consumer.group.id}")
	private String consumerGroupId; // 카프카 컨슈머 그룹아이디
	
	@Value("${kafka.notification.consumer.enable.auto.commit}")
	private String consumerEnableAutoCommit; // 카프카 옵션
	
	@Value("${kafka.notification.consumer.auto.offset.reset.config}")
	private String consumerAutoOffsetResetConfig; // 카프카 옵션

	@Value("${kafka.notification.consumer.poll.duration.millis:1000}")
	private int consumerPollDurationMillis; // 카프카 옵션

	@Autowired
	private IBulkChannelManager<NotificationReqeustVO> bulkChannelManager;
	@Autowired
	private ObjectMapper objectMapper;

	private IKafkaConsumer kafkaConsumer;

	@PostConstruct
	public void registKafkaConsumer() {
		this.kafkaConsumer = createKafkaConsumer(consumerTopic);
		kafkaConsumer.start();
	}

	@PreDestroy
	public void deregistKafkaConsumer() {
		if(kafkaConsumer != null) {
			kafkaConsumer.stop();
		}
	}

	private IKafkaConsumer createKafkaConsumer(String topic) {
		// 카프카 컨슈머 초기화
		Properties kafkaProperties = new Properties();

		if(UseYn.YES.getCode().equals(kafkaSecurityYn)) {
			String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
			String jaasCfg = String.format(jaasTemplate, kafkaUsername, kafkaPassword); // 사용자 계정정보

			kafkaProperties.put(StreamsConfig.SECURITY_PROTOCOL_CONFIG, kafkaSecurityProcol); // SASL_PLAINTEXT
			kafkaProperties.put(SaslConfigs.SASL_MECHANISM, kafkaSaslMechanism); // PLAIN
			kafkaProperties.put(SaslConfigs.SASL_JAAS_CONFIG, jaasCfg); // 보안 설정
		}

		kafkaProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUrl);
		kafkaProperties.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);  // 컨슈머 그룹 ID 기입
		kafkaProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, consumerEnableAutoCommit);
		kafkaProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerAutoOffsetResetConfig);
		kafkaProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class.getName());
		kafkaProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());

		IKafkaConsumer kafkaConsumer = new NotificationRequestKafkaConsumer(bulkChannelManager, objectMapper);
		kafkaConsumer.init(topic + "-Consumer", kafkaProperties, topic, consumerThreadCount, consumerPollDurationMillis);
		return kafkaConsumer;
	}
}
