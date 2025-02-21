package cz.diplomka.pivovar.controller;

import cz.diplomka.pivovar.dto.HistoryListDto;
import cz.diplomka.pivovar.model.BrewSession;
import cz.diplomka.pivovar.repository.BrewSessionRepository;
import cz.diplomka.pivovar.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
