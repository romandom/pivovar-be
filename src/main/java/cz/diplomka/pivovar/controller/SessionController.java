package cz.diplomka.pivovar.controller;

import cz.diplomka.pivovar.entity.BrewSession;
import cz.diplomka.pivovar.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("session")
public class SessionController {

    private final SessionService sessionService;

    @PostMapping("/{recipeId}")
    public ResponseEntity<BrewSession> createSession(@RequestBody BrewSession session, @PathVariable int recipeId) {
        return ResponseEntity.ok(sessionService.createSession(session, recipeId));
    }

    @PostMapping("/cancelled")
    public ResponseEntity<Void> changeSessionStatus() {
        sessionService.changeSessionStatus();
        return ResponseEntity.noContent().build();
    }
}
