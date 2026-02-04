package fr.plop.contexts.game.config.board.persistence.entity;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.generic.enumerate.Priority;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "LO_BOARD_SPACE")
public class BoardSpaceEntity {

    @Id
    private String id;

    private String label;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @ManyToOne
    @JoinColumn(name = "board_id")
    private BoardConfigEntity board;

    @OneToMany(mappedBy = "space")
    private final Set<BoardRectEntity> rects = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public BoardConfigEntity getBoard() {
        return board;
    }

    public void setBoard(BoardConfigEntity board) {
        this.board = board;
    }

    public BoardSpace toModel() {
        return new BoardSpace(toModelId(), label, priority, rects.stream().map(BoardRectEntity::toModel).toList());
    }
    public BoardSpace.Id toModelId() {
        return new BoardSpace.Id(id);
    }
}
