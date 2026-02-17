package fr.plop.contexts.game.instance.scenario.domain.model;

import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.instance.core.domain.model.GamePlayer;

import java.util.HashMap;
import java.util.Map;

public record ScenarioInstance(ScenarioConfig config, Map<GamePlayer.Id, ScenarioInstancePlayer> byPlayers) {

    public static ScenarioInstance build(ScenarioConfig config) {
        return new ScenarioInstance(config, new HashMap<>());
    }





}
