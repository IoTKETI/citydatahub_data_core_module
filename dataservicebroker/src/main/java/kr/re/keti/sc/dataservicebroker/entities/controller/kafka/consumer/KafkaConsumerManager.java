package kr.re.keti.sc.dataservicebroker.entities.controller.kafka.consumer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import kr.re.keti.sc.dataservicebroker.common.bulk.IBulkChannelManager;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.UseYn;
import kr.re.keti.sc.dataservicebroker.common.vo.IngestMessageVO;

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

	@Value("${kafka.ingest.request.consumer.threadcount:1}")
	private int ingestRequestThreadCount; // 컨슈머 쓰레드 수 (Default 1)

	@Value("${kafka.ingest.request.consumer.group.id}")
	private String ingestRequestGroupId; // 카프카 컨슈머 그룹아이디
	
	@Value("${kafka.ingest.request.consumer.enable.auto.commit}")
	private String ingestRequestEnableAutoCommit; // 카프카 옵션
	
	@Value("${kafka.ingest.request.consumer.auto.offset.reset.config}")
	private String ingestRequestAutoOffsetResetConfig; // 카프카 옵션

	@Value("${kafka.ingest.request.consumer.poll.duration.millis:1000}")
	private int ingestRequestPollDurationMillis; // 카프카 옵션

	@Autowired
	private IBulkChannelManager<IngestMessageVO> bulkChannelManager;
	@Autowired
	private ObjectMapper objectMapper;

	/** INGEST_REQUEST 카프카 컨슈머 맵 */
	private Map<String, IKafkaConsumer> ingestRequestConsumerMap = new HashMap<>();

	public void registKafkaConsumer(String datasetId) {
		IKafkaConsumer kafkaConsumer = ingestRequestConsumerMap.get(datasetId);
		if(kafkaConsumer == null) {
			kafkaConsumer = createKafkaConsumer(datasetId);
			ingestRequestConsumerMap.put(datasetId, kafkaConsumer);
			kafkaConsumer.start();
		}
	}

	public void deregistKafkaConsumer(String datasetId) {
		IKafkaConsumer kafkaConsumer = ingestRequestConsumerMap.remove(datasetId);
		if(kafkaConsumer != null) {
			kafkaConsumer.stop();
		}
	}

	public boolean isRunKafkaConsumer(String datasetId) {
		if(ingestRequestConsumerMap.get(datasetId) != null) {
			return true;
		}
		return false;
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
		kafkaProperties.put(ConsumerConfig.GROUP_ID_CONFIG, ingestRequestGroupId);  // 컨슈머 그룹 ID 기입
		kafkaProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, ingestRequestEnableAutoCommit);
		kafkaProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, ingestRequestAutoOffsetResetConfig);
		kafkaProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class.getName());
		kafkaProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());

		IKafkaConsumer kafkaConsumer = new IngestRequestKafkaConsumer(bulkChannelManager, objectMapper);
		kafkaConsumer.init(topic + "-Consumer", kafkaProperties, topic, ingestRequestThreadCount, ingestRequestPollDurationMillis);
		return kafkaConsumer;
	}
}
