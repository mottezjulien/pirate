package fr.plop.contexts.game.instance.core.adapter;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.board.persistence.entity.BoardSpaceEntity;
import fr.plop.contexts.game.instance.board.persistence.BoardPositionEntity;
import fr.plop.contexts.game.instance.board.persistence.BoardPositionRepository;
import fr.plop.contexts.game.instance.core.domain.GameInstanceException;
import fr.plop.contexts.game.instance.core.domain.model.GamePlayer;
import fr.plop.contexts.game.instance.core.domain.usecase.GameInstanceMoveUseCase;
import fr.plop.contexts.game.instance.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.instance.core.persistence.GamePlayerRepository;
import fr.plop.generic.tools.StringTools;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class GameMoveAdapter implements GameInstanceMoveUseCase.OutPort {

    private final GamePlayerRepository playerRepository;

    private final BoardPositionRepository positionRepository;

    public GameMoveAdapter(GamePlayerRepository playerRepository, BoardPositionRepository positionRepository) {
        this.playerRepository = playerRepository;
        this.positionRepository = positionRepository;
    }

    @Override
    public void savePosition(GamePlayer.Id playerId, List<BoardSpace.Id> spaceIds) throws GameInstanceException {

        GamePlayerEntity playerEntity = playerRepository.findById(playerId.value())
                .orElseThrow(() -> new GameInstanceException(GameInstanceException.Type.PLAYER_NOT_FOUND));

        BoardPositionEntity positionEntity = new BoardPositionEntity();
        positionEntity.setId(StringTools.generate());
        positionEntity.setPlayer(playerEntity);
        positionEntity.setSpaces(spaceIds.stream().map(spaceId -> {
            BoardSpaceEntity spaceEntity = new BoardSpaceEntity();
            spaceEntity.setId(spaceId.value());
            return spaceEntity;
        }).collect(Collectors.toSet()));
        positionEntity.setDateTime(Instant.now());
        positionRepository.save(positionEntity);

        //UPDATE LAST POSITION
        playerEntity.setLastPosition(positionEntity);
        playerRepository.save(playerEntity);

    }


}
