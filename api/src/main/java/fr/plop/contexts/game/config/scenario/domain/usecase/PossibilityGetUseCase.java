package fr.plop.contexts.game.config.scenario.domain.usecase;

import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.instance.core.domain.model.GameAction;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.instance.core.domain.port.GamePlayerActionPort;
import fr.plop.contexts.game.instance.event.domain.GameEvent;
import fr.plop.contexts.game.instance.scenario.domain.GameInstanceScenarioGoalPort;
import fr.plop.contexts.game.instance.situation.domain.GameInstanceSituation;
import fr.plop.contexts.game.instance.situation.domain.port.GameInstanceSituationGetPort;

import java.util.List;
import java.util.stream.Stream;

public class PossibilityGetUseCase {

    private final GameInstanceSituationGetPort situationGet;

    private final GamePlayerActionPort action;

    private final GameConfigCache cache;

    private final GameInstanceScenarioGoalPort scenarioGoalPort;

    public PossibilityGetUseCase(GameInstanceSituationGetPort situationGet, GamePlayerActionPort action, GameConfigCache cache, GameInstanceScenarioGoalPort scenarioGoalPort) {
        this.situationGet = situationGet;
        this.action = action;
        this.cache = cache;
        this.scenarioGoalPort = scenarioGoalPort;
    }


    public Stream<Possibility> findByEvent(GameInstanceContext context, GameEvent event) {
        List<GameAction> previousActions = action.findByPlayerId(context.playerId());
        GameInstanceSituation situation = situationGet.get(context);
        List<ScenarioConfig.Step.Id> steps = situation.scenario().stepIds();
        System.out.println("DEBUG findByEvent: event=" + event + " active steps=" + steps);
        ScenarioConfig cacheScenario = cache.scenario(context.instanceId());
        Stream<Possibility> stepPossibilities = cacheScenario.steps().stream()
                .filter(step -> {
                    boolean active = steps.contains(step.id());
                    System.out.println("DEBUG findByEvent: step=" + step.id() + " active=" + active);
                    return active;
                })
                .flatMap(step -> step.possibilities().stream());
        return Stream.concat(stepPossibilities, cacheScenario.genericPossibilities().stream())
                .filter(possibility -> {
                    boolean accept = possibility.accept(event, previousActions, situation);
                    System.out.println("DEBUG findByEvent: possibility=" + possibility.id() + " accept=" + accept);
                    return accept;
                });
    }

}
