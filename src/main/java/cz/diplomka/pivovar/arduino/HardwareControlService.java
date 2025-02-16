package cz.diplomka.pivovar.arduino;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class HardwareControlService {

    private final ArduinoService arduinoService;
    //private final SessionService sessionService;

    public void turnOnHeater(int targetTemperature) throws IOException {
        executeCommand("START_HEATING:" + targetTemperature, "Heater turned on successfully.", "Failed to turn on heater");
    }

    public void turnOffHeater() throws IOException {
        executeCommand("STOP_HEATING", "Heater turned off successfully.", "Failed to turn off heater");
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
            //sessionService.saveMashingTemperature(Double.parseDouble(temperaturesArray[0]));
        }
        if (temperaturesArray.length > 1) {
            if (!temperaturesArray[1].isEmpty()) {
                //sessionService.saveDougingTemperature(Double.parseDouble(temperaturesArray[1]));
            }
        }
    }

    private String[] sendGetTempAndSplitResponse() throws IOException {
        final String response = arduinoService.sendCommand("GET_TEMPS");
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
