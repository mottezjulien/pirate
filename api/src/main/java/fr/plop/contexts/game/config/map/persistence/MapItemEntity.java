package fr.plop.contexts.game.config.map.persistence;

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

    private String label;

    @Column(name = "image_type")
    @Enumerated(EnumType.STRING)
    private Image.Type imageType;

    @Column(name = "image_value")
    private String imageValue;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @OneToMany(mappedBy = "map")
    private final Set<MapItemObjectEntity> objects = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "condition_id")
    private ConditionEntity nullableCondition;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public MapConfigEntity getConfig() {
        return config;
    }

    public void setConfig(MapConfigEntity config) {
        this.config = config;
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

    public MapItem toModel() {
        Image image = new Image(imageType, imageValue);
        Optional<Condition> optCondition = Optional.ofNullable(nullableCondition).map(ConditionEntity::toModel);
        List<MapItem._Object> objects = this.objects.stream().map(MapItemObjectEntity::toModel).toList();
        return new MapItem(new MapItem.Id(id), label, image, priority, objects, optCondition);
    }

}
