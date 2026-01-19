package fr.plop.contexts.game.config.inventory.domain.model;

import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.image.Image;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public record GameConfigInventoryItem(Id id, I18n label, Image image, Optional<I18n> optDescription, Type type,
                                      Optional<ScenarioConfig.Target.Id> optLinkTargetId,
                                      List<InventoryItemActionRule> actionRules) {
    public Stream<InventoryItemActionRule> consumeRules() {
        return actionRules.stream().filter(InventoryItemActionRule::canConsumed);
    }

    public Stream<InventoryItemActionRule> useRules() {
        return actionRules.stream().filter(InventoryItemActionRule::canUsed);
    }

    public record Id(String value) {

    }

    public enum Type { UNIQUE, COLLECTION }

}
