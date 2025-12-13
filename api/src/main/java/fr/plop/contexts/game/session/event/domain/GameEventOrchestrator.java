package fr.plop.contexts.game.session.event.domain;

import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;

public interface GameEventOrchestrator {

    void fire(GameSessionContext context, GameEvent event);

}
