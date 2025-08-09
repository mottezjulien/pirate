package fr.plop.contexts.game.session.scenario.persistence;

import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioStepRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScenarioGoalRepository extends JpaRepository<ScenarioGoalEntity, String> {

    @Query("FROM ScenarioGoalEntity goal" +
            " WHERE goal.player.id = :playerId" +
            " AND goal.step.id = :stepId")
    Optional<ScenarioGoalEntity> byPlayerIdAndStepId(@Param("playerId") String playerId, @Param("stepId") String stepId);

    @Query("FROM ScenarioGoalEntity goal" +
            " LEFT JOIN FETCH goal.step step" +
            ScenarioStepRepository.FETCH_ALL +
            " LEFT JOIN FETCH goal.targets goal_target" +
            " LEFT JOIN FETCH goal_target.target goal_target_target" +
            " WHERE goal.player.id = :playerId")
    List<ScenarioGoalEntity> fullByPlayerId(@Param("playerId") String playerId);


}
