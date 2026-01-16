package fr.plop.contexts.game.session.inventory.domain;

import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.config.inventory.domain.model.InventoryConfig;
import fr.plop.contexts.game.config.inventory.domain.model.InventoryItem;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;

import java.util.Map;
import java.util.stream.Stream;

public class GameSessionInventoryUseCase {

    public interface Port {
        Map<InventoryItem.Id, Integer> list(GamePlayer.Id id);

    }

    private final Port port;

    private final GameConfigCache cache;

    public GameSessionInventoryUseCase(Port port, GameConfigCache cache) {
        this.port = port;
        this.cache = cache;
    }

    public Stream<GameSessionInventoryItem> list(GameSessionContext context) {
        final InventoryConfig config = cache.inventory(context.sessionId());
        Map<InventoryItem.Id, Integer> inventoryIds = port.list(context.playerId());
        return inventoryIds.keySet().stream()
                .map(config::byId)
                .map(item -> item.toSession(inventoryIds.get(item.id()), config.isMergeable(item.id())));
    }
}
