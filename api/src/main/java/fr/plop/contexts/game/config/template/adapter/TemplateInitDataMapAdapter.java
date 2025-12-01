package fr.plop.contexts.game.config.template.adapter;

import fr.plop.contexts.game.config.Image.domain.ImageObject;
import fr.plop.contexts.game.config.Image.persistence.*;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.map.persistence.*;
import org.springframework.stereotype.Component;


@Component
public class TemplateInitDataMapAdapter {

    private final MapConfigRepository mapConfigRepository;
    private final MapItemRepository mapItemRepository;
    private final ImageObjectRepository imageObjectRepository;
    private final ImageGenericRepository imageGenericRepository;
    private final TemplateInitDataConditionAdapter conditionAdapter;

    public TemplateInitDataMapAdapter(MapConfigRepository mapConfigRepository, MapItemRepository mapItemRepository, ImageObjectRepository imageObjectRepository, ImageGenericRepository imageGenericRepository, TemplateInitDataConditionAdapter conditionAdapter) {
        this.mapConfigRepository = mapConfigRepository;
        this.mapItemRepository = mapItemRepository;
        this.imageObjectRepository = imageObjectRepository;
        this.imageGenericRepository = imageGenericRepository;
        this.conditionAdapter = conditionAdapter;
    }

    public void deleteAll() {
        //imageObjectRepository.deleteAll();
        mapItemRepository.deleteAll();
        mapConfigRepository.deleteAll();
    }


    public MapConfigEntity create(MapConfig mapConfig) {
        MapConfigEntity configEntity = new MapConfigEntity();
        configEntity.setId(mapConfig.id().value());
        mapConfigRepository.save(configEntity);
        mapConfig.items().forEach(item -> {
            ImageGenericEntity imageGenericEntity = new ImageGenericEntity();//item.imageGeneric());
            imageGenericEntity.setId(item.imageGeneric().id().value());
            imageGenericEntity.setLabel(item.imageGeneric().label());
            imageGenericEntity.setType(item.imageType());
            imageGenericEntity.setValue(item.imageValue());
            imageGenericRepository.save(imageGenericEntity);
            item.imageObjects().forEach(object -> imageObjectRepository.save(buildObjectEntity(object, imageGenericEntity)));

            MapItemEntity itemEntity = new MapItemEntity();
            itemEntity.setId(item.id().value());
            itemEntity.setConfig(configEntity);
            itemEntity.setImageGeneric(imageGenericEntity);
            itemEntity.setPriority(item.priority());
            item.optCondition()
                    .ifPresent(condition -> itemEntity.setNullableCondition(conditionAdapter.create(condition)));
            mapItemRepository.save(itemEntity);

        });
        return configEntity;
    }

    private ImageObjectEntity buildObjectEntity(ImageObject object, ImageGenericEntity imageEntity) {
        ImageObjectEntity objectEntity = new ImageObjectEntity();
        objectEntity.setId(object.id().value());
        objectEntity.setLabel(object.label());
        objectEntity.setImage(imageEntity);
        object.atom().optCondition()
                .ifPresent(condition -> objectEntity.setNullableCondition(conditionAdapter.create(condition)));
        switch (object) {
            case ImageObject.Point point -> {
                objectEntity.setType(ImageObjectEntity.Type.POINT);
                objectEntity.setTop(point.top());
                objectEntity.setTop(point.left());
                objectEntity.setPointColor(point.color());
            }
            case ImageObject._Image _image -> {
                objectEntity.setType(ImageObjectEntity.Type.IMAGE);
                objectEntity.setTop(_image.top());
                objectEntity.setLeft(_image.left());
                objectEntity.setImageType(_image.value().type());
                objectEntity.setImageValue(_image.value().value());
            }
        }
        return objectEntity;
    }


}
