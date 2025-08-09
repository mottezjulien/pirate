package fr.plop.contexts.game.config.scenario.persistence.core;

import fr.plop.contexts.game.config.scenario.persistence.possibility.ScenarioPossibilityRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScenarioStepRepository extends JpaRepository<ScenarioStepEntity, String> {
    String FETCH_ALL = " LEFT JOIN FETCH step.label" +
            " LEFT JOIN FETCH step.targets step_target" +
            " LEFT JOIN FETCH step_target.label" +
            " LEFT JOIN FETCH step.possibilities possibility" +
            ScenarioPossibilityRepository.FETCH_ALL;

}
