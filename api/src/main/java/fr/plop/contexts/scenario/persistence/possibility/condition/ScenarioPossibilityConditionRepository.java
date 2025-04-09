package fr.plop.contexts.scenario.persistence.possibility.condition;

import fr.plop.contexts.scenario.persistence.possibility.condition.entity.ScenarioPossibilityConditionAbstractEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScenarioPossibilityConditionRepository extends JpaRepository<ScenarioPossibilityConditionAbstractEntity, String> {


}
