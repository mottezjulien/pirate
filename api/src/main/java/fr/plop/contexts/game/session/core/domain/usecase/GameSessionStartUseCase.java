package fr.plop.contexts.game.session.core.domain.usecase;

import fr.plop.contexts.game.session.core.domain.GameException;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.time.GameSessionTimer;

import java.util.Optional;

public class GameSessionStartUseCase {

    public interface DataOutput {
        Optional<GameSession.Atom> findSessionById(GameSession.Id sessionId);
    }

    private final DataOutput dataOutput;
    private final GameSessionTimer gameSessionTimer;

    public GameSessionStartUseCase(DataOutput dataOutput, GameSessionTimer gameSessionTimer) {
        this.dataOutput = dataOutput;
        this.gameSessionTimer = gameSessionTimer;
    }

    public GameSession.Atom apply(GameSession.Id sessionId, GamePlayer.Id playerId) throws GameException {

        final GameSession.Atom sessionAtom = dataOutput.findSessionById(sessionId)
                .orElseThrow(() -> new GameException(GameException.Type.SESSION_NOT_FOUND));

        //TODO check if player can start in the session
        //TODO check if session is active or not
        gameSessionTimer.start(sessionId);
        //TODO notify other players that the game has started

        return sessionAtom;
    }

}
