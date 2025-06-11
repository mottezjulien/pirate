package fr.plop.contexts.connect.domain;

import java.time.Instant;

public record ConnectAuth(ConnectToken token, DeviceConnect connect, Instant createdAt) {
    public static final int ONE_DAY_IN_SECOND = 60 * 60 * 24;

    public boolean isValid() {
        return createdAt.isAfter(Instant.now().minusSeconds(ONE_DAY_IN_SECOND));
    }

    public boolean isExpiry() {
        return !isValid();
    }
}
