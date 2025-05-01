package fr.plop.contexts.board.persistence.adapter;

import fr.plop.contexts.board.domain.model.Board;
import fr.plop.contexts.board.domain.model.BoardSpace;
import fr.plop.contexts.board.persistence.entity.BoardPositionEntity;
import fr.plop.contexts.board.persistence.entity.BoardSpaceEntity;
import fr.plop.contexts.board.persistence.repository.BoardPositionRepository;
import fr.plop.contexts.game.domain.model.GamePlayer;
import fr.plop.contexts.game.persistence.GamePlayerEntity;
import fr.plop.generic.tools.StringTools;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class BoardPositionAdapterRepository {

    private final BoardPositionRepository repository;

    public BoardPositionAdapterRepository(BoardPositionRepository repository) {
        this.repository = repository;
    }

    public void save(Board board, GamePlayer.Id playerId) {
        Optional<BoardPositionEntity> opt = repository.findByPlayerIdFetchSpaces(playerId.value());
        BoardPositionEntity position = opt.orElseGet(() -> {
            BoardPositionEntity entity = new BoardPositionEntity();
            entity.setId(StringTools.generate());
            GamePlayerEntity player = new GamePlayerEntity();
            player.setId(playerId.value());
            entity.setPlayer(player);
            return repository.save(entity);
        });

        List<BoardSpace> spaces = board.spacesByPlayerId(playerId);

        position.getSpaces().clear();
        position.getSpaces().addAll(spaces.stream().map(space -> {
            BoardSpaceEntity entity = new BoardSpaceEntity();
            entity.setId(space.id().value());
            return entity;
        }).toList());
        position.setDateTime(Instant.now());

        repository.save(position);
    }
}
