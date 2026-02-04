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

    private final Map<GameInstance.Id, Data> sessions = new HashMap<>();

    @Override
    public void insert(GameInstance session) {
        sessions.put(session.id(), new Data(session.players().map(GamePlayer::id).toList(),
                session.scenario().config(), session.board(), session.map(), session.talk(), session.image(), session.inventory()));
    }

    @Override
    public void remove(GameInstance.Id sessionId) {
        sessions.remove(sessionId);
    }

    @Override
    public ScenarioConfig scenario(GameInstance.Id sessionId) {
        return data(sessionId).scenario;
    }

    @Override
    public BoardConfig board(GameInstance.Id sessionId) {
        return data(sessionId).board;
    }

    @Override
    public MapConfig map(GameInstance.Id sessionId) {
        return data(sessionId).map;
    }

    @Override
    public TalkConfig talk(GameInstance.Id sessionId) {
        return data(sessionId).talk;
    }

    @Override
    public ImageConfig image(GameInstance.Id sessionId) {
        return data(sessionId).image;
    }

    @Override
    public InventoryConfig inventory(GameInstance.Id sessionId) {
        return data(sessionId).inventory;
    }

    private Data data(GameInstance.Id sessionId) {
        return sessions.get(sessionId);
    }

}


