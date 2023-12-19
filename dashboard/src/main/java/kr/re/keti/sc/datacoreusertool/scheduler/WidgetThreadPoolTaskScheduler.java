package kr.re.keti.sc.datacoreusertool.scheduler;

import java.util.concurrent.ScheduledFuture;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * ThreadPoolTaskScheduler for widget
 * @FileName WidgetThreadPoolTaskScheduler.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Configuration
public class WidgetThreadPoolTaskScheduler extends ThreadPoolTaskScheduler {
	
	private static final long serialVersionUID = -1356947166267670665L;

	/**
	 * Scheduler creation.
	 */
	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long period) {
		if (period <= 0) {
			return null;
		}
		
		ScheduledFuture<?> scheduledFuture = super.scheduleAtFixedRate(task, period);
		return scheduledFuture;
	}
}
