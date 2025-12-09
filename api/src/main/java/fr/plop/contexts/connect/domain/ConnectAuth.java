package fr.plop.contexts.connect.domain;

import fr.plop.contexts.game.session.core.domain.model.GamePlayer;

import java.time.Instant;

public record ConnectAuth(ConnectToken token, DeviceConnect connect, Instant createdAt) {
    public static final int ONE_DAY_IN_SECOND = 60 * 60 * 24;

    public boolean isValid() {
        return createdAt.isAfter(Instant.now().minusSeconds(ONE_DAY_IN_SECOND));
    }

    public boolean isExpiry() {
        return !isValid();
    }

    public ConnectUser.Id userId() {
        return connect.user().id();
    }

    public ConnectAuth withPlayerId(GamePlayer.Id playerId) {
        return new ConnectAuth(token, connect.withPlayerId(playerId), createdAt);
    }

}
