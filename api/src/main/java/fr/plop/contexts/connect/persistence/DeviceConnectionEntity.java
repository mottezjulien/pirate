package fr.plop.contexts.connect.persistence;

import fr.plop.contexts.connect.domain.Connect;
import fr.plop.contexts.connect.domain.ConnectAuth;
import fr.plop.contexts.connect.domain.DeviceConnect;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.util.Comparator;
import java.util.Optional;

@Entity
@DiscriminatorValue("DEVICE")
public class DeviceConnectionEntity extends ConnectionEntity {

    @Column(name = "device_id")
    private String deviceId;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public DeviceConnect toModel() {
        Optional<ConnectAuth> lastAuth = auths
                .stream().max(Comparator.comparing(ConnectionAuthEntity::getCreatedAt))
                .map(ConnectionAuthEntity::toModel);
        return new DeviceConnect(new Connect.Id(id), lastAuth, user.toModel(), deviceId);
    }

}
