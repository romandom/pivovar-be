//package cz.diplomka.pivovar.service;
//
//import jakarta.persistence.EntityNotFoundException;
//import lombok.RequiredArgsConstructor;
//import lombok.val;
//import org.springframework.stereotype.Service;
//
//@RequiredArgsConstructor
//@Service
//public class BrewLogService {
//
//    private final BrewSessionRepository brewSessionRepository;
//
//    private final BrewLogRepository brewLogRepository;
//
//    public BrewLog createBrewLog(BrewLog brewLog, int sessionId) {
//        return brewSessionRepository.findById(sessionId).map(recipe -> {
//            val brewLogSaved = brewLogRepository.save(brewLog);
//            recipe.getLogs().add(brewLogSaved);
//            return brewLogSaved;
//        }).orElseThrow(() -> new EntityNotFoundException("BrewSession not found with ID " + sessionId));
//    }
//}
