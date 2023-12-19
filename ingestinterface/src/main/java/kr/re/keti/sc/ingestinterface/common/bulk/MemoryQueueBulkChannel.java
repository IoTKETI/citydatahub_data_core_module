package kr.re.keti.sc.ingestinterface.common.bulk;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;

import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.ErrorCode;
import kr.re.keti.sc.ingestinterface.common.exception.CoreException;
import lombok.extern.slf4j.Slf4j;

/**
 * 메모리 큐 기반 벌크채널 클래스
 *  - 메모리 큐를 통해 T 데이터를 묶음처리 하고 비동기 병렬 쓰레드를 통해 묶음데이터를 벌크처리
 * @param <T> 벌크처리 대상 Object Type
 */
@Slf4j
public class MemoryQueueBulkChannel<T> implements IBulkChannel<T> {

	public enum ExecuteMode {
		ASYNC_SINGLE, ASYNC_BULK
	}

	/** 디폴트 실행모드 */
	private static final ExecuteMode DEFAULT_EXECUTE_MODE = ExecuteMode.ASYNC_BULK;
	/** 디폴트 쓰레드 수 */
	private static final int DEFAULT_WORK_THREAD_COUNT = 10;
	/** 디폴트 메모리 큐 크기 */
	private static final int DEFAULT_QUEUE_SIZE = 100000;
	/** 디폴트 벌크처리 간격 (millisecond) */
	private static final int DEFAULT_BULK_INTERVAL_MILLIS = 500;
	/** 벌크처리 최대 크기 */
	private static final int MAX_BULK_SIZE = 10000;

	/** 실행모드 */
	private ExecuteMode executeMode = DEFAULT_EXECUTE_MODE;
	/** 쓰레드 수 */
	private int workThreadCount = DEFAULT_WORK_THREAD_COUNT;
	/** 메모리 큐 크기 */
	private int queueSize = DEFAULT_QUEUE_SIZE;
	/** 벌크처리 간격(millisecond) */
	private int bulkIntervalMillis = DEFAULT_BULK_INTERVAL_MILLIS;
	/** 인스턴스 명 */
	private String instanceName;

	/** 메모리 큐 */
	private BlockingQueue<T> queue = null;
	/** 벌크처리기 */
	private IBulkProcessor<T> bulkProcessor = null;
	/** 쓰레드 그룹 */
	private Thread[] workThreads = null;

	public MemoryQueueBulkChannel(String instanceName, IBulkProcessor<T> processor, Integer workThreadCount, Integer queueSize, Integer bulkIntervalMillis) {
		this(instanceName, processor, ExecuteMode.ASYNC_BULK, workThreadCount, queueSize, bulkIntervalMillis);
	}

	public MemoryQueueBulkChannel(String instanceName, IBulkProcessor<T> processor, ExecuteMode executeMode, Integer workThreadCount, Integer queueSize, Integer bulkIntervalMillis) {
		this.instanceName = instanceName;

		if(workThreadCount != null && workThreadCount > 0) {
			this.workThreadCount = workThreadCount.intValue();
		}

		if(queueSize != null && queueSize > 0) {
			this.queueSize = queueSize.intValue();
		}

		if(bulkIntervalMillis != null && bulkIntervalMillis > 0) {
			this.bulkIntervalMillis = bulkIntervalMillis.intValue();
		}

		this.queue = new ArrayBlockingQueue<T>(this.queueSize);
		this.bulkProcessor = processor;
		this.workThreads = new Thread[this.workThreadCount];
		this.executeMode = executeMode;
	}

	@Override
	public boolean produceData(T object, Boolean doWait) throws CoreException {
		try {
			if(doWait) {
				queue.put(object);
				return true;
			} else {
				return queue.add(object);
			}
		} catch(Exception e) {
			throw new CoreException(ErrorCode.MEMORY_QUEUE_INPUT_ERROR, e);
		}
	}

