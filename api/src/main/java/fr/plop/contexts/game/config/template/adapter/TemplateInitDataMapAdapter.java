package fr.plop.contexts.game.config.template.adapter;

import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.map.domain.MapObject;
import fr.plop.contexts.game.config.map.persistence.*;
import org.springframework.stereotype.Component;


@Component
public class TemplateInitDataMapAdapter {

    private final MapConfigRepository mapConfigRepository;
    private final MapItemRepository mapItemRepository;
    private final MapObjectRepository mapObjectRepository;
    private final TemplateInitDataConditionAdapter conditionAdapter;

    public TemplateInitDataMapAdapter(MapConfigRepository mapConfigRepository, MapItemRepository mapItemRepository, MapObjectRepository mapObjectRepository, TemplateInitDataConditionAdapter conditionAdapter) {
        this.mapConfigRepository = mapConfigRepository;
        this.mapItemRepository = mapItemRepository;
        this.mapObjectRepository = mapObjectRepository;
        this.conditionAdapter = conditionAdapter;
    }

    public void deleteAll() {
        mapObjectRepository.deleteAll();
        mapItemRepository.deleteAll();
        mapConfigRepository.deleteAll();
    }


    public MapConfigEntity create(MapConfig mapConfig) {
        MapConfigEntity configEntity = new MapConfigEntity();
        configEntity.setId(mapConfig.id().value());
        mapConfigRepository.save(configEntity);
        mapConfig.items().forEach(item -> {
            MapItemEntity itemEntity = new MapItemEntity();
            itemEntity.setId(item.id().value());
            itemEntity.setConfig(configEntity);
            itemEntity.setLabel(item.label());
            itemEntity.setImageType(item.image().type());
            itemEntity.setImageValue(item.image().value());
            itemEntity.setBoundsBottomLeftLat(item.bounds().bottomLeft().lat());
            itemEntity.setBoundsBottomLeftLng(item.bounds().bottomLeft().lng());
            itemEntity.setBoundsTopRightLat(item.bounds().topRight().lat());
            itemEntity.setBoundsTopRightLng(item.bounds().topRight().lng());
            itemEntity.setPriority(item.priority());
            item.optPointer().ifPresent(pointer -> {
                itemEntity.setNullableImagePointerType(pointer.type());
                itemEntity.setNullableImagePointerValue(pointer.value());
            });
            item.optCondition()
                    .ifPresent(condition -> itemEntity.setNullableCondition(conditionAdapter.create(condition)));
            mapItemRepository.save(itemEntity);

            item.objects().forEach(object -> mapObjectRepository.save(buildObjectEntity(object, itemEntity)));
        });
        return configEntity;
    }

    private MapObjectEntity buildObjectEntity(MapObject object, MapItemEntity itemEntity) {
        MapObjectEntity objectEntity = new MapObjectEntity();
        objectEntity.setId(object.id().value());
        objectEntity.setLabel(object.label());
        objectEntity.setItem(itemEntity);
        objectEntity.setLatitude(object.position().lat());
        objectEntity.setLongitude(object.position().lng());
        object.optCondition()
                .ifPresent(condition -> objectEntity.setNullableCondition(conditionAdapter.create(condition)));
        switch (object) {
            case MapObject.Point point -> {
                objectEntity.setType(MapObjectEntity.Type.POINT);
                objectEntity.setPointColor(point.color());
            }
            case MapObject._Image image -> {
                objectEntity.setType(MapObjectEntity.Type.IMAGE);
                objectEntity.setImageType(image.image().type());
                objectEntity.setImageValue(image.image().value());
            }
        }
        return objectEntity;
    }

}
