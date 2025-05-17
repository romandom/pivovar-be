package cz.diplomka.pivovar.arduino;

import com.fazecast.jSerialComm.SerialPort;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArduinoService {

    private final SerialPort serialPort;
    private final AtomicReference<String> lastValidMessage = new AtomicReference<>();

    @PostConstruct
    public void startSerialReader() {
        Thread readerThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(serialPort.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("{") && line.endsWith("}")) {
                        lastValidMessage.set(line); // nastaví novú správu
                        log.debug("Prijatý JSON: {}", line);
                    } else {
                        log.debug("Invalid JSON format, ignoring: {}", line);
                        // nič nemeníme
                    }
                }
            } catch (Exception e) {
                log.error("Chyba pri čítaní zo sériového portu", e);
            }
        });
        readerThread.setDaemon(true);
        readerThread.start();
    }

    public void sendCommand(String command) {
        try {
            serialPort.getOutputStream().write((command.trim() + "\n").getBytes());
            serialPort.getOutputStream().flush();
            log.info("Odoslaný príkaz: {}", command);
        } catch (Exception e) {
            log.error("Chyba pri odosielaní príkazu na Arduino", e);
        }
    }

    public String getLastValidMessage() {
        return lastValidMessage.get();
    }
}
