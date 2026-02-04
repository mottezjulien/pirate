package fr.plop.contexts.game.config.inventory.domain.model;

import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.image.Image;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public record GameConfigInventoryItem(Id id, I18n label, Image image, Optional<I18n> optDescription, Type type,
                                      int initValue,
                                      Optional<ScenarioConfig.Target.Id> optTargetId,
                                      ActionType actionType) {
    public record Id(String value) {

    }

    public enum Type { UNIQUE, COLLECTION }

    public enum ActionType {
        NONE, EQUIPPABLE, CONSUMABLE, USABLE
    }

    public boolean canEquipped() {
        return actionType == ActionType.EQUIPPABLE;
    }

    public boolean canConsumed() {
        return actionType == ActionType.CONSUMABLE;
    }

    public boolean canUsed() {
        return actionType == ActionType.USABLE;
    }

}
