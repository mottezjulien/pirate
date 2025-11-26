package fr.plop.contexts.game.session.adapter;

import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.port.GamePlayerGetPort;
import fr.plop.contexts.game.session.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.session.core.persistence.GamePlayerRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GamePlayerGetAdapter implements GamePlayerGetPort {

    private final GamePlayerRepository repository;

    public GamePlayerGetAdapter(GamePlayerRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<GamePlayer> findById(GamePlayer.Id playerId) {
        return repository.fullById(playerId.value())
                .map(GamePlayerEntity::toModel);
    }

}