package kr.re.keti.sc.datamanager.common.controller.kafka.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.datamanager.common.code.DataManagerCode;
import kr.re.keti.sc.datamanager.common.exception.InternalServerErrorException;
import lombok.extern.slf4j.Slf4j;

/**
 * <pre>
 * 카프카 Rest Proxy 처리 클래스
 *  - 데이터 셋 생성/삭제 시 카프카 토픽 생성/삭제 처리
 *  - 데이터 셋의 데이터 보존기간 설정 시 카프카 토픽 보존 기간을 설정
 * </pre>
 */
@Component
@Slf4j
public class KafkaRestManager {

    private static final String KAFKA_REST_PROXY_CLUSTER_ID = "cluster_id";
    private static final String KAFKA_REST_PROXY_RELATED = "related";
    private static final String KAFKA_CONFIG = "configs";
    private static final String KAFKA_TOPIC_NAME = "topic_name";
    private static final String KAFKA_PARTITIONS_COUNT = "partitions_count";
    private static final String KAFKA_REPLICATION_FACTOR = "replication_factor";
    private static final String KAFKA_NAME = "name";
    private static final String KAFKA_RETENTION = "retention.ms";
    private static final String KAFKA_VALUE = "value";
    private static final String KAFKA_TOPICS = "topics";

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    
    @Value("${rest.proxy.url}")
    private String kafkaProxyUrl;
    @Value("${rest.proxy.partitions.count}")
    private Integer partitionsCount;
    @Value("${rest.proxy.replication.factor}")
    private Integer replicationFactor;

    private List<HashMap<String, Object>> getClusters() {

        ResponseEntity<HashMap> responseEntity = restTemplate.getForEntity(kafkaProxyUrl + "/v3/clusters", HashMap.class);

        HashMap<String, Object> body = responseEntity.getBody();
        List<HashMap<String, Object>> clusters = (List<HashMap<String, Object>>) body.get("data");

        return clusters;
    }

    /**
     * KAFKA 토픽 생성
     * @param topicName 토픽명
     * @param topicRetention 데이터 보존 기간
     */
    public void createTopic(String topicName, Long topicRetention) {

        log.info("Create Dataset Topic. topic={}", topicName);

        try {

	        List<HashMap<String, Object>> clusters = getClusters();
	
	        for (HashMap<String, Object> cluster : clusters) {
	
	            String createTopicUrl = restProxyTopicInfoUrl(cluster);
	
	            HashMap<String, Object> requestBodyMap = new HashMap<>();
	            requestBodyMap.put(KAFKA_TOPIC_NAME, topicName);
	            requestBodyMap.put(KAFKA_PARTITIONS_COUNT, partitionsCount);
	            requestBodyMap.put(KAFKA_REPLICATION_FACTOR, replicationFactor);
	
	            /* 보유 기간 설정이 있을 경우 config설정 추가 */
	            if (topicRetention != null) {
	                Map<String, Object> retentionMs = new HashMap<>();
	                retentionMs.put(KAFKA_NAME, KAFKA_RETENTION);
	                retentionMs.put(KAFKA_VALUE, topicRetention);
	
	                List<Map> configArr = new ArrayList<>();
	                configArr.add(retentionMs);
	
	                requestBodyMap.put(KAFKA_CONFIG, configArr);
	            }
	
	            String requestBody = objectMapper.writeValueAsString(requestBodyMap);
	
	            // header 설정
	            HttpHeaders headers = new HttpHeaders();
	            headers.setContentType(MediaType.APPLICATION_JSON);
	            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
	
	            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
	
	            try {
	                restTemplate.postForEntity(createTopicUrl, entity, HashMap.class);
	            } catch (HttpStatusCodeException e) {
	
	                try {
	                    HashMap<String, String> errorMap = objectMapper.readValue(e.getResponseBodyAsString(), HashMap.class);
	                    if (errorMap.containsKey("message")) {
	                        if (errorMap.get("message").contains("exists")) {
	                            return;
	                        }
	                    }
	                } catch (Exception e1) {
	
	                }
	                throw new InternalServerErrorException(DataManagerCode.ErrorCode.UNKNOWN_ERROR, "Can't create Topic : " + topicName, e);
	            }
	        }
        } catch (Exception e) {
            throw new InternalServerErrorException(DataManagerCode.ErrorCode.UNKNOWN_ERROR,
                    "Can't create Topic : " + topicName, e);
        }
    }

