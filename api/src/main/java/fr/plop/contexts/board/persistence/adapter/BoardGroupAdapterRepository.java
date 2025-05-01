package fr.plop.contexts.board.persistence.adapter;

import fr.plop.contexts.board.domain.model.Board;
import fr.plop.contexts.game.domain.model.GamePlayer;
import org.springframework.stereotype.Repository;

@Repository
public class BoardGroupAdapterRepository {

    private final BoardPositionAdapterRepository positionRepository;

    public BoardGroupAdapterRepository(BoardPositionAdapterRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    public void savePosition(Board board, GamePlayer.Id playerId) {
        positionRepository.save(board, playerId);
    }

}
