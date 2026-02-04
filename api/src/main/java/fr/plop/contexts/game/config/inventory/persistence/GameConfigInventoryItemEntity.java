package fr.plop.contexts.game.config.inventory.persistence;


import fr.plop.contexts.game.config.inventory.domain.model.GameConfigInventoryItem;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.subs.i18n.persistence.I18nEntity;
import fr.plop.subs.image.Image;
import jakarta.persistence.*;

import java.util.Optional;

@Entity
@Table(name = "LO_CONFIG_INVENTORY_ITEM")
public class GameConfigInventoryItemEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "label_i18n_id")
    private I18nEntity label;

    @ManyToOne
    @JoinColumn(name = "nullable_description_i18n_id")
    private I18nEntity nullableDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type")
    private Image.Type imageType;

    @Column(name = "image_value")
    private String imageValue;

    @ManyToOne
    @JoinColumn(name = "config_id")
    private GameConfigInventoryEntity config;

    @Enumerated(EnumType.STRING)
    GameConfigInventoryItem.Type type;

    @Column(name = "nullable_scenario_target_id")
    private String nullableScenarioTargetId;

    @Column(name = "init_value")
    private int initValue = 0;

    @Enumerated(EnumType.STRING)
    private GameConfigInventoryItem.ActionType actionType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNullableDescription(I18nEntity nullableDescription) {
        this.nullableDescription = nullableDescription;
    }

    public Image.Type getImageType() {
        return imageType;
    }

    public void setImageType(Image.Type imageType) {
        this.imageType = imageType;
    }

    public String getImageValue() {
        return imageValue;
    }

    public void setImageValue(String imageValue) {
        this.imageValue = imageValue;
    }

    public GameConfigInventoryItem.Type getType() {
        return type;
    }

    public void setType(GameConfigInventoryItem.Type type) {
        this.type = type;
    }

    public I18nEntity getLabel() {
        return label;
    }

    public void setLabel(I18nEntity label) {
        this.label = label;
    }

    public GameConfigInventoryEntity getConfig() {
        return config;
    }

    public void setConfig(GameConfigInventoryEntity config) {
        this.config = config;
    }

    public String getNullableScenarioTargetId() {
        return nullableScenarioTargetId;
    }

    public void setNullableScenarioTargetId(String nullableScenarioTargetId) {
        this.nullableScenarioTargetId = nullableScenarioTargetId;
    }

    public void setActionType(GameConfigInventoryItem.ActionType actionType) {
        this.actionType = actionType;
    }

    public GameConfigInventoryItem toModel() {
        Optional<ScenarioConfig.Target.Id> optTargetId = Optional.ofNullable(nullableScenarioTargetId)
                .map(ScenarioConfig.Target.Id::new);
        return new GameConfigInventoryItem(new GameConfigInventoryItem.Id(id), label.toModel(),
                new Image(imageType, imageValue),
                Optional.ofNullable(nullableDescription).map(I18nEntity::toModel), type,
                initValue, optTargetId, actionType);
    }

}
