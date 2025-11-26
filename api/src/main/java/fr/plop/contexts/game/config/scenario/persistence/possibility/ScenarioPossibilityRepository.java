package fr.plop.contexts.game.config.scenario.persistence.possibility;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScenarioPossibilityRepository extends JpaRepository<ScenarioPossibilityEntity, String> {
    String FETCH_ALL = " LEFT JOIN FETCH possibility.recurrence possibility_recurrence" +
            " LEFT JOIN FETCH possibility.trigger possibility_trigger" +
            " LEFT JOIN FETCH possibility_trigger.keyValues possibility_trigger_values" +
            " LEFT JOIN FETCH possibility.nullableCondition possibility_condition" +
            " LEFT JOIN FETCH possibility_condition.keyValues possibility_condition_values" +
            " LEFT JOIN FETCH possibility.consequences possibility_consequence";

}
