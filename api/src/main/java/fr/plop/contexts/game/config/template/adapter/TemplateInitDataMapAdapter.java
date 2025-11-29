package fr.plop.contexts.game.config.template.adapter;

import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.map.domain.MapItem;
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
            itemEntity.setPriority(item.priority());
            item.optCondition()
                    .ifPresent(condition -> itemEntity.setNullableCondition(conditionAdapter.create(condition)));
            mapItemRepository.save(itemEntity);
            item.objects().forEach(object -> mapObjectRepository.save(buildObjectEntity(object, itemEntity)));
        });
        return configEntity;
    }

    private MapItemObjectEntity buildObjectEntity(MapItem._Object object, MapItemEntity itemEntity) {
        MapItemObjectEntity objectEntity = new MapItemObjectEntity();
        objectEntity.setId(object.id().value());
        objectEntity.setLabel(object.label());
        objectEntity.setMap(itemEntity);
        object.atom().optCondition()
                .ifPresent(condition -> objectEntity.setNullableCondition(conditionAdapter.create(condition)));
        switch (object) {
            case MapItem._Object.Point point -> {
                objectEntity.setType(MapItemObjectEntity.Type.POINT);
                objectEntity.setTop(point.top());
                objectEntity.setTop(point.left());
                objectEntity.setPointColor(point.color());
            }
            case MapItem._Object._Image _image -> {
                objectEntity.setType(MapItemObjectEntity.Type.IMAGE);
                objectEntity.setTop(_image.top());
                objectEntity.setLeft(_image.left());
                objectEntity.setImageType(_image.value().type());
                objectEntity.setImageValue(_image.value().value());
            }
        }
        return objectEntity;
    }


}
