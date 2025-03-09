package cz.diplomka.pivovar.service;

import cz.diplomka.pivovar.dto.HistoryListDto;
import cz.diplomka.pivovar.dto.TemperatureGraphDto;
import cz.diplomka.pivovar.model.BrewLog;
import cz.diplomka.pivovar.model.BrewSession;
import cz.diplomka.pivovar.repository.BrewSessionRepository;
import cz.diplomka.pivovar.repository.RecipeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional
public class HistoryService {

    private final RecipeRepository recipeRepository;
    private final BrewSessionRepository brewSessionRepository;

    public List<HistoryListDto> getHistoryList() {
        val recipes = recipeRepository.findAll();
        return recipes
                .stream()
                .flatMap(recipe -> recipe.getBrewSessions().stream()
                        .map(brewSession -> new HistoryListDto(
                                brewSession.getId(),
                                brewSession.getStartTime().toLocalDate(),
                                recipe.getName(),
                                brewSession.getStatus()
                        ))
                )
                .sorted(Comparator.comparing(HistoryListDto::date).reversed())
                .toList();
    }

    public List<TemperatureGraphDto> getTemperatureByHistoryId(int historyId) {
        final BrewSession brewSession = brewSessionRepository.findById(historyId).orElseThrow();

        final LocalDateTime startTime = brewSession.getStartTime();

        return brewSession.getBrewLogs()
                .stream()
                .sorted(Comparator.comparing(BrewLog::getTimestamp))
                .map(brewLog -> {
                    final long minutes = Duration.between(brewSession.getStartTime(), brewLog.getTimestamp()).toMinutes();
                    return new TemperatureGraphDto(minutes, brewLog.getMashTemperature(), brewLog.getWorthTemperature());
                }).toList();
    }

}
