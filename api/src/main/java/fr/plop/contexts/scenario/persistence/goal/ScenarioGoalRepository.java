package fr.plop.contexts.scenario.persistence.goal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScenarioGoalRepository extends JpaRepository<ScenarioGoalEntity, String> {

    @Query("FROM ScenarioGoalEntity goal" +
            " LEFT JOIN FETCH goal.step step" +
            " LEFT JOIN FETCH step.targets target" +
            " WHERE goal.player.id = :playerId" +
            " AND goal.state = 'ACTIVE'")
    List<ScenarioGoalEntity> findActiveByPlayerIdFetchStepAndTarget(@Param("playerId") String playerId);

}
