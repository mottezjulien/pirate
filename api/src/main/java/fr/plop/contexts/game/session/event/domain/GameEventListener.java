package fr.plop.contexts.game.session.event.domain;

import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;

public interface GameEventListener {
    void listen(GameSessionContext context, GameEvent event);

}
