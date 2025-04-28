package cz.diplomka.pivovar.repository;

import cz.diplomka.pivovar.constant.BrewingStatus;
import cz.diplomka.pivovar.model.BrewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrewSessionRepository extends JpaRepository<BrewSession, Integer> {
    List<BrewSession> findBrewSessionByStatus(BrewingStatus status);
}
