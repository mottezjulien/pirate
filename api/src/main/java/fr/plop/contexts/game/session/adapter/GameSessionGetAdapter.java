package fr.plop.contexts.game.session.adapter;

import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.port.GameSessionGetPort;
import fr.plop.contexts.game.session.core.persistence.GameSessionEntity;
import fr.plop.contexts.game.session.core.persistence.GameSessionRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GameSessionGetAdapter implements GameSessionGetPort {

    private final GameSessionRepository sessionRepository;

    public GameSessionGetAdapter(GameSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public Optional<GameSession.Atom> findById(GameSession.Id sessionId) {
        return sessionRepository.findById(sessionId.value())
                .map(GameSessionEntity::toAtomModel);
    }

}