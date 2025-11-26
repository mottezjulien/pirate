package fr.plop.contexts.game.config.cache;

import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.TalkConfig;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GameConfigCacheAdapter implements GameConfigCache {

    private record Data(List<GamePlayer.Id> playerIds, ScenarioConfig scenario, BoardConfig board, MapConfig map, TalkConfig talk) {

    }

    private final Map<GameSession.Id, Data> sessions = new HashMap<>();

    @Override
    public void insert(GameSession session) {
        sessions.put(session.id(), new Data(session.players().stream().map(GamePlayer::id).toList(),
                session.scenario().config(), session.board(), session.map(), session.talk()));
    }

    @Override
    public void remove(GameSession.Id sessionId) {
        sessions.remove(sessionId);
    }

    @Override
    public ScenarioConfig scenario(GameSession.Id sessionId) {
        return sessions.get(sessionId).scenario;
    }

    @Override
    public MapConfig map(GameSession.Id sessionId) {
        return sessions.get(sessionId).map;
    }

}


