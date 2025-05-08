package fr.plop.contexts.game.session.core.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GameSessionRepository extends JpaRepository<GameSessionEntity, String> {

    //TODO Et le cache :)

    @Query("SELECT board.id FROM GameSessionEntity session" +
            " LEFT JOIN session.board board" +
            " WHERE session.id = :sessionId")
    Optional<String> boardId(@Param("sessionId") String sessionId);

}
