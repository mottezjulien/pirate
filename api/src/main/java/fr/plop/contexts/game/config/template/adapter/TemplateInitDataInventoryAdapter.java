package fr.plop.contexts.game.config.template.adapter;

import fr.plop.contexts.game.config.inventory.domain.model.InventoryConfig;
import fr.plop.contexts.game.config.inventory.persistence.GameConfigInventoryEntity;
import fr.plop.contexts.game.config.inventory.persistence.GameConfigInventoryItemEntity;
import fr.plop.contexts.game.config.inventory.persistence.GameConfigInventoryItemRepository;
import fr.plop.contexts.game.config.inventory.persistence.GameConfigInventoryRepository;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.i18n.persistence.I18nEntity;
import fr.plop.subs.i18n.persistence.I18nRepository;
import org.springframework.stereotype.Component;

@Component
public class TemplateInitDataInventoryAdapter {

    private final I18nRepository i18nRepository;

    private final GameConfigInventoryRepository configRepository;
    private final GameConfigInventoryItemRepository configItemRepository;

    public TemplateInitDataInventoryAdapter(I18nRepository i18nRepository, GameConfigInventoryRepository configRepository, GameConfigInventoryItemRepository configItemRepository) {
        this.i18nRepository = i18nRepository;
        this.configRepository = configRepository;
        this.configItemRepository = configItemRepository;
    }

    public void deleteAll() {
        configItemRepository.deleteAll();
        configRepository.deleteAll();
    }

    public GameConfigInventoryEntity create(InventoryConfig inventoryConfig) {
        GameConfigInventoryEntity configEntity = new GameConfigInventoryEntity();
        configEntity.setId(inventoryConfig.id().value());
        configEntity.setMergeRules(inventoryConfig.mergedRules());
        configRepository.save(configEntity);
        inventoryConfig.items().forEach(item -> {
            GameConfigInventoryItemEntity itemEntity = new GameConfigInventoryItemEntity();
            itemEntity.setId(item.id().value());
            itemEntity.setConfig(configEntity);
            itemEntity.setType(item.type());
            itemEntity.setLabel(createI18n(item.label()));
            item.optDescription().ifPresent(desc -> itemEntity.setNullableDescription(createI18n(desc)));
            itemEntity.setImageType(item.image().type());
            itemEntity.setImageValue(item.image().value());
            itemEntity.setActionType(item.actionType());
            item.optTargetId().ifPresent(targetId -> itemEntity.setNullableScenarioTargetId(targetId.value()));
            configItemRepository.save(itemEntity);
        });
        return configEntity;
    }

    /*private GameConfigInventoryItemActionRuleEntity createActionRuleEntity(GameConfigInventoryItemActionRule actionRule) {
        GameConfigInventoryItemActionRuleEntity entity = new GameConfigInventoryItemActionRuleEntity();
        entity.setId(StringTools.generate());
        entity.setType(actionRule.status());

        // Extract consequences from the sealed interface
        List<Consequence> consequences = switch (actionRule.consequence()) {
            case GameConfigInventoryItemActionRule.Consequence.Direct direct -> direct.consequences();
            case GameConfigInventoryItemActionRule.Consequence.Event event -> List.of(); // Events not persisted as consequences
        };
        entity.setConsequences(consequences);

        return entity;
    }*/

    private I18nEntity createI18n(I18n i18n) {
        return i18nRepository.save(I18nEntity.fromModel(i18n));
    }

}
