package fr.plop.contexts.game.config.inventory.domain.model;

import fr.plop.generic.tools.StringTools;

import java.util.List;
import java.util.Optional;

public record InventoryConfig(Id id, List<GameConfigInventoryItem> items, List<InventoryMergedRule> mergedRules) {

    public InventoryConfig() {
        this(new Id(), List.of(), List.of());
    }

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public Optional<GameConfigInventoryItem> byId(GameConfigInventoryItem.Id itemId) {
        return  items.stream().filter(item -> item.id().equals(itemId)).findFirst();
    }

    public boolean isMergeable(GameConfigInventoryItem.Id itemId) {
        return mergedRules.stream().anyMatch(rule -> rule.canMerged(itemId));
    }

    public boolean isMergeable(GameConfigInventoryItem.Id oneId, GameConfigInventoryItem.Id otherId) {
        return mergedRules.stream().anyMatch(rule -> rule.canMerged(oneId, otherId));
    }

}
