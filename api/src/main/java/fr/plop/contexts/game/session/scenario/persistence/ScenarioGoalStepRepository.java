package fr.plop.contexts.game.session.scenario.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScenarioGoalStepRepository extends JpaRepository<ScenarioGoalStepEntity, String> {

    @Query("FROM ScenarioGoalStepEntity goal" +
            " WHERE goal.player.id = :playerId" +
            " AND goal.step.id = :stepId")
    Optional<ScenarioGoalStepEntity> byPlayerIdAndStepId(@Param("playerId") String playerId, @Param("stepId") String stepId);

    @Query("FROM ScenarioGoalStepEntity goal" +
            " LEFT JOIN FETCH goal.step step" +
            " WHERE goal.player.id = :playerId")
    List<ScenarioGoalStepEntity> byPlayerIdFetchStep(@Param("playerId") String playerId);

}
