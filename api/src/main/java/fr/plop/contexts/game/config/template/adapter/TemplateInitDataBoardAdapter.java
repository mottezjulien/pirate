package fr.plop.contexts.game.config.template.adapter;

import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.persistence.entity.BoardConfigEntity;
import fr.plop.contexts.game.config.board.persistence.entity.BoardRectEntity;
import fr.plop.contexts.game.config.board.persistence.entity.BoardSpaceEntity;
import fr.plop.contexts.game.config.board.persistence.repository.BoardConfigRepository;
import fr.plop.contexts.game.config.board.persistence.repository.BoardRectRepository;
import fr.plop.contexts.game.config.board.persistence.repository.BoardSpaceRepository;
import org.springframework.stereotype.Component;

@Component
public class TemplateInitDataBoardAdapter {

    private final BoardConfigRepository boardRepository;
    private final BoardSpaceRepository boardSpaceRepository;
    private final BoardRectRepository boardRectRepository;

    public TemplateInitDataBoardAdapter(BoardConfigRepository boardRepository, BoardSpaceRepository boardSpaceRepository,
                                        BoardRectRepository boardRectRepository) {
        this.boardRepository = boardRepository;
        this.boardSpaceRepository = boardSpaceRepository;
        this.boardRectRepository = boardRectRepository;
    }


    public void deleteAll() {
        boardRectRepository.deleteAll();
        boardSpaceRepository.deleteAll();
        boardRepository.deleteAll();
    }

    public BoardConfigEntity createBoard(BoardConfig board) {
        BoardConfigEntity boardEntity = new BoardConfigEntity();
        boardEntity.setId(board.id().value());
        boardRepository.save(boardEntity);
        board.spaces().forEach(space -> {
            BoardSpaceEntity spaceEntity = new BoardSpaceEntity();
            spaceEntity.setId(space.id().value());
            spaceEntity.setBoard(boardEntity);
            spaceEntity.setLabel(space.label());
            spaceEntity.setPriority(space.priority());
            boardSpaceRepository.save(spaceEntity);
            space.rects().forEach(rect -> boardRectRepository.save(BoardRectEntity.fromModel(space.id(), rect)));
        });
        return boardEntity;
    }



}
