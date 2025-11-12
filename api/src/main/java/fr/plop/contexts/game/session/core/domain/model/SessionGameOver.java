package fr.plop.contexts.game.session.core.domain.model;

import fr.plop.subs.i18n.domain.I18n;

import java.util.Optional;

public record SessionGameOver(Type type, Optional<I18n.Id> optReasonId) {

    public SessionGameOver(Type type) {
        this(type, Optional.empty());
    }

    public enum Type {
        SUCCESS_ALL_ENDED, SUCCESS_ONE_CONTINUE, FAILURE_ALL_ENDED, FAILURE_ONE_CONTINUE
    }


}
