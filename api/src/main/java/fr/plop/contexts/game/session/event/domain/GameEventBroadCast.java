package fr.plop.contexts.game.session.event.domain;

import fr.plop.contexts.game.session.core.domain.model.GameContext;

public interface GameEventBroadCast {

    void fire(GameEvent event, GameContext context);

}
