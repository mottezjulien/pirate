package fr.plop.contexts.game.session.core.adapter;

import fr.plop.contexts.connect.domain.ConnectAuthGameSession;
import fr.plop.contexts.connect.persistence.repository.ConnectionAuthGameSessionRepository;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.usecase.GameSessionStartUseCase;
import fr.plop.contexts.game.session.core.persistence.GameSessionEntity;
import fr.plop.contexts.game.session.core.persistence.GameSessionRepository;
import org.springframework.stereotype.Component;

@Component
public class GameSessionStartAdapter implements GameSessionStartUseCase.Port {

    private final GameSessionRepository sessionRepository;

    private final ConnectionAuthGameSessionRepository authGameSessionRepository;

    public GameSessionStartAdapter(GameSessionRepository sessionRepository, ConnectionAuthGameSessionRepository authGameSessionRepository) {
        this.sessionRepository = sessionRepository;
        this.authGameSessionRepository = authGameSessionRepository;
    }


    @Override
    public void active(GameSession.Id sessionId) {
        GameSessionEntity session = sessionRepository.findById(sessionId.value())
                .orElseThrow(() -> new RuntimeException("No session found with id : " + sessionId));
        session.setState(GameSession.State.ACTIVE);
        sessionRepository.save(session);
    }

    @Override
    public void active(ConnectAuthGameSession.Id id) {
        authGameSessionRepository.findById(id.value())
            .ifPresent(entity -> {
                entity.setType(ConnectAuthGameSession.Type.OPENED);
                authGameSessionRepository.save(entity);
            });
    }
}
