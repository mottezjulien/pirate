package fr.plop.contexts.game.session.scenario.domain.usecase;

import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.session.core.domain.model.GameContext;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionPlayer;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScenarioSessionPlayerGetUseCase {

    public interface Port {
        ScenarioConfig scenario(GameSession.Id sessionId);
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

    public ScenarioSessionPlayer findByPlayerId(GameContext context) {
        ScenarioConfig config = port.scenario(context.sessionId());
        Map<ScenarioConfig.Step.Id, ScenarioSessionState> savedBySteps = port.steps(context.playerId());
        Map<ScenarioConfig.Target.Id, ScenarioSessionState> savedByTargets = port.targets(context.playerId());

        Map<ScenarioConfig.Step.Id, ScenarioSessionState> bySteps = new HashMap<>();
        Map<ScenarioConfig.Target.Id, ScenarioSessionState> byTargets = new HashMap<>();
        config.steps().forEach(step -> {
            if(savedBySteps.containsKey(step.id())) {
                ScenarioSessionState state = savedBySteps.get(step.id());
                bySteps.put(step.id(), state);
                step.targets().forEach(target -> byTargets.put(target.id(), savedByTargets.get(target.id())));
            }
        });
        return new ScenarioSessionPlayer(bySteps, byTargets);
    }


}
