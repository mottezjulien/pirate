package fr.plop.contexts.game.config.scenario.persistence.possibility.consequence;

import fr.plop.contexts.game.config.scenario.persistence.possibility.consequence.entity.ScenarioPossibilityConsequenceAbstractEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScenarioPossibilityConsequenceRepository extends JpaRepository<ScenarioPossibilityConsequenceAbstractEntity, String> {


}
