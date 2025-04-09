package fr.plop.contexts.board.persistence.repository;

import fr.plop.contexts.board.persistence.entity.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<BoardEntity, String> {

    /*@Query("FROM BoardEntity board" +
            " LEFT JOIN board.games game" +
            " LEFT JOIN FETCH board.spaces space" +
            " LEFT JOIN FETCH space.rects rect" +
            " WHERE game.id = :gameId")
    Optional<BoardEntity> findByGameIdFetchSpacesAndRects(@Param("gameId") String gameId);*/

}
