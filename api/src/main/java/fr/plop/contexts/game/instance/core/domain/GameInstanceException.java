package fr.plop.contexts.game.instance.core.domain;

public class GameInstanceException extends Exception {

    public enum Type {
        SESSION_NOT_FOUND, SESSION_INVALID, PLAYER_NOT_FOUND, PLAYER_INVALID, TEMPLATE_NOT_FOUND
    }

    private final Type type;

    public GameInstanceException(Type type) {
        this.type = type;
    }

    public Type type() {
        return type;
    }
}
