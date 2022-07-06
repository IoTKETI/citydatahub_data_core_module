package kr.re.keti.sc.pushagent.notification.sender.http;

import java.util.Collections;
import java.util.List;

import kr.re.keti.sc.pushagent.notification.vo.KeyValuePair;
import kr.re.keti.sc.pushagent.notification.vo.NotificationVO;
import kr.re.keti.sc.pushagent.util.ValidateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.pushagent.common.exception.NotificationException;
import kr.re.keti.sc.pushagent.notification.sender.NotificationSender;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HttpSender implements NotificationSender {

	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private ObjectMapper objectMapper;
	

	@Override
	public boolean sendNotification(String requestId, String endpointUri, String accept, List<KeyValuePair> notifierInfo,
									List<KeyValuePair> receiverInfo, NotificationVO notificationVO) throws NotificationException {

		try {
			HttpHeaders headers = new HttpHeaders();
			if(!ValidateUtil.isEmptyData(accept)) {
				headers.setContentType(MediaType.parseMediaType(accept));
			} else {
				headers.setContentType(MediaType.APPLICATION_JSON);
			}

			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
	
			HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(notificationVO), headers);
	
			long startTime = System.currentTimeMillis();
			ResponseEntity<String> responseEntity = restTemplate.postForEntity(endpointUri, entity, String.class);
			long elapsedTime = System.currentTimeMillis() - startTime;
	
			if(responseEntity == null) {
				log.warn("HTTP Notification Response is null. requestId={}, subsciprionId={}, entityId={}, endpointUri={}, elapsedTime={}ms",
						requestId, notificationVO.getSubscriptionId(), notificationVO.getId(), endpointUri, elapsedTime);
			} else {
				log.info("HTTP Notification Response code={}, requestId={}, subsciprionId={}, entityId={}, endpointUri={}, elapsedTime={}ms",
						responseEntity.getStatusCodeValue(), requestId, notificationVO.getSubscriptionId(), notificationVO.getId(), endpointUri, elapsedTime);
			}
			
			if(responseEntity.getStatusCodeValue() == HttpStatus.OK.value()) {
				return true;
			}

		} catch(HttpClientErrorException e) {
			log.warn("HTTP Notification Response code={}. requestId={}, subsciprionId={}, entityId={}, endpointUri={}",
					e.getRawStatusCode(), requestId, notificationVO.getSubscriptionId(), notificationVO.getId(), endpointUri, e);
		} catch(Exception e) {
			log.warn("HTTP Notification error. requestId={}, subsciprionId={}, entityId={}, endpointUri={}",
					requestId, notificationVO.getSubscriptionId(), notificationVO.getId(), endpointUri, e);
		}
		return false;
	}

}
