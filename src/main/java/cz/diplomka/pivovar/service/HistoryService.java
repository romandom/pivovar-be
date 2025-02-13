package cz.diplomka.pivovar.service;

import cz.diplomka.pivovar.dto.HistoryList;
import cz.diplomka.pivovar.repository.RecipeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional
public class HistoryService {

    private final RecipeRepository recipeRepository;

    public List<HistoryList> getHistoryList() {
        val recipes = recipeRepository.findAll();
        return recipes
                .stream()
                .flatMap(recipe -> recipe.getBrewSessions().stream()
                        .map(brewSession -> new HistoryList(
                                brewSession.getId(),
                                brewSession.getStartTime().toLocalDate(),
                                recipe.getName(),
                                brewSession.getStatus()
                        ))
                )
                .sorted(Comparator.comparing(HistoryList::date).reversed())
                .toList();
    }

}
