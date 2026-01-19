package fr.plop.contexts.game.config.inventory.domain.model;

import java.util.Collections;
import java.util.List;

public record InventoryMergedRule(List<GameConfigInventoryItem.Id> accept, GameConfigInventoryItem.Id convertTo) {
    public boolean canMerged(GameConfigInventoryItem.Id oneId, GameConfigInventoryItem.Id otherId) {
        if(oneId.equals(otherId)) {
            return Collections.frequency(accept, oneId) == 2;
        }
        return canMerged(oneId) && canMerged(otherId);
    }

    public boolean canMerged(GameConfigInventoryItem.Id itemId) {
        return accept.contains(itemId);
    }


}
