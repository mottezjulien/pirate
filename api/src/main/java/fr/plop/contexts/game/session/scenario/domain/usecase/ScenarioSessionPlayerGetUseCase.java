package fr.plop.contexts.game.session.scenario.domain.usecase;

import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionPlayer;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionState;

import java.util.List;
import java.util.Map;

public class ScenarioSessionPlayerGetUseCase {

    public interface Port {
        Map<ScenarioConfig.Step.Id, ScenarioSessionState> steps(GamePlayer.Id playerId);
        Map<ScenarioConfig.Target.Id, ScenarioSessionState> targets(GamePlayer.Id playerId);
    }

    private final Port port;

    public ScenarioSessionPlayerGetUseCase(Port port) {
        this.port = port;
    }

    public List<ScenarioConfig.Step.Id> findActiveStepIdsByPlayerId(GamePlayer.Id playerId) {
        return port.steps(playerId).entrySet().stream()
                .filter(entry -> entry.getValue() == ScenarioSessionState.ACTIVE)
                .map(Map.Entry::getKey).toList();
    }

    public ScenarioSessionPlayer findByPlayerId(GamePlayer.Id playerId) {
        return new ScenarioSessionPlayer(port.steps(playerId), port.targets(playerId));
    }

}
