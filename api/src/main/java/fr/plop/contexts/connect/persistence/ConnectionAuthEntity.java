package fr.plop.contexts.connect.persistence;

import fr.plop.contexts.connect.domain.ConnectAuth;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.domain.DeviceConnect;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(
    name = "TEST2_CONNECTION_AUTH",
    indexes = {
        @Index(columnList = "token", unique = true)
    }
)
public class ConnectionAuthEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "connection_id")
    private DeviceConnectionEntity connection;

    private Instant createdAt;

    private String token;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DeviceConnectionEntity getConnection() {
        return connection;
    }

    public void setConnection(DeviceConnectionEntity connection) {
        this.connection = connection;
    }

    public Instant getCreatedAt() {
        return createdAt;
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

    public ConnectAuth toModelWithConnect(DeviceConnect connect) {
        return new ConnectAuth(new ConnectToken(token), connect, createdAt);
    }

    public ConnectAuth toModel(GamePlayer nullablePlayer) {
        return new ConnectAuth(new ConnectToken(token), connection.toModel(nullablePlayer), createdAt);
    }

    public ConnectAuth toModel() {
        return new ConnectAuth(new ConnectToken(token), connection.toModel(null), createdAt);
    }
}
