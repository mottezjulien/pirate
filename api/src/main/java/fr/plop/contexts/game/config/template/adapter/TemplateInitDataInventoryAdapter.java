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
        configRepository.save(configEntity);
        inventoryConfig.items().forEach(item -> {
            GameConfigInventoryItemEntity itemEntity = new GameConfigInventoryItemEntity();//item.imageGeneric());
            itemEntity.setId(item.id().value());
            itemEntity.setConfig(configEntity);
            itemEntity.setType(item.type());
            itemEntity.setLabel(createI18n(item.label()));
            item.optDescription().ifPresent(desc -> itemEntity.setNullableDescription(createI18n(desc)));
            itemEntity.setImageType(item.image().type());
            itemEntity.setImageValue(item.image().value());
            configItemRepository.save(itemEntity);
        });
        return configEntity;
    }

    private I18nEntity createI18n(I18n i18n) {
        return i18nRepository.save(I18nEntity.fromModel(i18n));
    }

}
