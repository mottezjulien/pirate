package fr.plop.contexts.game.config.scenario.domain.usecase;

import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.session.core.domain.model.GameAction;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.core.domain.port.GamePlayerActionPort;
import fr.plop.contexts.game.session.event.domain.GameEvent;
import fr.plop.contexts.game.session.scenario.domain.GameSessionScenarioGoalPort;
import fr.plop.contexts.game.session.situation.domain.GameSessionSituation;
import fr.plop.contexts.game.session.situation.domain.port.GameSessionSituationGetPort;

import java.util.List;
import java.util.stream.Stream;

public class PossibilityGetUseCase {

    private final GameSessionSituationGetPort situationGet;

    private final GamePlayerActionPort action;

    private final GameConfigCache cache;

    private final GameSessionScenarioGoalPort scenarioGoalPort;

    public PossibilityGetUseCase(GameSessionSituationGetPort situationGet, GamePlayerActionPort action, GameConfigCache cache, GameSessionScenarioGoalPort scenarioGoalPort) {
        this.situationGet = situationGet;
        this.action = action;
        this.cache = cache;
        this.scenarioGoalPort = scenarioGoalPort;
    }


    public Stream<Possibility> findByEvent(GameSessionContext context, GameEvent event) {
        List<GameAction> previousActions = action.findByPlayerId(context.playerId());
        GameSessionSituation situation = situationGet.get(context);
        List<ScenarioConfig.Step.Id> steps = situation.scenario().stepIds();
        ScenarioConfig cacheScenario = cache.scenario(context.sessionId());
        Stream<Possibility> possibilities = cacheScenario.steps().stream()
                .filter(step -> steps.contains(step.id()))
                .flatMap(step -> step.possibilities().stream());
        return possibilities
                .filter(possibility -> possibility.accept(event, previousActions, situation));
    }


}
