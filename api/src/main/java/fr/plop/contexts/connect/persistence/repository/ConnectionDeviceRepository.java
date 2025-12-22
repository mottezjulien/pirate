package fr.plop.contexts.connect.persistence.repository;

import fr.plop.contexts.connect.persistence.entity.ConnectionUserDeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConnectionDeviceRepository extends JpaRepository<ConnectionUserDeviceEntity, String> {

    @Query(value = "FROM ConnectionUserDeviceEntity connect" +
            " LEFT JOIN FETCH connect.user user" +
            " WHERE connect.deviceId = :deviceId")
    List<ConnectionUserDeviceEntity> findByDeviceIdFetchUser(@Param("deviceId") String deviceId);

}
