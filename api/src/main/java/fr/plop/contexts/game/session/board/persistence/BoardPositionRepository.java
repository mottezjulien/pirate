package fr.plop.contexts.game.session.board.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardPositionRepository extends JpaRepository<BoardPositionEntity, String> {

    /*@Query("FROM BoardPositionEntity position" +
            " LEFT JOIN FETCH position.spaces spaces" +
            " WHERE position.player.id = :playerId")
    Optional<BoardPositionEntity> findByPlayerIdFetchSpaces(@Param("playerId") String playerId);*/

}
