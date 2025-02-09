//package cz.diplomka.pivovar.websocket;
//
//import org.springframework.context.event.EventListener;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.messaging.SessionConnectEvent;
//import org.springframework.web.socket.messaging.SessionDisconnectEvent;
//
//@Component
//public class WebSocketEventListener {
//
//    private final WebSocketController webSocketController;
//
//    public WebSocketEventListener(WebSocketController webSocketController) {
//        this.webSocketController = webSocketController;
//    }
//
//    @EventListener
//    public void handleSessionConnect(SessionConnectEvent event) {
//        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
//        String sessionId = headerAccessor.getSessionId();
//        String username = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : "Unknown User";
//
//        webSocketController.addActiveUser(sessionId, username);
//    }
//
//    @EventListener
//    public void handleSessionDisconnect(SessionDisconnectEvent event) {
//        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
//        String sessionId = headerAccessor.getSessionId();
//
//        webSocketController.removeActiveUser(sessionId);
//    }
//}
//
