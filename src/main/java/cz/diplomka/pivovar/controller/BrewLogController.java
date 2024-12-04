package cz.diplomka.pivovar.controller;

import cz.diplomka.pivovar.entity.BrewLog;
import cz.diplomka.pivovar.service.BrewLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("brewLog")
public class BrewLogController {

    private final BrewLogService brewLogService;

    @PostMapping("{sessionId}")
    public ResponseEntity<BrewLog> createBrewLog(@RequestBody BrewLog brewLog, @PathVariable int sessionId) {
        return ResponseEntity.ok(brewLogService.createBrewLog(brewLog,sessionId));
    }
}
