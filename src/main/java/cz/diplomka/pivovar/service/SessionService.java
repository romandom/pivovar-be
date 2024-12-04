package cz.diplomka.pivovar.service;

import cz.diplomka.pivovar.entity.BrewSession;
import cz.diplomka.pivovar.repository.BrewSessionRepository;
import cz.diplomka.pivovar.repository.RecipeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SessionService {

    private final RecipeRepository recipeRepository;

    private final BrewSessionRepository brewSessionRepository;

    @Transactional
    public BrewSession createSession(BrewSession brewSession, int recipeId) {
        return recipeRepository.findById(recipeId).map(recipe -> {
            val brewSessionSaved = brewSessionRepository.save(brewSession);
            recipe.getSessions().add(brewSessionSaved);
            return brewSessionSaved;
        }).orElseThrow(() -> new EntityNotFoundException("Recipe not found with ID " + recipeId));
    }
}
