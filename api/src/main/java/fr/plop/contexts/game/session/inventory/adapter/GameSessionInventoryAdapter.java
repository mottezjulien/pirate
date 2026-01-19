package fr.plop.contexts.game.session.inventory.adapter;

import fr.plop.contexts.game.config.inventory.domain.model.GameConfigInventoryItem;
import fr.plop.contexts.game.config.inventory.persistence.GameConfigInventoryItemEntity;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.session.inventory.domain.GameSessionInventoryItem;
import fr.plop.contexts.game.session.inventory.domain.GameSessionInventoryUseCase;
import fr.plop.contexts.game.session.inventory.persistence.GameSessionInventoryEntity;
import fr.plop.contexts.game.session.inventory.persistence.GameSessionInventoryItemEntity;
import fr.plop.contexts.game.session.inventory.persistence.GameSessionInventoryItemRepository;
import fr.plop.contexts.game.session.inventory.persistence.GameSessionInventoryRepository;
import fr.plop.generic.tools.StringTools;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class GameSessionInventoryAdapter implements GameSessionInventoryUseCase.Port {

    private final GameSessionInventoryRepository sessionRepository;
    private final GameSessionInventoryItemRepository sessionItemRepository;

    public GameSessionInventoryAdapter(GameSessionInventoryRepository sessionRepository, GameSessionInventoryItemRepository sessionItemRepository) {
        this.sessionRepository = sessionRepository;
        this.sessionItemRepository = sessionItemRepository;
    }

    @Override
    public Stream<GameSessionInventoryUseCase.SessionItemRaw> list(GamePlayer.Id playerId) {
        createIfNeeded(playerId);
        List<GameSessionInventoryItemEntity> entities = sessionItemRepository.fetchConfigItemByPlayerId(playerId.value());
        return entities.stream()
                .map(GameSessionInventoryItemEntity::toRawModel);
    }

    private void createIfNeeded(GamePlayer.Id playerId) {
        sessionRepository.findByPlayerId(playerId.value())
                .orElseGet(() -> {
                    var entity = new GameSessionInventoryEntity();
                    entity.setId(StringTools.generate());
                    entity.setPlayer(GamePlayerEntity.fromModelId(playerId));
                    return sessionRepository.save(entity);
                });
    }

    @Override
    public Optional<GameSessionInventoryUseCase.SessionItemRaw> findById(GameSessionInventoryItem.Id id) {
        return sessionItemRepository.fetchConfigItemById(id.value())
                .map(GameSessionInventoryItemEntity::toRawModel);
    }

    @Override
    public Optional<GameSessionInventoryUseCase.SessionItemRaw> findByConfigId(GamePlayer.Id playerId, GameConfigInventoryItem.Id configId) {
        return sessionItemRepository.findByPlayerIdAndConfigId(playerId.value(), configId.value())
                .map(GameSessionInventoryItemEntity::toRawModel);
    }

    @Override
    public GameSessionInventoryItem.Id add(GamePlayer.Id playerId, GameConfigInventoryItem.Id configId, int count) {
        GameSessionInventoryEntity inventory = getOrCreateInventory(playerId);

        GameSessionInventoryItemEntity itemEntity = new GameSessionInventoryItemEntity();
        itemEntity.setId(StringTools.generate());
        itemEntity.setInventory(inventory);

        GameConfigInventoryItemEntity configEntity = new GameConfigInventoryItemEntity();
        configEntity.setId(configId.value());
        itemEntity.setConfig(configEntity);

        itemEntity.setCollectionCount(count);
        itemEntity.setAvailability(GameSessionInventoryItem.Availability.FREE);

        sessionItemRepository.save(itemEntity);
        return new GameSessionInventoryItem.Id(itemEntity.getId());
    }

    private GameSessionInventoryEntity getOrCreateInventory(GamePlayer.Id playerId) {
        return sessionRepository.findByPlayerId(playerId.value())
                .orElseGet(() -> {
                    var entity = new GameSessionInventoryEntity();
                    entity.setId(StringTools.generate());
                    entity.setPlayer(GamePlayerEntity.fromModelId(playerId));
                    return sessionRepository.save(entity);
                });
    }

    @Override
    public void updateCount(GameSessionInventoryItem.Id id, int count) {
        sessionItemRepository.findById(id.value())
                .ifPresent(entity -> {
                    entity.setCollectionCount(count);
                    sessionItemRepository.save(entity);
                });
    }

    @Override
    public void delete(GameSessionInventoryItem.Id id) {
        sessionItemRepository.deleteById(id.value());
    }

    @Override
    public Stream<GameSessionInventoryUseCase.SessionItemRaw> findEquipped(GamePlayer.Id playerId) {
        return sessionItemRepository.fetchConfigItemByEquippedAndPlayerId(playerId.value())
                .stream()
                .map(GameSessionInventoryItemEntity::toRawModel);
    }

    @Override
    public void equip(GameSessionInventoryItem.Id id) {
        sessionItemRepository.findById(id.value())
                .ifPresent(entity -> {
                    entity.setAvailability(GameSessionInventoryItem.Availability.EQUIP);
                    sessionItemRepository.save(entity);
                });
    }

    @Override
    public void unequip(GameSessionInventoryItem.Id id) {
        sessionItemRepository.findById(id.value())
                .ifPresent(entity -> {
                    entity.setAvailability(GameSessionInventoryItem.Availability.FREE);
                    sessionItemRepository.save(entity);
                });
    }

}
