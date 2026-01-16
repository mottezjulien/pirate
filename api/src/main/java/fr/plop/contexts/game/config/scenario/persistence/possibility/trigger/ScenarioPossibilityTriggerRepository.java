package fr.plop.contexts.game.config.scenario.persistence.possibility.trigger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ScenarioPossibilityTriggerRepository extends JpaRepository<ScenarioPossibilityTriggerEntity, String> {

    @Query("SELECT DISTINCT t FROM ScenarioPossibilityTriggerEntity t LEFT JOIN FETCH t.subs WHERE t.id = :id")
    Optional<ScenarioPossibilityTriggerEntity> findByIdWithSubs(@Param("id") String id);
}
