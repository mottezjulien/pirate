package fr.plop.contexts.connect.adapter;

import fr.plop.contexts.connect.domain.ConnectAuth;
import fr.plop.contexts.connect.domain.ConnectionCreateAuthUseCase;
import fr.plop.contexts.connect.domain.DeviceConnect;
import fr.plop.contexts.connect.persistence.ConnectionAuthEntity;
import fr.plop.contexts.connect.persistence.ConnectionAuthRepository;
import fr.plop.contexts.connect.persistence.ConnectionUserEntity;
import fr.plop.contexts.connect.persistence.ConnectionUserRepository;
import fr.plop.contexts.connect.persistence.DeviceConnectionEntity;
import fr.plop.contexts.connect.persistence.DeviceConnectionRepository;
import fr.plop.generic.tools.StringTools;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class ConnectionCreateAuthAdapter implements ConnectionCreateAuthUseCase.DataOutPort {

    private final DeviceConnectionRepository repository;
    private final ConnectionAuthRepository authRepository;
    private final ConnectionUserRepository userRepository;

    public ConnectionCreateAuthAdapter(DeviceConnectionRepository repository, ConnectionAuthRepository authRepository, ConnectionUserRepository userRepository) {
        this.repository = repository;
        this.authRepository = authRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Optional<DeviceConnect> findByDeviceId(String deviceId) {
        return repository.findByDeviceIdFetchUser(deviceId)
                .stream().findFirst()
                .map(DeviceConnectionEntity::toModel);
    }

    @Override
    public ConnectAuth createAuth(DeviceConnect connect) {
        ConnectionAuthEntity entity = new ConnectionAuthEntity();
        entity.setId(StringTools.generate());
        entity.setToken(StringTools.generate());
        entity.setCreatedAt(Instant.now());
        DeviceConnectionEntity connectionEntity = new DeviceConnectionEntity(); //TODO manage other type of connection
        connectionEntity.setId(connect.id().value());
        entity.setConnection(connectionEntity);
        return authRepository.save(entity).toModelWithConnect(connect);
    }

    @Override
    public DeviceConnect createDeviceConnectWithEmptyUser(String deviceId) {
        ConnectionUserEntity userEntity = new ConnectionUserEntity();
        userEntity.setId(StringTools.generate());

        DeviceConnectionEntity entity = new DeviceConnectionEntity();
        entity.setId(StringTools.generate());
        entity.setDeviceId(deviceId);
        entity.setUser(userRepository.save(userEntity));
        repository.save(entity);
        return entity.toModel();
    }

    @Override
    public Optional<ConnectAuth> lastAuth(DeviceConnect.Id id) {
        List<ConnectionAuthEntity> list = authRepository.findByConnectIdFetchsOrderByCreatedAtDesc(
                id.value(),
                Limit.of(1));
        if(list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(list.getFirst().toModel());

    }

}
