package cz.diplomka.pivovar.service;

import cz.diplomka.pivovar.dto.HistoryList;
import cz.diplomka.pivovar.repository.RecipeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

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
                        .limit(1)
                        .map(
                                brewSession -> new HistoryList(recipe.getId(), brewSession.getStartTime().toLocalDate(), recipe.getName(), brewSession.getStatus()))
                )
                .toList();
    }
}
