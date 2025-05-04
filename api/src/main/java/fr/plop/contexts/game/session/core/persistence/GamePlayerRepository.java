package fr.plop.contexts.game.session.core.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GamePlayerRepository extends JpaRepository<GamePlayerEntity, String> {

    @Query("FROM GamePlayerEntity player" +
            " LEFT JOIN FETCH player.session session" +
            " LEFT JOIN FETCH player.user user" +
            " LEFT JOIN FETCH player.position position" +
            " LEFT JOIN FETCH position.spaces spaces" +
            " WHERE user.id = :userId" +
            " AND session.id = :sessionId")
    Optional<GamePlayerEntity> fullBySessionIdAndUserId(@Param("sessionId") String sessionId, @Param("userId") String userId);

}
