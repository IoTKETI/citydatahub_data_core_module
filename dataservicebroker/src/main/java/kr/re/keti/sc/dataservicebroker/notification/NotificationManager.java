package kr.re.keti.sc.dataservicebroker.notification;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import kr.re.keti.sc.dataservicebroker.common.bulk.IBulkChannel;
import kr.re.keti.sc.dataservicebroker.common.bulk.IBulkChannelManager;
import kr.re.keti.sc.dataservicebroker.common.bulk.MemoryQueueBulkChannel;
import kr.re.keti.sc.dataservicebroker.common.bulk.MemoryQueueBulkChannel.ExecuteMode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.exception.CoreException;
import kr.re.keti.sc.dataservicebroker.notification.processor.NotificationProcessor;
import kr.re.keti.sc.dataservicebroker.notification.vo.NotificationProcessVO;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NotificationManager implements IBulkChannelManager<NotificationProcessVO> {

	@Autowired
	private NotificationProcessor notificationProcessor;

	@Value("${notification.change.event.use.yn:N}")
	private String changeEventNotificationUseYn; // change event 기반 notification 기능 사용 여부

	@Value("${notification.time.interval.use.yn:N}")
	private String timeIntervalNotificationUseYn; // time interval 기반 notification 기능 사용 여부

	@Value("${notification.processor.thread.count:10}")
	private Integer bulkProcessorCount; // 벌크처리 프로세서 쓰레드 수 (default : 10)

	@Value("${notification.queue.size:100000}")
	private Integer bulkQueueSize; // 벌크처리 큐 크기 (default : 100000)

	private IBulkChannel<NotificationProcessVO> bulkChannel;
	

	@Override
	@PostConstruct
	public void init() {

		// 1. notification 사용 여부 확인
		// change event 기반, time interval 기반 notification 기능 모두 off 인 경우 thread 기동 하지 않음
		if(!DataServiceBrokerCode.UseYn.YES.getCode().equals(changeEventNotificationUseYn)
				&& !DataServiceBrokerCode.UseYn.YES.getCode().equals(timeIntervalNotificationUseYn)) {
			return;
		}

		log.info("Initilize NotificationManager.");

		// 2. 비동기 Queue 처리채널 초기화
		bulkChannel = new MemoryQueueBulkChannel<NotificationProcessVO>("NotificationBulkChannel", notificationProcessor, 
				ExecuteMode.ASYNC_SINGLE, bulkProcessorCount, bulkQueueSize, null);
		bulkChannel.start();
	}

	public boolean enableChangeEventNotification() {
		if(DataServiceBrokerCode.UseYn.YES.getCode().equals(changeEventNotificationUseYn)) {
			return true;
		}

		return false;
	}

	@Override
	public void produceData(NotificationProcessVO message) throws CoreException {
		if(bulkChannel != null) {
			bulkChannel.produceData(message, false);
		}
	}

	@Override
	public void destroy() {
		if(bulkChannel != null) {
			bulkChannel.destroy();
		}
	}
}
