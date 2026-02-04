package fr.plop.contexts.game.instance.event.domain;

import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;

public interface GameEventListener {
    void listen(GameInstanceContext context, GameEvent event);

}
