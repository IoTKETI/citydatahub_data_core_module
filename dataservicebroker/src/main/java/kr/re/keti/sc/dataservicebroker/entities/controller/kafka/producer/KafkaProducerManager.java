package kr.re.keti.sc.dataservicebroker.entities.controller.kafka.producer;

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import kr.re.keti.sc.dataservicebroker.common.bulk.IBulkChannel;
import kr.re.keti.sc.dataservicebroker.common.bulk.IBulkChannelManager;
import kr.re.keti.sc.dataservicebroker.common.bulk.MemoryQueueBulkChannel;
import kr.re.keti.sc.dataservicebroker.common.bulk.MemoryQueueBulkChannel.ExecuteMode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.UseYn;
import kr.re.keti.sc.dataservicebroker.common.exception.CoreException;
import kr.re.keti.sc.dataservicebroker.entities.controller.kafka.producer.processor.KafkaProduceProcessor;
import kr.re.keti.sc.dataservicebroker.entities.controller.kafka.producer.vo.KafkaProcessVO;

@Component
public class KafkaProducerManager implements IBulkChannelManager<KafkaProcessVO> {

	@Value("${kafka.url}")
	private String kafkaUrl; // 카프카 접속 URL
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

	@Value("${kafka.notification.producer.client.id}")
	private String kafkaProducerClientId; // Client Id
	@Value("${kafka.notification.producer.acks.config}")
	private String kafkaProducerAcksConfig; // Acks Config
	@Value("${kafka.notification.producer.processor.thread.count:10}")
	private Integer bulkProcessorCount; // 벌크처리 프로세서 쓰레드 수 (default : 10)
	@Value("${kafka.notification.producer.queue.size:100000}")
	private Integer bulkQueueSize; // 벌크처리 큐 크기 (default : 100000)

	/** KAFKA 전송 프로듀서 */
	private IKafkaProducer kafkaProducer;
	@Autowired
	@Qualifier("kafkaProduceProcessor")
	private KafkaProduceProcessor kafkaProduceProcessor;
	/** 벌크처리 채널 */
	private IBulkChannel<KafkaProcessVO> bulkChannel;
	
	@Override
	@PostConstruct
	public void init() {

		// 1. Full Topic 용 Event Notification Producer 초기화
		initProducer();

        // 2. KafkaNotificationProcessor 초기화
        kafkaProduceProcessor.setKafkaProducer(kafkaProducer);

		// 3. 비동기 Queue 처리채널 초기화
		bulkChannel = new MemoryQueueBulkChannel<KafkaProcessVO>("KafkaProduceBulkChannel", kafkaProduceProcessor, 
				ExecuteMode.ASYNC_SINGLE, bulkProcessorCount, bulkQueueSize, null);
		bulkChannel.start();
	}

	@Override
	public void produceData(KafkaProcessVO message) throws CoreException {
		if(bulkChannel != null) {
			bulkChannel.produceData(message, false);
		}
	}

	@Override
	@PreDestroy
	public void destroy() {
		if(kafkaProducer != null) {
			kafkaProducer.destroy();
		}

		if(bulkChannel != null) {
			bulkChannel.destroy();
		}
	}

	private void initProducer() {

		Properties kafkaProperties = new Properties();

		if(UseYn.YES.getCode().equals(kafkaSecurityYn)) {
			String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
			String jaasCfg = String.format(jaasTemplate, kafkaUsername, kafkaPassword); // 사용자 계정정보

			kafkaProperties.put(StreamsConfig.SECURITY_PROTOCOL_CONFIG, kafkaSecurityProcol); // SASL_PLAINTEXT
			kafkaProperties.put(SaslConfigs.SASL_MECHANISM, kafkaSaslMechanism); // PLAIN
			kafkaProperties.put(SaslConfigs.SASL_JAAS_CONFIG, jaasCfg); // 보안 설정
		}

		kafkaProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUrl);
		kafkaProperties.put(ProducerConfig.CLIENT_ID_CONFIG, kafkaProducerClientId);
		kafkaProperties.put(ProducerConfig.ACKS_CONFIG, kafkaProducerAcksConfig);
		kafkaProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
		kafkaProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());

		kafkaProducer = new NotificationKafkaProducer();
		kafkaProducer.init("notificationProducer", kafkaProperties);
	}
}