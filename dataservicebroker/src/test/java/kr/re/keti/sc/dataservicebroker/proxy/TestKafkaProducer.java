package kr.re.keti.sc.dataservicebroker.proxy;

import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import kr.re.keti.sc.dataservicebroker.util.ConvertUtil;

@Component
public class TestKafkaProducer {

	@Value("${kafka.url:localhost:9092}")
	private String kafkaUrl; // 카프카 접속 URL

	@Value("${kafka.message.version:1.0}")
	private String kafkaMessageVersionStr; // 카프카 메시지 버전

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

	@Value("${kafka.ingest.request.topic:STRUCTURED_DATA_INGEST_REQUEST}")
	private String ingestRequestTopic; // INGEST_REQUEST 토픽

	private KafkaProducer<String, byte[]> producer = null;

	@PostConstruct
	public void init() {
		Properties kafkaProperties = new Properties(); 

		if("Y".equals(kafkaSecurityYn)) {
			String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
			String jaasCfg = String.format(jaasTemplate, kafkaUsername, kafkaPassword); // 사용자 계정정보

			kafkaProperties.put(StreamsConfig.SECURITY_PROTOCOL_CONFIG, kafkaSecurityProcol); // SASL_PLAINTEXT
			kafkaProperties.put(SaslConfigs.SASL_MECHANISM, kafkaSaslMechanism); // PLAIN
			kafkaProperties.put(SaslConfigs.SASL_JAAS_CONFIG, jaasCfg); // 보안 설정
		}

		kafkaProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUrl);
		kafkaProperties.put(ProducerConfig.CLIENT_ID_CONFIG, "testProducer");
		kafkaProperties.put(ProducerConfig.ACKS_CONFIG, "1");
		kafkaProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
		kafkaProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
		
		this.producer = new KafkaProducer<>(kafkaProperties);
	}

	public void send(byte[] messageBytes) {
		byte[] test = new byte[messageBytes.length + 5];
		test[0] = 0x00;
		System.arraycopy(ConvertUtil.intTobytes(messageBytes.length), 0, test, 1, 4);

		System.arraycopy(messageBytes, 0, test, 5, messageBytes.length);
		producer.send(new ProducerRecord<>(ingestRequestTopic, test));
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
