package fr.plop.contexts.game.config.map.persistence;

import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.config.condition.persistence.ConditionEntity;
import fr.plop.contexts.game.config.map.domain.MapItem;
import fr.plop.contexts.game.config.map.domain.MapObject;
import fr.plop.generic.enumerate.Priority;
import fr.plop.generic.position.Point;
import fr.plop.generic.position.Rectangle;
import fr.plop.subs.image.Image;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Entity
@Table(name = "LO_MAP_ITEM")
public class MapItemEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "config_id")
    private MapConfigEntity config;

    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type")
    private Image.Type imageType;

    @Column(name = "image_value")
    private String imageValue;

    @Column(name = "bounds_bottom_left_lat", precision = 18, scale = 12)
    private BigDecimal boundsBottomLeftLat;

    @Column(name = "bounds_bottom_left_lng", precision = 18, scale = 12)
    private BigDecimal boundsBottomLeftLng;

    @Column(name = "bounds_top_right_lat", precision = 18, scale = 12)
    private BigDecimal boundsTopRightLat;

    @Column(name = "bounds_top_right_lng", precision = 18, scale = 12)
    private BigDecimal boundsTopRightLng;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_pointer_type")
    private Image.Type nullableImagePointerType;

    @Column(name = "image_pointer_value")
    private String nullableImagePointerValue;

    @ManyToOne
    @JoinColumn(name = "condition_id")
    private ConditionEntity nullableCondition;

    @OneToMany(mappedBy = "item")
    private final Set<MapObjectEntity> objects = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MapConfigEntity getConfig() {
        return config;
    }

    public void setConfig(MapConfigEntity config) {
        this.config = config;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setImageType(Image.Type imageType) {
        this.imageType = imageType;
    }

    public void setImageValue(String imageValue) {
        this.imageValue = imageValue;
    }

    public void setBoundsBottomLeftLat(BigDecimal boundsBottomLeftLat) {
        this.boundsBottomLeftLat = boundsBottomLeftLat;
    }

    public void setBoundsBottomLeftLng(BigDecimal boundsBottomLeftLng) {
        this.boundsBottomLeftLng = boundsBottomLeftLng;
    }

    public void setBoundsTopRightLat(BigDecimal boundsTopRightLat) {
        this.boundsTopRightLat = boundsTopRightLat;
    }

    public void setBoundsTopRightLng(BigDecimal boundsTopRightLng) {
        this.boundsTopRightLng = boundsTopRightLng;
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

    public void setNullableImagePointerType(Image.Type nullableImagePointerType) {
        this.nullableImagePointerType = nullableImagePointerType;
    }

    public void setNullableImagePointerValue(String nullableImagePointerValue) {
        this.nullableImagePointerValue = nullableImagePointerValue;
    }

    public MapItem toModel() {
        Optional<Condition> optCondition = Optional.ofNullable(nullableCondition).map(ConditionEntity::toModel);
        Optional<Image> optPointer = Optional.ofNullable(nullableImagePointerType)
                .map(type -> new Image(type, nullableImagePointerValue));
        Rectangle bounds = Rectangle.ofPoints(
                Point.from(boundsBottomLeftLat, boundsBottomLeftLng),
                Point.from(boundsTopRightLat, boundsTopRightLng));
        List<MapObject> mapObjects = objects.stream().map(MapObjectEntity::toModel).toList();
        return new MapItem(new MapItem.Id(id), label, new Image(imageType, imageValue), bounds,
                priority, optCondition, optPointer, mapObjects);
    }

}
