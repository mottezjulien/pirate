package fr.plop.contexts.connect.adapter;


import fr.plop.contexts.connect.domain.ConnectAuthGameInstance;
import fr.plop.contexts.connect.domain.ConnectAuthUser;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.persistence.entity.ConnectionAuthGameInstanceEntity;
import fr.plop.contexts.connect.persistence.repository.ConnectionAuthGameInstanceRepository;
import fr.plop.contexts.connect.usecase.ConnectAuthGameInstanceUseCase;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.user.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class ConnectAuthGameInstanceCreateAdapter implements ConnectAuthGameInstanceUseCase.Port {

    private final ConnectionAuthGameInstanceRepository repository;

    public ConnectAuthGameInstanceCreateAdapter(ConnectionAuthGameInstanceRepository repository) {
        this.repository = repository;
    }

    @Override
    public Stream<ConnectAuthGameInstance> findOpenedByUserId(User.Id id) {
        //TODO Le check uniquement avec le status n'est pas bon, il faut vérifier que la date n'est pas bonne ...
        return repository.fullByUserIdAndTypes(id.value(), ConnectAuthGameInstance.Status.OPENED)
                .stream().map(ConnectionAuthGameInstanceEntity::toModel);
    }

    @Override
    public Optional<ConnectAuthGameInstance> findByToken(ConnectToken connectToken) {
        return repository.fullByToken(connectToken.value())
                .map(ConnectionAuthGameInstanceEntity::toModel);
    }


    @Override
    public ConnectAuthGameInstance create(ConnectAuthUser.Id authUserId, GameInstanceContext context) {
        ConnectAuthGameInstance model = ConnectAuthGameInstance.create(authUserId, context);
        repository.save(ConnectionAuthGameInstanceEntity.fromModel(model));
        return model;
    }

    @Override
    public Optional<ConnectAuthGameInstance> close(ConnectAuthGameInstance.Id authId) {
        return repository.findById(authId.value())
                .map(entity -> {
                    entity.setType(ConnectAuthGameInstance.Status.CLOSED);
                    return repository.save(entity).toModel();
                });
    }


}
