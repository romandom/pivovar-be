package cz.diplomka.pivovar.arduino;

import cz.diplomka.pivovar.constant.BrewingVessel;
import cz.diplomka.pivovar.service.SessionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class HardwareControlService {

    private final ArduinoService arduinoService;
    private final SessionService sessionService;

    // Mash heater
    public void turnOnMashHeater(int targetTemperature) throws IOException {
        executeCommand("START_MASH_HEATING:" + targetTemperature, "Mash heater turned on successfully.", "Failed to turn on mash heater");
    }

    public void turnOffMashHeater() throws IOException {
        executeCommand("STOP_MASH_HEATING", "Mash heater turned off successfully.", "Failed to turn mash off heater");
    }

    // Worth heater
    public void turnOnWorthHeater(int targetTemperature) throws IOException {
        executeCommand("START_WORTH_HEATING:" + targetTemperature, "Worth heater turned on successfully.", "Failed to turn on worth heater");
    }

    public void turnOffWorthHeater(int targetTemperature) throws IOException {
        executeCommand("STOP_WORTH_HEATING:" + targetTemperature, "Worth heater turned off successfully.", "Failed to turn off worth heater");
    }

    // Mash mixing
    public void turnOnMashMixing() throws IOException {
        executeCommand("START_MASH_MIXING","Mash mixing turned on successfully.", "Failed to turn on mash mixing");
    }

    public void turnOffMashMixing() throws IOException {
        executeCommand("STOP_MASH_MIXING","Mash mixing turned off successfully.", "Failed to turn off mash mixing");
    }

    // Worth mixing
    public void turnOnWorthMixing() throws IOException {
        executeCommand("START_WORTH_MIXING","Worth mixing turned on successfully.", "Failed to turn on worth mixing");
    }

    public void turnOffWorthMixing() throws IOException {
        executeCommand("STOP_WORTH_MIXING","Worth mixing turned off successfully.", "Failed to turn off worth mixing");
    }

    // Transfer
    public void turnOnKettleTransfer() throws IOException {
        executeCommand("START_TRANSFER", "Transfer turned on successfully.", "Failed to turn on transfer");
    }

    public void turnOffKettleTransfer() throws IOException {
        executeCommand("STOP_TRANSFER", "Transfer turned off successfully.", "Failed to turn off transfer");
    }

    // temperatures
    public Map<String, String> getTemperature() throws IOException {
        final String[] temperaturesArray = sendGetTempAndSplitResponse();

        final Map<String, String> temperatures = new HashMap<>();
        temperatures.put("mashTemperature", temperaturesArray[0]);
        temperatures.put("worthTemperature", temperaturesArray[1]);

        return temperatures;
    }

    public void getAndSaveTemperatures() throws IOException {
        final String[] temperaturesArray = sendGetTempAndSplitResponse();

        if (!temperaturesArray[0].isEmpty()) {
            sessionService.saveActualTemperatures(Double.parseDouble(temperaturesArray[0]), BrewingVessel.MAIN_KETTLE);
        }
        if (temperaturesArray.length > 1) {
            if (!temperaturesArray[1].isEmpty()) {
                sessionService.saveActualTemperatures(Double.parseDouble(temperaturesArray[1]), BrewingVessel.DECOCTION_KETTLE);
            }
        }
    }

    private String[] sendGetTempAndSplitResponse() throws IOException {
        final String response = arduinoService.sendCommand("GET_TEMP");
        return response.split(",");
    }

    private void executeCommand(String command, String successMessage, String errorMessage) throws IOException {
        String response = arduinoService.sendCommand(command);
        if ("OK".equals(response)) {
            System.out.println(successMessage);
        } else {
            System.err.println(errorMessage + ": " + response);
        }
    }
}
