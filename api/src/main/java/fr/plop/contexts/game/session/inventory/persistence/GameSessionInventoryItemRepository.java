package fr.plop.contexts.game.session.inventory.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GameSessionInventoryItemRepository extends JpaRepository<GameSessionInventoryItemEntity, String> {

    String FROM = "FROM GameSessionInventoryItemEntity session_item";
    String FETCH_ITEM_CONFIG = " LEFT JOIN FETCH session_item.config config_item";
    String FROM_FETCH_ITEM_CONFIG = FROM + FETCH_ITEM_CONFIG;


    String WHERE = " WHERE ";
    String EQUALS_PLAYER_ID = "session_item.inventory.player.id = :playerId";
    String WHERE_EQUALS_ID = WHERE + "session_item.id = :id";

    @Query(FROM_FETCH_ITEM_CONFIG + WHERE + EQUALS_PLAYER_ID)
    List<GameSessionInventoryItemEntity> fetchConfigItemByPlayerId(@Param("playerId") String playerId);

    @Query(FROM_FETCH_ITEM_CONFIG + WHERE_EQUALS_ID)
    Optional<GameSessionInventoryItemEntity> fetchConfigItemById(@Param("id") String id);

    @Query(FROM_FETCH_ITEM_CONFIG + WHERE + EQUALS_PLAYER_ID + " AND session_item.availability = fr.plop.contexts.game.session.inventory.domain.GameSessionInventoryItem.Availability.EQUIP")
    List<GameSessionInventoryItemEntity> fetchConfigItemByEquippedAndPlayerId(@Param("playerId") String playerId);

    @Query(FROM_FETCH_ITEM_CONFIG + WHERE + EQUALS_PLAYER_ID + " AND session_item.config.id = :configId")
    Optional<GameSessionInventoryItemEntity> findByPlayerIdAndConfigId(@Param("playerId") String playerId, @Param("configId") String configId);

}
