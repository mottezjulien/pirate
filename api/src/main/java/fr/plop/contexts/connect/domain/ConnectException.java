package fr.plop.contexts.connect.domain;

public class ConnectException extends Exception {

    public enum Type {
        EMPTY,
        ANONYMOUS,
        EXPIRED_TOKEN
    }

    private final Type type;

    public ConnectException(Type type) {
        this.type = type;
    }

    public Type type() {
        return type;
    }

}
