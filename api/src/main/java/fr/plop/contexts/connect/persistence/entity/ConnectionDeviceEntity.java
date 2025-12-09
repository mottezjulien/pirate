package fr.plop.contexts.connect.persistence.entity;

import fr.plop.contexts.connect.domain.DeviceConnect;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "TEST2_CONNECTION_DEVICE")
public class ConnectionDeviceEntity {

    @Id
    private String id;

    @Column(name = "device_id")
    private String deviceId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private ConnectionUserEntity user;

    @OneToMany(mappedBy = "connection")
    private Set<ConnectionAuthEntity> auths = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ConnectionUserEntity getUser() {
        return user;
    }

    public void setUser(ConnectionUserEntity user) {
        this.user = user;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public DeviceConnect toModel() {
        return new DeviceConnect(new DeviceConnect.Id(id), user.toModel(), deviceId);
    }

}
