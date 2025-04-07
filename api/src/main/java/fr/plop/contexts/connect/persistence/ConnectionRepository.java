package fr.plop.contexts.connect.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConnectionRepository extends JpaRepository<ConnectionEntity, String> {
    @Query(value = "FROM DeviceConnectionEntity connect" +
            " LEFT JOIN FETCH connect.auths" +
            " WHERE connect.deviceId = :deviceId")
    List<DeviceConnectionEntity> findByDeviceIdFetchAuth(@Param("deviceId") String deviceId);

}
