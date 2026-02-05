package fr.plop.contexts.game.config.map.persistence;

import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.config.condition.persistence.ConditionEntity;
import fr.plop.contexts.game.config.map.domain.MapObject;
import fr.plop.generic.position.Point;
import fr.plop.subs.image.Image;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Optional;

@Entity
@Table(name = "LO_MAP_OBJECT")
public class MapObjectEntity {

    public enum Type {
        POINT, IMAGE
    }

    @Id
    private String id;

    private String label;

    @ManyToOne
    @JoinColumn(name = "map_item_id")
    private MapItemEntity item;

    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(precision = 18, scale = 12)
    private BigDecimal latitude;

    @Column(precision = 18, scale = 12)
    private BigDecimal longitude;

    @Column(name = "point_color")
    private String pointColor;

    @Column(name = "image_type")
    @Enumerated(EnumType.STRING)
    private Image.Type imageType;

    @Column(name = "image_value")
    private String imageValue;

    @ManyToOne
    @JoinColumn(name = "condition_id")
    private ConditionEntity nullableCondition;

    public void setId(String id) {
        this.id = id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setItem(MapItemEntity item) {
        this.item = item;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
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

    public void setNullableCondition(ConditionEntity nullableCondition) {
        this.nullableCondition = nullableCondition;
    }

    public MapObject toModel() {
        Point position = fr.plop.generic.position.Point.from(latitude, longitude);
        Optional<Condition> optCondition = Optional.ofNullable(nullableCondition).map(ConditionEntity::toModel);
        MapObject.Atom atom = new MapObject.Atom(new MapObject.Id(id), label, position, optCondition);
        return switch (type) {
            case POINT -> new MapObject.Point(atom, pointColor);
            case IMAGE -> new MapObject._Image(atom, new Image(imageType, imageValue));
        };
    }

}
