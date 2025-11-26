package fr.plop.contexts.game.session.time;


import fr.plop.contexts.game.session.core.domain.model.GameSession;

public interface GameSessionTimerGet {
    GameSessionTimeUnit current(GameSession.Id sessionId);

}
