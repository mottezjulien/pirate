package fr.plop.contexts.connect.domain;

import fr.plop.contexts.user.User;
import fr.plop.generic.tools.StringTools;

import java.time.Instant;

public record ConnectAuthUser(Id id, ConnectToken token, DeviceUserConnect connect, Instant createdAt) {

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }
    private static final int ONE_DAY_HOURS_IN_SECOND = 60 * 60 * 24; //TODO BETTER (2 heures)

    public boolean isValid() {
        return createdAt.isAfter(Instant.now().minusSeconds(ONE_DAY_HOURS_IN_SECOND));
    }

    public boolean isExpiry() {
        return !isValid();
    }

    public User.Id userId() {
        return connect.userId();
    }

}
