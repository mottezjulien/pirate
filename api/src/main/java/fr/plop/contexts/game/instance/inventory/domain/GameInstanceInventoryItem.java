package fr.plop.contexts.game.instance.inventory.domain;

import fr.plop.contexts.game.config.inventory.domain.model.GameConfigInventoryItem;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.image.Image;

import java.util.List;
import java.util.Optional;

public record GameInstanceInventoryItem(Id sessionId, GameConfigInventoryItem.Id configId,
                                        I18n label, Image image, Optional<I18n> optDescription, List<Action> actions,
                                        State state) {


    public record Id(String value) {

    }

    public enum Action {
        DROP, EQUIP, MERGE, CONSUME, USE
    }

    public sealed interface State permits State.Unique, State.Collection {

        int count();

        Availability availability();

        record Unique(int pourcentUsury, Availability availability) implements State {
            @Override
            public int count() {
                return 1;
            }

        }

        record Collection(int count) implements State {
            @Override
            public Availability availability() {
                return Availability.UNAVAILABLE;
            }
        }

    }

    public enum Availability {
        FREE, EQUIP, UNAVAILABLE
    }

    public boolean isUsable() {
        return actions.contains(Action.USE);
    }

    public boolean isDroppable() {
        return actions.contains(Action.DROP);
    }

    public boolean isConsumable() {
        return actions.contains(Action.CONSUME);
    }

    public boolean isEquippable() {
        return actions.contains(Action.EQUIP);
    }

    public boolean isFree() {
        return availability() == Availability.FREE;
    }

    public boolean isEquipped() {
        return availability() == Availability.EQUIP;
    }

    public Availability availability() {
        return state.availability();
    }

    public int count() {
        return state.count();
    }

}
