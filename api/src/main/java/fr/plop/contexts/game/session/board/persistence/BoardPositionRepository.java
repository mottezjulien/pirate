package fr.plop.contexts.game.session.board.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BoardPositionRepository extends JpaRepository<BoardPositionEntity, String> {

    @Query("FROM BoardPositionEntity position" +
            " LEFT JOIN FETCH position.spaces spaces" +
            " WHERE position.player.id = :playerId")
    Optional<BoardPositionEntity> findByPlayerIdFetchSpaces(@Param("playerId") String playerId);

}
