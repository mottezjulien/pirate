package fr.plop.contexts.game.session.core.persistence;

import fr.plop.contexts.game.config.map.persistence.MapEntity;
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

    //TODO HERE ??
    @Query("SELECT map FROM GameSessionEntity session" +
            " LEFT JOIN session.map map_config" +
            " LEFT JOIN map_config.items map_config_item" +
            " LEFT JOIN map_config_item.map map" +
            " LEFT JOIN FETCH map.label map_label" +
            " WHERE session.id = :sessionId")
    List<MapEntity> fullMap(@Param("sessionId") String sessionId);

}
