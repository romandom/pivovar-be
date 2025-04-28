package cz.diplomka.pivovar.controller;

import cz.diplomka.pivovar.dto.*;
import cz.diplomka.pivovar.model.BrewLog;
import cz.diplomka.pivovar.model.BrewSession;
import cz.diplomka.pivovar.repository.BrewSessionRepository;
import cz.diplomka.pivovar.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("history")
public class HistoryController {

    private final HistoryService historyService;
    private final BrewSessionRepository brewSessionRepository;

    @GetMapping("list")
    public ResponseEntity<List<HistoryListDto>> getAllHistory() {
        return ResponseEntity.ok(historyService.getHistoryList());
    }

    @GetMapping("{id}")
    public ResponseEntity<BrewSession> getHistoryById(@PathVariable int id) {
        return ResponseEntity.ok(brewSessionRepository.findById(id).orElseThrow());
    }

    @GetMapping("/history-data/{historyId}")
    public ResponseEntity<List<HistoryGraphDataDto>> getTemperatureHistoryByHistoryId(@PathVariable int historyId) {
        return ResponseEntity.ok(historyService.getHistoryGraphData(historyId));
    }

    @PostMapping("/{historyId}")
    public void postSessions(@RequestBody List<BrewLog> logs, @PathVariable int historyId) {
        BrewSession brewSession = brewSessionRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("BrewSession s ID " + historyId + " neexistuje"));
        brewSession.getBrewLogs().addAll(logs);
        brewSessionRepository.save(brewSession);
    }
}
