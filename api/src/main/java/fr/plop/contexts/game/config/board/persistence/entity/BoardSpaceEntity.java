package fr.plop.contexts.game.config.board.persistence.entity;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "TEST2_BOARD_SPACE")
public class BoardSpaceEntity {

    @Id
    private String id;

    private String label;

    private int priority;

    @ManyToOne
    @JoinColumn(name = "board_id")
    private BoardConfigEntity board;

    @OneToMany(mappedBy = "space")
    private Set<BoardRectEntity> rects = new HashSet<>();

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

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public BoardConfigEntity getBoard() {
        return board;
    }

    public void setBoard(BoardConfigEntity board) {
        this.board = board;
    }

    public Set<BoardRectEntity> getRects() {
        return rects;
    }

    public void setRects(Set<BoardRectEntity> rects) {
        this.rects = rects;
    }

    public BoardSpace toModel() {
        return new BoardSpace(new BoardSpace.Id(id), label, BoardSpace.Priority.values()[priority], rects.stream().map(BoardRectEntity::toModel).toList());
    }
}
