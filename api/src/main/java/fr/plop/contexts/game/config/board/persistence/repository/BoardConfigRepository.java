package fr.plop.contexts.game.config.board.persistence.repository;

import fr.plop.contexts.game.config.board.persistence.entity.BoardConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BoardConfigRepository extends JpaRepository<BoardConfigEntity, String> {

    String FETCH_ALL = " LEFT JOIN FETCH board.spaces space" +
            " LEFT JOIN FETCH space.rects rect";

    @Query("FROM BoardConfigEntity board" + FETCH_ALL +
            " WHERE board.id = :id")
    Optional<BoardConfigEntity> fullById(@Param("id") String id);



}
