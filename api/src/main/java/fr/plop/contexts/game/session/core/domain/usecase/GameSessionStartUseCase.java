package fr.plop.contexts.game.session.core.domain.usecase;

import fr.plop.contexts.game.session.core.domain.GameException;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.time.GameSessionTimer;

public class GameSessionStartUseCase {
    private final GameSessionGetPort getPort;
    private final GameSessionTimer gameSessionTimer;

    public GameSessionStartUseCase(GameSessionGetPort getPort, GameSessionTimer gameSessionTimer) {
        this.getPort = getPort;
        this.gameSessionTimer = gameSessionTimer;
    }

    public GameSession.Atom apply(GameSession.Id sessionId, GamePlayer.Id playerId) throws GameException {

        final GameSession.Atom sessionAtom = getPort.findById(sessionId)
                .orElseThrow(() -> new GameException(GameException.Type.SESSION_NOT_FOUND));

        //TODO check if player can start in the session
        //TODO check if session is active or not
        gameSessionTimer.start(sessionId);
        //TODO notify other players that the game has started

        return sessionAtom;
    }

}
