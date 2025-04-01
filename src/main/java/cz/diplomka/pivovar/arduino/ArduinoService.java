package cz.diplomka.pivovar.arduino;

import com.fazecast.jSerialComm.SerialPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArduinoService {

    private final SerialPort serialPort;

    public void sendCommand(String command) throws IOException {
        if (serialPort == null || !serialPort.isOpen()) {
            throw new IllegalStateException("Serial port is not open");
        }

        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING,5000,0);
        serialPort.getOutputStream().write((command.trim() + "\n").getBytes());
        serialPort.getOutputStream().flush();
        //return null;
    }

    public String readSerialData() throws IOException {
        if (serialPort == null || !serialPort.isOpen()) {
            throw new IllegalStateException("Serial port is not open");
        }
        var reader = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));

        String json;
        while (true) {
            json = reader.readLine();

            if (json == null || json.trim().isEmpty()) {
                log.debug("Empty line received, waiting for valid JSON...");
                continue;
            }

            json = json.trim();

            if (json.startsWith("{") && json.endsWith("}")) {
                return json;
            } else {
                log.debug("Invalid JSON format, ignoring: {}", json);
            }
        }
    }
}

