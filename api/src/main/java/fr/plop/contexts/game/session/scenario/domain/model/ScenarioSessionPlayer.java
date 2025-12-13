package fr.plop.contexts.game.session.scenario.domain.model;

import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;

import java.util.Map;

public record ScenarioSessionPlayer(Map<ScenarioConfig.Step.Id, ScenarioSessionState> bySteps,
                                    Map<ScenarioConfig.Target.Id, ScenarioSessionState> byTargets) {
}
