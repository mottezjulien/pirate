package fr.plop.contexts.game.config.cache;

import fr.plop.contexts.game.config.Image.domain.ImageConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.inventory.domain.model.InventoryConfig;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.TalkConfig;
import fr.plop.contexts.game.instance.core.domain.model.GameInstance;
import fr.plop.contexts.game.instance.core.domain.model.GamePlayer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GameConfigCacheAdapter implements GameConfigCache {

    private record Data(List<GamePlayer.Id> playerIds, ScenarioConfig scenario,
                        BoardConfig board, MapConfig map, TalkConfig talk, ImageConfig image, InventoryConfig inventory) {

    }

    private final Map<GameInstance.Id, Data> instances = new HashMap<>();

    @Override
    public void insert(GameInstance instance) {
        instances.put(instance.id(), new Data(instance.players().map(GamePlayer::id).toList(),
                instance.scenario().config(), instance.board(), instance.map(), instance.talk(), instance.image(), instance.inventory()));
    }

    @Override
    public void remove(GameInstance.Id instanceId) {
        instances.remove(instanceId);
    }

    @Override
    public ScenarioConfig scenario(GameInstance.Id instanceId) {
        return data(instanceId).scenario;
    }

    @Override
    public BoardConfig board(GameInstance.Id instanceId) {
        return data(instanceId).board;
    }

    @Override
    public MapConfig map(GameInstance.Id instanceId) {
        return data(instanceId).map;
    }

    @Override
    public TalkConfig talk(GameInstance.Id instanceId) {
        return data(instanceId).talk;
    }

    @Override
    public ImageConfig image(GameInstance.Id instanceId) {
        return data(instanceId).image;
    }

    @Override
    public InventoryConfig inventory(GameInstance.Id instanceId) {
        return data(instanceId).inventory;
    }

    private Data data(GameInstance.Id instanceId) {
        return instances.get(instanceId);
    }

}


