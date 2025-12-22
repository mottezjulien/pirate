package fr.plop.contexts.connect.persistence.entity;

import fr.plop.contexts.connect.domain.ConnectAuthGameSession;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.session.core.persistence.GameSessionEntity;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "TEST2_CONNECTION_GAME_SESSION_AUTH",
        indexes = {@Index(columnList = "token", unique = true)}
)
public class ConnectionAuthGameSessionEntity {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    private ConnectAuthGameSession.Type type;

    @ManyToOne
    @JoinColumn(name = "origin_auth_user_id")
    private ConnectionAuthUserEntity originAuthUser;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(unique = true)
    private String token;

    @ManyToOne
    @JoinColumn(name = "game_session_id")
    private GameSessionEntity gameSession;

    @ManyToOne
    @JoinColumn(name = "game_player_id")
    private GamePlayerEntity gamePlayer;

    public void setType(ConnectAuthGameSession.Type type) {
        this.type = type;
    }

    public static ConnectionAuthGameSessionEntity fromModel(ConnectAuthGameSession model) {
        ConnectionAuthGameSessionEntity entity = new ConnectionAuthGameSessionEntity();
        entity.id = model.id().value();
        entity.type = model.type();
        entity.originAuthUser = ConnectionAuthUserEntity.fromModelId(model.authUserId());
        entity.createdAt = model.createdAt();
        entity.token = model.token().value();
        entity.gameSession = GameSessionEntity.fromModelId(model.context().sessionId());
        entity.gamePlayer = GamePlayerEntity.fromModelId(model.context().playerId());
        return entity;
    }

    public ConnectAuthGameSession toModel() {
        ConnectToken connectToken = new ConnectToken(token);
        GameSessionContext context = new GameSessionContext(gameSession.toModelId(), gamePlayer.toModelId());
        return new ConnectAuthGameSession(new ConnectAuthGameSession.Id(id), type, connectToken,
                originAuthUser.toModelId(), context, createdAt);
    }
}
