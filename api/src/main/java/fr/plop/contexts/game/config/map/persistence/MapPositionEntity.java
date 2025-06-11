package fr.plop.contexts.game.config.map.persistence;


import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.board.persistence.entity.BoardSpaceEntity;
import fr.plop.contexts.game.config.map.domain.Map;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "TEST2_MAP_POSITION")
public class MapPositionEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "map_id")
    private MapEntity map;

    private double x;

    private double y;

    @Enumerated(EnumType.STRING)
    private Map.Priority priority;

    @ManyToMany
    @JoinTable(name = "TEST2_MAP_POSITION_SPACE",
            joinColumns = @JoinColumn(name = "position_id"),
            inverseJoinColumns = @JoinColumn(name = "space_id"))
    private Set<BoardSpaceEntity> spaces = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MapEntity getMap() {
        return map;
    }

    public void setMap(MapEntity map) {
        this.map = map;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public Map.Priority getPriority() {
        return priority;
    }

    public void setPriority(Map.Priority priority) {
        this.priority = priority;
    }

    public Set<BoardSpaceEntity> getSpaces() {
        return spaces;
    }

    public void setSpaces(Set<BoardSpaceEntity> spaces) {
        this.spaces = spaces;
    }

    public Map.Position toModel() {
        return new Map.Position(new Map.Position.Point(x, y), priority, spaces.stream()
                .map(entity -> new BoardSpace.Id(entity.getId()))
                .toList());
    }
}
