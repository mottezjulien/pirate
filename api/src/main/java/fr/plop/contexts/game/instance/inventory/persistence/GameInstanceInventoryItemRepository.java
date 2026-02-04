package fr.plop.contexts.game.instance.inventory.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GameInstanceInventoryItemRepository extends JpaRepository<GameInstanceInventoryItemEntity, String> {

    String FROM = "FROM GameInstanceInventoryItemEntity session_inventory_item";
    String FETCH_CONFIG_ITEM = " LEFT JOIN FETCH session_inventory_item.config config_item";
    String FROM_FETCH_ITEM_CONFIG = FROM + FETCH_CONFIG_ITEM;

    String WHERE = " WHERE ";
    String WHERE_EQUALS_ID = WHERE + "session_inventory_item.id = :id";
    String WHERE_EQUALS_CONFIG_ID = WHERE + "session_inventory_item.config.id = :configId";

    @Query(FROM_FETCH_ITEM_CONFIG + WHERE_EQUALS_ID)
    Optional<GameInstanceInventoryItemEntity> fetchConfigById(@Param("id") String id);

    @Query(FROM_FETCH_ITEM_CONFIG + WHERE_EQUALS_CONFIG_ID)
    Optional<GameInstanceInventoryItemEntity> fetchConfigByConfigId(@Param("configId") String configId);

    boolean existsByInventory_Player_IdAndConfig_Id(String playerId, String configId);

    Optional<GameInstanceInventoryItemEntity> findByInventory_Player_IdAndConfig_Id(String playerId, String configId);

}
