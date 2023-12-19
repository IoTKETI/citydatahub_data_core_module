package kr.re.keti.sc.datacoreusertool.notification.websocket;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

/**
 * WebSocketSessionManager enumerate
 * @FileName WebSocketSessionManager.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Slf4j
public enum WebSocketSessionManager {
	INSTANCE;

	private Map<String, WebSocketServerEndpoint> sessionMap = new ConcurrentHashMap<>();

	/**
	 * Add session
	 * @param webSocketServerEndpoint	WebSocketServerEndpoint
	 */
    public void addSession(WebSocketServerEndpoint webSocketServerEndpoint) {
    	sessionMap.put(webSocketServerEndpoint.getId(), webSocketServerEndpoint);
    }
    
    /**
     * Remove session
     * @param webSocketServerEndpointId		End point ID of web socket server
     * @return								Result of remove session
     */
    public WebSocketServerEndpoint removeSession(String webSocketServerEndpointId) {
    	return sessionMap.remove(webSocketServerEndpointId);
    }
    
    /**
     * Get sessions
     * @return	Session map
     */
    public Map<String, WebSocketServerEndpoint> getSessions() {
    	return sessionMap;
    }

    /**
     * Send messages only to the Websocket session where the entity information received as a notification is registered
     * @param entityId Notification EntityId
     * @param message Notification Message
     */
    public void sendToEntitySession(String entityId, String message) {
    	for(WebSocketServerEndpoint webSocketServerEndpoint : sessionMap.values()) {
    		List<String> entityIds = webSocketServerEndpoint.getEntityIds();
    		if(entityIds != null && !entityIds.contains(entityId)) {
    			continue;
    		}
    		try {
    			webSocketServerEndpoint.getSession().getBasicRemote().sendText(message);
            } catch (IOException e) {
                log.warn("Caught exception while sending message to Session " + webSocketServerEndpoint.getId(), e);
            }
    	}
    }
    
}
