package kr.re.keti.sc.dataservicebroker.notification.processor;

import java.util.Date;
import java.util.List;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import kr.re.keti.sc.dataservicebroker.notification.NotificationManager;
import kr.re.keti.sc.dataservicebroker.notification.vo.NotificationProcessVO;
import kr.re.keti.sc.dataservicebroker.subscription.service.SubscriptionSVC;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionBaseDaoVO;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionEntitiesDaoVO;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@ConditionalOnProperty(value="notification.time.interval.use.yn", havingValue = "Y", matchIfMissing = false)
public class NotificationTimeIntervalProcessor implements Tasklet {

	@Autowired
	private NotificationManager notificationManager;
	@Autowired
	private SubscriptionSVC subscriptionSVC;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		List<SubscriptionBaseDaoVO> subscriptionBaseDaoVOs = subscriptionSVC.retrieveTimeIntervalSubscription();

		if(subscriptionBaseDaoVOs != null) {
			for(SubscriptionBaseDaoVO subscriptionBasDaoVO : subscriptionBaseDaoVOs) {
				List<SubscriptionEntitiesDaoVO> subscriptionEntitiesDaoVOs = subscriptionBasDaoVO.getSubscriptionEntitiesDaoVOs();
				if(subscriptionEntitiesDaoVOs != null) {
					NotificationProcessVO notificationProcessVO = new NotificationProcessVO();
					notificationProcessVO.setEventTime(new Date());
					notificationProcessVO.setTimeIntervalEvent(true);
					notificationProcessVO.setSubscriptionBaseDaoVOs(subscriptionBaseDaoVOs);
					log.debug("TimeInterval Notification ProduceData={}", notificationProcessVO);
					notificationManager.produceData(notificationProcessVO);
				}
			}
		}
		return RepeatStatus.FINISHED;
	}
}
