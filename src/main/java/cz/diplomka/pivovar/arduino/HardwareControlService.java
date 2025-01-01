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

    // Transfer to worth kettle
    public void turnOnWorthKettleTransfer() throws IOException {
        executeCommand("START_WORTH_TRANSFER", "Worth transfer turned on successfully.", "Failed to turn on worth transfer");
    }

    public void turnOffWorthKettleTransfer() throws IOException {
        executeCommand("STOP_WORTH_TRANSFER", "Worth transfer turned off successfully.", "Failed to turn off worth transfer");
    }

    // Transfer to mash kettle
    public void turnOnMashKettleTransfer() throws IOException {
        executeCommand("START_MASH_TRANSFER", "Mash transfer turned on successfully.", "Failed to turn on mash transfer");
    }

    public void turnOffMashKettleTransfer() throws IOException {
        executeCommand("STOP_MASH_TRANSFER", "Mash transfer turned off successfully.", "Failed to turn off mash transfer");
    }

    // Mash selenoid
    public void openMashKettle() throws IOException {
        executeCommand("OPEN_MASH_KETTLE", "Mash kettle opened successfully.", "Failed to open mash kettle");
    }

    public void closeMashKettle() throws IOException {
        executeCommand("CLOSE_MASH_KETTLE", "Mash kettle closed successfully.", "Failed to open mash kettle");
    }

    // Worth selenoid
    public void openWorthKettle() throws IOException {
        executeCommand("OPEN_WORTH_KETTLE", "Worth kettle opened successfully.", "Failed to open worth kettle");
    }

    public void closeWorthKettle() throws IOException {
        executeCommand("CLOSE_WORTH_KETTLE", "Worth kettle closed successfully.", "Failed to open worth kettle");
    }

    // temperatures
    public Map<String, String> getTemperature() throws IOException {
        final String response = arduinoService.sendCommand("GET_TEMP");
        final String[] temperaturesArray = response.split(",");

        final Map<String, String> temperatures = new HashMap<>();
        temperatures.put("mashTemperature", temperaturesArray[0]);
        temperatures.put("worthTemperature", temperaturesArray[1]);

        return temperatures;
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
