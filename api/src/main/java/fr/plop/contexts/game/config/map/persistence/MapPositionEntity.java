package fr.plop.contexts.game.config.map.persistence;


import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.board.persistence.entity.BoardSpaceEntity;
import fr.plop.contexts.game.config.map.domain.MapItem;
import fr.plop.subs.image.Image;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "TEST2_MAP_POSITION")
public class MapPositionEntity {

    public enum Type {
        POINT, IMAGE
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

    @Column(name = "point_color")
    private String pointColor;

    @Column(name = "image_type")
    @Enumerated(EnumType.STRING)
    private Image.Type imageType;

    @Column(name = "image_value")
    private String imageValue;

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

    public void setPointColor(String pointColor) {
        this.pointColor = pointColor;
    }

    public void setImageType(Image.Type imageType) {
        this.imageType = imageType;
    }

    public void setImageValue(String imageValue) {
        this.imageValue = imageValue;
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
            case POINT -> new MapItem.Position.Point(atom, top, left, pointColor);
            case IMAGE -> new MapItem.Position._Image(atom, top, left, new Image(imageType, imageValue));
        };
    }
}
