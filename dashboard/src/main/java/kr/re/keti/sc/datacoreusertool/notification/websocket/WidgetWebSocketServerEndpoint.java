package kr.re.keti.sc.datacoreusertool.notification.websocket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.springframework.stereotype.Component;

import kr.re.keti.sc.datacoreusertool.api.widgetdashboard.service.WidgetDashboardSVC;
import kr.re.keti.sc.datacoreusertool.api.widgetdashboard.vo.WidgetDashboardResponseVO;
import kr.re.keti.sc.datacoreusertool.common.code.WidgetDashboardCode;
import kr.re.keti.sc.datacoreusertool.common.code.WidgetDashboardCode.WidgetMethod;
import kr.re.keti.sc.datacoreusertool.notification.vo.WidgetWebSocketRegistVO;
import kr.re.keti.sc.datacoreusertool.util.BeanUtil;
import kr.re.keti.sc.datacoreusertool.util.ConvertUtil;
import kr.re.keti.sc.datacoreusertool.util.ValidateUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Web socket server end point for widget.</br>
 * This class is not a Spring Bean and is newly created every time a session is connected.
 * @FileName WidgetWebSocketServerEndpoint.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Slf4j
@Component
@Data
@ServerEndpoint(value = "/widgetevents")
public class WidgetWebSocketServerEndpoint {
	
	/** Websocket Session Manager */
	private WidgetWebSocketSessionManager sessionManager = WidgetWebSocketSessionManager.INSTANCE;
	/** Websocket Session */
	private Session session;
	/** Websocket session id */
	private String id;
	
	/** entity widgetId (Receive widget information (widgetId, updateType) after session connection) */
	private Map<String, WidgetDashboardResponseVO> widgetInfos = new HashMap<String, WidgetDashboardResponseVO>();
	/** entity userId (Receive userId information after session connection) */
	private String userId;

	/**
	 * Web socket session open message handler
	 * @param session	Session
	 */
    @OnOpen
    public void onOpen(Session session) {
    	this.session = session;
    	this.id = UUID.randomUUID().toString();
    	sessionManager.addSession(this);
        log.info("Websocket onOpen sessionId={}", id);
    }

    /**
     * Web socket session close message handler
     * @param session	Session
     */
    @OnClose
    public void onClose(Session session) {
    	// 1. Check if subscription information is being used by another session
    	boolean useSubscription = false;
    	Map<String, WidgetWebSocketServerEndpoint> sessions = sessionManager.getSessions();
    	for(WidgetWebSocketServerEndpoint webSocketWidgetServerEndpoint : sessions.values()) {
    		if(id.equals(webSocketWidgetServerEndpoint.getId())) continue;
    		if(!ValidateUtil.isEmptyData(userId) 
    				&& userId.equals(webSocketWidgetServerEndpoint.getUserId())) {
    			useSubscription = true;
    			break;
    		}
    	}

    	// 2. If the subscription information is not being used by any session, unsubscribe
    	if(!useSubscription) {
    		WidgetDashboardSVC widgetDashboardSVC = (WidgetDashboardSVC) BeanUtil.getBean("widgetDashboardSVC");
    		
    		for(String widgetId : widgetInfos.keySet()) {
    			deleteUpdateSetting(widgetDashboardSVC, widgetId, userId);
    		}
    		this.widgetInfos.clear();
    	}

    	// 3. Remove session from SessionManager
    	sessionManager.removeSession(id);
    }

