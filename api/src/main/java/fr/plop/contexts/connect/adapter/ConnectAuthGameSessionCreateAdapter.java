package fr.plop.contexts.connect.adapter;


import fr.plop.contexts.connect.domain.ConnectAuthGameSession;
import fr.plop.contexts.connect.domain.ConnectAuthUser;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.persistence.entity.ConnectionAuthGameSessionEntity;
import fr.plop.contexts.connect.persistence.repository.ConnectionAuthGameSessionRepository;
import fr.plop.contexts.connect.usecase.ConnectAuthGameSessionUseCase;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.user.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class ConnectAuthGameSessionCreateAdapter implements ConnectAuthGameSessionUseCase.Port {

    private final ConnectionAuthGameSessionRepository authGameSessionRepository;

    public ConnectAuthGameSessionCreateAdapter(ConnectionAuthGameSessionRepository authGameSessionRepository) {
        this.authGameSessionRepository = authGameSessionRepository;
    }

    @Override
    public Stream<ConnectAuthGameSession> findOpenedByUserId(User.Id id) {
        return authGameSessionRepository.fullByUserIdAndTypes(id.value(), openedTypes())
                .stream().map(ConnectionAuthGameSessionEntity::toModel);
    }

    private List<ConnectAuthGameSession.Type> openedTypes() {
        return List.of(ConnectAuthGameSession.Type.INIT, ConnectAuthGameSession.Type.OPENED);
    }

    @Override
    public Optional<ConnectAuthGameSession> findByToken(ConnectToken connectToken) {
        return authGameSessionRepository.fullByToken(connectToken.value())
                .map(ConnectionAuthGameSessionEntity::toModel);
    }


    @Override
    public ConnectAuthGameSession create(ConnectAuthUser.Id authUserId, GameSessionContext context) {
        ConnectAuthGameSession model = ConnectAuthGameSession.init(authUserId, context);
        authGameSessionRepository.save(ConnectionAuthGameSessionEntity.fromModel(model));
        return model;
    }

    @Override
    public void close(ConnectAuthGameSession.Id authSessionId) {
        authGameSessionRepository.findById(authSessionId.value())
                .ifPresent(entity -> {
                    entity.setType(ConnectAuthGameSession.Type.CLOSED);
                    authGameSessionRepository.save(entity);
                });
    }


}
