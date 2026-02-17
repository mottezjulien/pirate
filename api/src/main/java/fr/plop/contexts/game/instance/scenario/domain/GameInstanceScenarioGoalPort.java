package fr.plop.contexts.game.instance.scenario.domain;

import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.instance.core.domain.model.GamePlayer;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.instance.scenario.domain.model.ScenarioInstancePlayer;
import fr.plop.contexts.game.instance.scenario.domain.model.ScenarioState;

import java.util.List;
import java.util.Map;

public interface GameInstanceScenarioGoalPort {

    ScenarioInstancePlayer findByPlayerId(GamePlayer.Id playerId);

    List<ScenarioConfig.Step.Id> findActiveSteps(GamePlayer.Id playerId);

    List<ScenarioConfig.Target.Id> findActiveTargets(GamePlayer.Id playerId);

    Map<ScenarioConfig.Step.Id, ScenarioState> findSteps(GamePlayer.Id playerId);

    Map<ScenarioConfig.Target.Id, ScenarioState> findTargets(GamePlayer.Id playerId);

    void saveStep(GameInstanceContext context, ScenarioConfig.Step.Id stepId, ScenarioState state);

    void saveTarget(GamePlayer.Id playerId, ScenarioConfig.Target.Id targetId, ScenarioState state);


}
