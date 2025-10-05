package fr.plop.contexts.game.config.scenario.persistence.core;

import fr.plop.contexts.game.config.scenario.persistence.possibility.ScenarioPossibilityRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScenarioConfigRepository extends JpaRepository<ScenarioConfigEntity, String> {
    String FETCH_ALL = " LEFT JOIN FETCH scenario.steps step" +
            " LEFT JOIN FETCH step.targets target" +
            " LEFT JOIN FETCH target.label label" +
            " LEFT JOIN FETCH target.description description" +
            " LEFT JOIN FETCH step.possibilities possibility" +
            ScenarioPossibilityRepository.FETCH_ALL;

}
