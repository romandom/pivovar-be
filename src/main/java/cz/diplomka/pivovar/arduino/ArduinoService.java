package cz.diplomka.pivovar.arduino;

import com.fazecast.jSerialComm.SerialPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ArduinoService {

    private final SerialPort serialPort;

    public String sendCommand(String command) throws IOException, InterruptedException {
        if (serialPort == null || !serialPort.isOpen()) {
            throw new IllegalStateException("Serial port is not open");
        }

        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING,5000,0);
        serialPort.getOutputStream().write((command.trim() + "\n").getBytes());
        serialPort.getOutputStream().flush();

        Thread.sleep(2000);

        byte[] buffer = new byte[1024];
        int len = serialPort.getInputStream().read(buffer);
        return new String(buffer, 0, len).trim();
        //return null;
    }
}

