package fr.plop.contexts.game.instance.scenario.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScenarioGoalTargetRepository extends JpaRepository<ScenarioGoalTargetEntity, String> {

    @Query("FROM ScenarioGoalTargetEntity goalTarget" +
            " WHERE goalTarget.player.id = :currentPlayerId" +
            " AND goalTarget.target.id = :targetId")
    Optional<ScenarioGoalTargetEntity> byPlayerIdAndTargetId(@Param("currentPlayerId") String playerId, @Param("targetId") String targetId);


    @Query("FROM ScenarioGoalTargetEntity goal" +
            " LEFT JOIN FETCH goal.target target" +
            " WHERE goal.player.id = :currentPlayerId")
    List<ScenarioGoalTargetEntity> byPlayerIdFetchTarget(@Param("currentPlayerId") String playerId);

    @Query("FROM ScenarioGoalTargetEntity goal" +
            " LEFT JOIN FETCH goal.target target" +
            " WHERE goal.player.id = :playerId" +
            " AND target.step.id = :stepId")
    List<ScenarioGoalTargetEntity> byPlayerIdAndStepIdFetchTarget(@Param("playerId") String playerId, @Param("stepId") String stepId);
}
