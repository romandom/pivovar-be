package cz.diplomka.pivovar.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.diplomka.pivovar.arduino.ArduinoService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ArduinoService arduinoService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(fixedRate = 2000)
    public void sendTemperatureUpdates() {
        try {
            final String response = arduinoService.sendCommand("GET_TEMP");
            final String[] temperaturesArray = response.split(",");

            final Map<String, String> temperatures = new HashMap<>();
            temperatures.put("sensor1", temperaturesArray[0]);
            temperatures.put("sensor2", temperaturesArray[1]);

            final String jsonTemperatures = objectMapper.writeValueAsString(temperatures);

            messagingTemplate.convertAndSend("/topic/temperature", jsonTemperatures);
        } catch (Exception e) {
            messagingTemplate.convertAndSend("/topic/errors", e.getMessage());
        }
    }
}

