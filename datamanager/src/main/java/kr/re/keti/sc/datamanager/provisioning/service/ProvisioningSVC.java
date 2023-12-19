package kr.re.keti.sc.datamanager.provisioning.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ErrorCode;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ProvisionProtocol;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ProvisionServerType;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ProvisioningSubUri;
import kr.re.keti.sc.datamanager.common.controller.kafka.producer.KafkaProducerManager;
import kr.re.keti.sc.datamanager.common.controller.kafka.producer.vo.KafkaProcessVO;
import kr.re.keti.sc.datamanager.common.exception.ProvisionException;
import kr.re.keti.sc.datamanager.provisioning.vo.ProvisionNotiVO;
import kr.re.keti.sc.datamanager.provisioning.vo.ProvisionResultVO;
import kr.re.keti.sc.datamanager.provisionserver.vo.ProvisionServerBaseVO;
import lombok.extern.slf4j.Slf4j;

/**
 * 데이터모델, 데이터셋, 데이터셋흐름, ACL Rule 정보 Provisining 처리 클래스
 */
@Service
@Slf4j
public class ProvisioningSVC {

    public static enum KafkaProvisioningType {
        DATA_MODEL, DATASET, ACL_RULE;
    }

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private KafkaProducerManager kafkaProducerManager;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${kafka.topic.change.event.datamodel}")
    private String dataModelChangeEventTopic;
    @Value("${kafka.topic.change.event.dataset}")
    private String datasetChangeEventTopic;
    @Value("${kafka.topic.change.event.acl-rule}")
    private String aclRuleChangeEventTopic;

    /**
     * 데이터모델, 데이터 셋 변경 이벤트 Kafka 전달
     * @param provisioningTarget Provisioning Entity 유형
     * @param provisionNotiVO    Provisioning 전송 VO
     */
    public void sendKafkaEvent(KafkaProvisioningType provisioningTarget, ProvisionNotiVO provisionNotiVO) {
        if (provisioningTarget == null || provisionNotiVO == null) {
            return;
        }

        String topic = null;
        if (provisioningTarget == KafkaProvisioningType.DATA_MODEL) {
            topic = dataModelChangeEventTopic;
        } else if (provisioningTarget == KafkaProvisioningType.DATASET) {
            topic = datasetChangeEventTopic;
        } else if (provisioningTarget == KafkaProvisioningType.ACL_RULE) {
            topic = aclRuleChangeEventTopic;
        } else {
            return;
        }

        KafkaProcessVO kafkaProcessVO = new KafkaProcessVO();
        kafkaProcessVO.setProvisionNotiVO(provisionNotiVO);
        kafkaProcessVO.setTopic(topic);

        try {
            kafkaProducerManager.produceData(kafkaProcessVO);
        } catch (Exception e) {
            log.error("Kafka Provisioning error.", e);
        }
    }

    /**
     * 특정 서버군에 Provisioning 전송 및 결과 반환
     * @param provisionServerType Provisioning 대상 서버 유형
     * @param provisionServerVOs  Provisioning 대상 서버 정보
     * @param provisionNotiVO     Provisioning 전송 데이터 VO
     * @param subUrl              sub url
     * @return Provisioning 처리 결과
     */
    public List<ProvisionResultVO> provisioning(ProvisionServerType provisionServerType,
                                                List<ProvisionServerBaseVO> provisionServerVOs,
                                                ProvisionNotiVO provisionNotiVO,
                                                ProvisioningSubUri subUrl) {

        if (provisionServerVOs != null) {

            List<ProvisionResultVO> provisionResultVOs = new ArrayList<>();

            for (ProvisionServerBaseVO provisionServerBaseVO : provisionServerVOs) {

                // 1. Provisioning 결과 정보 생성
                ProvisionResultVO provisionResultVO = new ProvisionResultVO();
                provisionResultVO.setRequestId(provisionNotiVO.getRequestId());
                provisionResultVO.setEventTime(provisionNotiVO.getEventTime());
                provisionResultVO.setProvisionServerId(provisionServerBaseVO.getId());
                provisionResultVO.setProvisionEventType(provisionNotiVO.getEventType());
                provisionResultVO.setProvisionServerType(provisionServerType);

                try {
                    // 2. Provisioning 전송
                    boolean result = sendProvisionData(
                            provisionServerBaseVO.getProvisionProtocol(),
                            provisionServerBaseVO.getProvisionUri() + subUrl.getCode(),
                            provisionNotiVO);

                    provisionResultVO.setResult(result);

                } catch (ProvisionException e) {
                    log.warn("Provisioning error. provisionServerId={}, eventType={}", provisionServerBaseVO.getId(), provisionResultVO.getProvisionEventType().getCode(), e);
                    provisionResultVO.setResult(false);
                    provisionResultVO.setProvisionException(e);

                } catch (Exception e) {
                    log.warn("Provisioning error. provisionServerId={}, eventType={}", provisionServerBaseVO.getId(), provisionResultVO.getProvisionEventType().getCode(), e);
                    provisionResultVO.setResult(false);
                    provisionResultVO.setProvisionException(
                            new ProvisionException(ErrorCode.PROVISIONING_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.value(), e));
                }
                provisionResultVOs.add(provisionResultVO);
            }

            return provisionResultVOs;
        }

        return null;
    }