    /**
     * Web socket session message handler
     * @param message
     */
    @OnMessage
    public void onMessage(String message) {
        log.info("Websocket onMessage sessionId={}, message={}", id, message);

        WidgetWebSocketRegistVO widgetWebSocketRegistVO = ConvertUtil.getNgsiLdGson().fromJson(message, WidgetWebSocketRegistVO.class);
        if(widgetWebSocketRegistVO != null) {
        	WidgetDashboardSVC widgetDashboardSVC = (WidgetDashboardSVC) BeanUtil.getBean("widgetDashboardSVC");
        	String dashboardId = widgetWebSocketRegistVO.getDashboardId();
        	String widgetId = widgetWebSocketRegistVO.getWidgetId();
        	String widgetMethod = widgetWebSocketRegistVO.getMethod();
        	
        	// In case of modification of widget
        	if(WidgetMethod.UPDATE.getCode().equals(widgetMethod)) {
        		deleteUpdateSetting(widgetDashboardSVC, widgetId, widgetWebSocketRegistVO.getUserId());
        		this.widgetInfos.remove(widgetId);
        	}
        	// In case of deletion of widget
        	else if (WidgetMethod.DELETE.getCode().equals(widgetMethod)) {
        		deleteUpdateSetting(widgetDashboardSVC, widgetId, widgetWebSocketRegistVO.getUserId());
        		this.widgetInfos.remove(widgetId);
        		return;
        	}
        	else {
        		// do nothing
        	}
        	
        	if(ValidateUtil.isEmptyData(widgetId)
        			|| ValidateUtil.isEmptyData(widgetWebSocketRegistVO.getUserId())) {
        		log.warn("WebSocketWidgetServerEndponit - onMessage() missing required values. widgetId:{}, userId:{}", 
        				widgetId, widgetWebSocketRegistVO.getUserId());
        		return;
        	}
        	this.userId = widgetWebSocketRegistVO.getUserId();
        	WidgetDashboardResponseVO widgetDashboardResponseVO = widgetDashboardSVC.getWidgetDashboardVO(dashboardId, widgetId, userId);
        	this.widgetInfos.put(widgetId, widgetDashboardResponseVO);
        	widgetDashboardSVC.setRequestChartResult(widgetWebSocketRegistVO, id);
        }
    }

    /**
     * Web socket session error handler
     * @param session		Session
     * @param throwable		Throwable
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        log.warn("Websocket onError sessionId={}", id, throwable);
    }
    
    /**
     * Delete subscription and scheduler when session closed or updated.
     * @param widgetDashboardSVC	WidgetDashboardSVC
     * @param widgetId				Widget ID
     * @param userId				User ID
     */
    private void deleteUpdateSetting(WidgetDashboardSVC widgetDashboardSVC, String widgetId, String userId) {
    	WidgetDashboardResponseVO widgetDashboard = widgetInfos.get(widgetId);
    	
    	if(widgetDashboard == null) {
    		return;
    	}
    	
    	if ((widgetDashboard.getUpdateInterval() == null || widgetDashboard.getUpdateInterval() < 1) 
    			&& (widgetDashboard.getRealtimeUpdateEnabled() == null || !widgetDashboard.getRealtimeUpdateEnabled())) {
    		return;
    	}
    	
    	if(getUpdateType(widgetDashboard).equals(WidgetDashboardCode.UpdateType.REALTIME.name())) {
			widgetDashboardSVC.deleteSubscription(userId + ":" + widgetId);
		} 
		else if(getUpdateType(widgetDashboard).equals(WidgetDashboardCode.UpdateType.PERIODIC.name())) {
			widgetDashboardSVC.deleteScheduler(userId + ":" + widgetId);
		} else {
			// do nothing.
		}
    }
    
    /**
     * Get update type of widget data 
     * @param widgetDashboard	WidgetDashboardResponseVO
     * @return	Update type of widget data
     */
    private String getUpdateType(WidgetDashboardResponseVO widgetDashboard) {
    	String updateType = null;
    	
    	if(widgetDashboard.getUpdateInterval() != null && widgetDashboard.getUpdateInterval() > 0) {
    		updateType = WidgetDashboardCode.UpdateType.PERIODIC.name();
		} else if(widgetDashboard.getRealtimeUpdateEnabled() != null && widgetDashboard.getRealtimeUpdateEnabled()) {
			updateType = WidgetDashboardCode.UpdateType.REALTIME.name();
		} else {
			// do nothing
		}
    	
    	return updateType;
    }
}
