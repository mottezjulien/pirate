package fr.plop.contexts.game.config.inventory.domain.model;

import java.util.List;

public record InventoryConfig(List<InventoryItem> items, List<InventoryMergedRule> mergedRules) {
    public InventoryItem byId(InventoryItem.Id itemId) {
        return null;
    }

    public boolean isMergeable(InventoryItem.Id itemId) {
        return false;
    }
}
