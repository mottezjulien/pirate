package fr.plop.contexts.game.config.inventory.domain.model;

public record InventoryItemActionRule() {

    public boolean canEquiped() {
        return false;
    }

    public boolean canConsumed() {
        return false;
    }
}
