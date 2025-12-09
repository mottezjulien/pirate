package fr.plop.contexts.connect.adapter;

import fr.plop.contexts.connect.domain.ConnectAuth;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.domain.ConnectUseCase;
import fr.plop.contexts.connect.persistence.entity.ConnectionAuthEntity;
import fr.plop.contexts.connect.persistence.repository.ConnectionAuthRepository;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.session.core.persistence.GamePlayerRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ConnectionAdapter implements ConnectUseCase.OutPort {

    private final ConnectionAuthRepository authRepository;
    private final GamePlayerRepository playerRepository;

    public ConnectionAdapter(ConnectionAuthRepository authRepository, GamePlayerRepository playerRepository) {
        this.authRepository = authRepository;
        this.playerRepository = playerRepository;
    }

    @Override
    public Optional<ConnectAuth> findByToken(ConnectToken token) {
        return authRepository.findByTokenFetchs(token.value()).map(ConnectionAuthEntity::toModel);
    }

    @Override
    public Optional<ConnectAuth> findBySessionIdAndToken(GameSession.Id sessionId, ConnectToken token) {
        return findByToken(token)
                .map(auth -> playerRepository.findBySessionIdAndUserId(sessionId.value(), auth.userId().value())
                        .map(GamePlayerEntity::toModelId)
                        .map(auth::withPlayerId)
                        .orElse(auth));
    }

}
