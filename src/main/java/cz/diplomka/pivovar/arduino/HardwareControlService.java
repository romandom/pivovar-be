package cz.diplomka.pivovar.arduino;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.diplomka.pivovar.dto.SensorsResponseDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@AllArgsConstructor
public class HardwareControlService {

    private final ArduinoService arduinoService;

    public void turnOnHeater(int targetTemperature) throws IOException, InterruptedException {
        executeCommand("START_HEATING:" + targetTemperature, "Heater turned on successfully.", "Failed to turn on heater");
    }

    public void turnOffHeater() throws IOException, InterruptedException {
        executeCommand("STOP_HEATING", "Heater turned off successfully.", "Failed to turn off heater");
    }

    // Mash mixing
    public void turnOnMashMixing() throws IOException, InterruptedException {
        executeCommand("START_MASH_MIXING","Mash mixing turned on successfully.", "Failed to turn on mash mixing");
    }

    public void turnOffMashMixing() throws IOException, InterruptedException {
        executeCommand("STOP_MASH_MIXING","Mash mixing turned off successfully.", "Failed to turn off mash mixing");
    }

    // Worth mixing
    public void turnOnWorthMixing() throws IOException, InterruptedException {
        executeCommand("START_WORTH_MIXING","Worth mixing turned on successfully.", "Failed to turn on worth mixing");
    }

    public void turnOffWorthMixing() throws IOException, InterruptedException {
        executeCommand("STOP_WORTH_MIXING","Worth mixing turned off successfully.", "Failed to turn off worth mixing");
    }

    public SensorsResponseDto getSensorsData() throws IOException, InterruptedException {
        final ObjectMapper mapper = new ObjectMapper();

        for (int attempt = 0; attempt < 3; attempt++) {
            String jsonResponse = arduinoService.sendCommand("GET_SENSORS");

            if (jsonResponse != null && !jsonResponse.trim().isEmpty()) {
                try {
                    JsonNode jsonNode = mapper.readTree(jsonResponse);
                    if (jsonNode.isObject()) {
                        return mapper.readValue(jsonResponse, SensorsResponseDto.class);
                    }
                } catch (JsonProcessingException e) {
                    System.err.println("WARNING: Invalid JSON format, retrying... (" + (attempt + 1) + "/3)");
                }
            } else {
                System.err.println("WARNING: Received empty JSON response, retrying... (" + (attempt + 1) + "/3)");
            }

            Thread.sleep(500); //
        }
        throw new IOException("ERROR: Failed to receive valid JSON after 3 attempts.");


//        final String response = arduinoService.sendCommand("GET_SENSORS");
//        final JsonNode jsonNode = new ObjectMapper().readTree(response);
//
//        final double mashTemperature = jsonNode.get("mashTemperature").asDouble();
//        final double worthTemperature = jsonNode.get("worthTemperature").asDouble();
//        final double mashWeight = jsonNode.get("mashWeight").asDouble();
//        final double worthWeight = jsonNode.get("worthWeight").asDouble();
//
//        return SensorsResponseDto.builder()
//                .mashTemperature(mashTemperature)
//                .worthTemperature(worthTemperature)
//                .mashWeight(mashWeight)
//                .worthWeight(worthWeight)
//                .build();
    }

    private void executeCommand(String command, String successMessage, String errorMessage) throws IOException, InterruptedException {
        String response = arduinoService.sendCommand(command);
        if ("OK".equals(response)) {
            System.out.println(successMessage);
        } else {
            System.err.println(errorMessage + ": " + response);
        }
    }
}
