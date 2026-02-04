package fr.plop.contexts.game.instance.board.persistence;

import fr.plop.contexts.game.config.board.persistence.entity.BoardSpaceEntity;
import fr.plop.contexts.game.instance.core.persistence.GamePlayerEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "LO_BOARD_POSITION")
public class BoardPositionEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private GamePlayerEntity player;

    @Column(name = "date_time")
    private Instant dateTime;

    @ManyToMany
    @JoinTable(name = "LO_BOARD_POSITION_SPACE",
            joinColumns = @JoinColumn(name = "position_id"),
            inverseJoinColumns = @JoinColumn(name = "space_id"))
    private Set<BoardSpaceEntity> spaces = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GamePlayerEntity getPlayer() {
        return player;
    }

    public void setPlayer(GamePlayerEntity player) {
        this.player = player;
    }

    public Instant getDateTime() {
        return dateTime;
    }

    public void setDateTime(Instant dateTime) {
        this.dateTime = dateTime;
    }

    public Set<BoardSpaceEntity> getSpaces() {
        return spaces;
    }

    public void setSpaces(Set<BoardSpaceEntity> spaces) {
        this.spaces = spaces;
    }
}
