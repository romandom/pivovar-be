package cz.diplomka.pivovar.arduino;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.diplomka.pivovar.dto.SensorsResponseDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@AllArgsConstructor
public class HardwareControlService {

    private final ArduinoService arduinoService;
    private final ObjectMapper mapper = new ObjectMapper();

    public void turnOnHeater(int targetTemperature) throws IOException {
        executeCommand("START_HEATING:" + targetTemperature, "Heater turned on successfully.", "Failed to turn on heater");
    }

    public void turnOffHeater() throws IOException {
        executeCommand("STOP_HEATING", "Heater turned off successfully.", "Failed to turn off heater");
    }

    // Mash mixing
    public void turnOnMashMixing() throws IOException {
        executeCommand("START_MASH_MIXING", "Mash mixing turned on successfully.", "Failed to turn on mash mixing");
    }

    public void turnOffMashMixing() throws IOException {
        executeCommand("STOP_MASH_MIXING", "Mash mixing turned off successfully.", "Failed to turn off mash mixing");
    }

    // Worth mixing
    public void turnOnWorthMixing() throws IOException {
        executeCommand("START_WORTH_MIXING", "Worth mixing turned on successfully.", "Failed to turn on worth mixing");
    }

    public void turnOffWorthMixing() throws IOException {
        executeCommand("STOP_WORTH_MIXING", "Worth mixing turned off successfully.", "Failed to turn off worth mixing");
    }

    public SensorsResponseDto getSensorsData() throws IOException, InterruptedException {
        String jsonResponse = arduinoService.readSerialData();
        return mapper.readValue(jsonResponse, SensorsResponseDto.class);
    }

    private void executeCommand(String command, String successMessage, String errorMessage) throws IOException {
        arduinoService.sendCommand(command);
        String response = arduinoService.readSerialData();
        if ("OK".equals(response)) {
            log.debug("{} Using command {}", successMessage, command);
        } else {
            log.error(errorMessage);
        }
    }
}
