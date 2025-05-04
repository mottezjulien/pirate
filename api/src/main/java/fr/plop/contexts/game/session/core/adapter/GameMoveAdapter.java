package fr.plop.contexts.game.session.core.adapter;

import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.board.persistence.entity.BoardConfigEntity;
import fr.plop.contexts.game.config.board.persistence.entity.BoardSpaceEntity;
import fr.plop.contexts.game.config.board.persistence.repository.BoardConfigRepository;
import fr.plop.contexts.game.session.board.persistence.BoardPositionEntity;
import fr.plop.contexts.game.session.board.persistence.BoardPositionRepository;
import fr.plop.contexts.game.session.core.domain.GameException;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.usecase.GameMoveUseCase;
import fr.plop.contexts.game.session.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.session.core.persistence.GamePlayerRepository;
import fr.plop.contexts.game.session.core.persistence.GameSessionRepository;
import fr.plop.generic.tools.StringTools;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;


@Configuration
public class GameMoveAdapter implements GameMoveUseCase.OutPort {

    //TODO Cache ??
    private final GamePlayerRepository playerRepository;

    private final GameSessionRepository sessionRepository;

    private final BoardConfigRepository boardConfigRepository;

    private final BoardPositionRepository positionRepository;

    public GameMoveAdapter(GamePlayerRepository playerRepository, GameSessionRepository sessionRepository, BoardConfigRepository boardConfigRepository, BoardPositionRepository positionRepository) {
        this.playerRepository = playerRepository;
        this.sessionRepository = sessionRepository;
        this.boardConfigRepository = boardConfigRepository;
        this.positionRepository = positionRepository;
    }



    @Override
    public BoardConfig boardBySessionId(GameSession.Id gameId) throws GameException {
        BoardConfig.Id boardId = new BoardConfig.Id(sessionRepository.boardId(gameId.value())
                .orElseThrow(() -> new GameException(GameException.Type.SESSION_NOT_FOUND)));
        BoardConfigEntity entity = boardConfigRepository.fullById(boardId.value())
                .orElseThrow(() -> new GameException(GameException.Type.SESSION_NOT_FOUND));
        return entity.toModel();
    }

    @Override
    public void savePosition(GamePlayer.Id playerId, List<BoardSpace.Id> spaceIds) throws GameException {

        GamePlayerEntity playerEntity =  playerRepository.findById(playerId.value())
                .orElseThrow(() -> new GameException(GameException.Type.PLAYER_NOT_FOUND));

        //TODO Insert Position
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
        playerEntity.setPosition(positionEntity);
        playerRepository.save(playerEntity);

    }


}
