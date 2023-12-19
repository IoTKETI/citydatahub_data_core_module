package kr.re.keti.sc.datacoreusertool.notification.controller;

import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.datacoreusertool.api.widgetdashboard.service.WidgetDashboardSVC;
import kr.re.keti.sc.datacoreusertool.api.widgetdashboard.vo.WidgetDashboardUIResponseVO;
import kr.re.keti.sc.datacoreusertool.common.code.Constants;
import kr.re.keti.sc.datacoreusertool.notification.vo.NotificationVO;
import kr.re.keti.sc.datacoreusertool.notification.websocket.WebSocketSessionManager;
import kr.re.keti.sc.datacoreusertool.notification.websocket.WidgetWebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;

/**
 * Notification receiving class
 * @FileName NotificationController.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Slf4j
@Controller
public class NotificationController {
	
	@Autowired
	private WidgetDashboardSVC widgetDashboardSVC;

	private ObjectMapper objectMapper;

	/**
	 * Constructor of NotificationController class
	 */
    public NotificationController() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.objectMapper.setDateFormat(new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT));
    }
    
    /**
     * Receive notification
     * @param notificationVO	NotificationVO
     * @return					Http status
     */
	@PostMapping(value="/notification")
	public ResponseEntity<Void> receiveNotification(HttpServletRequest request, 
			HttpServletResponse response, @RequestBody NotificationVO notificationVO) {

		// 1. Parsing incoming data
		String message = null;
		try {
			message = objectMapper.writeValueAsString(notificationVO);
		} catch(JsonProcessingException e) {
			log.error("receiveNotification parse error.", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}

		// 2. Send to webSocket session
		WebSocketSessionManager.INSTANCE.sendToEntitySession(notificationVO.getId(), message);

		// 3. Response
		return ResponseEntity.status(HttpStatus.OK).build();
	}
	
	/**
	 * Receive widget notification
	 * @param notificationVO	NotificationVO
	 * @return					Http status
	 */
	@PostMapping(value="/widgetnotification")
	public ResponseEntity<Void> receiveWidgetNotification(HttpServletRequest request, 
			HttpServletResponse response, @RequestBody NotificationVO notificationVO) {

		// 1. Parsing incoming data
		String message = null;
		try {
			String[] subscriptionIdInfos = notificationVO.getSubscriptionId().split(":");
			// subscriptionIdInfos[0] = "urn"
			ResponseEntity<WidgetDashboardUIResponseVO> widgetDashboardUIResponseVO = widgetDashboardSVC.getWidget(subscriptionIdInfos[1], subscriptionIdInfos[2], subscriptionIdInfos[3]);
			
			notificationVO.setWidgetId(widgetDashboardUIResponseVO.getBody().getWidgetId());
			notificationVO.setChartType(widgetDashboardUIResponseVO.getBody().getChartType());
			notificationVO.setAttributeId(widgetDashboardUIResponseVO.getBody().getEntityRetrieveVO().getAttrs().get(0));
			
			message = objectMapper.writeValueAsString(notificationVO);
		} catch(JsonProcessingException e) {
			log.error("receiveNotification parse error.", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		} catch(Exception e) {
			log.error("Invalid widget. subscriptionId: {}", notificationVO.getSubscriptionId(), e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}

		// 2. Send to webSocket session
		WidgetWebSocketSessionManager.INSTANCE.sendToEntitySession(notificationVO, message, null);

		// 3. Response
		return ResponseEntity.status(HttpStatus.OK).build();
	}
}
