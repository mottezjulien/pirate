package fr.plop.contexts.game.instance.inventory.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GameInstanceInventoryRepository extends JpaRepository<GameInstanceInventoryEntity, String> {
    String EQUALS_PLAYER_ID = "session_inventory.player.id = :playerId";

    Optional<GameInstanceInventoryEntity> findByPlayerId(String playerId);

    @Query("FROM GameInstanceInventoryEntity session_inventory LEFT JOIN FETCH session_inventory.items session_inventory_item " + GameInstanceInventoryItemRepository.FETCH_CONFIG_ITEM + " WHERE " + EQUALS_PLAYER_ID)
    Optional<GameInstanceInventoryEntity> fetchConfigItemByPlayerId(@Param("playerId") String playerId);

}
