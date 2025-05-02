package fr.plop.contexts.game.config.scenario.persistence.possibility.condition;

import fr.plop.contexts.game.config.scenario.persistence.possibility.condition.entity.ScenarioPossibilityConditionAbstractEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScenarioPossibilityConditionRepository extends JpaRepository<ScenarioPossibilityConditionAbstractEntity, String> {


}
