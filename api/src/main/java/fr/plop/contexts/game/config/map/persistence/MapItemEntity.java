package fr.plop.contexts.game.config.map.persistence;

import fr.plop.contexts.game.config.Image.persistence.ImageGenericEntity;
import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.config.condition.persistence.ConditionEntity;
import fr.plop.contexts.game.config.map.domain.MapItem;
import fr.plop.generic.enumerate.Priority;
import fr.plop.subs.image.Image;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Entity
@Table(name = "TEST2_MAP_ITEM")
public class MapItemEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "config_id")
    private MapConfigEntity config;

    @ManyToOne
    @JoinColumn(name = "image_generic_id")
    private ImageGenericEntity imageGeneric;

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
    private final Set<MapItemPositionEntity> positions = new HashSet<>();


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

    public void setImageGeneric(ImageGenericEntity imageGeneric) {
        this.imageGeneric = imageGeneric;
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
                .map(imageType -> new Image(imageType, nullableImagePointerValue));
        List<MapItem.Position> _positions = positions.stream().map(MapItemPositionEntity::toModel).toList();
        return new MapItem(new MapItem.Id(id), imageGeneric.toModel(),
                priority, optCondition, optPointer, _positions);
    }

}
