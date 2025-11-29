package fr.plop.contexts.game.config.map.persistence;


import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.config.condition.persistence.ConditionEntity;
import fr.plop.contexts.game.config.map.domain.MapItem;
import fr.plop.generic.ImagePoint;
import fr.plop.generic.enumerate.Priority;
import fr.plop.subs.image.Image;
import jakarta.persistence.*;
import java.util.Optional;

@Entity
@Table(name = "TEST2_MAP_ITEM_OBJECT")
public class MapItemObjectEntity {

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
    private Priority priority;

    @ManyToOne
    @JoinColumn(name = "condition_id")
    private ConditionEntity nullableCondition;

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

    public void setTop(double top) {
        this.top = top;
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

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public void setNullableCondition(ConditionEntity nullableCondition) {
        this.nullableCondition = nullableCondition;
    }

    public MapItem._Object toModel() {
        ImagePoint center = new ImagePoint(top, left);
        Optional<Condition> optCondition = Optional.ofNullable(nullableCondition).map(ConditionEntity::toModel);
        MapItem._Object.Atom atom = new MapItem._Object.Atom(new MapItem._Object.Id(id), label, center, priority, optCondition);
        return switch (type) {
            case POINT -> new MapItem._Object.Point(atom, pointColor);
            case IMAGE -> new MapItem._Object._Image(atom, new Image(imageType, imageValue));
        };
    }

}
