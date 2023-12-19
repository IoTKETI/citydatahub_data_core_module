package kr.re.keti.sc.dataservicebroker.common.configuration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import kr.re.keti.sc.dataservicebroker.notification.processor.NotificationTimeIntervalProcessor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@ConditionalOnProperty(value="notification.time.interval.use.yn", havingValue = "Y", matchIfMissing = false)
@Slf4j
public class BatchConfiguration {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	private SimpleJobLauncher simpleJobLauncher;

	@Autowired
	private NotificationTimeIntervalProcessor notificationTimeIntervalProcessor;
	
	@Value("${notification.batch.interval.millis:10000}")
	private Long notificationBatchIntervalMillis;

	@Scheduled(fixedDelayString = "${notification.batch.interval.millis:10000}"
			, initialDelayString = "${notification.batch.initDelay:30000}")
	public void runTimeIntervalNotificationJob() {
		try {
			JobParameters jobParameters = new JobParametersBuilder().addLong("batch-date",
	                System.currentTimeMillis()).toJobParameters();
			simpleJobLauncher.run(timeIntervalNotificationJob(), jobParameters);
		} catch (Exception e) {
			log.error("RunTimeIntervalNotificationJob error.", e);
		}
	}

	@Bean
	public Job timeIntervalNotificationJob() {
		return jobBuilderFactory.get("timeIntervalNotificationJob")
				.start(timeIntervalNotificationStep())
				.build();
	}

	@Bean
	public Step timeIntervalNotificationStep() {
		return stepBuilderFactory.get("timeIntervalNotificationStep")
				.tasklet(notificationTimeIntervalProcessor)
				.build();
	}

    @Bean
    public SimpleJobLauncher simpleJobLauncher(JobRepository jobRepository) {
        SimpleJobLauncher launcher = new SimpleJobLauncher();
        launcher.setJobRepository(jobRepository);
        return launcher;
    }
}