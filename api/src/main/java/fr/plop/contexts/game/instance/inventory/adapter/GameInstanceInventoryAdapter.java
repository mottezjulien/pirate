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

    private final GameInstanceInventoryRepository sessionRepository;
    private final GameInstanceInventoryItemRepository sessionItemRepository;

    public GameInstanceInventoryAdapter(GameInstanceInventoryRepository sessionRepository, GameInstanceInventoryItemRepository sessionItemRepository) {
        this.sessionRepository = sessionRepository;
        this.sessionItemRepository = sessionItemRepository;
    }

    @Override
    public Stream<GameInstanceInventoryUseCase.SessionItemRaw> inventory(GamePlayer.Id playerId) {
        return sessionRepository.fetchConfigItemByPlayerId(playerId.value())
                .stream()
                .flatMap(session -> session.getItems().stream().map(GameInstanceInventoryItemEntity::toRawModel));
    }

    @Override
    public Optional<GameInstanceInventoryUseCase.SessionItemRaw> findBySessionId(GameInstanceInventoryItem.Id sessionId) {
        return sessionItemRepository.fetchConfigById(sessionId.value())
                .map(GameInstanceInventoryItemEntity::toRawModel);
    }

    @Override
    public Optional<GameInstanceInventoryUseCase.SessionItemRaw> findByConfigId(GameInstanceContext context, GameConfigInventoryItem.Id configId) {
        return sessionItemRepository.fetchConfigById(configId.value())
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

        sessionItemRepository.save(itemEntity);
        return new GameInstanceInventoryItem.Id(itemEntity.getId());
    }

    private GameInstanceInventoryEntity createIfNeeded(GamePlayer.Id playerId) {
        return sessionRepository.findByPlayerId(playerId.value()).orElseGet(() -> {
                var entity = new GameInstanceInventoryEntity();
                entity.setId(StringTools.generate());
                entity.setPlayer(GamePlayerEntity.fromModelId(playerId));
                return sessionRepository.save(entity);
            });
    }

    @Override
    public void updateCount(GameInstanceInventoryItem.Id id, int count) {
        sessionItemRepository.findById(id.value()).ifPresent(entity -> {
            entity.setCollectionCount(count);
            sessionItemRepository.save(entity);
        });
    }

    @Override
    public void delete(GameInstanceInventoryItem.Id id) {
        sessionItemRepository.deleteById(id.value());
    }

    @Override
    public Stream<GameInstanceInventoryUseCase.SessionItemRaw> findEquipped(GamePlayer.Id playerId) {
        return inventory(playerId) //TODO IN QUERY ?
                .filter(raw -> raw.availability() == GameInstanceInventoryItem.Availability.EQUIP);
    }

    @Override
    public void equip(GameInstanceInventoryItem.Id id) {
        sessionItemRepository.findById(id.value())
            .ifPresent(entity -> {
                entity.setAvailability(GameInstanceInventoryItem.Availability.EQUIP);
                sessionItemRepository.save(entity);
            });
    }

    @Override
    public void unequip(GameInstanceInventoryItem.Id id) {
        sessionItemRepository.findById(id.value())
            .ifPresent(entity -> {
                entity.setAvailability(GameInstanceInventoryItem.Availability.FREE);
                sessionItemRepository.save(entity);
            });
    }

    @Override
    public boolean exist(GamePlayer.Id playerId, GameConfigInventoryItem.Id configItemId) {
        return sessionItemRepository.existsByInventory_Player_IdAndConfig_Id(playerId.value(), configItemId.value());
    }

    @Override
    public int count(GamePlayer.Id playerId, GameConfigInventoryItem.Id configItemId) {
        return sessionItemRepository.findByInventory_Player_IdAndConfig_Id(playerId.value(), configItemId.value())
                .map(GameInstanceInventoryItemEntity::getCollectionCount)
                .orElse(0);
    }

    @Override
    public void count(GamePlayer.Id playerId, GameConfigInventoryItem.Id configItemId, int count) {
        sessionItemRepository.findByInventory_Player_IdAndConfig_Id(playerId.value(), configItemId.value())
                .ifPresentOrElse(
                        entity -> {
                            entity.setCollectionCount(count);
                            sessionItemRepository.save(entity);
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
                            sessionItemRepository.save(itemEntity);
                        }
                );
    }

}
