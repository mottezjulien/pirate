package fr.plop.contexts.game.session.core.domain.model;

import fr.plop.contexts.i18n.domain.I18n;

public record SessionGameOver(Type type, I18n.Id reasonId) {

    public enum Type {
        SUCCESS_ALL_ENDED, SUCCESS_ONE_CONTINUE,
    }


}
