package fr.plop.contexts.game.session.inventory.adapter;

import fr.plop.contexts.game.config.inventory.domain.model.InventoryItem;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.inventory.domain.GameSessionInventoryUseCase;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GameSessionInventoryAdapter implements GameSessionInventoryUseCase.Port {

    @Override
    public Map<InventoryItem.Id, Integer> list(GamePlayer.Id id) {
        return Map.of(); //TODO
    }
}
