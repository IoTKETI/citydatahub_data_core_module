package kr.re.keti.sc.dataservicebroker.entities.controller.kafka.consumer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Properties;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.dataservicebroker.common.bulk.IBulkChannelManager;
import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.dataservicebroker.common.exception.BaseException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdBadRequestException;
import kr.re.keti.sc.dataservicebroker.common.vo.IngestMessageVO;
import kr.re.keti.sc.dataservicebroker.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IngestRequestKafkaConsumer extends DefaultKafkaConsumer {

	/** 카프카메시지 버전 파라미터 길이 */
	private int MESSAGE_VERSION_LENGTH = 1;
	/** 카프카메시지 바디 시작위치 */
	private int MESSAGE_BODY_START_INDEX = MESSAGE_VERSION_LENGTH;
	/** Json parser */
	private ObjectMapper objectMapper;

	/** 벌크처리매니저 */
	private IBulkChannelManager<IngestMessageVO> bulkChannelManager;

	public IngestRequestKafkaConsumer(IBulkChannelManager<IngestMessageVO> bulkChannelManager, ObjectMapper objectMapper) {
		this.bulkChannelManager = bulkChannelManager;
		this.objectMapper = objectMapper;
	}

	@Override
	public void init(String name, Properties kafkaProperties, String topic, Integer consumerThreadCount, Integer pollDurationMillis) {
		super.init(name, kafkaProperties, topic, consumerThreadCount, pollDurationMillis);
	}

	@Override
	public void processReceiveData(byte[] receivedData) throws BaseException {
		
		// 1. Message Version 추출
		byte[] messageVersion = new byte[1];
		System.arraycopy(receivedData, 0, messageVersion, 0, MESSAGE_VERSION_LENGTH);

		// 2. Header, Body 추출
		byte[] bodyBytes = new byte[receivedData.length - MESSAGE_BODY_START_INDEX];
		System.arraycopy(receivedData, MESSAGE_BODY_START_INDEX, bodyBytes, 0, receivedData.length - MESSAGE_BODY_START_INDEX);

		log.info("processReceiveData. Message={}", new String(bodyBytes));

		// body 파싱
		IngestMessageVO ingestMessageVO = bodyToIngestMessageVO(bodyBytes);

		// 벌크처리
		bulkChannelManager.produceData(ingestMessageVO);
	}

	/**
	 * Body 파싱
	 * @param bodyBytes Body
	 * @return RequestMessageVO 요청메시지VO
	 */
	private IngestMessageVO bodyToIngestMessageVO(byte[] bodyBytes) throws BaseException {
		try {
			IngestMessageVO ingestMessageVO = objectMapper.readValue(bodyBytes, IngestMessageVO.class);
			ingestMessageVO.setDatasetId(topic); // topic명과 데이터셋아이디를 동일하게 사용

			// contentType이 없을 경우 application/ld+json 으로 설정
			if(ValidateUtil.isEmptyData(ingestMessageVO.getContentType())) {
				ingestMessageVO.setContentType(Constants.APPLICATION_LD_JSON_VALUE);
			}

			return ingestMessageVO;
		} catch (NgsiLdBadRequestException e) {
			throw e;
		} catch (IOException e) {
			throw new NgsiLdBadRequestException(ErrorCode.REQUEST_MESSAGE_PARSING_ERROR, "Is Invalid Request Message Format", e);
		} catch (Exception e) {
			throw new NgsiLdBadRequestException(ErrorCode.REQUEST_MESSAGE_PARSING_ERROR, e);
		}
	}
}