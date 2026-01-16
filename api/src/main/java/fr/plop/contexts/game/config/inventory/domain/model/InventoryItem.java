package fr.plop.contexts.game.config.inventory.domain.model;

import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.session.inventory.domain.GameSessionInventoryItem;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.image.Image;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record InventoryItem(Id id, I18n label, Image image, Optional<I18n> optDescription,
                            Optional<ScenarioConfig.Target.Id> optLinkTargetId,
                            List<InventoryItemActionRule> actionRules
) {

    public GameSessionInventoryItem toSession(Integer count, boolean mergeable) {
        List<GameSessionInventoryItem.Action> actions = new ArrayList<>();
        if(optLinkTargetId.isEmpty()){
            actions.add(GameSessionInventoryItem.Action.DELETE);
        }
        if(actionRules.stream().anyMatch(InventoryItemActionRule::canEquiped)) {
            actions.add(GameSessionInventoryItem.Action.EQUIP);
        }
        if(actionRules.stream().anyMatch(InventoryItemActionRule::canConsumed)) {
            actions.add(GameSessionInventoryItem.Action.CONSUME);
        }
        if (mergeable) {
            actions.add(GameSessionInventoryItem.Action.MERGED);
        }
        return new GameSessionInventoryItem(id, label, image, optDescription, count, actions);
    }

    public record Id(String value) {

    }

}
