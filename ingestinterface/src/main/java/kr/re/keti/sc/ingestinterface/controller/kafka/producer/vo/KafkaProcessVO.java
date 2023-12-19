package kr.re.keti.sc.ingestinterface.controller.kafka.producer.vo;

public class KafkaProcessVO {
	private String topic;
	private IngestEventKafkaMessageVO ingestEventKafkaMessageVO;

	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public IngestEventKafkaMessageVO getIngestEventKafkaMessageVO() {
		return ingestEventKafkaMessageVO;
	}
	public void setIngestEventKafkaMessageVO(IngestEventKafkaMessageVO ingestEventKafkaMessageVO) {
		this.ingestEventKafkaMessageVO = ingestEventKafkaMessageVO;
	}
}
