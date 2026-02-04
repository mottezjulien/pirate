package fr.plop.contexts.game.instance.talk.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GameInstanceTalkRepository extends JpaRepository<GameInstanceTalkEntity, String> {

    @Query("FROM GameInstanceTalkEntity talk" +
            " LEFT JOIN FETCH talk.items item_session" +
            " LEFT JOIN FETCH item_session.configItem config_item" +
            " LEFT JOIN FETCH item_session.configCharacter config_character" +
            " WHERE talk.player.id = :playerId")
    Optional<GameInstanceTalkEntity> fullByPlayerId(@Param("playerId") String playerId);

}
