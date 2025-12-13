package fr.plop.contexts.game.session.scenario.domain;

import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionPlayer;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionState;

import java.util.List;
import java.util.Map;

public interface GameSessionScenarioGoalPort {

    ScenarioSessionPlayer findByPlayerId(GamePlayer.Id playerId);

    List<ScenarioConfig.Step.Id> findActiveSteps(GamePlayer.Id playerId);

    List<ScenarioConfig.Target.Id> findActiveTargets(GamePlayer.Id playerId);

    Map<ScenarioConfig.Step.Id, ScenarioSessionState> findSteps(GamePlayer.Id playerId);

    Map<ScenarioConfig.Target.Id, ScenarioSessionState> findTargets(GamePlayer.Id playerId);

    void saveStep(GameSessionContext context, ScenarioConfig.Step.Id stepId, ScenarioSessionState state);

    void saveTarget(GamePlayer.Id playerId, ScenarioConfig.Target.Id targetId, ScenarioSessionState state);


}
