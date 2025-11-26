package fr.plop.contexts.game.session.scenario.domain.model;

import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;

import java.util.HashMap;
import java.util.Map;

public record ScenarioSession(ScenarioConfig config, Map<GamePlayer.Id, ScenarioSessionPlayer> byPlayers) {

    public static ScenarioSession build(ScenarioConfig config) {
        return new ScenarioSession(config, new HashMap<>());
    }

    public ScenarioSessionPlayer player(GamePlayer.Id playerId) {
        if(!byPlayers.containsKey(playerId)) {
            init(playerId);
        }
        return byPlayers.get(playerId);
    }

    private void init(GamePlayer.Id playerId) {
        ScenarioConfig.Step step = config.firstStep();
        ScenarioSessionPlayer sessionPlayer = ScenarioSessionPlayer.build();
        sessionPlayer.initStep(step);
        byPlayers.put(playerId, sessionPlayer);
    }

}
