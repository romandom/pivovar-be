//package cz.diplomka.pivovar.websocket;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import cz.diplomka.pivovar.arduino.HardwareControlService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Controller;
//import org.springframework.scheduling.annotation.Scheduled;
//
//import java.io.IOException;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Controller
//public class WebSocketController {
//
//    private final HardwareControlService hardwareControlService;
//    private final SimpMessagingTemplate messagingTemplate;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//    private final Map<String, String> activeUsers = new ConcurrentHashMap<>();
//
//    @Autowired
//    public WebSocketController(HardwareControlService hardwareControlService, SimpMessagingTemplate messagingTemplate) {
//        this.hardwareControlService = hardwareControlService;
//        this.messagingTemplate = messagingTemplate;
//    }
//
//    public void addActiveUser(String sessionId, String username) {
//        activeUsers.put(sessionId, username);
//    }
//
//    public void removeActiveUser(String sessionId) {
//        activeUsers.remove(sessionId);
//    }
//
//    @Scheduled(fixedRate = 2000)
//    public void sendTemperatureUpdates() {
//        if (!activeUsers.isEmpty()) {
//            try {
//                final Map<String, String> temperatures = hardwareControlService.getTemperature();
//
//                final String jsonTemperatures = objectMapper.writeValueAsString(temperatures);
//
//                messagingTemplate.convertAndSend("/topic/temperature", jsonTemperatures);
//            } catch (Exception e) {
//                messagingTemplate.convertAndSend("/topic/errors", e.getMessage());
//            }
//        }
//    }
//
//    @Scheduled(fixedRate = 60000)
//    public void saveTemperatureUpdates() {
//        if (!activeUsers.isEmpty()) {
//            try {
//                hardwareControlService.getAndSaveTemperatures();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
//}
