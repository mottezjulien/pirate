package fr.plop.contexts.connect.adapter;

import fr.plop.contexts.connect.domain.ConnectAuth;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.domain.ConnectUseCase;
import fr.plop.contexts.connect.persistence.entity.ConnectionAuthEntity;
import fr.plop.contexts.connect.persistence.entity.ConnectionDeviceEntity;
import fr.plop.contexts.connect.persistence.repository.ConnectionAuthRepository;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
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
        return authRepository.findByTokenFetchs(token.value())
                .map(entity -> {
                    ConnectionDeviceEntity connection = entity.getConnection();
                    Optional<GamePlayerEntity> optFullPlayerEntity = fullBySessionIdAndUserIdFetchLastPosition(sessionId, connection);
                    if (optFullPlayerEntity.isPresent()) {
                        return entity.toModelWithConnect(connection.toModel(optFullPlayerEntity.get().toModel()));
                    }
                    return entity.toModel();
                });
    }

    private Optional<GamePlayerEntity> fullBySessionIdAndUserIdFetchLastPosition(GameSession.Id sessionId, ConnectionDeviceEntity connection){
        //fetch space && goal bugs -> separate queries
        Optional<GamePlayerEntity> optWithSpaceIds = playerRepository.findBySessionIdAndUserIdFetchLastPosition(sessionId.value(), connection.getUser().getId());
        if (optWithSpaceIds.isPresent()) {
            GamePlayerEntity withSpaceIds = optWithSpaceIds.get();
            Optional<GamePlayerEntity> optWithGoals = playerRepository.findByIdFetchGoals(withSpaceIds.getId());
            if (optWithGoals.isPresent()) {
                withSpaceIds.setGoals(optWithGoals.get().getGoals());
                return Optional.of(withSpaceIds);
            }
        }
        return Optional.empty();
    }


}