    /**
     * KAFKA 토픽 데이터 보존 기간 수정
     * @param topicName 토픽명
     * @param topicRetention 데이터 보존 기간
     */
    public void updateTopic(String topicName, Long topicRetention) {

        log.info("Update Dataset Topic. topic={}", topicName);

        try {
	        List<HashMap<String, Object>> clusters = getClusters();
	
	        for (HashMap<String, Object> cluster : clusters) {
	
	            String topicInfoUrl = restProxyTopicInfoUrl(cluster);
	
	            StringBuilder updateTopicUrlBuilder = new StringBuilder();
	            updateTopicUrlBuilder.append(topicInfoUrl);
	            updateTopicUrlBuilder.append("/");
	            updateTopicUrlBuilder.append(topicName);
	            updateTopicUrlBuilder.append("/");
	            updateTopicUrlBuilder.append("configs");
	            updateTopicUrlBuilder.append("/");
	            updateTopicUrlBuilder.append(KAFKA_RETENTION);
	            String updateUrl = updateTopicUrlBuilder.toString();
	
	            HashMap<String, Object> requestBodyMap = new HashMap<>();
	            requestBodyMap.put("value", topicRetention);
	
	
	            String requestBody = objectMapper.writeValueAsString(requestBodyMap);
	
	            // header 설정
	            HttpHeaders headers = new HttpHeaders();
	            headers.setContentType(MediaType.APPLICATION_JSON);
	            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
	
	            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
	
	            try {
	                restTemplate.put(updateUrl, entity, HashMap.class);
	            } catch (HttpStatusCodeException e) {
	
	                try {
	                    HashMap<String, String> errorMap = objectMapper.readValue(e.getResponseBodyAsString(), HashMap.class);
	                    if (errorMap.containsKey("message")) {
	                        if (errorMap.get("message").contains("exists")) {
	                            return;
	                        }
	                    }
	                } catch (Exception e1) {
	
	                }
	
	                throw new InternalServerErrorException(DataManagerCode.ErrorCode.UNKNOWN_ERROR,
	                        "Can't update Topic : " + topicName, e);
	            }
	        }
        } catch(Exception e) {
        	throw new InternalServerErrorException(DataManagerCode.ErrorCode.UNKNOWN_ERROR,
                    "Can't update Topic : " + topicName, e);
        }
    }

    /**
     * KAFKA 토픽 삭제
     * @param topicName 토픽명
     */
    public void deleteTopic(String topicName) {

        log.info("Delete Dataset Topic. topic={}", topicName);
        List<HashMap<String, Object>> clusters = getClusters();
        for (HashMap<String, Object> cluster : clusters) {

            HashMap<String, Object> topicsMap = (HashMap<String, Object>) cluster.get(KAFKA_TOPICS);
            String url = topicsMap.get(KAFKA_REST_PROXY_RELATED).toString();
            String cluesterId = cluster.get(KAFKA_REST_PROXY_CLUSTER_ID).toString();

            try {
                restTemplate.delete(url + "/" + topicName);
            } catch (HttpStatusCodeException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    //이미 topic이 지워진 경우, 정상으로 처리
                    return;
                }
                throw new InternalServerErrorException(DataManagerCode.ErrorCode.UNKNOWN_ERROR,
                        "Can't delete Topic : " + topicName + ", clusterId : " + cluesterId, e);
            }
        }
    }

    /**
     * KAFKA Proxy API 요청 URL 생성
     * @param cluster
     * @return
     */
    private String restProxyTopicInfoUrl(HashMap<String, Object> cluster) {

        String clusterId = cluster.get("cluster_id").toString();

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(kafkaProxyUrl + "/v3/clusters");
        urlBuilder.append("/");
        urlBuilder.append(clusterId);
        urlBuilder.append("/");
        urlBuilder.append("topics");

        return urlBuilder.toString();
    }
}
