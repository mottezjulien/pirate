package fr.plop.contexts.game.session.scenario.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ScenarioGoalTargetRepository extends JpaRepository<ScenarioGoalTargetEntity, String> {

    @Query("FROM ScenarioGoalTargetEntity goalTarget" +
            " WHERE goalTarget.goal.player.id = :playerId" +
            " AND goalTarget.target.id = :targetId")
    Optional<ScenarioGoalTargetEntity> byPlayerIdAndTargetId(@Param("playerId") String playerId, @Param("targetId") String targetId);

}
