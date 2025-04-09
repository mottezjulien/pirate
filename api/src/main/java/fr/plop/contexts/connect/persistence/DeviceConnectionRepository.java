package fr.plop.contexts.connect.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DeviceConnectionRepository extends JpaRepository<DeviceConnectionEntity, String> {

    @Query(value = "FROM DeviceConnectionEntity connect" +
            " LEFT JOIN FETCH connect.user" +
            " WHERE connect.deviceId = :deviceId")
    List<DeviceConnectionEntity> findByDeviceIdFetchUser(@Param("deviceId") String deviceId);

}
