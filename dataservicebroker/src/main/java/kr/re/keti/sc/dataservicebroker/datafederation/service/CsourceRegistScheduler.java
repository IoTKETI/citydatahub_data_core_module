package kr.re.keti.sc.dataservicebroker.datafederation.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty(value="data-federation.standalone", havingValue = "false", matchIfMissing = false)
public class CsourceRegistScheduler {

	private final DataFederationService dataFederationService;

	public CsourceRegistScheduler(DataFederationService dataFederationService) {
		this.dataFederationService = dataFederationService;
	}

	@Scheduled(
			fixedRateString = "${data-federation.csource.regist-interval-millis:60000}",
			initialDelay = 10000
	)
	public void synchronizeDataRegistryCsource() {
		try {
			// 1. data registry 로 csource 정보 등록
			dataFederationService.registerCsource();

			// 2. data registry와 local 에 존재하는 csource 정보 조회하여 캐시 갱신
			dataFederationService.retrieveAndCachingCsource();

		} catch (Exception e) {
			log.error("DataFederationService initialize error", e);
		}
	}
}
