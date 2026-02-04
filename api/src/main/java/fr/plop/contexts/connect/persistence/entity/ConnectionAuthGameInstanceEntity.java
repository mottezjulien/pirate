package fr.plop.contexts.connect.persistence.entity;

import fr.plop.contexts.connect.domain.ConnectAuthGameInstance;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.instance.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.instance.core.persistence.GameInstanceEntity;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "LO_CONNECTION_GAME_SESSION_AUTH",
        indexes = {@Index(columnList = "token", unique = true)}
)
public class ConnectionAuthGameInstanceEntity {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    private ConnectAuthGameInstance.Status status;

    @ManyToOne
    @JoinColumn(name = "origin_auth_user_id")
    private ConnectionAuthUserEntity originAuthUser;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(unique = true)
    private String token;

    @ManyToOne
    @JoinColumn(name = "instance_id")
    private GameInstanceEntity instance;

    @ManyToOne
    @JoinColumn(name = "game_player_id")
    private GamePlayerEntity gamePlayer;

    public void setType(ConnectAuthGameInstance.Status status) {
        this.status = status;
    }

    public static ConnectionAuthGameInstanceEntity fromModel(ConnectAuthGameInstance model) {
        ConnectionAuthGameInstanceEntity entity = new ConnectionAuthGameInstanceEntity();
        entity.id = model.id().value();
        entity.status = model.status();
        entity.originAuthUser = ConnectionAuthUserEntity.fromModelId(model.authUserId());
        entity.createdAt = model.createdAt();
        entity.token = model.token().value();
        entity.instance = GameInstanceEntity.fromModelId(model.context().instanceId());
        entity.gamePlayer = GamePlayerEntity.fromModelId(model.context().playerId());
        return entity;
    }

    public ConnectAuthGameInstance toModel() {
        ConnectToken connectToken = new ConnectToken(token);
        GameInstanceContext context = new GameInstanceContext(instance.toModelId(), gamePlayer.toModelId());
        return new ConnectAuthGameInstance(new ConnectAuthGameInstance.Id(id), status, connectToken,
                originAuthUser.toModelId(), context, createdAt);
    }
}
