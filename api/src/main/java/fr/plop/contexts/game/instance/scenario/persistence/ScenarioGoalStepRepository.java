package fr.plop.contexts.game.instance.scenario.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScenarioGoalStepRepository extends JpaRepository<ScenarioGoalStepEntity, String> {

    @Query("FROM ScenarioGoalStepEntity goal" +
            " WHERE goal.player.id = :currentPlayerId" +
            " AND goal.step.id = :stepId")
    Optional<ScenarioGoalStepEntity> byPlayerIdAndStepId(@Param("currentPlayerId") String playerId, @Param("stepId") String stepId);

    @Query("FROM ScenarioGoalStepEntity goal" +
            " LEFT JOIN FETCH goal.step step" +
            " WHERE goal.player.id = :currentPlayerId")
    List<ScenarioGoalStepEntity> byPlayerIdFetchStep(@Param("currentPlayerId") String playerId);

}
