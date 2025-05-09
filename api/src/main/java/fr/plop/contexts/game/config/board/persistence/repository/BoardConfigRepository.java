package fr.plop.contexts.game.config.board.persistence.repository;

import fr.plop.contexts.game.config.board.persistence.entity.BoardConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BoardConfigRepository extends JpaRepository<BoardConfigEntity, String> {

    @Query("FROM BoardConfigEntity board" +
            " LEFT JOIN FETCH board.spaces space" +
            " LEFT JOIN FETCH space.rects rect" +
            " WHERE board.id = :id")
    Optional<BoardConfigEntity> fullById(@Param("id") String id);

}
