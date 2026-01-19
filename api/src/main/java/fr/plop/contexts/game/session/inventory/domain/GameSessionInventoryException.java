package fr.plop.contexts.game.session.inventory.domain;

public class GameSessionInventoryException extends Exception {



    enum Type {
        ITEM_NOT_FOUND,
        ACTION_NOT_ALLOWED,
        CONSEQUENCE_ERROR
    }

    private final Type type;

    public GameSessionInventoryException(Type type, String message) {
        super(message);
        this.type = type;
    }

    public GameSessionInventoryException(Type type) {
        this.type = type;
    }

}
