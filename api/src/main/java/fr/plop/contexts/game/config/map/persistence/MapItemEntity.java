package fr.plop.contexts.game.config.map.persistence;

import fr.plop.contexts.game.config.map.domain.MapItem;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.i18n.persistence.I18nEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.List;
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
    @JoinColumn(name = "label_i18n_id")
    private I18nEntity label;

    @Column(name = "image_type")
    @Enumerated(EnumType.STRING)
    private MapItem.Image.Type imageType;

    @Column(name = "image_value")
    private String imageValue;

    @Column(name = "image_size_width")
    private int imageSizeWidth;

    @Column(name = "image_size_height")
    private int imageSizeHeight;

    @Enumerated(EnumType.STRING)
    private MapItem.Priority priority;

    @OneToMany(mappedBy = "map")
    private Set<MapPositionEntity> positions = new HashSet<>();

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


    public I18nEntity getLabel() {
        return label;
    }

    public void setLabel(I18nEntity label) {
        this.label = label;
    }

    public MapItem.Image.Type getImageType() {
        return imageType;
    }

    public void setImageType(MapItem.Image.Type imageType) {
        this.imageType = imageType;
    }

    public String getImageValue() {
        return imageValue;
    }

    public void setImageValue(String imageValue) {
        this.imageValue = imageValue;
    }

    public int getImageSizeWidth() {
        return imageSizeWidth;
    }

    public void setImageSizeWidth(int imageSizeWidth) {
        this.imageSizeWidth = imageSizeWidth;
    }

    public int getImageSizeHeight() {
        return imageSizeHeight;
    }

    public void setImageSizeHeight(int imageSizeHeight) {
        this.imageSizeHeight = imageSizeHeight;
    }

    public MapItem.Priority getPriority() {
        return priority;
    }

    public void setPriority(MapItem.Priority priority) {
        this.priority = priority;
    }

    public Set<MapPositionEntity> getPositions() {
        return positions;
    }

    public void setPositions(Set<MapPositionEntity> positions) {
        this.positions = positions;
    }

    public MapItem toModel() {
        MapItem.Image.Size size = new MapItem.Image.Size(imageSizeWidth, imageSizeHeight);
        MapItem.Image image = new MapItem.Image(imageType, imageValue, size);
        List<MapItem.Position> positions = this.positions.stream().map(MapPositionEntity::toModel).toList();
        List<ScenarioConfig.Step.Id> stepIds = List.of(); //TODO
        return new MapItem(new MapItem.Id(id), label.toModel(), image, priority, positions, stepIds);
    }
}
