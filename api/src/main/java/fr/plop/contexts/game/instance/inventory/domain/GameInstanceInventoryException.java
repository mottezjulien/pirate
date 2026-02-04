package fr.plop.contexts.game.instance.inventory.domain;

public class GameInstanceInventoryException extends Exception {



    enum Type {
        ITEM_NOT_FOUND,
        ACTION_NOT_ALLOWED,
        CONSEQUENCE_ERROR
    }

    private final Type type;

    public GameInstanceInventoryException(Type type, String message) {
        super(message);
        this.type = type;
    }

    public GameInstanceInventoryException(Type type) {
        this.type = type;
    }

}
