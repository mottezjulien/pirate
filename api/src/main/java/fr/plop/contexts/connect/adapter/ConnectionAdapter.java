package fr.plop.contexts.connect.adapter;

import fr.plop.contexts.connect.domain.ConnectAuth;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.domain.ConnectUseCase;
import fr.plop.contexts.connect.persistence.ConnectionAuthEntity;
import fr.plop.contexts.connect.persistence.ConnectionAuthRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ConnectionAdapter implements ConnectUseCase.OutPort {

    private final ConnectionAuthRepository authRepository;

    public ConnectionAdapter(ConnectionAuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    @Override
    public Optional<ConnectAuth> findByToken(ConnectToken token) {
         return authRepository.findByTokenFetchs(token.value())
                 .map(ConnectionAuthEntity::toModel);
    }

}
