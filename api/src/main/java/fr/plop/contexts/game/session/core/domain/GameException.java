package fr.plop.contexts.game.session.core.domain;

public class GameException extends Exception {

    public enum Type {
        SESSION_NOT_FOUND, PLAYER_NOT_FOUND, TEMPLATE_NOT_FOUND
    }

    private final Type type;

    public GameException(Type type) {
        this.type = type;
    }

    public Type type() {
        return type;
    }
}
