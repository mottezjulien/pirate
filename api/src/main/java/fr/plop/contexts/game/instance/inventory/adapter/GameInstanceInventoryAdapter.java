package fr.plop.contexts.game.instance.inventory.adapter;

import fr.plop.contexts.game.config.inventory.domain.model.GameConfigInventoryItem;
import fr.plop.contexts.game.config.inventory.persistence.GameConfigInventoryItemEntity;
import fr.plop.contexts.game.instance.core.domain.model.GamePlayer;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.instance.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.instance.inventory.domain.GameInstanceInventoryItem;
import fr.plop.contexts.game.instance.inventory.domain.GameInstanceInventoryUseCase;
import fr.plop.contexts.game.instance.inventory.persistence.GameInstanceInventoryEntity;
import fr.plop.contexts.game.instance.inventory.persistence.GameInstanceInventoryItemEntity;
import fr.plop.contexts.game.instance.inventory.persistence.GameInstanceInventoryItemRepository;
import fr.plop.contexts.game.instance.inventory.persistence.GameInstanceInventoryRepository;
import fr.plop.generic.tools.StringTools;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Stream;

@Component
public class GameInstanceInventoryAdapter implements GameInstanceInventoryUseCase.Port {

    private final GameInstanceInventoryRepository repository;
    private final GameInstanceInventoryItemRepository itemRepository;

    public GameInstanceInventoryAdapter(GameInstanceInventoryRepository repository, GameInstanceInventoryItemRepository itemRepository) {
        this.repository = repository;
        this.itemRepository = itemRepository;
    }

    @Override
    public Stream<GameInstanceInventoryUseCase.ItemRaw> inventory(GamePlayer.Id playerId) {
        return repository.fetchConfigItemByPlayerId(playerId.value())
                .stream()
                .flatMap(entity -> entity.getItems().stream().map(GameInstanceInventoryItemEntity::toRawModel));
    }

    @Override
    public Optional<GameInstanceInventoryUseCase.ItemRaw> findByInstanceId(GameInstanceInventoryItem.Id instanceId) {
        return itemRepository.fetchConfigById(instanceId.value())
                .map(GameInstanceInventoryItemEntity::toRawModel);
    }

    @Override
    public Optional<GameInstanceInventoryUseCase.ItemRaw> findByConfigId(GameInstanceContext context, GameConfigInventoryItem.Id configId) {
        return itemRepository.fetchConfigById(configId.value())
                .map(GameInstanceInventoryItemEntity::toRawModel);
    }

    @Override
    public GameInstanceInventoryItem.Id add(GamePlayer.Id playerId, GameConfigInventoryItem.Id configId) {
        GameInstanceInventoryEntity inventory = createIfNeeded(playerId);

        GameInstanceInventoryItemEntity itemEntity = new GameInstanceInventoryItemEntity();
        itemEntity.setId(StringTools.generate());
        itemEntity.setInventory(inventory);

        GameConfigInventoryItemEntity configEntity = new GameConfigInventoryItemEntity();
        configEntity.setId(configId.value());
        itemEntity.setConfig(configEntity);

        itemEntity.setCollectionCount(1);
        itemEntity.setAvailability(GameInstanceInventoryItem.Availability.FREE);

        itemRepository.save(itemEntity);
        return new GameInstanceInventoryItem.Id(itemEntity.getId());
    }

    private GameInstanceInventoryEntity createIfNeeded(GamePlayer.Id playerId) {
        return repository.findByPlayerId(playerId.value()).orElseGet(() -> {
                var entity = new GameInstanceInventoryEntity();
                entity.setId(StringTools.generate());
                entity.setPlayer(GamePlayerEntity.fromModelId(playerId));
                return repository.save(entity);
            });
    }

    @Override
    public void updateCount(GameInstanceInventoryItem.Id id, int count) {
        itemRepository.findById(id.value()).ifPresent(entity -> {
            entity.setCollectionCount(count);
            itemRepository.save(entity);
        });
    }

    @Override
    public void delete(GameInstanceInventoryItem.Id id) {
        itemRepository.deleteById(id.value());
    }

    @Override
    public Stream<GameInstanceInventoryUseCase.ItemRaw> findEquipped(GamePlayer.Id playerId) {
        return inventory(playerId) //TODO IN QUERY ?
                .filter(raw -> raw.availability() == GameInstanceInventoryItem.Availability.EQUIP);
    }

    @Override
    public void equip(GameInstanceInventoryItem.Id id) {
        itemRepository.findById(id.value())
            .ifPresent(entity -> {
                entity.setAvailability(GameInstanceInventoryItem.Availability.EQUIP);
                itemRepository.save(entity);
            });
    }

    @Override
    public void unequip(GameInstanceInventoryItem.Id id) {
        itemRepository.findById(id.value())
            .ifPresent(entity -> {
                entity.setAvailability(GameInstanceInventoryItem.Availability.FREE);
                itemRepository.save(entity);
            });
    }

    @Override
    public boolean exist(GamePlayer.Id playerId, GameConfigInventoryItem.Id configItemId) {
        return itemRepository.existsByInventory_Player_IdAndConfig_Id(playerId.value(), configItemId.value());
    }

    @Override
    public int count(GamePlayer.Id playerId, GameConfigInventoryItem.Id configItemId) {
        return itemRepository.findByInventory_Player_IdAndConfig_Id(playerId.value(), configItemId.value())
                .map(GameInstanceInventoryItemEntity::getCollectionCount)
                .orElse(0);
    }

    @Override
    public void count(GamePlayer.Id playerId, GameConfigInventoryItem.Id configItemId, int count) {
        itemRepository.findByInventory_Player_IdAndConfig_Id(playerId.value(), configItemId.value())
                .ifPresentOrElse(
                        entity -> {
                            entity.setCollectionCount(count);
                            itemRepository.save(entity);
                        },
                        () -> {
                            // L'item n'existe pas encore, on le crée
                            GameInstanceInventoryEntity inventory = createIfNeeded(playerId);
                            GameInstanceInventoryItemEntity itemEntity = new GameInstanceInventoryItemEntity();
                            itemEntity.setId(StringTools.generate());
                            itemEntity.setInventory(inventory);

                            GameConfigInventoryItemEntity configEntity = new GameConfigInventoryItemEntity();
                            configEntity.setId(configItemId.value());
                            itemEntity.setConfig(configEntity);

                            itemEntity.setCollectionCount(count);
                            itemEntity.setAvailability(GameInstanceInventoryItem.Availability.FREE);
                            itemRepository.save(itemEntity);
                        }
                );
    }

}
