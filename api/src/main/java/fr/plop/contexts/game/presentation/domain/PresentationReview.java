package fr.plop.contexts.game.presentation.domain;

import fr.plop.contexts.user.User;

public record PresentationReview(Id id, User.Id userId, Value value, String details) {

    public record Id(String value) {

    }

    public record Value(int value) {

    }

    public enum State {
        OPEN, VALIDATED, REFUSED
    }

    public Integer valueInt() {
        return value.value();
    }

}
