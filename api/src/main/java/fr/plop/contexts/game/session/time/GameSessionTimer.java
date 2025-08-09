package fr.plop.contexts.game.session.time;


import fr.plop.contexts.game.session.core.domain.model.GameSession;

public interface GameSessionTimer {
    void start(GameSession.Id id);

    TimeUnit current(GameSession.Id sessionId);
}
