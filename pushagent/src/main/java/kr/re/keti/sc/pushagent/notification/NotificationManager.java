package kr.re.keti.sc.pushagent.notification;

import javax.annotation.PostConstruct;

import kr.re.keti.sc.pushagent.notification.processor.NotificationProcessor;
import kr.re.keti.sc.pushagent.notification.vo.NotificationReqeustVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import kr.re.keti.sc.pushagent.common.bulk.IBulkChannel;
import kr.re.keti.sc.pushagent.common.bulk.IBulkChannelManager;
import kr.re.keti.sc.pushagent.common.bulk.MemoryQueueBulkChannel;
import kr.re.keti.sc.pushagent.common.bulk.MemoryQueueBulkChannel.ExecuteMode;
import kr.re.keti.sc.pushagent.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.pushagent.common.exception.CoreException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NotificationManager implements IBulkChannelManager<NotificationReqeustVO> {

	@Autowired
	private NotificationProcessor notificationProcessor;

	@Value("${notification.processor.thread.count:10}")
	private Integer bulkProcessorCount; // 벌크처리 프로세서 쓰레드 수 (default : 10)

	@Value("${notification.queue.size:100000}")
	private Integer bulkQueueSize; // 벌크처리 큐 크기 (default : 100000)

	private IBulkChannel<NotificationReqeustVO> bulkChannel;
	

	@Override
	@PostConstruct
	public void init() {

		log.info("Initilize NotificationManager.");

		// 비동기 Queue 처리채널 초기화
		bulkChannel = new MemoryQueueBulkChannel<NotificationReqeustVO>("NotificationBulkChannel", notificationProcessor,
				ExecuteMode.ASYNC_SINGLE, bulkProcessorCount, bulkQueueSize, null);
		bulkChannel.start();
	}

	@Override
	public void produceData(NotificationReqeustVO message) throws CoreException {
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
