package cz.diplomka.pivovar.arduino;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.diplomka.pivovar.dto.SensorsResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class HardwareControlService {

    private final ArduinoService arduinoService;
    private final ObjectMapper mapper = new ObjectMapper();

    private volatile SensorsResponseDto lastKnownSensorsData;

    public void turnOnHeater(int targetTemperature) {
        executeCommand("HEATING:" + targetTemperature + "\n");
    }

    public void turnOffHeater() {
        executeCommand("STOP-HEATING" + "\n");
    }

    public SensorsResponseDto getSensorsData() throws IOException {
        String jsonResponse = arduinoService.getLastValidMessage();
        if (jsonResponse != null) {
            SensorsResponseDto sensors = mapper.readValue(jsonResponse, SensorsResponseDto.class);
            lastKnownSensorsData = sensors;
            return sensors;
        }

        if (lastKnownSensorsData != null) {
            log.warn("No new data received — using last known sensor data.");
            return lastKnownSensorsData;
        }

        throw new IOException("No data received yet from Arduino and no fallback available.");
    }

    private void executeCommand(String command) {
        arduinoService.sendCommand(command);
        log.debug("Using command {}", command);
    }

    public void turnOnMashMixing() {
        executeCommand("START-MASH-MIXING");
    }

    public void turnOffMashMixing() {
        executeCommand("STOP-MASH-MIXING");
    }

    public void turnOnWorthMixing() {
        executeCommand("START-WORTH-MIXING");
    }

    public void turnOffWorthMixing() {
        executeCommand("STOP-WORTH-MIXING");
    }
}
