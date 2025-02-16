package cz.diplomka.pivovar.service;

import cz.diplomka.pivovar.constant.BrewingStatus;
import cz.diplomka.pivovar.repository.BrewSessionRepository;
import cz.diplomka.pivovar.repository.RecipeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
@Transactional
public class SessionService {

    private final RecipeService recipeService;

    private final BrewSessionRepository brewSessionRepository;

    //public void saveTemperatureLogToSession(long recipeId, )

//    public BrewSession createSession(BrewSession brewSession, int recipeId) {
//        return recipeRepository.findById(recipeId).map(recipe -> {
//            val brewSessionSaved = brewSessionRepository.save(brewSession);
//            recipe.getSessions().add(brewSessionSaved);
//            return brewSessionSaved;
//        }).orElseThrow(() -> new EntityNotFoundException("Recipe not found with ID " + recipeId));
//    }
//
//    public void saveActualTemperatures(double temperature, BrewingVessel brewingVessel) {
//        val sessions = brewSessionRepository.findAll();
//        val actualSession = sessions.stream().filter(s -> s.getStatus().equals(BrewSessionStatus.IN_PROGRESS)).findFirst().orElseThrow();
//
//        val brewLog = new BrewLog();
//        brewLog.setTemperature(temperature);
//        brewLog.setVessel(brewingVessel);
//        brewLog.setTimestamp(LocalDateTime.now());
//
//        actualSession.getLogs().add(brewLog);
//        brewSessionRepository.save(actualSession);
//    }

    public void saveMashingTemperature(double temperature) {
        val brewedRecipeId = recipeService.getBrewedRecipe();
        if (brewedRecipeId == null) {
            return;
        }

        //val bws = brewSessionRepository.findBrewSessionsByStatus(BrewingStatus.IN_PROGRESS);

        //bws.stream().filter(bw -> bw.)

    }
//
//    public void changeSessionStatus() {
//        val sessions = brewSessionRepository.findAll();
//        val actualSession = sessions.stream().filter(s -> s.getStatus().equals(BrewSessionStatus.IN_PROGRESS)).findFirst().orElseThrow();
//        actualSession.setStatus(BrewSessionStatus.CANCELLED);
//        actualSession.setEndTime(LocalDateTime.now());
//        brewSessionRepository.save(actualSession);
//    }
}
