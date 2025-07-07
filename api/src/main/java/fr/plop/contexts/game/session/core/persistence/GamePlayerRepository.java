package fr.plop.contexts.game.session.core.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GamePlayerRepository extends JpaRepository<GamePlayerEntity, String> {

    @Query("FROM GamePlayerEntity player" +
            " LEFT JOIN FETCH player.position position" +
            " LEFT JOIN FETCH position.spaces spaces" +
            " LEFT JOIN FETCH player.goals goal" +
            " LEFT JOIN FETCH goal.step step" +
            " WHERE player.user.id = :userId" +
            " AND player.session.id = :sessionId" +
            " AND player.state = fr.plop.contexts.game.session.core.domain.model.GamePlayer.State.ACTIVE")
    Optional<GamePlayerEntity> fullBySessionIdAndUserId(@Param("sessionId") String sessionId, @Param("userId") String userId);

    @Query("SELECT player.id FROM GamePlayerEntity player" +
            " WHERE player.session.id = :sessionId" +
            " AND player.state = fr.plop.contexts.game.session.core.domain.model.GamePlayer.State.ACTIVE")
    List<String> activeIdsBySessionId(@Param("sessionId") String sessionId);

}
