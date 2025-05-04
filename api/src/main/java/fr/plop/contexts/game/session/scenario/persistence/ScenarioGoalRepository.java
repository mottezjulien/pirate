package fr.plop.contexts.game.session.scenario.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScenarioGoalRepository extends JpaRepository<ScenarioGoalEntity, String> {

    @Query("FROM ScenarioGoalEntity goal" +
            " WHERE goal.player.id = :playerId" +
            " AND goal.step.id = :stepId")
    Optional<ScenarioGoalEntity> byPlayerIdAndStepId(String value, String stepId);

    @Query("FROM ScenarioGoalEntity goal" +
            " LEFT JOIN FETCH goal.step step" +
            " LEFT JOIN FETCH step.label" +
            " LEFT JOIN FETCH step.targets target" +
            " LEFT JOIN FETCH target.label" +
            " LEFT JOIN FETCH step.possibilities possibility" +
            " LEFT JOIN FETCH possibility.trigger trigger" +
            " LEFT JOIN FETCH possibility.conditions condition" +
            " WHERE goal.player.id = :playerId")
    List<ScenarioGoalEntity> fullByPlayerId(@Param("playerId") String playerId);


}
