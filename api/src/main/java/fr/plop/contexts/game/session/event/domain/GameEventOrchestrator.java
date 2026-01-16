package fr.plop.contexts.game.session.event.domain;

import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;

public interface GameEventOrchestrator {

    void fire(GameSessionContext context, GameEvent event);

    /**
     * Fire an event and wait for all listeners to complete processing.
     * Useful for tests to avoid race conditions.
     */
    void fireAndWait(GameSessionContext context, GameEvent event);

}
