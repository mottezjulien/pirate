package fr.plop.contexts.game.session.core.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GamePlayerRepository extends JpaRepository<GamePlayerEntity, String> {

    String FROM = "FROM GamePlayerEntity player";

    //fetch space && goal bugs -> separate queries
    String FETCH_LAST_POSITION = "LEFT JOIN FETCH player.lastPosition position LEFT JOIN FETCH position.spaces spaces";
    String W_PLAYER_ID_EQUALS = "player.id = :currentPlayerId";
    String W_SESSION_ID_EQUALS = "player.session.id = :sessionId";
    String W_PLAYER_IS_ACTIVE = "player.state = fr.plop.contexts.game.session.core.domain.model.GamePlayer.State.ACTIVE";

    @Query(FROM + " " + FETCH_LAST_POSITION + " WHERE " + W_PLAYER_ID_EQUALS)
    Optional<GamePlayerEntity> findByIdFetchLastPosition(@Param("currentPlayerId") String playerId);

    @Query("SELECT player.id " + FROM +
            " WHERE " + W_SESSION_ID_EQUALS +
            " AND " + W_PLAYER_IS_ACTIVE)
    List<String> activeIdsBySessionId(@Param("sessionId") String sessionId);

    @Query(FROM + " LEFT JOIN FETCH player.user user" +
            " WHERE " + W_PLAYER_ID_EQUALS)
    Optional<GamePlayerEntity> findByIdFetchUser(@Param("currentPlayerId") String playerId);

}
