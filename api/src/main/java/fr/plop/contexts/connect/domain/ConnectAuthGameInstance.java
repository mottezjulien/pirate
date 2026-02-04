package fr.plop.contexts.connect.domain;

import fr.plop.contexts.game.instance.core.domain.model.GameInstance;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.generic.tools.StringTools;

import java.time.Instant;


//TODO -> à revoir, attention ConnectToken represente le token auth et celui de player
public record ConnectAuthGameInstance(Id id, Status status, ConnectToken token, ConnectAuthUser.Id authUserId,
                                      GameInstanceContext context, Instant createdAt) {

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public enum Status {
        OPENED, CLOSED
    }

    public static ConnectAuthGameInstance create(ConnectAuthUser.Id authUserId, GameInstanceContext context) {
        return new ConnectAuthGameInstance(new Id(), Status.OPENED, new ConnectToken(), authUserId, context, Instant.now());
    }

    public boolean isSessionId(GameInstance.Id sessionId) {
        return context.isInstanceId(sessionId);
    }

    private static final int SIX_HOURS_IN_SECOND = 60 * 60 * 6; //TODO BETTER (5 minutes)

    public boolean isValid() {
        return status != Status.CLOSED && createdAt.isAfter(Instant.now().minusSeconds(SIX_HOURS_IN_SECOND));
    }
}
