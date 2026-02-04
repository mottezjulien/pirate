package fr.plop.contexts.game.instance.core.persistence;

import fr.plop.contexts.game.config.cache.GameConfigCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GameInstanceRepository extends JpaRepository<GameInstanceEntity, String> {

    String FROM = "FROM GameInstanceEntity instance";
    String FETCH = " LEFT JOIN FETCH instance.players player" + " LEFT JOIN FETCH player.user";
    String WHERE_ID = " WHERE instance.id = :instanceId";

    @Query("SELECT _talk.id " + FROM +
            " LEFT JOIN instance.talk _talk" +
            WHERE_ID)
    Optional<String> talkId(@Param("instanceId") String instanceId);

    @Query(FROM + FETCH + " WHERE user.id = :userId AND instance.overAt is null")
    List<GameInstanceEntity> fullOpenedByUserId(@Param("userId") String userId);

    @Query(FROM + FETCH + WHERE_ID)
    Optional<GameInstanceEntity> fullById(@Param("instanceId") String instanceId);

}
