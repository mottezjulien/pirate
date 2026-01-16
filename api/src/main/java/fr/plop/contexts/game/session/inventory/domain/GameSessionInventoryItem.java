package fr.plop.contexts.game.session.inventory.domain;

import fr.plop.contexts.game.config.inventory.domain.model.InventoryItem;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.image.Image;

import java.util.List;
import java.util.Optional;

public record GameSessionInventoryItem(InventoryItem.Id id, I18n label, Image image, Optional<I18n> optDescription,
                                       int count, List<Action> actions) {

    public enum Action {
        DELETE, EQUIP, MERGED, CONSUME
    }

}
