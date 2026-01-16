package fr.plop.contexts.game.config.scenario.persistence.core;

import fr.plop.contexts.game.config.scenario.persistence.possibility.ScenarioPossibilityRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ScenarioConfigRepository extends JpaRepository<ScenarioConfigEntity, String> {
    String FETCH_ALL = " LEFT JOIN FETCH scenario.steps step" +
            " LEFT JOIN FETCH step.targets target" +
            " LEFT JOIN FETCH target.hints target_hints" +
            " LEFT JOIN FETCH target.label label" +
            " LEFT JOIN FETCH target.description description" +
            " LEFT JOIN FETCH step.possibilities possibility" +
            ScenarioPossibilityRepository.FETCH_ALL;

    @Query("SELECT DISTINCT scenario FROM ScenarioConfigEntity scenario" +
            " LEFT JOIN FETCH scenario.steps step" +
            " LEFT JOIN FETCH step.targets target" +
            " LEFT JOIN FETCH target.hints target_hints" +
            " LEFT JOIN FETCH target.label label" +
            " LEFT JOIN FETCH target.description description" +
            " LEFT JOIN FETCH step.possibilities possibility" +
            " LEFT JOIN FETCH possibility.recurrence possibility_recurrence" +
            " LEFT JOIN FETCH possibility.trigger possibility_trigger" +
            " LEFT JOIN FETCH possibility_trigger.keyValues possibility_trigger_values" +
            " LEFT JOIN FETCH possibility_trigger.subs possibility_trigger_subs" +
            " LEFT JOIN FETCH possibility_trigger_subs.keyValues possibility_trigger_subs_values" +
            " LEFT JOIN FETCH possibility.nullableCondition possibility_condition" +
            " LEFT JOIN FETCH possibility_condition.keyValues possibility_condition_values" +
            " LEFT JOIN FETCH possibility.consequences possibility_consequence" +
            " WHERE scenario.id = :id")
    Optional<ScenarioConfigEntity> findByIdWithAllFetches(@Param("id") String id);
}
