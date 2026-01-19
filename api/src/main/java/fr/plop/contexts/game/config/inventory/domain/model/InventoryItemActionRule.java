package fr.plop.contexts.game.config.inventory.domain.model;

import fr.plop.contexts.game.session.event.domain.GameEvent;

import java.util.List;

public record InventoryItemActionRule(Type type, Consequence consequence) {

    public enum Type {
        EQUIPPABLE, CONSUMABLE, USABLE
    }

    public boolean canEquipped() {
        return type == Type.EQUIPPABLE;
    }

    public boolean canConsumed() {
        return  type == Type.CONSUMABLE;
    }

    public boolean canUsed() {
        return  type == Type.USABLE;
    }

    public sealed interface Consequence permits Consequence.Event, Consequence.Direct {

        record Event(GameEvent value) implements Consequence  {

        }

        record Direct(List<fr.plop.contexts.game.config.consequence.Consequence> consequences) implements Consequence  {

        }

    }



}
