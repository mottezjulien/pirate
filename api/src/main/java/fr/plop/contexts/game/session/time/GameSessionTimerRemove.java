package fr.plop.contexts.game.session.time;


import fr.plop.contexts.game.session.core.domain.model.GameSession;

public interface GameSessionTimerRemove {
    void remove(GameSession.Id sessionId);

}
