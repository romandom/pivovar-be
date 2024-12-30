package cz.diplomka.pivovar.arduino;

import com.fazecast.jSerialComm.SerialPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ArduinoService {

    private final SerialPort serialPort;

    public String sendCommand(String command) throws IOException {
        if (serialPort == null || !serialPort.isOpen()) {
            throw new IllegalStateException("Serial port is not open");
        }

        serialPort.getOutputStream().write((command.trim() + "\n").getBytes());
        serialPort.getOutputStream().flush();

        byte[] buffer = new byte[1024];
        int len = serialPort.getInputStream().read(buffer);
        return new String(buffer, 0, len).trim();
    }
}

