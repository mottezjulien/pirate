package fr.plop.contexts.game.instance.core.adapter;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.board.persistence.entity.BoardSpaceEntity;
import fr.plop.contexts.game.instance.core.domain.model.GamePlayer;
import fr.plop.contexts.game.instance.core.domain.port.GamePlayerGetPort;
import fr.plop.contexts.game.instance.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.instance.core.persistence.GamePlayerRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class GamePlayerGetAdapter implements GamePlayerGetPort {

    private final GamePlayerRepository repository;

    public GamePlayerGetAdapter(GamePlayerRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<BoardSpace.Id> findSpaceIdsByPlayerId(GamePlayer.Id playerId) {
        Function<GamePlayerEntity, List<BoardSpace.Id>> toModelFct = gamePlayerEntity -> {
            if (gamePlayerEntity.getLastPosition() != null) {
                return gamePlayerEntity.getLastPosition().getSpaces().stream().map(BoardSpaceEntity::toModelId).toList();
            }
            return List.of();
        };
        return repository.findByIdFetchLastPosition(playerId.value())
                .map(toModelFct).orElse(List.of());
    }

}