package fr.plop.contexts.game.session.core.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface GameSessionRepository extends JpaRepository<GameSessionEntity, String> {


    @Query("SELECT board.id FROM GameSessionEntity game" +
            " LEFT JOIN game.board board" +
            " WHERE game.id = :id")
    Optional<String> boardId(String value);

    //TODO Et le cache :)

    /*@Query("FROM GameEntity game" +
            " LEFT JOIN FETCH game.scenario" +
            " LEFT JOIN FETCH game.board" +
            " WHERE game.id = :gameId")
    Optional<GameEntity> findByIdFetchAll(@Param("gameId") String gameId);*/

}
