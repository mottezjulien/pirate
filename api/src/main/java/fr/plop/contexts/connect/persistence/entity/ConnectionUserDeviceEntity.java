package fr.plop.contexts.connect.persistence.entity;

import fr.plop.contexts.connect.domain.ConnectUserDevice;
import fr.plop.contexts.user.persistence.UserEntity;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "LO_CONNECTION_USER_DEVICE")
public class ConnectionUserDeviceEntity {

    @Id
    private String id;

    @Column(name = "device_id")
    private String deviceId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @OneToMany(mappedBy = "connection")
    private final Set<ConnectionAuthUserEntity> auths = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }


    public static ConnectionUserDeviceEntity fromModelId(ConnectUserDevice.Id modelId) {
        ConnectionUserDeviceEntity entity = new ConnectionUserDeviceEntity();
        entity.setId(modelId.value());
        return entity;
    }

    public ConnectUserDevice toModel() {
        return new ConnectUserDevice(new ConnectUserDevice.Id(id), user.toModelId(), deviceId);
    }

}
