package fr.plop.contexts.connect.domain;

import java.time.Instant;

public record ConnectAuth(String token, Instant createdAt) {

    public static final int ONE_DAY_IN_SECOND = 60 * 60 * 24;

    public boolean isValid() {
        return createdAt.isAfter(Instant.now().minusSeconds(ONE_DAY_IN_SECOND));
    }
}
