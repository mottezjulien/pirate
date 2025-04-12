package fr.plop.contexts.game.adapter;

import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.domain.model.GamePlayer;
import fr.plop.contexts.game.domain.usecase.GameConnectUseCase;
import fr.plop.contexts.game.persistence.GamePlayerEntity;
import fr.plop.contexts.game.persistence.GamePlayerRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GameConnectAdapter implements GameConnectUseCase.OutPort {

    private final GamePlayerRepository gamePlayerRepository;

    public GameConnectAdapter(GamePlayerRepository gamePlayerRepository) {
        this.gamePlayerRepository = gamePlayerRepository;
    }

    @Override
    public Optional<GamePlayer> findByUserId(ConnectUser.Id id) {
        return gamePlayerRepository.findByUserIdAndActiveGameFetchGame(id.value())
                .map(GamePlayerEntity::toModel);
    }
}
