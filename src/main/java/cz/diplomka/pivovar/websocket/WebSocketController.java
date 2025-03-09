package cz.diplomka.pivovar.websocket;

import cz.diplomka.pivovar.arduino.HardwareControlService;
import cz.diplomka.pivovar.dto.SensorsResponseDto;
import cz.diplomka.pivovar.service.BrewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class WebSocketController {

    private final HardwareControlService hardwareControlService;
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, String> activeUsers = new ConcurrentHashMap<>();
    private final BrewService brewService;

    @Autowired
    public WebSocketController(HardwareControlService hardwareControlService, SimpMessagingTemplate messagingTemplate, BrewService brewService) {
        this.hardwareControlService = hardwareControlService;
        this.messagingTemplate = messagingTemplate;
        this.brewService = brewService;
    }

    public void addActiveUser(String sessionId, String username) {
        activeUsers.put(sessionId, username);
    }

    public void removeActiveUser(String sessionId) {
        activeUsers.remove(sessionId);
    }

    @Scheduled(fixedRate = 2000)
    public void sendTemperatureUpdates() {
        if (!activeUsers.isEmpty()) {
            try {
                final SensorsResponseDto sensorsData = hardwareControlService.getSensorsData();

                messagingTemplate.convertAndSend("/topic/sensors", sensorsData);
            } catch (Exception e) {
                messagingTemplate.convertAndSend("/topic/errors", e.getMessage());
            }
        }
    }

//    @Scheduled(fixedRate = 60000)
//    public void saveTemperatureUpdates() {
//        if (!activeUsers.isEmpty()) {
//            try {
//                final SensorsResponseDto sensorsData = hardwareControlService.getSensorsData();
//                brewService.saveBrewSensorData(sensorsData);
//            } catch (IOException | InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
}
