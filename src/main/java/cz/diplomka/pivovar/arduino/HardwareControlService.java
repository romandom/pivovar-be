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
        executeCommand("HEATING:" + targetTemperature + "\n");
    }

    public void turnOffHeater() throws IOException {
        executeCommand("STOP-HEATING" + "\n");
    }


    public SensorsResponseDto getSensorsData() throws IOException, InterruptedException {
        String jsonResponse = arduinoService.readSerialData();
        return mapper.readValue(jsonResponse, SensorsResponseDto.class);
    }

    private void executeCommand(String command) throws IOException {
        arduinoService.sendCommand(command);
        log.debug("Using command {}", command);
    }
}
