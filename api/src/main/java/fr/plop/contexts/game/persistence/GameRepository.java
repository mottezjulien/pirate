package fr.plop.contexts.game.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GameRepository extends JpaRepository<GameEntity, String> {

    @Query("FROM GameEntity game" +
            " LEFT JOIN FETCH game.scenario" +
            " LEFT JOIN FETCH game.board" +
            " WHERE game.id = :gameId")
    Optional<GameEntity> findByIdFetchAll(@Param("gameId") String gameId);

}
