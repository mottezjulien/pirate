package fr.plop.contexts.game.session.scenario.domain.model;

import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;

import java.util.HashMap;
import java.util.Map;

public record ScenarioSessionPlayer(Map<ScenarioConfig.Step.Id, ScenarioSessionState> bySteps,
                                    Map<ScenarioConfig.Target.Id, ScenarioSessionState> byTargets) {

    public static ScenarioSessionPlayer build() {
        return new ScenarioSessionPlayer(new HashMap<>(), new HashMap<>());
    }

    public void initStep(ScenarioConfig.Step step) {
        bySteps.put(step.id(), ScenarioSessionState.ACTIVE);
        step.targets().forEach(target -> byTargets.put(target.id(), ScenarioSessionState.ACTIVE));
    }
}
