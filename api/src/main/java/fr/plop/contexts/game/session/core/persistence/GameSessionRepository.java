package fr.plop.contexts.game.session.core.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    @Query("SELECT _talk.id FROM GameSessionEntity session" +
            " LEFT JOIN session.talk _talk" +
            " WHERE session.id = :sessionId")
    Optional<String> talkId(@Param("sessionId") String sessionId);

    @Query("FROM GameSessionEntity session " +
            " LEFT JOIN session.players player" +
            " WHERE player.user.id = :userId")
    List<GameSessionEntity> findByUserId(@Param("userId") String userId);

    @Query("FROM GameSessionEntity session" +
            " LEFT JOIN FETCH session.players player" +
            " LEFT JOIN FETCH player.user" +
            " WHERE session.id = :sessionId")
    Optional<GameSessionEntity> findByIdFetchPlayerAndUser(@Param("sessionId") String sessionId);

}
