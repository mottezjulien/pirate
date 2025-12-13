package fr.plop.contexts.game.session.event.domain;

import fr.plop.contexts.game.config.consequence.ConsequenceUseCase;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.usecase.PossibilityGetUseCase;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.core.domain.port.GamePlayerActionPort;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;
import fr.plop.contexts.game.session.time.GameSessionTimerGet;

import java.util.stream.Stream;

public class GameEventListenerPossibility implements GameEventListener {

    private final GameSessionTimerGet timer;

    private final PossibilityGetUseCase possibilityGetUseCase;

    private final ConsequenceUseCase consequenceUseCase;

    private final GamePlayerActionPort action;

    public GameEventListenerPossibility(GameSessionTimerGet timer, PossibilityGetUseCase possibilityGetUseCase, ConsequenceUseCase consequenceUseCase, GamePlayerActionPort action) {
        this.timer = timer;
        this.possibilityGetUseCase = possibilityGetUseCase;
        this.consequenceUseCase = consequenceUseCase;
        this.action = action;
    }

    @Override
    public void listen(GameSessionContext context, GameEvent event) {
        GameSessionTimeUnit timeUnit = timer.current(context.sessionId());
        Stream<Possibility> possibilities = possibilityGetUseCase.findByEvent(context, event);
        possibilities.forEach(possibility -> {
            possibility.consequences().forEach(consequence -> consequenceUseCase.action(context, consequence));
            action.save(context.playerId(), possibility.id(), timeUnit);
        });
    }
}
