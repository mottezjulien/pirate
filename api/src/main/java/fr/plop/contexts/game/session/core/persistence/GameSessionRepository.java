package fr.plop.contexts.game.session.core.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GameSessionRepository extends JpaRepository<GameSessionEntity, String> {

    @Query("SELECT _talk.id FROM GameSessionEntity session" +
            " LEFT JOIN session.talk _talk" +
            " WHERE session.id = :sessionId")
    Optional<String> talkId(@Param("sessionId") String sessionId);

    @Query("FROM GameSessionEntity session" +
            " LEFT JOIN FETCH session.players player" +
            " LEFT JOIN FETCH player.user" +
            " LEFT JOIN FETCH session.scenario scenario" +
            " LEFT JOIN FETCH scenario.steps step" +
            " LEFT JOIN FETCH step.targets target" +
            " LEFT JOIN FETCH target.hints target_hints" +
            " LEFT JOIN FETCH target.label label" +
            " LEFT JOIN FETCH target.description description" +
            " LEFT JOIN FETCH step.possibilities possibility" +
            " LEFT JOIN FETCH possibility.recurrence possibility_recurrence" +
            " LEFT JOIN FETCH possibility.trigger possibility_trigger" +
            " LEFT JOIN FETCH possibility_trigger.keyValues possibility_trigger_values" +
            " LEFT JOIN FETCH possibility_trigger.subs possibility_trigger_subs" +
            " LEFT JOIN FETCH possibility_trigger_subs.keyValues possibility_trigger_subs_values" +
            " LEFT JOIN FETCH possibility.nullableCondition possibility_condition" +
            " LEFT JOIN FETCH possibility_condition.keyValues possibility_condition_values" +
            " LEFT JOIN FETCH possibility.consequences possibility_consequence" +
            " WHERE session.id = :sessionId")
    Optional<GameSessionEntity> findByIdFetchPlayerAndUser(@Param("sessionId") String sessionId);

}
