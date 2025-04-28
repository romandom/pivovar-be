package cz.diplomka.pivovar.arduino;

import com.fazecast.jSerialComm.SerialPort;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ArduinoConfig {

    private SerialPort serialPort;

    @Bean
    public SerialPort serialPort() {
        serialPort = SerialPort.getCommPort("COM4");
        //serialPort = SerialPort.getCommPort("/dev/ttyUSB0");
        serialPort.setComPortParameters(250000, 8, 1, 0);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);

        if (serialPort.openPort()) {
            log.debug("Succesfully opened serial port.");
        } else {
            throw new IllegalStateException("Cannot open serial port");
        }

        return serialPort;
    }

    @PreDestroy
    public void closePort() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            log.debug("Succesfully closed serial port.");
        }
    }
}

