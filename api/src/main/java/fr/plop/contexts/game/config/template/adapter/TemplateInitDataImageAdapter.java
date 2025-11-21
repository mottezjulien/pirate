package fr.plop.contexts.game.config.template.adapter;

import fr.plop.contexts.game.config.Image.domain.ImageConfig;
import fr.plop.contexts.game.config.Image.domain.ImageItem;
import fr.plop.contexts.game.config.Image.persistence.ImageConfigEntity;
import fr.plop.contexts.game.config.Image.persistence.ImageConfigRepository;
import fr.plop.contexts.game.config.Image.persistence.ImageItemEntity;
import fr.plop.contexts.game.config.Image.persistence.ImageItemRepository;
import org.springframework.stereotype.Component;

@Component
public class TemplateInitDataImageAdapter {

    private final ImageConfigRepository configRepository;
    private final ImageItemRepository itemRepository;

    public TemplateInitDataImageAdapter(ImageConfigRepository configRepository, ImageItemRepository itemRepository) {
        this.configRepository = configRepository;
        this.itemRepository = itemRepository;
    }

    public void deleteAll() {
        itemRepository.deleteAll();
        configRepository.deleteAll();
    }

    public ImageConfigEntity createImage(ImageConfig model) {
        ImageConfigEntity entity = new ImageConfigEntity();
        entity.setId(model.id().value());
        configRepository.save(entity);
        model.items().forEach(item -> createItem(model.id(), item));
        return entity;
    }

    private void createItem(ImageConfig.Id configId, ImageItem model) {
        itemRepository.save(ImageItemEntity.fromModel(configId, model));
    }

}
