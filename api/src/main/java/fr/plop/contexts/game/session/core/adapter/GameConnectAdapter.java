package fr.plop.contexts.game.session.core.adapter;

import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.usecase.GameConnectUseCase;
import fr.plop.contexts.game.session.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.session.core.persistence.GamePlayerRepository;
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
        return gamePlayerRepository.fullByUserIdAndActiveSession(id.value())
                .map(GamePlayerEntity::toModel);
    }
}
