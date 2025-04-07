package fr.plop.contexts.connect.persistence;


import fr.plop.contexts.connect.domain.ConnectAuth;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "TEST2_CONNECTION_AUTH")
public class ConnectionAuthEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "connection_id")
    private ConnectionEntity connection;

    private Instant createdAt;

    private String token;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ConnectionEntity getConnection() {
        return connection;
    }

    public void setConnection(ConnectionEntity connection) {
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

    public ConnectAuth toModel() {
        return new ConnectAuth(getToken(), getCreatedAt());
    }
}
