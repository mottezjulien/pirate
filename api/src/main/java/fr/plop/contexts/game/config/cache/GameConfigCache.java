package fr.plop.contexts.game.config.cache;

import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.session.core.domain.model.GameSession;

public interface GameConfigCache {
    void insert(GameSession session);
    void remove(GameSession.Id sessionId);
    ScenarioConfig scenario(GameSession.Id sessionId);
    MapConfig map(GameSession.Id sessionId);

}
