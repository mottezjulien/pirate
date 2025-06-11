package fr.plop.contexts.connect.persistence.repository;

import fr.plop.contexts.connect.persistence.entity.ConnectionDeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConnectionDeviceRepository extends JpaRepository<ConnectionDeviceEntity, String> {

    @Query(value = "FROM ConnectionDeviceEntity connect" +
            " LEFT JOIN FETCH connect.user user" +
            " WHERE connect.deviceId = :deviceId")
    List<ConnectionDeviceEntity> findByDeviceIdFetchUser(@Param("deviceId") String deviceId);

    @Query(value = "FROM ConnectionDeviceEntity connect" +
            " LEFT JOIN FETCH connect.user user" +
            " LEFT JOIN user.players player" +
            " WHERE player.id = :playerId")
    List<ConnectionDeviceEntity> findByPlayerId(@Param("playerId") String playerId);

}
