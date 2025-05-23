package cz.diplomka.pivovar.arduino;

import com.fazecast.jSerialComm.SerialPort;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Configuration
public class ArduinoConfig {

    private SerialPort serialPort;

    @Bean
    public SerialPort serialPort() {
        //serialPort = SerialPort.getCommPort("COM4");
        serialPort = SerialPort.getCommPort("/dev/ttyACM0");
        serialPort.setComPortParameters(115200, 8, 1, 0);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

        if (serialPort.openPort()) {
            log.debug("Succesfully opened serial port.");
        } else {
            throw new IllegalStateException("Cannot open serial port");
        }

        return serialPort;
    }

    @Bean
    public BlockingQueue<String> serialQueue() {
        return new LinkedBlockingQueue<>();
    }

    @PreDestroy
    public void closePort() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            log.debug("Succesfully closed serial port.");
        }
    }
}

