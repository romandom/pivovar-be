//package cz.diplomka.pivovar.arduino;
//
//import com.fazecast.jSerialComm.SerialPort;
//import jakarta.annotation.PreDestroy;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//
//@Configuration
//public class ArduinoConfig {
//
//    private SerialPort serialPort;
//
//    @Bean
//    public SerialPort serialPort() {
//        serialPort = SerialPort.getCommPort("COM3");
//        //serialPort = SerialPort.getCommPort("/dev/ttyUSB0");
//        serialPort.setComPortParameters(9600, 8, 1, 0);
//        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);
//
//        if (serialPort.openPort()) {
//            System.out.println("Succesfully opened serial port.");
//        } else {
//            throw new IllegalStateException("Cannot open serial port");
//        }
//
//        return serialPort;
//    }
//
//    @PreDestroy
//    public void closePort() {
//        if (serialPort != null && serialPort.isOpen()) {
//            serialPort.closePort();
//            System.out.println("Serial port closed.");
//        }
//    }
//}
//
