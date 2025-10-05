package fr.plop.contexts.game.session.event.domain;

public interface GameEventBroadCast {

    void fire(GameEvent event, GameEventContext context);

}
