package fr.plop.contexts.connect.adapter;

import fr.plop.contexts.connect.domain.Connect;
import fr.plop.contexts.connect.domain.ConnectAuth;
import fr.plop.contexts.connect.domain.ConnectionCreateAuthUseCase;
import fr.plop.contexts.connect.domain.DeviceConnect;
import fr.plop.contexts.connect.persistence.ConnectionAuthEntity;
import fr.plop.contexts.connect.persistence.ConnectionAuthRepository;
import fr.plop.contexts.connect.persistence.ConnectionEntity;
import fr.plop.contexts.connect.persistence.ConnectionRepository;
import fr.plop.contexts.connect.persistence.ConnectionUserEntity;
import fr.plop.contexts.connect.persistence.ConnectionUserRepository;
import fr.plop.contexts.connect.persistence.DeviceConnectionEntity;
import fr.plop.generic.tools.StringTools;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class ConnectionCreateAuthAdapter implements ConnectionCreateAuthUseCase.DataOutPort {

    private final ConnectionRepository repository;
    private final ConnectionAuthRepository authRepository;
    private final ConnectionUserRepository userRepository;

    public ConnectionCreateAuthAdapter(ConnectionRepository repository, ConnectionAuthRepository authRepository, ConnectionUserRepository userRepository) {
        this.repository = repository;
        this.authRepository = authRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Optional<DeviceConnect> findByDeviceId(String deviceId) {
        return repository.findByDeviceIdFetchAuth(deviceId)
                .stream().findFirst()
                .map(DeviceConnectionEntity::toModel);
    }

    @Override
    public ConnectAuth createAuth(Connect connect) {
        ConnectionAuthEntity entity = new ConnectionAuthEntity();
        entity.setId(StringTools.generate());
        entity.setToken(StringTools.generate());
        entity.setCreatedAt(Instant.now());
        entity.setConnection(ConnectionEntity.fromModel(connect));
        return authRepository.save(entity).toModel();
    }

    @Override
    public DeviceConnect createDeviceConnectWithEmptyUser(String deviceId) {
        ConnectionUserEntity userEntity = new ConnectionUserEntity();
        userEntity.setId(StringTools.generate());

        DeviceConnectionEntity entity = new DeviceConnectionEntity();
        entity.setId(StringTools.generate());
        entity.setDeviceId(deviceId);
        entity.setUser(userRepository.save(userEntity));
        return repository.save(entity).toModel();
    }

}
