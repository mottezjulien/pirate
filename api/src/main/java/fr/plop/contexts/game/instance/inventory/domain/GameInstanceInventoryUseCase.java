package fr.plop.contexts.game.instance.inventory.domain;

import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.config.inventory.domain.model.InventoryConfig;
import fr.plop.contexts.game.config.inventory.domain.model.GameConfigInventoryItem;
import fr.plop.contexts.game.config.inventory.domain.model.InventoryMergedRule;
import fr.plop.contexts.game.instance.core.domain.model.GamePlayer;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.instance.event.domain.GameEvent;
import fr.plop.contexts.game.instance.event.domain.GameEventOrchestrator;
import fr.plop.contexts.game.instance.push.PushEvent;
import fr.plop.contexts.game.instance.push.PushPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class GameInstanceInventoryUseCase {

    public interface Port {
        Stream<ItemRaw> inventory(GamePlayer.Id playerId);
        Optional<ItemRaw> findByInstanceId(GameInstanceInventoryItem.Id instanceId);
        Optional<ItemRaw> findByConfigId(GameInstanceContext context, GameConfigInventoryItem.Id configId);
        GameInstanceInventoryItem.Id add(GamePlayer.Id playerId, GameConfigInventoryItem.Id configId);
        void updateCount(GameInstanceInventoryItem.Id id, int count);
        void delete(GameInstanceInventoryItem.Id id);
        Stream<ItemRaw> findEquipped(GamePlayer.Id playerId);
        void equip(GameInstanceInventoryItem.Id id);
        void unequip(GameInstanceInventoryItem.Id id);
        boolean exist(GamePlayer.Id playerId, GameConfigInventoryItem.Id configItemId);
        int count(GamePlayer.Id playerId, GameConfigInventoryItem.Id configItemId);
        void count(GamePlayer.Id playerId, GameConfigInventoryItem.Id configItemId, int count);
    }


    public record ItemRaw(GameInstanceInventoryItem.Id instanceId, GameConfigInventoryItem.Id configId, int pourcentUsury, GameInstanceInventoryItem.Availability availability, int collectionCount) {

        public GameInstanceInventoryItem toInstance(GameConfigInventoryItem configItem, boolean mergeable) {
            List<GameInstanceInventoryItem.Action> actions = new ArrayList<>();
            if(configItem.optTargetId().isEmpty()){
                actions.add(GameInstanceInventoryItem.Action.DROP);
            }
            switch (configItem.actionType()) {
                case NONE -> {
                }
                case EQUIPPABLE -> actions.add(GameInstanceInventoryItem.Action.EQUIP);
                case CONSUMABLE -> actions.add(GameInstanceInventoryItem.Action.CONSUME);
                case USABLE -> actions.add(GameInstanceInventoryItem.Action.USE);
            }
            if (mergeable) {
                actions.add(GameInstanceInventoryItem.Action.MERGE);
            }
            GameInstanceInventoryItem.State state = switch (configItem.type()) {
                case UNIQUE -> new GameInstanceInventoryItem.State.Unique(pourcentUsury, availability);
                case COLLECTION -> new GameInstanceInventoryItem.State.Collection(collectionCount);
            };
            return new GameInstanceInventoryItem(instanceId, configItem.id(), configItem.label(), configItem.image(), configItem.optDescription(),
                    actions, state);
        }
    }
    
    private final Port port;
    private final GameConfigCache cache;
    //private final ConsequenceUseCase consequenceUseCase;
    private final GameEventOrchestrator eventOrchestrator;
    private final PushPort pushPort;

    public GameInstanceInventoryUseCase(Port port, GameConfigCache cache, GameEventOrchestrator eventOrchestrator, PushPort pushPort) {
        this.port = port;
        this.cache = cache;
        this.eventOrchestrator = eventOrchestrator;
        this.pushPort = pushPort;
    }

    public Stream<GameInstanceInventoryItem> list(GameInstanceContext context) {
        final InventoryConfig config = cache.inventory(context.instanceId());
        Stream<ItemRaw> rawList = port.inventory(context.playerId());
        return rawList
                .flatMap(raw -> toInstance(raw, config).stream());
    }


    
    public Optional<GameInstanceInventoryItem> details(GameInstanceContext context, GameInstanceInventoryItem.Id itemId) {
        final InventoryConfig config = cache.inventory(context.instanceId());
        return port.findByInstanceId(itemId)
                .flatMap(raw -> toInstance(raw, config));
    }

    private static Optional<GameInstanceInventoryItem> toInstance(ItemRaw raw, InventoryConfig config) {
        return config.byId(raw.configId()).map(configItem -> raw.toInstance(configItem, config.isMergeable(configItem.id())));
    }
    
    public void drop(GameInstanceContext context, GameInstanceInventoryItem.Id itemId) throws GameInstanceInventoryException {
        final GameInstanceInventoryItem instanceItem = details(context, itemId).orElseThrow(() -> new GameInstanceInventoryException(GameInstanceInventoryException.Type.ITEM_NOT_FOUND));
        if(!instanceItem.isDroppable()) {
            throw new GameInstanceInventoryException(GameInstanceInventoryException.Type.ACTION_NOT_ALLOWED, "Action drop not allowed for this item");
        }
        deleteOne_noPush(instanceItem);
        pushPort.push(new PushEvent.Inventory(context));
    }

    public void use(GameInstanceContext context, GameInstanceInventoryItem.Id itemId) throws GameInstanceInventoryException {
        final GameInstanceInventoryItem instanceItem = details(context, itemId).orElseThrow(() -> new GameInstanceInventoryException(GameInstanceInventoryException.Type.ITEM_NOT_FOUND));
        if(!instanceItem.isUsable()) {
            throw new GameInstanceInventoryException(GameInstanceInventoryException.Type.ACTION_NOT_ALLOWED, "Action consume not allowed for this item");
        }
        final InventoryConfig config = cache.inventory(context.instanceId());
        GameConfigInventoryItem configItem = config.byId(instanceItem.configId()).orElseThrow(() -> new GameInstanceInventoryException(GameInstanceInventoryException.Type.ITEM_NOT_FOUND));
        eventOrchestrator.fire(context, new GameEvent.InventoryItemAction(configItem.id()));
    }

    public void consume(GameInstanceContext context, GameInstanceInventoryItem.Id id) throws GameInstanceInventoryException {
        final GameInstanceInventoryItem item = details(context, id).orElseThrow(() -> new GameInstanceInventoryException(GameInstanceInventoryException.Type.ITEM_NOT_FOUND));
        if(!item.isConsumable()) {
            throw new GameInstanceInventoryException(GameInstanceInventoryException.Type.ACTION_NOT_ALLOWED, "Action consume not allowed for this item");
        }
        final InventoryConfig config = cache.inventory(context.instanceId());
        GameConfigInventoryItem configItem = config.byId(item.configId()).orElseThrow(() -> new GameInstanceInventoryException(GameInstanceInventoryException.Type.ITEM_NOT_FOUND));
        eventOrchestrator.fire(context, new GameEvent.InventoryItemAction(configItem.id()));

        deleteOne_noPush(item);
        pushPort.push(new PushEvent.Inventory(context));
    }
    
    public void equip(GameInstanceContext context, GameInstanceInventoryItem.Id id) throws GameInstanceInventoryException {
        final GameInstanceInventoryItem item = details(context, id).orElseThrow(() -> new GameInstanceInventoryException(GameInstanceInventoryException.Type.ITEM_NOT_FOUND));
        if(!item.isEquippable()) {
            throw new GameInstanceInventoryException(GameInstanceInventoryException.Type.ACTION_NOT_ALLOWED, "Action equip not allowed for this item");
        }
        if (!item.isFree()) {
            throw new GameInstanceInventoryException(GameInstanceInventoryException.Type.ACTION_NOT_ALLOWED, "Item is not free, cannot equip");
        }

        // Déséquiper l'item actuellement équipé s'il y en a un
        port.findEquipped(context.playerId())
                .forEach(equipped -> {
                    try {
                        unequip(context, equipped.instanceId());
                    } catch (GameInstanceInventoryException e) {
                        throw new RuntimeException(e);
                    }
                });

        // Équiper le nouvel item
        port.equip(id);
    }

    public void unequip(GameInstanceContext context, GameInstanceInventoryItem.Id id) throws GameInstanceInventoryException {
        final GameInstanceInventoryItem item = details(context, id).orElseThrow(() -> new GameInstanceInventoryException(GameInstanceInventoryException.Type.ITEM_NOT_FOUND));
        if(!item.isEquipped()) {
            throw new GameInstanceInventoryException(GameInstanceInventoryException.Type.ACTION_NOT_ALLOWED, "Item is not equipped, cannot unequip");
        }
        port.unequip(id);
    }

    public void useEquip(GameInstanceContext context, GameInstanceInventoryItem.Id id) throws GameInstanceInventoryException {
        final GameInstanceInventoryItem item = details(context, id).orElseThrow(() -> new GameInstanceInventoryException(GameInstanceInventoryException.Type.ITEM_NOT_FOUND));
        if(!item.isEquipped()) {
            throw new GameInstanceInventoryException(GameInstanceInventoryException.Type.ACTION_NOT_ALLOWED, "Item is equipped, cannot use");
        }
        final InventoryConfig config = cache.inventory(context.instanceId());
        GameConfigInventoryItem configItem = config.byId(item.configId()).orElseThrow(() -> new GameInstanceInventoryException(GameInstanceInventoryException.Type.ITEM_NOT_FOUND));
        eventOrchestrator.fire(context, new GameEvent.InventoryItemAction(configItem.id()));
    }

    public void merge(GameInstanceContext context, GameInstanceInventoryItem.Id oneId, GameInstanceInventoryItem.Id otherId) throws GameInstanceInventoryException {
        final GameInstanceInventoryItem oneItem = details(context, oneId).orElseThrow(() -> new GameInstanceInventoryException(GameInstanceInventoryException.Type.ITEM_NOT_FOUND));
        final GameInstanceInventoryItem otherItem = details(context, otherId).orElseThrow(() -> new GameInstanceInventoryException(GameInstanceInventoryException.Type.ITEM_NOT_FOUND));
        
        final InventoryConfig config = cache.inventory(context.instanceId());
        final Optional<InventoryMergedRule> optRule = config.mergedRule(oneItem.configId(), otherItem.configId());
        if(optRule.isEmpty()) {
            throw new GameInstanceInventoryException(GameInstanceInventoryException.Type.ACTION_NOT_ALLOWED, "Items not mergeable");
        }

        deleteOne_noPush(oneItem);
        deleteOne_noPush(otherItem);
        insertOne(context, optRule.get().convertTo());

    }

    public void insertOne(GameInstanceContext context, GameConfigInventoryItem.Id configItemId) throws GameInstanceInventoryException {
        final InventoryConfig config = cache.inventory(context.instanceId());

        // Vérifier que l'item existe dans la config
        GameConfigInventoryItem configItem = config.byId(configItemId)
                .orElseThrow(() -> new GameInstanceInventoryException(GameInstanceInventoryException.Type.ITEM_NOT_FOUND));

        switch (configItem.type()) {
            case UNIQUE -> {
                if(!port.exist(context.playerId(), configItemId)) {
                    port.add(context.playerId(), configItemId);
                }
            }
            case COLLECTION -> {
                int currentCount = port.count(context.playerId(), configItemId);
                port.count(context.playerId(), configItemId, currentCount + 1);
            }
        }
        pushPort.push(new PushEvent.Inventory(context));
    }

    public void deleteOne(GameInstanceContext context, GameConfigInventoryItem.Id configItemId) throws GameInstanceInventoryException {
        final InventoryConfig config = cache.inventory(context.instanceId());
        final GameInstanceInventoryItem item =  port.findByConfigId(context, configItemId)
                .flatMap(raw -> toInstance(raw, config))
                .orElseThrow(() -> new GameInstanceInventoryException(GameInstanceInventoryException.Type.ITEM_NOT_FOUND));
        if(!item.isDroppable()) {
            throw new GameInstanceInventoryException(GameInstanceInventoryException.Type.ACTION_NOT_ALLOWED, "Action drop not allowed for this item");
        }
        deleteOne_noPush(item);
        pushPort.push(new PushEvent.Inventory(context));
    }

    private void deleteOne_noPush(GameInstanceInventoryItem item) {
        switch (item.state()) {
            case GameInstanceInventoryItem.State.Collection collection -> {
                if(collection.count() > 1){
                    port.updateCount(item.instanceId(), collection.count()-1);
                } else{
                    port.delete(item.instanceId());
                }
            }
            case GameInstanceInventoryItem.State.Unique ignored -> port.delete(item.instanceId());
        }
    }



    
}
