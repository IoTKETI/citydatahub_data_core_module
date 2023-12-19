package kr.re.keti.sc.dataservicebroker.common.configuration;

import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InMemoryBatchContextConfigurer extends DefaultBatchConfigurer {
    @Bean
    protected ResourcelessTransactionManager resoucelessTransactionManager() {
        return new ResourcelessTransactionManager();
    }

    @Override
    protected JobRepository createJobRepository() throws Exception {
        MapJobRepositoryFactoryBean factoryBean = new MapJobRepositoryFactoryBean();
        factoryBean.setTransactionManager(resoucelessTransactionManager());
        return factoryBean.getObject();
    }
}