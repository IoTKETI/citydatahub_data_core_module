package kr.re.keti.sc.datacoreusertool.notification.websocket;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import kr.re.keti.sc.datacoreusertool.api.widgetdashboard.vo.WidgetDashboardResponseVO;
import kr.re.keti.sc.datacoreusertool.notification.vo.NotificationVO;
import lombok.extern.slf4j.Slf4j;

/**
 * WidgetWebSocketSessionManager enumerate
 * @FileName WidgetWebSocketSessionManager.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Slf4j
public enum WidgetWebSocketSessionManager {
	INSTANCE;

	private Map<String, WidgetWebSocketServerEndpoint> sessionMap = new ConcurrentHashMap<>();

	/**
	 * Add session
	 * @param webSocketWidgetServerEndpoint		WidgetWebSocketServerEndpoint
	 */
    public void addSession(WidgetWebSocketServerEndpoint webSocketWidgetServerEndpoint) {
    	sessionMap.put(webSocketWidgetServerEndpoint.getId(), webSocketWidgetServerEndpoint);
    }
    
    /**
     * Remove session
     * @param webSocketWidgetServerEndpointId	Widget server ID
     * @return									Result of remove session.
     */
    public WidgetWebSocketServerEndpoint removeSession(String webSocketWidgetServerEndpointId) {
    	return sessionMap.remove(webSocketWidgetServerEndpointId);
    }
    
    /**
     * Get session
     * @return	Session map
     */
    public Map<String, WidgetWebSocketServerEndpoint> getSessions() {
    	return sessionMap;
    }
    
    /**
     * Get session
     * @param webSocketWidgetServerEndpoint		WidgetWebSocketServerEndpoint
     * @return									WidgetWebSocketServerEndpoint data
     */
    public WidgetWebSocketServerEndpoint getSession(WidgetWebSocketServerEndpoint webSocketWidgetServerEndpoint) {
    	return sessionMap.get(webSocketWidgetServerEndpoint.getId());
    }

    /**
     * Send messages only to the Websocket session where the entity information received as a notification is registered
     * @param notificationVO Notification EntityId
     * @param message Notification Message
     */
    public void sendToEntitySession(NotificationVO notificationVO, String message, String sessionId) {
    	if(sessionId != null) {
    		for(String id : sessionMap.keySet()) {
    			if(id.equals(sessionId)) {
    				WidgetWebSocketServerEndpoint webSocketWidgetServerEndpoint = sessionMap.get(id);
    				sendMessage(notificationVO, webSocketWidgetServerEndpoint, message);
    			}
    		}
    	} else {
    		for(WidgetWebSocketServerEndpoint webSocketWidgetServerEndpoint : sessionMap.values()) {
    			sendMessage(notificationVO, webSocketWidgetServerEndpoint, message);
        	}
    	}
    }
    
    /**
     * Send message received as notification to registered web socket session.
     * @param notificationVO					NotificationVO
     * @param webSocketWidgetServerEndpoint		WidgetWebSocketServerEndpoint
     * @param message							Notification Message
     */
    private void sendMessage(NotificationVO notificationVO, WidgetWebSocketServerEndpoint webSocketWidgetServerEndpoint, String message) {
    	String userId = webSocketWidgetServerEndpoint.getUserId();
		Map<String, WidgetDashboardResponseVO> widgetIds = webSocketWidgetServerEndpoint.getWidgetInfos();
		
		if(widgetIds == null) {
			log.warn("No session for widget.");
			return;
		}
		
		for(String key : widgetIds.keySet()) {
			if(notificationVO.getSubscriptionId() == null || !notificationVO.getSubscriptionId().equals(userId + ":" + key)) {
    			continue;
    		}
    		
    		try {
    			if(webSocketWidgetServerEndpoint.getSession().isOpen()) {
    				webSocketWidgetServerEndpoint.getSession().getBasicRemote().sendText(message);
    			} else {
    				removeSession(webSocketWidgetServerEndpoint.getId());
    			}
            } catch (IOException e) {
                log.warn("Caught exception while sending message to Session " + webSocketWidgetServerEndpoint.getId(), e);
            }
		}
    }
}
