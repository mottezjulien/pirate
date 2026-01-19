package fr.plop.contexts.game.session.talk;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GameSessionTalkRepository extends JpaRepository<GameSessionTalkEntity, String> {

    @Query("FROM GameSessionTalkEntity talk" +
            " LEFT JOIN FETCH talk.items item_session" +
            " LEFT JOIN FETCH item_session.config item_sconfig" +
            " WHERE talk.player.id = :playerId")
    GameSessionTalkEntity fullByPlayerId(@Param("playerId") String playerId);

}
