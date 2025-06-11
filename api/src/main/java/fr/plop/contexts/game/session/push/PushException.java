package fr.plop.contexts.game.session.push;

public class PushException extends Exception {


    public enum Type {
        PROVIDER_EXCEPTION, INVALID_TOKEN, USER_NOT_FOUND

    }

    private final Type type;

    public PushException(Type type) {
        this.type = type;
    }

    public PushException(Type type, Throwable cause) {
        super(cause);
        this.type = type;
    }

    public Type type() {
        return type;
    }
}
