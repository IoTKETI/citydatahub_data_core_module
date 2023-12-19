package kr.re.keti.sc.dataservicebroker.entities.bulkprocessor;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import kr.re.keti.sc.dataservicebroker.common.bulk.IBulkChannel;
import kr.re.keti.sc.dataservicebroker.common.bulk.IBulkChannelManager;
import kr.re.keti.sc.dataservicebroker.common.bulk.IBulkProcessor;
import kr.re.keti.sc.dataservicebroker.common.bulk.MemoryQueueBulkChannel;
import kr.re.keti.sc.dataservicebroker.common.exception.CoreException;
import kr.re.keti.sc.dataservicebroker.common.vo.IngestMessageVO;

@Component
public class BulkChannelManager implements IBulkChannelManager<IngestMessageVO> {

	@Value("${bulk.processor.thread.count:10}")
	private Integer bulkProcessorCount; // 벌크처리 프로세서 쓰레드 수 (default : 10)

	@Value("${bulk.queue.size:100000}")
	private Integer bulkQueueSize; // 벌크처리 큐 크기 (default : 100000)

	@Value("${bulk.interval.millis:100}")
	private Integer bulkIntervalMillis; // 벌크처리 묵음 간격 (default : 100ms)

	@Autowired
	private IBulkProcessor<IngestMessageVO> bulkProcessor;
	private IBulkChannel<IngestMessageVO> bulkChannel;
	
	@Override
	@PostConstruct
	public void init() {

		// 벌크처리채널
		bulkChannel = new MemoryQueueBulkChannel<IngestMessageVO>("MemoryQueueBulkChannel", 
				bulkProcessor, bulkProcessorCount, bulkQueueSize, bulkIntervalMillis);
		bulkChannel.start();
	}

	@Override
	public void produceData(IngestMessageVO message) throws CoreException {
		bulkChannel.produceData(message, true);
	}

	@Override
	public void destroy() {
		bulkChannel.destroy();
	}
}
