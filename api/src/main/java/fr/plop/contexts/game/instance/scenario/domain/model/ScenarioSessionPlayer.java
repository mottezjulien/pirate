package fr.plop.contexts.game.instance.scenario.domain.model;

import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;

import java.util.Map;
import java.util.Optional;

public record ScenarioSessionPlayer(Map<ScenarioConfig.Step.Id, ScenarioSessionState> steps,
                                    Map<ScenarioConfig.Target.Id, ScenarioSessionState> targets) {
    public boolean isStepPresent(ScenarioConfig.Step.Id stepId) {
        return optStepState(stepId).isPresent();
    }

    public boolean isTargetDone(ScenarioConfig.Target.Id targetId) {
        return optTargetState(targetId).map(state -> state == ScenarioSessionState.SUCCESS)
                .orElse(false);
    }

    public Optional<ScenarioSessionState> optStepState(ScenarioConfig.Step.Id stepId) {
        return Optional.ofNullable(steps.get(stepId));
    }

    public Optional<ScenarioSessionState> optTargetState(ScenarioConfig.Target.Id targetId) {
        return Optional.ofNullable(targets.get(targetId));
    }

}
