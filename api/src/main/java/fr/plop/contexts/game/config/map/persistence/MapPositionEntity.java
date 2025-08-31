package fr.plop.contexts.game.config.map.persistence;


import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.board.persistence.entity.BoardSpaceEntity;
import fr.plop.contexts.game.config.map.domain.MapItem;
import jakarta.persistence.Column;
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
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "TEST2_MAP_POSITION")
public class MapPositionEntity {

    public enum Type {
        ZONE, POINT
    }


    @Id
    private String id;

    private String label;

    @ManyToOne
    @JoinColumn(name = "map_id")
    private MapItemEntity map;

    private Type type;

    private double top;


    @Column(name = "_left")
    private double left;
    private double bottom;
    @Column(name = "_right")
    private double right;

    private double x;

    private double y;

    @Enumerated(EnumType.STRING)
    private MapItem.Priority priority;

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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public MapItemEntity getMap() {
        return map;
    }

    public void setMap(MapItemEntity map) {
        this.map = map;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public double getTop() {
        return top;
    }

    public void setTop(double top) {
        this.top = top;
    }

    public double getLeft() {
        return left;
    }

    public void setLeft(double left) {
        this.left = left;
    }

    public double getBottom() {
        return bottom;
    }

    public void setBottom(double bottom) {
        this.bottom = bottom;
    }

    public double getRight() {
        return right;
    }

    public void setRight(double right) {
        this.right = right;
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

    public MapItem.Priority getPriority() {
        return priority;
    }

    public void setPriority(MapItem.Priority priority) {
        this.priority = priority;
    }

    public Set<BoardSpaceEntity> getSpaces() {
        return spaces;
    }

    public void setSpaces(Set<BoardSpaceEntity> spaces) {
        this.spaces = spaces;
    }

    public MapItem.Position toModel() {
        List<BoardSpace.Id> spaceIds = spaces.stream()
                .map(entity -> new BoardSpace.Id(entity.getId()))
                .toList();
        MapItem.Position.Atom atom = new MapItem.Position.Atom(new MapItem.Position.Id(id), label, priority, spaceIds);
        return switch (type) {
            case ZONE -> new MapItem.Position.Zone(atom, top, left, bottom, right);
            case POINT -> new MapItem.Position.Point(atom, x, y);
        };
    }
}
