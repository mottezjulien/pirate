package fr.plop.contexts.connect.persistence;


import fr.plop.contexts.connect.domain.Connect;
import fr.plop.contexts.connect.domain.DeviceConnect;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "TEST2_CONNECTION")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "_type")
public abstract class ConnectionEntity {

    @Id
    protected String id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    protected ConnectionUserEntity user;

    @OneToMany(mappedBy = "connection")
    protected Set<ConnectionAuthEntity> auths = new HashSet<>();

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

    public Set<ConnectionAuthEntity> getAuths() {
        return auths;
    }

    public void setAuths(Set<ConnectionAuthEntity> auths) {
        this.auths = auths;
    }

    public static ConnectionEntity fromModel(Connect connect) {
        return switch (connect) {
            case DeviceConnect deviceConnect -> {
                DeviceConnectionEntity entity = new DeviceConnectionEntity();
                entity.setId(deviceConnect.id().value());
                entity.setDeviceId(deviceConnect.deviceId());
                entity.setUser(ConnectionUserEntity.fromModel(deviceConnect.user()));
                yield entity;
            }
        };
    }

}
