package fr.plop.contexts.game.config.scenario.persistence.possibility;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScenarioPossibilityRepository extends JpaRepository<ScenarioPossibilityAbstractEntity, String> {

    String FETCH_ALL_CONFIG = " LEFT JOIN FETCH config_possibility.recurrence config_possibility_recurrence" +
            " LEFT JOIN FETCH config_possibility.trigger config_possibility_trigger" +
            " LEFT JOIN FETCH config_possibility_trigger.keyValues config_possibility_trigger_values" +
            " LEFT JOIN FETCH config_possibility_trigger.subs config_possibility_trigger_subs" +
            " LEFT JOIN FETCH config_possibility_trigger_subs.keyValues config_possibility_trigger_subs_values" +
            " LEFT JOIN FETCH config_possibility.nullableCondition config_possibility_condition" +
            " LEFT JOIN FETCH config_possibility_condition.keyValues config_possibility_condition_values" +
            " LEFT JOIN FETCH config_possibility.consequences config_possibility_consequence";

    String FETCH_ALL_STEP = " LEFT JOIN FETCH step_possibility.recurrence step_possibility_recurrence" +
            " LEFT JOIN FETCH step_possibility.trigger step_possibility_trigger" +
            " LEFT JOIN FETCH step_possibility_trigger.keyValues step_possibility_trigger_values" +
            " LEFT JOIN FETCH step_possibility_trigger.subs step_possibility_trigger_subs" +
            " LEFT JOIN FETCH step_possibility_trigger_subs.keyValues step_possibility_trigger_subs_values" +
            " LEFT JOIN FETCH step_possibility.nullableCondition step_possibility_condition" +
            " LEFT JOIN FETCH step_possibility_condition.keyValues step_possibility_condition_values" +
            " LEFT JOIN FETCH step_possibility.consequences step_possibility_consequence";
}
