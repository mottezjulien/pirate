package fr.plop.contexts.game.instance.event.domain;

import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;

public interface GameEventOrchestrator {

    void fire(GameInstanceContext context, GameEvent event);

    /**
     * Fire an event and wait for all listeners to complete processing.
     * Useful for tests to avoid race conditions.
     */
    void fireAndWait(GameInstanceContext context, GameEvent event);

}
