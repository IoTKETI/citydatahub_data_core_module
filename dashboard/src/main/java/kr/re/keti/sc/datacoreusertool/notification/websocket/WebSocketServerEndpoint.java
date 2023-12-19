package kr.re.keti.sc.datacoreusertool.notification.websocket;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import kr.re.keti.sc.datacoreusertool.api.subscription.service.SubscriptionSVC;
import kr.re.keti.sc.datacoreusertool.notification.vo.WebSocketRegistVO;
import kr.re.keti.sc.datacoreusertool.util.BeanUtil;
import kr.re.keti.sc.datacoreusertool.util.ConvertUtil;
import kr.re.keti.sc.datacoreusertool.util.ValidateUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Web socket server end point.</br>
 * This class is not a Spring Bean and is newly created every time a session is connected.
 * @FileName WebSocketServerEndpoint.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Slf4j
@Component
@Data
@ServerEndpoint(value = "/events")
public class WebSocketServerEndpoint {
	
	/** Websocket Session Manager */
	private WebSocketSessionManager sessionManager = WebSocketSessionManager.INSTANCE;
	/** Websocket Session */
	private Session session;
	/** Websocket session id */
	private String id;
	/** entity subscriptionId (session 연결 후 subscriptionId 정보 수신) */
	private String subscriptionId;
	/** entity entityIds (session 연결 후 entityIds 정보 수신) */
	private List<String> entityIds;

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
    	log.info("Websocket onClose sessionId={}, subscriptionId={}", id, subscriptionId);

    	// 1. Check if subscription information is being used by another session
    	boolean useSubscription = false;
    	Map<String, WebSocketServerEndpoint> sessions = sessionManager.getSessions();
    	for(WebSocketServerEndpoint webSocketServerEndpoint : sessions.values()) {
    		if(id.equals(webSocketServerEndpoint.getId())) continue;
    		if(!ValidateUtil.isEmptyData(subscriptionId) && subscriptionId.equals(webSocketServerEndpoint.getSubscriptionId())) {
    			useSubscription = true;
    			break;
    		}
    	}

    	// 2. If the subscription information is not being used by any session, unsubscribe.
    	if(!useSubscription) {
    		SubscriptionSVC subscriptionSVC = (SubscriptionSVC) BeanUtil.getBean("subscriptionSVC");
        	ResponseEntity<Void> responseEntity = subscriptionSVC.deleteSubscription(subscriptionId);
        	log.info("Unsubscription to dataServiceBroker resultCode={}", responseEntity.getStatusCodeValue());
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

        WebSocketRegistVO webSocketRegistVO = ConvertUtil.getNgsiLdGson().fromJson(message, WebSocketRegistVO.class);
        if(webSocketRegistVO != null) {
        	this.entityIds = webSocketRegistVO.getEntityIds();
            this.subscriptionId = webSocketRegistVO.getSubscriptionId();
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
}
