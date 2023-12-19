package kr.re.keti.sc.ingestinterface.ingest.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.ingestinterface.common.bulk.IBulkProcessor;
import kr.re.keti.sc.ingestinterface.common.code.Constants;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.ErrorCode;
import kr.re.keti.sc.ingestinterface.common.exception.CoreException;
import kr.re.keti.sc.ingestinterface.common.vo.CommonEntityFullVO;
import kr.re.keti.sc.ingestinterface.common.vo.EntityProcessVO;
import kr.re.keti.sc.ingestinterface.common.vo.RequestMessageVO;
import kr.re.keti.sc.ingestinterface.controller.kafka.producer.KafkaProducerManager;
import kr.re.keti.sc.ingestinterface.controller.kafka.producer.vo.IngestEventKafkaMessageVO;
import kr.re.keti.sc.ingestinterface.controller.kafka.producer.vo.KafkaProcessVO;
import kr.re.keti.sc.ingestinterface.verificationhistory.service.VerificationHistorySVC;
import lombok.extern.slf4j.Slf4j;

/**
 * Entity operation bulk processor class
 * @param <T> processing class type
 */
@Component
@Slf4j
public class IngestBulkProcessor<T extends CommonEntityFullVO> implements IBulkProcessor<RequestMessageVO> {

	/** Operation 처리 서비스 */
	@Autowired
	@Qualifier("ingestInterfaceSVC")
	private IngestProcessorInterface<T> ingestInterfaceSVC;
	/** Kafka Produce Manager */
	@Autowired
	private KafkaProducerManager kafkaProducerManager;
	@Autowired
	private VerificationHistorySVC<T> verificationHistorySVC;
	@Autowired
	private ObjectMapper objectMapper;

	/** 에러데이터 기록용 로거 */
	private final Logger errorDataLogger = LoggerFactory.getLogger(Constants.KAFKA_REQUEST_ERROR_LOGGER_NAME);

	@Override
	public Object processBulk(List<RequestMessageVO> requestMessageVOList, IngestInterfaceCode.Operation operation) {

		// 1. Operation 별 벌크 처리
		List<EntityProcessVO<T>> processVOList = ingestInterfaceSVC.processBulk(requestMessageVOList, operation);

		// 2. 품질 체크 이력 저장
		verificationHistorySVC.storeQualityHistory(processVOList);

		// 3. 처리 이벤트 카프카 전송
        sendKafkaEvent(processVOList);

		return processVOList;
	}

	/**
	 * 처리 이벤트 카프카 전달
	 * @param kafkaSendVOList 카프카전달 대상 VO리스트
	 */
	private void sendKafkaEvent(List<EntityProcessVO<T>> entityProcessVOList) {

		for (EntityProcessVO<T> entityProcessVO : entityProcessVOList) {

			// 처리결과가 true인 경우 성공 메시지 Produce
			if (entityProcessVO.getProcessResultVO().isProcessResult()) {
				try {
					IngestEventKafkaMessageVO ingestEventKafkaMessageVO = new IngestEventKafkaMessageVO();
					ingestEventKafkaMessageVO.setOperation(entityProcessVO.getOperation());
					ingestEventKafkaMessageVO.setContentType("application/ld+json");
					ingestEventKafkaMessageVO.setIngestTime(entityProcessVO.getIngestTime());
					ingestEventKafkaMessageVO.setContent(objectMapper.readValue(entityProcessVO.getContent(), CommonEntityFullVO.class));

					KafkaProcessVO kafkaProcessVO = new KafkaProcessVO();
					kafkaProcessVO.setTopic(entityProcessVO.getDatasetId());
					kafkaProcessVO.setIngestEventKafkaMessageVO(ingestEventKafkaMessageVO);
					kafkaProducerManager.produceData(kafkaProcessVO);
				} catch(Exception e) {
					log.error("Kafka produce error. datasetId=" + entityProcessVO.getDatasetId() + ", content=" + entityProcessVO.getContent(), e);
					errorDataLogger.error("Kafka produce error. datasetId=" + entityProcessVO.getDatasetId() + ", content=" + entityProcessVO.getContent(), e);
				}
			}
		}
	}


	@Override
	public Object processBulk(List<RequestMessageVO> objects) {
		return null;
	}

	@Override
	public Object processSingle(RequestMessageVO object) {
		throw new CoreException(ErrorCode.NOT_SUPPORTED_METHOD, "EntityBulkProcessor not support processSingle.");
	}
}
