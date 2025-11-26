package fr.plop.contexts.game.session.core.domain.usecase;

import fr.plop.contexts.game.session.core.domain.GameException;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.port.GameSessionGetPort;
import fr.plop.contexts.game.session.time.GameSessionTimer;

public class GameSessionStartUseCase {

    public interface Port {
        void active(GameSession.Id sessionId);
    }

    private final Port port;
    private final GameSessionGetPort get;
    private final GameSessionTimer timer;

    public GameSessionStartUseCase(Port port, GameSessionGetPort get, GameSessionTimer timer) {
        this.port = port;
        this.get = get;
        this.timer = timer;
    }

    public GameSession.Atom apply(GameSession.Id sessionId, GamePlayer.Id playerId) throws GameException {

        final GameSession.Atom session = get.findById(sessionId)
                .orElseThrow(() -> new GameException(GameException.Type.SESSION_NOT_FOUND));

        //TODO check if player can start in the session
        //TODO check if session is active or not
        timer.start(sessionId);
        //TODO notify other players that the game has started

        port.active(sessionId);
        return session;
    }

}
