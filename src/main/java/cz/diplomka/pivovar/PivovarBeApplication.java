package cz.diplomka.pivovar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PivovarBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(PivovarBeApplication.class, args);
    }

}
