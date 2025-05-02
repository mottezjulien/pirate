package fr.plop.contexts.game.config.board.persistence.entity;

import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "TEST2_BOARD_CONFIG")
public class BoardConfigEntity {

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

    public BoardConfig toModel() {
        return new BoardConfig(new BoardConfig.Id(id), spaces.stream().map(BoardSpaceEntity::toModel).toList());
    }

}
