package fr.plop.contexts.connect.persistence;

import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.connect.domain.DeviceConnect;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "TEST2_DEVICE_CONNECTION")
public class DeviceConnectionEntity {

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

    public Set<ConnectionAuthEntity> getAuths() {
        return auths;
    }

    public void setAuths(Set<ConnectionAuthEntity> auths) {
        this.auths = auths;
    }

    public static DeviceConnectionEntity fromModel(DeviceConnect deviceConnect, ConnectUser user) {
        DeviceConnectionEntity entity = new DeviceConnectionEntity();
        entity.setId(deviceConnect.id().value());
        entity.setUser(ConnectionUserEntity.fromModel(user));
        entity.setDeviceId(deviceConnect.deviceId());
        return entity;
    }

    public DeviceConnect toModel() {
        return new DeviceConnect(new DeviceConnect.Id(id), user.toModel(), deviceId);
    }

}
