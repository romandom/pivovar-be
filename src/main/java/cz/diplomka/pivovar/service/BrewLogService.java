package cz.diplomka.pivovar.service;

import cz.diplomka.pivovar.entity.BrewLog;
import cz.diplomka.pivovar.repository.BrewLogRepository;
import cz.diplomka.pivovar.repository.BrewSessionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class BrewLogService {

    private final BrewSessionRepository brewSessionRepository;

    private final BrewLogRepository brewLogRepository;

    public BrewLog createBrewLog(BrewLog brewLog, int sessionId) {
        return brewSessionRepository.findById(sessionId).map(recipe -> {
            val brewLogSaved = brewLogRepository.save(brewLog);
            recipe.getLogs().add(brewLogSaved);
            return brewLogSaved;
        }).orElseThrow(() -> new EntityNotFoundException("BrewSession not found with ID " + sessionId));
    }
}
