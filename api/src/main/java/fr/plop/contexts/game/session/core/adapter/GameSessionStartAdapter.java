package fr.plop.contexts.game.session.core.adapter;

import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.usecase.GameSessionStartUseCase;
import fr.plop.contexts.game.session.core.persistence.GameSessionEntity;
import fr.plop.contexts.game.session.core.persistence.GameSessionRepository;
import org.springframework.stereotype.Component;

@Component
public class GameSessionStartAdapter implements GameSessionStartUseCase.Port {

    private final GameSessionRepository sessionRepository;

    public GameSessionStartAdapter(GameSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }


    @Override
    public void active(GameSession.Id sessionId) {
        GameSessionEntity session = sessionRepository.findById(sessionId.value())
                .orElseThrow(() -> new RuntimeException("No session found with id : " + sessionId));
        session.setState(GameSession.State.ACTIVE);
        sessionRepository.save(session);
    }
}
