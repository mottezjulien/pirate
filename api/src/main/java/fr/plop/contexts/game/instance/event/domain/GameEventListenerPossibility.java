package fr.plop.contexts.game.instance.event.domain;

import fr.plop.contexts.game.config.consequence.ConsequenceUseCase;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.usecase.PossibilityGetUseCase;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.instance.core.domain.port.GamePlayerActionPort;
import fr.plop.contexts.game.instance.time.GameInstanceTimeUnit;
import fr.plop.contexts.game.instance.time.GameInstanceTimerGet;

import java.util.List;

public class GameEventListenerPossibility implements GameEventListener {

    private final GameInstanceTimerGet timer;

    private final PossibilityGetUseCase possibilityGetUseCase;

    private final ConsequenceUseCase consequenceUseCase;

    private final GamePlayerActionPort action;

    public GameEventListenerPossibility(GameInstanceTimerGet timer, PossibilityGetUseCase possibilityGetUseCase, ConsequenceUseCase consequenceUseCase, GamePlayerActionPort action) {
        this.timer = timer;
        this.possibilityGetUseCase = possibilityGetUseCase;
        this.consequenceUseCase = consequenceUseCase;
        this.action = action;
    }

    @Override
    public void listen(GameInstanceContext context, GameEvent event) {
        GameInstanceTimeUnit timeUnit = timer.current(context.instanceId());
        List<Possibility> triggeredPossibilities = possibilityGetUseCase.findByEvent(context, event).toList();
        for (Possibility possibility : triggeredPossibilities) {
            possibility.consequences().forEach(consequence -> consequenceUseCase.action(context, consequence));
            action.save(context.playerId(), possibility.id(), timeUnit);
        }
    }
}
