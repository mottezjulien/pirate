package fr.plop.contexts.game.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GamePlayerRepository extends JpaRepository<GamePlayerEntity, String> {

    @Query("FROM GamePlayerEntity player" +
            " LEFT JOIN FETCH player.game game" +
            " WHERE player.user.id = :userId" +
            " AND game.state != 'OVER'")
    Optional<GamePlayerEntity> findByUserIdAndActiveGameFetchGame(@Param("userId") String userId);

}
