package kr.re.keti.sc.ingestinterface.controller.kafka.producer.processor;

import java.nio.ByteBuffer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.ingestinterface.common.bulk.IBulkProcessor;
import kr.re.keti.sc.ingestinterface.common.code.Constants;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.ErrorCode;
import kr.re.keti.sc.ingestinterface.common.exception.CoreException;
import kr.re.keti.sc.ingestinterface.controller.kafka.producer.IKafkaProducer;
import kr.re.keti.sc.ingestinterface.controller.kafka.producer.vo.KafkaProcessVO;
import lombok.extern.slf4j.Slf4j;

/**
 * 카프카 Producing 처리 클래스
 */
@Component
@Slf4j
public class KafkaProduceProcessor implements IBulkProcessor<KafkaProcessVO> {

	@Autowired
	private ObjectMapper objectMapper;
	
	@Value("${kafka.message.version:1.0}")
	private String kafkaMessageVersionStr; // 카프카 메시지 버전

	/** 에러데이터 기록용 로거  */
	private final Logger errorDataLogger = LoggerFactory.getLogger(Constants.KAFKA_REQUEST_ERROR_LOGGER_NAME);

	/** INGEST_EVENT 전송 프로듀서 */
	private IKafkaProducer ingestEventProducer;

	/** 디폴트 카프카 메시지 버전 1.0 (메인버전) */
	private final byte DEFAULT_KAFKA_MESSAGE_MAIN_VERSION = 0X01;
	/** 디폴트 카프카 메시지 버전 1.0 (서브버전) */
	private final byte DEFAULT_KAFKA_MESSAGE_SUB_VERSION = 0X00;
	/** 카프카 메시지 버전 메인버전 서브버전 구분자 */
	private final String KAFKA_MESSAGE_VERSION_SEPARATOR = ".";
	/** 카프카 메시지 버전 */
	private byte kafkaMessageVersion;

	public KafkaProduceProcessor() {
		// 카프카 메시지 버전 설정
		setKafkaMessageVersion();
	}

	@Override
	public Object processSingle(KafkaProcessVO kafkaProcessVO) {

		produceToTopic(kafkaProcessVO);

		return null;
	}

	/**
	 * 카프카메시지 버전 설정
	 *  - 프로퍼티 값으로 설정
	 *  - 파싱 에러 시 디폴트 값으로 설정 (1.0)
	 */
	private void setKafkaMessageVersion() {
		try {
			if(kafkaMessageVersionStr.contains(KAFKA_MESSAGE_VERSION_SEPARATOR)) {
				String[] messageVersionArr = kafkaMessageVersionStr.split(KAFKA_MESSAGE_VERSION_SEPARATOR);
				byte mainVersion = messageVersionArr[0].getBytes()[0];
				byte subVersion = messageVersionArr[1].getBytes()[0];

				kafkaMessageVersion = ((byte) ((mainVersion<<4) | subVersion));
			}
		} catch(Exception e) {
			log.warn("Kafka Message Version Parsing Fail. Properties kafka.message.version=" + kafkaMessageVersionStr);
			kafkaMessageVersion = ((byte) ((DEFAULT_KAFKA_MESSAGE_MAIN_VERSION<<4) | DEFAULT_KAFKA_MESSAGE_SUB_VERSION));
		}
	}

	/**
	 * Ingest Event Kafka 전송
	 * @param ingestSuccessEventVO INGEST_SUCCESS_REQUEST 전송메시지VO
	 */
	private void produceToTopic(KafkaProcessVO kafkaProcessVO) {
		
		// 1. 데이터 파싱
		String ingestSuccessEventMessage = null;
		try {
			ingestSuccessEventMessage = objectMapper.writeValueAsString(kafkaProcessVO.getIngestEventKafkaMessageVO());
		} catch (JsonProcessingException e) {
			String errorMessage = "IngestEvent Json Parsing ERROR. requestMessageVO=" + kafkaProcessVO.toString();
			log.warn(errorMessage, e);
			errorDataLogger.warn(errorMessage, e);
			return;
		}

		// 2. header 생성 및 바이트로 변환
		byte[] sendMessageBytes = createSendMessage(ingestSuccessEventMessage.getBytes());

		// 3. 데이터 전송
		try {
			ingestEventProducer.send(kafkaProcessVO.getTopic(), sendMessageBytes);
		} catch (Exception e) {
			String errorMessage = "IngestSuccessEvent Producing ERROR. message=" + new String(sendMessageBytes);
			log.warn(errorMessage, e);
			errorDataLogger.warn(errorMessage, e);
		}
	}

	/**
	 * 헤더를 추가하여 전송할 메시지 생성
	 * @param bodyMessage 바디메시지
	 * @return 전송할 메시지
	 */
	private byte[] createSendMessage(byte[] bodyMessage) {

		ByteBuffer byteBuffer = ByteBuffer.allocate(1 + bodyMessage.length);
		byteBuffer.put(kafkaMessageVersion);
		byteBuffer.put(bodyMessage);
		
		return byteBuffer.array();
	}

	@Override
	public Object processBulk(List<KafkaProcessVO> kafkaProcessVOList) {
		throw new CoreException(ErrorCode.NOT_SUPPORTED_METHOD, "KafkaNotificationProcessor not support processBulk.");
	}

	@Override
	public Object processBulk(List<KafkaProcessVO> objects, IngestInterfaceCode.Operation operation) {
		return null;
	}

	public void setIngestEventProducer(IKafkaProducer ingestSuccessEventFullProducer) {
		this.ingestEventProducer = ingestSuccessEventFullProducer;
	}
}
