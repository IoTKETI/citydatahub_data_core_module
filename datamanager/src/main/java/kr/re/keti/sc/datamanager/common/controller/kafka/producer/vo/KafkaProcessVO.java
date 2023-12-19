package kr.re.keti.sc.datamanager.common.controller.kafka.producer.vo;

import kr.re.keti.sc.datamanager.provisioning.vo.ProvisionNotiVO;
import lombok.Data;

/**
 * 카프카 Procuding 처리 VO 클래스
 */
@Data
public class KafkaProcessVO {
	/** 토픽명 */
	private String topic;
	/** provisiong 데이터 */
	private ProvisionNotiVO provisionNotiVO;
}
