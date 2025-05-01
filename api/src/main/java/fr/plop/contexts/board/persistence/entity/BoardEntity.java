package fr.plop.contexts.board.persistence.entity;

import fr.plop.contexts.board.domain.model.Board;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "TEST2_BOARD")
public class BoardEntity {

    @Id
    private String id;

    @OneToMany(mappedBy = "board")
    private Set<BoardSpaceEntity> spaces = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<BoardSpaceEntity> getSpaces() {
        return spaces;
    }

    public void setSpaces(Set<BoardSpaceEntity> spaces) {
        this.spaces = spaces;
    }

    public Board toModel() {
        return new Board(new Board.Id(id), spaces.stream().map(BoardSpaceEntity::toModel).toList());
    }

}