    /**
     * Provisioning 데이터 전송
     * @param provisionProtocol Provisioning 프로토콜 (http or kafka)
     * @param endpointUri       목적지 uri
     * @param provisionNotiVO   Provisioning 전송 데이터 VO
     * @return 전송 결과
     * @throws ProvisionException 파싱 혹은 통신 Exception
     */
    public boolean sendProvisionData(ProvisionProtocol provisionProtocol, String endpointUri,
                                     ProvisionNotiVO provisionNotiVO) throws ProvisionException {

        try {
            // 1. 전송 데이터 Json 으로 파싱
            String sendMessage = objectMapper.writeValueAsString(provisionNotiVO);

            // 2. HTTP 데이터 전송
            if (provisionProtocol == ProvisionProtocol.HTTP) {

                log.debug("Send HTTP Provisioning. endpointUri={}, sendMessage={}", endpointUri, sendMessage);

                // header 설정
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

                HttpEntity<String> entity = new HttpEntity<>(sendMessage, headers);

                ResponseEntity<String> responseEntity = null;
                long startTime = System.currentTimeMillis();
                try {
                    responseEntity = restTemplate.postForEntity(endpointUri, entity, String.class);
                } catch (HttpClientErrorException e) {
                    throw new ProvisionException(ErrorCode.PROVISIONING_ERROR, e.getRawStatusCode(), "Provisioning error. " + e.getResponseBodyAsString());
                }
                long elapsedTime = System.currentTimeMillis() - startTime;

                if (responseEntity == null) {
                    throw new ProvisionException(ErrorCode.PROVISIONING_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.value(), "Provisioning error. HTTP response is null");
                } else if (responseEntity.getStatusCode() != HttpStatus.OK
                        && responseEntity.getStatusCode() != HttpStatus.CREATED
                        && responseEntity.getStatusCode() != HttpStatus.NO_CONTENT) {
                    throw new ProvisionException(ErrorCode.PROVISIONING_ERROR,
                            responseEntity.getStatusCodeValue(), "Provisioning error." + responseEntity.getBody());
                } else {
                    log.info("HTTP sendProvisionData. StatusCode={}, sendMessage={}, elapsedTime={}ms",
                            responseEntity.getStatusCodeValue(), sendMessage, elapsedTime);
                }

                return true;

            } else if (provisionProtocol == ProvisionProtocol.KAFKA) {
                throw new ProvisionException(ErrorCode.PROVISIONING_ERROR, HttpStatus.BAD_REQUEST.value(), "Unsupported ProvisionProtocol. protocol=" + provisionProtocol);
            } else {
                throw new ProvisionException(ErrorCode.PROVISIONING_ERROR, HttpStatus.BAD_REQUEST.value(), "Unsupported ProvisionProtocol. protocol=" + provisionProtocol);
            }

        } catch (ProvisionException e) {
            throw e;
        } catch (Exception e) {
            throw new ProvisionException(ErrorCode.PROVISIONING_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.value(), "Provisioning error", e);
        }
    }
}
