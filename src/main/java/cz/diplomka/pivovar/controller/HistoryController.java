package cz.diplomka.pivovar.controller;

import cz.diplomka.pivovar.dto.HistoryList;
import cz.diplomka.pivovar.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("history")
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping("list")
    public ResponseEntity<List<HistoryList>> getAllRecipes() {
        return ResponseEntity.ok(historyService.getHistoryList());
    }

}
