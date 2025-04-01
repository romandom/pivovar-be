package cz.diplomka.pivovar.controller;

import cz.diplomka.pivovar.arduino.HardwareControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("hardware")
public class HardwareController {

    private final HardwareControlService hardwareControlService;

    @PostMapping("temperature/mash/{temperature}")
    public void setMashTemperature(@PathVariable("temperature") int temperature) throws IOException {
        hardwareControlService.turnOnHeater(temperature);
    }
}
