package cz.diplomka.pivovar.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.diplomka.pivovar.arduino.HardwareControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final HardwareControlService hardwareControlService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(fixedRate = 2000)
    public void sendTemperatureUpdates() {
        try {
            final Map<String, String> temperatures = hardwareControlService.getTemperature();

            final String jsonTemperatures = objectMapper.writeValueAsString(temperatures);

            messagingTemplate.convertAndSend("/topic/temperature", jsonTemperatures);
        } catch (Exception e) {
            messagingTemplate.convertAndSend("/topic/errors", e.getMessage());
        }
    }
}

