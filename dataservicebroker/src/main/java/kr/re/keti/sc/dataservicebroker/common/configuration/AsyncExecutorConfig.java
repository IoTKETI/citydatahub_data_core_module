package kr.re.keti.sc.dataservicebroker.common.configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncExecutorConfig extends AsyncConfigurerSupport {

	@Value("${service-execution.executor.thread-count:10}")
	private Integer threadCount;
	@Value("${service-execution.executor.queue-size:10000}")
	private Integer queueSize;

    @Bean
    public Executor serviceExecutionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadCount);
        executor.setMaxPoolSize(threadCount);
        executor.setQueueCapacity(queueSize);
        executor.setRejectedExecutionHandler(new BlockAvailableExecutionPolicy());
        executor.setThreadNamePrefix("serviceExecutionExecutor-");
        executor.initialize();
        return executor;
    }

    public static class BlockAvailableExecutionPolicy implements RejectedExecutionHandler {

    	@Override
    	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
    		if (!executor.isShutdown()) {
    			try {
    				executor.getQueue().put(r);
    			} catch (InterruptedException e) {

    			}
    		}
    	}
    }
}