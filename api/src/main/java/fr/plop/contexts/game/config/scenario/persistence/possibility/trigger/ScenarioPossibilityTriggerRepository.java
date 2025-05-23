package fr.plop.contexts.game.config.scenario.persistence.possibility.trigger;

import fr.plop.contexts.game.config.scenario.persistence.possibility.trigger.entity.ScenarioPossibilityTriggerAbstractEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScenarioPossibilityTriggerRepository extends JpaRepository<ScenarioPossibilityTriggerAbstractEntity, Long> {


}
