package fr.plop.contexts.game.config.inventory.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GameConfigInventoryRepository extends JpaRepository<GameConfigInventoryEntity, String> {
    String FETCH_ALL = " LEFT JOIN FETCH inventory.items inventory_item"
            + " LEFT JOIN FETCH inventory_item.label inventory_item_label"
            + " LEFT JOIN FETCH inventory_item.nullableDescription inventory_item_description";

}
