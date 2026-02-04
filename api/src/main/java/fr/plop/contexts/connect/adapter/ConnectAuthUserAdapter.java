package fr.plop.contexts.connect.adapter;

import fr.plop.contexts.connect.domain.ConnectAuthUser;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.usecase.ConnectAuthUserCreateUseCase;
import fr.plop.contexts.connect.domain.ConnectUserDevice;
import fr.plop.contexts.connect.persistence.entity.ConnectionAuthUserEntity;
import fr.plop.contexts.connect.persistence.entity.ConnectionUserDeviceEntity;
import fr.plop.contexts.connect.usecase.ConnectAuthUserGetUseCase;
import fr.plop.contexts.user.persistence.UserEntity;
import fr.plop.contexts.connect.persistence.repository.ConnectionAuthUserRepository;
import fr.plop.contexts.connect.persistence.repository.ConnectionDeviceRepository;
import fr.plop.contexts.user.persistence.UserRepository;
import fr.plop.generic.tools.StringTools;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class ConnectAuthUserAdapter implements ConnectAuthUserGetUseCase.Port, ConnectAuthUserCreateUseCase.Port {

    private final ConnectionDeviceRepository repository;
    private final ConnectionAuthUserRepository authRepository;
    private final UserRepository userRepository;

    public ConnectAuthUserAdapter(ConnectionDeviceRepository repository, ConnectionAuthUserRepository authRepository, UserRepository userRepository) {
        this.repository = repository;
        this.authRepository = authRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Optional<ConnectAuthUser> findByToken(ConnectToken token) {
        return authRepository.fullByToken(token.value())
                .map(ConnectionAuthUserEntity::toModel);
    }

    @Override
    public Optional<ConnectUserDevice> findByDeviceId(String deviceId) {
        return repository.findByDeviceIdFetchUser(deviceId)
                .stream().findFirst()
                .map(ConnectionUserDeviceEntity::toModel);
    }

    @Override
    public ConnectAuthUser createAuth(ConnectUserDevice connect) {
        ConnectionAuthUserEntity entity = new ConnectionAuthUserEntity();
        entity.setId(StringTools.generate());
        entity.setToken(StringTools.generate());
        entity.setCreatedAt(Instant.now());
        entity.setConnection(ConnectionUserDeviceEntity.fromModelId(connect.id()));
        return authRepository.save(entity)
                .toModelWithConnect(connect);
    }

    @Override
    public ConnectUserDevice createDeviceConnect(String deviceId) {
        ConnectionUserDeviceEntity entity = new ConnectionUserDeviceEntity();
        entity.setId(StringTools.generate());
        entity.setDeviceId(deviceId);
        entity.setUser(userRepository.save(UserEntity.buildNone()));
        repository.save(entity);
        return entity.toModel();
    }

    @Override
    public Optional<ConnectAuthUser> lastAuth(ConnectUserDevice.Id id) {
        List<ConnectionAuthUserEntity> list = authRepository.fullByConnectIdOrderByCreatedAtDesc(
                id.value(),
                Limit.of(1));
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(list.getFirst().toModel());

    }

}
