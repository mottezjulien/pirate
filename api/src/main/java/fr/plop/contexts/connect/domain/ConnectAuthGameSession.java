package fr.plop.contexts.connect.domain;

import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.generic.tools.StringTools;

import java.time.Instant;

public record ConnectAuthGameSession(Id id, Type type, ConnectToken token, ConnectAuthUser.Id authUserId,
                                     GameSessionContext context, Instant createdAt) {

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public enum Type {
        INIT, OPENED, CLOSED
    }

    public static ConnectAuthGameSession init(ConnectAuthUser.Id authUserId, GameSessionContext context) {
        return new ConnectAuthGameSession(new Id(), Type.INIT, new ConnectToken(), authUserId, context, Instant.now());
    }

    public boolean isSessionId(GameSession.Id sessionId) {
        return context.isSessionId(sessionId);
    }

    private static final int SIX_HOURS_IN_SECOND = 60 * 60 * 6; //TODO BETTER (5 minutes)

    public boolean isValid() {
        return type != Type.CLOSED && createdAt.isAfter(Instant.now().minusSeconds(SIX_HOURS_IN_SECOND));
    }
}
