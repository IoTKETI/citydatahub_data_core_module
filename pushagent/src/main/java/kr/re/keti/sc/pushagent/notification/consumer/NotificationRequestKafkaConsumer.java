package kr.re.keti.sc.pushagent.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.re.keti.sc.pushagent.common.bulk.IBulkChannelManager;
import kr.re.keti.sc.pushagent.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.pushagent.common.exception.BaseException;
import kr.re.keti.sc.pushagent.common.exception.ngsild.NgsiLdBadRequestException;
import kr.re.keti.sc.pushagent.notification.vo.NotificationReqeustVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Properties;

@Slf4j
public class NotificationRequestKafkaConsumer extends DefaultKafkaConsumer {

	/** 카프카메시지 버전 파라미터 길이 */
	private int MESSAGE_VERSION_LENGTH = 1;
	/** 카프카메시지 바디 시작위치 */
	private int MESSAGE_BODY_START_INDEX = MESSAGE_VERSION_LENGTH;

	private ObjectMapper objectMapper;

	/** 벌크처리매니저 */
	private IBulkChannelManager<NotificationReqeustVO> bulkChannelManager;

	public NotificationRequestKafkaConsumer(IBulkChannelManager<NotificationReqeustVO> bulkChannelManager, ObjectMapper objectMapper) {
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
		NotificationReqeustVO notificationReqeustVO = bodyToIngestMessageVO(bodyBytes);

		// 벌크처리
		bulkChannelManager.produceData(notificationReqeustVO);
	}

	/**
	 * Body 파싱
	 * @param bodyBytes Body
	 * @return RequestMessageVO 요청메시지VO
	 */
	private NotificationReqeustVO bodyToIngestMessageVO(byte[] bodyBytes) throws BaseException {
		try {
			NotificationReqeustVO notificationReqeustVO = this.objectMapper.readValue(bodyBytes, NotificationReqeustVO.class);
			return notificationReqeustVO;
		} catch (NgsiLdBadRequestException e) {
			throw e;
		} catch (IOException e) {
			throw new NgsiLdBadRequestException(ErrorCode.REQUEST_MESSAGE_PARSING_ERROR, "Is Invalid Request Message Format", e);
		} catch (Exception e) {
			throw new NgsiLdBadRequestException(ErrorCode.REQUEST_MESSAGE_PARSING_ERROR, e);
		}
	}
}