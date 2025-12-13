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
            " WHERE session.id = :sessionId")
    Optional<GameSessionEntity> findByIdFetchPlayerAndUser(@Param("sessionId") String sessionId);

}
