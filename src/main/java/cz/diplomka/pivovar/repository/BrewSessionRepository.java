package cz.diplomka.pivovar.repository;

import cz.diplomka.pivovar.model.BrewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrewSessionRepository extends JpaRepository<BrewSession, Integer> {
}
