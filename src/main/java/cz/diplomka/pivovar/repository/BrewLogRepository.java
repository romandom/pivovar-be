package cz.diplomka.pivovar.repository;

import cz.diplomka.pivovar.entity.BrewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrewLogRepository extends JpaRepository<BrewLog, Integer> {
}
