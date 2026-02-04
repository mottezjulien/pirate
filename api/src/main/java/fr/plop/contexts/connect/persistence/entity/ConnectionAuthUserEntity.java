package fr.plop.contexts.connect.persistence.entity;

import fr.plop.contexts.connect.domain.ConnectAuthUser;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.domain.ConnectUserDevice;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "LO_CONNECTION_USER_AUTH",
        indexes = {@Index(columnList = "token", unique = true)}
)
public class ConnectionAuthUserEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "connection_id")
    private ConnectionUserDeviceEntity connection;

    private Instant createdAt;

    @Column(unique=true)
    private String token;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setConnection(ConnectionUserDeviceEntity connection) {
        this.connection = connection;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public static ConnectionAuthUserEntity fromModelId(ConnectAuthUser.Id modelId) {
        ConnectionAuthUserEntity entity = new ConnectionAuthUserEntity();
        entity.setId(modelId.value());
        return entity;
    }

    public ConnectAuthUser toModel() {
        return toModelWithConnect(connection.toModel());
    }

    public ConnectAuthUser toModelWithConnect(ConnectUserDevice connect) {
        return new ConnectAuthUser(toModelId(), new ConnectToken(token), connect, createdAt);
    }

    public ConnectAuthUser.Id toModelId() {
        return new ConnectAuthUser.Id(id);
    }
}
