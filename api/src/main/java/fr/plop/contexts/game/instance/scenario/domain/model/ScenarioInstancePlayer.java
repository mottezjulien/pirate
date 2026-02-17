package fr.plop.contexts.game.instance.scenario.domain.model;

import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;

import java.util.Map;
import java.util.Optional;

public record ScenarioInstancePlayer(Map<ScenarioConfig.Step.Id, ScenarioState> steps,
                                     Map<ScenarioConfig.Target.Id, ScenarioState> targets) {
    public boolean isStepPresent(ScenarioConfig.Step.Id stepId) {
        return optStepState(stepId).isPresent();
    }

    public boolean isTargetDone(ScenarioConfig.Target.Id targetId) {
        return optTargetState(targetId).map(state -> state == ScenarioState.SUCCESS)
                .orElse(false);
    }

    public Optional<ScenarioState> optStepState(ScenarioConfig.Step.Id stepId) {
        return Optional.ofNullable(steps.get(stepId));
    }

    public Optional<ScenarioState> optTargetState(ScenarioConfig.Target.Id targetId) {
        return Optional.ofNullable(targets.get(targetId));
    }

}
