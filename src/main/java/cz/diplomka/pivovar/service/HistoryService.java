package cz.diplomka.pivovar.service;

import cz.diplomka.pivovar.dto.HistoryGraphDataDto;
import cz.diplomka.pivovar.dto.HistoryListDto;
import cz.diplomka.pivovar.model.BrewLog;
import cz.diplomka.pivovar.model.BrewSession;
import cz.diplomka.pivovar.repository.BrewSessionRepository;
import cz.diplomka.pivovar.repository.RecipeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class HistoryService {

    private final RecipeRepository recipeRepository;
    private final BrewSessionRepository brewSessionRepository;

    public List<HistoryListDto> getHistoryList() {
        val recipes = recipeRepository.findAll();
        log.debug("Getting history list");
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

    public List<HistoryGraphDataDto> getHistoryGraphData(int historyId) {
        final BrewSession brewSession = brewSessionRepository.findById(historyId).orElseThrow();
        log.debug("Getting temperature graph by history id {}", historyId);

        return brewSession.getBrewLogs()
                .stream()
                .sorted(Comparator.comparing(BrewLog::getTimestamp))
                .map(brewLog -> {
                    final long minutes = Duration.between(brewSession.getStartTime(), brewLog.getTimestamp()).toMinutes();
                    return HistoryGraphDataDto.builder()
                            .minute(minutes)
                            .mashTemperature(brewLog.getMashTemperature())
                            .worthTemperature(brewLog.getWorthTemperature())
                            .power(brewLog.getPower())
                            .mashWeight(brewLog.getMashWeight())
                            .worthHeight(brewLog.getWorthHeight())
                            .build();
                }).toList();
    }
}
