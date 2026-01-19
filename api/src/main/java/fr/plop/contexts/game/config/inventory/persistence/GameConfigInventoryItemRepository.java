package fr.plop.contexts.game.config.inventory.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GameConfigInventoryItemRepository extends JpaRepository<GameConfigInventoryItemEntity, String> {


}