	@Override
	public void start() {
		log.info("Start BulkProcess. name=[" + instanceName + "], threadCount=[" + workThreadCount + "], bulkIntervalMillis=[" + bulkIntervalMillis + "], queueSize=[" + queueSize + "]");
		for(int i=0; i<workThreadCount; i++) {
			try {
				if(executeMode == ExecuteMode.ASYNC_BULK) {
					ConsumeBulkThread consumeThread = new ConsumeBulkThread();
					log.info("Start BulkProcess. name=[" + instanceName + "-" + i + "]");
					workThreads[i] = new Thread(consumeThread, instanceName + "-" + i);
				} else if(executeMode == ExecuteMode.ASYNC_SINGLE) {
					ConsumeSingleThread consumeThread = new ConsumeSingleThread();
					log.info("Start BulkProcess. name=[" + instanceName + "-" + i + "]");
					workThreads[i] = new Thread(consumeThread, instanceName + "-" + i);
				}
				workThreads[i].start();
			} catch (Exception e) {
				log.error("MemoryQueueBulkChannel start ERROR. name=" + instanceName, e);
			}
		}
	}

	@Override
	public void stop() {
		for(int i=0; i<workThreadCount; i++) {
			try {
				log.info("Stop BulkProcess. name=[" + instanceName + "-" + i + "]");
				workThreads[i].interrupt();
				workThreads[i] = null;
			} catch (Exception e) {
				log.error("MemoryQueueBulkChannel stop ERROR. name=" + instanceName, e);
			}
		}
	}

	@Override
	public void init() {
	}

	@Override
	public void destroy() {
		stop();
	}

	@Override
	public void clear(boolean storeRemainingData) {
		clear(storeRemainingData, log);
	}

	@Override
	public void clear(boolean storeRemainingData, Logger logger) {
		logger.info("Clear MemoryQueueBulkChannel queue. Name=[" + instanceName + "], StoreRemainingData=[" + storeRemainingData + "]");
		if(storeRemainingData && queue.size() > 0) {
			List<T> objList = new ArrayList<T>();
			while(true) {
				T object = queue.poll();
				if(object == null) break;
				objList.add(object);
			}
			if(objList.size() > 0) {
				logger.info("Clear Data size=[" + objList.size() + "]");
				for (int i=0; i<objList.size(); i++) {
					logger.info("Clear Data=[" + instanceName + "-" + i + "]. Object=" + objList.get(i).toString());
				}
			} else {
				logger.info("Clear Data size=[0]");
			}
		} else {
			logger.info("Clear Data size=[" + queue.size() + "]");
			queue.clear();
		}
	}

	class ConsumeBulkThread implements Runnable {
		@Override
		public void run() {
			while(!Thread.currentThread().isInterrupted()) {
				try {
					// poll 로 벌크처리
					List<T> objects = pollData();
					if(objects.size() > 0) {
						bulkProcessor.processBulk(objects);
					}
				} catch(Exception e) {
					log.error("Process error. name=[" + instanceName + "]", e);
				}

				// queue에 잔여 데이터가 있으면 sleep 하지 않음
				if(queue.size() == 0) {
					try {
						Thread.sleep(bulkIntervalMillis);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}
			// 종료시 queue에 있는 모든 잔여 데이터를 처리
			List<T> objects = pollData();
			bulkProcessor.processBulk(objects);
		}

		/**
		 * Memory queue 데이터 polling
		 * @param maxBulkSize polling 최대 크기
		 * @return polling queue object list
		 */
		private List<T> pollData() {

			List<T> objects = new ArrayList<>();

			while(queue.size() > 0) {
				T object = queue.poll();
				if(object == null) break;
				objects.add(object);
				// if문 비교 오버헤드를 줄이기 위해 pollData method를 2개로 분리 (maxBulkSize가 있는 경우와 없는 경우)
				if(objects.size() >= MAX_BULK_SIZE) break;
			}
			return objects;
		}
	}

	class ConsumeSingleThread implements Runnable {
		@Override
		public void run() {
			while(!Thread.currentThread().isInterrupted()) {
				try {
					// poll 로 벌크처리
					T objects = queue.take();
					bulkProcessor.processSingle(objects);
				} catch(InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch(Exception e) {
					log.error("Process error. name=[" + instanceName + "]", e);
				}
			}
			// TODO: 종료시 queue에 있는 모든 잔여 데이터를 처리
		}
	}

}