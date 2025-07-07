package fr.plop.contexts.game.session.core.persistence;

import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GameSessionRepository extends JpaRepository<GameSessionEntity, String> {

    @Query("SELECT board.id FROM GameSessionEntity session" +
            " LEFT JOIN session.board board" +
            " WHERE session.id = :sessionId")
    Optional<String> boardId(@Param("sessionId") String sessionId);

    @Query("SELECT map.id FROM GameSessionEntity session" +
            " LEFT JOIN session.map map" +
            " WHERE session.id = :sessionId")
    Optional<String> mapId(@Param("sessionId") String sessionId);

    @Query("FROM GameSessionEntity session" +
            " LEFT JOIN FETCH session.board board" +
            " LEFT JOIN session.map map" +
            " LEFT JOIN session.players player" +
            " LEFT JOIN player.user user WHERE session.id = :sessionId")
    Optional<GameSessionEntity> allById(@Param("sessionId") String sessionId);

}
