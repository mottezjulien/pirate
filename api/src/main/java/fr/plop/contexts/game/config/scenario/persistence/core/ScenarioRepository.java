package fr.plop.contexts.game.config.scenario.persistence.core;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScenarioRepository extends JpaRepository<ScenarioConfigEntity, String> {

    /*
    @Query("FROM ScenarioGoalEntity goal" +
            " LEFT JOIN FETCH goal.step step" +
            " LEFT JOIN FETCH step.targets target" +
            " LEFT JOIN FETCH step.possibilities possibility" +
            " LEFT JOIN FETCH possibility.trigger trigger" +
            " LEFT JOIN FETCH possibility.conditions condition" +
            " WHERE goal.player.id = :playerId")
    List<ScenarioGoalEntity> findByPlayerIdFetchs(@Param("playerId") String playerId);*/
}
