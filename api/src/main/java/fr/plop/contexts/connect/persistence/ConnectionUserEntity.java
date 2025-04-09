package fr.plop.contexts.connect.persistence;


import fr.plop.contexts.connect.domain.ConnectUser;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "TEST2_CONNECTION_USER")
public class ConnectionUserEntity {

    @Id
    private String id;

    @OneToMany(mappedBy = "user")
    private Set<DeviceConnectionEntity> connections = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<DeviceConnectionEntity> getConnections() {
        return connections;
    }

    public void setConnections(Set<DeviceConnectionEntity> connections) {
        this.connections = connections;
    }

    public static ConnectionUserEntity fromModel(ConnectUser user) {
        ConnectionUserEntity entity = new ConnectionUserEntity();
        entity.setId(user.id().value());
        return entity;
    }

    public ConnectUser toModel() {
        return new ConnectUser(new ConnectUser.Id(id));
    }

}
