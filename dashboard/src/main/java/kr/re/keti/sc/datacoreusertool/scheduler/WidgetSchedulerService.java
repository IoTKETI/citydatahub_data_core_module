package kr.re.keti.sc.datacoreusertool.scheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Scheduler service for widget
 * @FileName WidgetSchedulerService.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Slf4j
@Service
public class WidgetSchedulerService {
	private Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

	@Autowired 
	private TaskScheduler taskScheduler;
	
	/**
	 * Scheduler registration
	 * @param runnable		Runnable
	 * @param scheduledId	Scheduled ID
	 * @param period		Period of task execution.
	 */
	public void register(Runnable runnable, String scheduledId, int period) {
		ScheduledFuture<?> task = taskScheduler.scheduleAtFixedRate(runnable, period);
		
		scheduledTasks.put(scheduledId, task);
	}
	
	/**
	 * Scheduler remove
	 * @param scheduledId	Scheduled ID
	 */
	public void remove(String scheduledId) {
		log.info("Remove schedule - {}", scheduledId);
		
		ScheduledFuture<?> task = scheduledTasks.get(scheduledId);
		
		if(task != null) {
			task.cancel(true);
			scheduledTasks.remove(scheduledId);
		}
	}
}
