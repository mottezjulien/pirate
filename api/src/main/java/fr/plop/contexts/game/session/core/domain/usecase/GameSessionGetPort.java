package fr.plop.contexts.game.session.core.domain.usecase;

import fr.plop.contexts.game.session.core.domain.model.GameSession;

import java.util.Optional;

public interface GameSessionGetPort {

    Optional<GameSession.Atom> findById(GameSession.Id sessionId);

}
