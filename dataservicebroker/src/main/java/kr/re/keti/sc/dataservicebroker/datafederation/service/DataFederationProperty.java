package kr.re.keti.sc.dataservicebroker.datafederation.service;

import kr.re.keti.sc.dataservicebroker.csource.vo.CsourceRegistrationVO;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "data-federation")
@Data
public class DataFederationProperty {

    private Boolean standalone;
    private CsourceProperty csource;
    private SubscriptionProperty subscription;
    private DataRegistry dataRegistry;

    @Data
    public static class CsourceProperty {
        private String id;
        private String endpoint;
        private String registIntervalMillis;
        private List<CsourceRegistrationVO.EntityInfo> entityInfos;
        private String location;
    }

    @Data
    public static class SubscriptionProperty {
        private String id;
        private String endpoint;
    }

    @Data
    public static class DataRegistry {
        private String baseUri;
        private DataRegistrySubUri subUri;
    }

    @Data
    public static class DataRegistrySubUri {
        private String csource;
        private String subscription;
    }

}
