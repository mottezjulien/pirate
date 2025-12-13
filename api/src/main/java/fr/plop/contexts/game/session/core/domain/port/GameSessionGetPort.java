package fr.plop.contexts.game.session.core.domain.port;

import fr.plop.contexts.game.session.core.domain.model.GameSession;

import java.util.Optional;

public interface GameSessionGetPort {

    Optional<GameSession.Id> findById(GameSession.Id sessionId);

}
