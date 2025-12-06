package fr.plop.contexts.game.config.map.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MapConfigRepository extends JpaRepository<MapConfigEntity, String> {

    String FETCH_ALL = " LEFT JOIN FETCH map.items map_item"
            + " LEFT JOIN FETCH map_item.imageGeneric map_item_image"
            + " LEFT JOIN FETCH map_item_image.objects map_item_image_object"
            + " LEFT JOIN FETCH map_item.nullableCondition map_item_condition"
            + " LEFT JOIN FETCH map_item.positions map_item_positions";

}
