package fr.plop.contexts.connect.adapter;

import fr.plop.contexts.connect.domain.ConnectAuth;
import fr.plop.contexts.connect.domain.ConnectionCreateAuthUseCase;
import fr.plop.contexts.connect.domain.DeviceConnect;
import fr.plop.contexts.connect.persistence.entity.ConnectionAuthEntity;
import fr.plop.contexts.connect.persistence.entity.ConnectionDeviceEntity;
import fr.plop.contexts.connect.persistence.entity.ConnectionUserEntity;
import fr.plop.contexts.connect.persistence.repository.ConnectionAuthRepository;
import fr.plop.contexts.connect.persistence.repository.ConnectionDeviceRepository;
import fr.plop.contexts.connect.persistence.repository.ConnectionUserRepository;
import fr.plop.contexts.i18n.domain.Language;
import fr.plop.generic.tools.StringTools;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class ConnectionCreateAuthAdapter implements ConnectionCreateAuthUseCase.DataOutPort {

    private final ConnectionDeviceRepository repository;
    private final ConnectionAuthRepository authRepository;
    private final ConnectionUserRepository userRepository;

    public ConnectionCreateAuthAdapter(ConnectionDeviceRepository repository, ConnectionAuthRepository authRepository, ConnectionUserRepository userRepository) {
        this.repository = repository;
        this.authRepository = authRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Optional<DeviceConnect> findByDeviceId(String deviceId) {
        return repository.findByDeviceIdFetchUser(deviceId)
                .stream().findFirst()
                .map(entity -> entity.toModel(null));
    }

    @Override
    public ConnectAuth createAuth(DeviceConnect connect) {
        ConnectionAuthEntity entity = new ConnectionAuthEntity();
        entity.setId(StringTools.generate());
        entity.setToken(StringTools.generate());
        entity.setCreatedAt(Instant.now());
        ConnectionDeviceEntity connectionEntity = new ConnectionDeviceEntity(); //TODO manage other type of connection
        connectionEntity.setId(connect.id().value());
        entity.setConnection(connectionEntity);
        return authRepository.save(entity).toModelWithConnect(connect);
    }

    @Override
    public DeviceConnect createDeviceConnectWithUnknownUser(String deviceId) {
        ConnectionUserEntity userEntity = new ConnectionUserEntity();
        userEntity.setId(StringTools.generate());
        userEntity.setLanguage(Language.byDefault());

        ConnectionDeviceEntity entity = new ConnectionDeviceEntity();
        entity.setId(StringTools.generate());
        entity.setDeviceId(deviceId);
        entity.setUser(userRepository.save(userEntity));
        repository.save(entity);
        return entity.toModel(null);
    }

    @Override
    public Optional<ConnectAuth> lastAuth(DeviceConnect.Id id) {
        List<ConnectionAuthEntity> list = authRepository.findByConnectIdFetchsOrderByCreatedAtDesc(
                id.value(),
                Limit.of(1));
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(list.getFirst().toModel(null));

    }

}
