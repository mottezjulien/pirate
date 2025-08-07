package fr.plop.contexts.game.session.core.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GamePlayerActionRepository extends JpaRepository<GamePlayerActionEntity, String> {

    @Query("FROM GamePlayerActionEntity action " +
            "LEFT JOIN FETCH action.player player " +
            "LEFT JOIN FETCH action.possibility possibility " +
            "WHERE player.id = :playerId")
    List<GamePlayerActionEntity> fullByPlayerId(@Param("playerId") String playerId);

}
