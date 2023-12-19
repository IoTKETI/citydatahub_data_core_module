package kr.re.keti.sc.dataservicebroker.entities.controller.kafka.consumer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.exception.BaseException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class DefaultKafkaConsumer implements IKafkaConsumer {

	/** 카프카컨슈머명 */
	private String name;
	/** 카프카 컨슈머 클라이언트 */
	private KafkaConsumer<Integer, byte[]> consumer;
	/** 카프카 컨슘 토픽 */
	protected String topic;
	/** 카프카 컨슘 처리 쓰레드 */
	private Thread consumeThread;

	public DefaultKafkaConsumer() {}

	@Override
	public void init(String name, Properties kafkaProperties, String topic, Integer consumerThreadCount, Integer pollDurationMillis) {
		this.name = name;
		this.consumer = new KafkaConsumer<>(kafkaProperties);
		this.topic = topic;

		if(consumerThreadCount == null || consumerThreadCount.intValue() == 0) {
			consumerThreadCount = 1;
		}

		for(int i=0; i<consumerThreadCount; i++) {
			this.consumeThread = new Thread(new ConsumeThread(this.name + "-"+ i, this, consumer, topic, pollDurationMillis));
		}
	}

	public static class ConsumeThread implements Runnable {

		private String name;
		private DefaultKafkaConsumer defaultKafkaConsumer;
		private KafkaConsumer<Integer, byte[]> consumer;
		private String topic;
		private Integer pollDurationMillis;

		private final int DEFAULT_POLL_DURATION_MILLIS = 1000;

		/** 에러데이터 기록용 로거 */
		private final Logger errorDataLogger = LoggerFactory.getLogger(Constants.KAFKA_REQUEST_ERROR_LOGGER_NAME);

		public ConsumeThread(String name, DefaultKafkaConsumer defaultKafkaConsumer, KafkaConsumer<Integer, byte[]> consumer, String topic, Integer pollDurationMillis) {
			this.name = name;
			this.defaultKafkaConsumer = defaultKafkaConsumer;
			this.consumer = consumer; 
			this.topic = topic;
			this.pollDurationMillis = pollDurationMillis;

			if(this.pollDurationMillis == null || this.pollDurationMillis == 0) {
				this.pollDurationMillis = DEFAULT_POLL_DURATION_MILLIS;
			}
		}

		@Override
		public void run() {

			log.info("Start Kafka Consumer Thread. name=[" + name + "]");

			consumer.subscribe(Collections.singletonList(this.topic));

			while(!Thread.currentThread().isInterrupted()) {
				
				try {
					ConsumerRecords<Integer, byte[]> records = consumer.poll(Duration.ofMillis(pollDurationMillis));
					for (ConsumerRecord<Integer, byte[]> record : records) {
						try {
							defaultKafkaConsumer.processReceiveData(record.value());
						} catch (Exception e) {
							String errorMessage = "Kafka processReceiveData error. record=[" + new String(record.value()) + "]";
							log.warn(errorMessage, e);
							errorDataLogger.warn(errorMessage, e);
						}
					}
				} catch(org.apache.kafka.common.errors.InterruptException ie) {
					Thread.currentThread().interrupt();
				} catch(Exception e) {
					log.error("Kafka Consume Error.", e);
					try {
						// 카프카 consume 에러 발생 시 무한루프로 인한 과부하를 막기 위한 방어로직 추가
						Thread.sleep(10);
					} catch (InterruptedException e1) {
						Thread.currentThread().interrupt();
					}
				}
			}
			try {
				consumer.close();
			} catch(org.apache.kafka.common.errors.InterruptException ie) {
				
			} catch(Exception e) {
				log.error("Kafka consumer close error. name={}", name, e);
			}
			

			log.info("Stop Kafka Consumer Thread. name=[" + name + "]");
		}
	}

	@Override
	public void start() {
		try {
			consumeThread.start();
		} catch (Exception e) {
			log.error("DefaultKafkaConsumer Thread START ERROR. name=" + name, e);
		}
	}

	@Override
	public void stop() {
		
		if(consumeThread != null && !consumeThread.isInterrupted()) {
			try {
				consumeThread.interrupt();
			} catch (Exception e) {
				log.error("DefaultKafkaConsumer Thread INTERRUPT ERROR. name=" + name, e);
			}
		}
	}

	@Override
	public void destroy() {
		stop();
	}

	public abstract void processReceiveData(byte[] receivedData) throws BaseException;
}
