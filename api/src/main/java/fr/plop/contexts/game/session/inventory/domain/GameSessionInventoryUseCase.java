package fr.plop.contexts.game.session.inventory.domain;

import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.config.consequence.ConsequenceUseCase;
import fr.plop.contexts.game.config.inventory.domain.model.InventoryConfig;
import fr.plop.contexts.game.config.inventory.domain.model.GameConfigInventoryItem;
import fr.plop.contexts.game.config.inventory.domain.model.InventoryItemActionRule;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.event.domain.GameEventOrchestrator;
import fr.plop.contexts.game.session.push.PushEvent;
import fr.plop.contexts.game.session.push.PushPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class GameSessionInventoryUseCase {

    public interface Port {
        Stream<SessionItemRaw> list(GamePlayer.Id playerId);
        Optional<SessionItemRaw> findById(GameSessionInventoryItem.Id id);
        Optional<SessionItemRaw> findByConfigId(GamePlayer.Id playerId, GameConfigInventoryItem.Id configId);
        GameSessionInventoryItem.Id add(GamePlayer.Id playerId, GameConfigInventoryItem.Id configId, int count);
        void updateCount(GameSessionInventoryItem.Id id, int count);
        void delete(GameSessionInventoryItem.Id id);
        Stream<SessionItemRaw> findEquipped(GamePlayer.Id playerId);
        void equip(GameSessionInventoryItem.Id id);
        void unequip(GameSessionInventoryItem.Id id);
    }


    public record SessionItemRaw(GameSessionInventoryItem.Id sessionId, GameConfigInventoryItem.Id configId, int pourcentUsury, GameSessionInventoryItem.Availability availability, int collectionCount) {

        public GameSessionInventoryItem toSessionItem(GameConfigInventoryItem configItem, boolean mergeable) {
            List<GameSessionInventoryItem.Action> actions = new ArrayList<>();
            if(configItem.optLinkTargetId().isEmpty()){
                actions.add(GameSessionInventoryItem.Action.DROP);
            }
            if(configItem.actionRules().stream().anyMatch(InventoryItemActionRule::canEquipped)) {
                actions.add(GameSessionInventoryItem.Action.EQUIP);
            }
            if(configItem.actionRules().stream().anyMatch(InventoryItemActionRule::canConsumed)) {
                actions.add(GameSessionInventoryItem.Action.CONSUME);
            }
            if(configItem.actionRules().stream().anyMatch(InventoryItemActionRule::canUsed)) {
                actions.add(GameSessionInventoryItem.Action.USE);
            }
            if (mergeable) {
                actions.add(GameSessionInventoryItem.Action.MERGE);
            }
            GameSessionInventoryItem.State state = switch (configItem.type()) {
                case UNIQUE -> new GameSessionInventoryItem.State.Unique(pourcentUsury, availability);
                case COLLECTION -> new GameSessionInventoryItem.State.Collection(collectionCount);
            };
            return new GameSessionInventoryItem(sessionId, configItem.id(), configItem.label(), configItem.image(), configItem.optDescription(),
                    actions, state);
        }
    }
    
    private final Port port;
    private final GameConfigCache cache;
    private final ConsequenceUseCase consequenceUseCase;
    private final GameEventOrchestrator eventOrchestrator;
    private final PushPort pushPort;

    public GameSessionInventoryUseCase(Port port, GameConfigCache cache, ConsequenceUseCase consequenceUseCase,
                                       GameEventOrchestrator eventOrchestrator, PushPort pushPort) {
        this.port = port;
        this.cache = cache;
        this.consequenceUseCase = consequenceUseCase;
        this.eventOrchestrator = eventOrchestrator;
        this.pushPort = pushPort;
    }

    public Stream<GameSessionInventoryItem> list(GameSessionContext context) {
        final InventoryConfig config = cache.inventory(context.sessionId());
        Stream<SessionItemRaw> rawList = port.list(context.playerId());
        return rawList
                .flatMap(raw -> toSessionItem(raw, config).stream());
    }

    public void addItem(GameSessionContext context, GameConfigInventoryItem.Id configItemId, int count) {
        final InventoryConfig config = cache.inventory(context.sessionId());

        // Vérifier que l'item existe dans la config
        GameConfigInventoryItem configItem = config.byId(configItemId).orElse(null);
        if (configItem == null) {
            return; // Item non trouvé dans la config, on ignore
        }

        // Vérifier si le joueur a déjà cet item
        Optional<SessionItemRaw> existingItem = port.findByConfigId(context.playerId(), configItemId);

        if (existingItem.isPresent()) {
            // Si c'est un item COLLECTION, on incrémente le count
            if (configItem.type() == GameConfigInventoryItem.Type.COLLECTION) {
                SessionItemRaw raw = existingItem.get();
                port.updateCount(raw.sessionId(), raw.collectionCount() + count);
            }
            // Si c'est UNIQUE, on ne fait rien (le joueur l'a déjà)
        } else {
            // Créer un nouvel item
            port.add(context.playerId(), configItemId, count);
        }

        pushPort.push(new PushEvent.Inventory(context));
    }
    
    public Optional<GameSessionInventoryItem> details(GameSessionContext context, GameSessionInventoryItem.Id sessionItemId) {
        final InventoryConfig config = cache.inventory(context.sessionId());
        return port.findById(sessionItemId)
                .flatMap(raw -> toSessionItem(raw, config));
    }

    private static Optional<GameSessionInventoryItem> toSessionItem(SessionItemRaw raw, InventoryConfig config) {
        return config.byId(raw.configId()).map(configItem -> raw.toSessionItem(configItem, config.isMergeable(configItem.id())));
    }
    
    public void drop(GameSessionContext context, GameSessionInventoryItem.Id sessionItemId) throws GameSessionInventoryException {
        GameSessionInventoryItem sessionItem = details(context, sessionItemId).orElseThrow(() -> new GameSessionInventoryException(GameSessionInventoryException.Type.ITEM_NOT_FOUND));
        if(!sessionItem.isDroppable()) {
            throw new GameSessionInventoryException(GameSessionInventoryException.Type.ACTION_NOT_ALLOWED, "Action drop not allowed for this item");
        }
        deleteOne(sessionItem);
        pushPort.push(new PushEvent.Inventory(context));
    }

    public void use(GameSessionContext context, GameSessionInventoryItem.Id sessionItemId) throws GameSessionInventoryException {
        GameSessionInventoryItem sessionItem = details(context, sessionItemId).orElseThrow(() -> new GameSessionInventoryException(GameSessionInventoryException.Type.ITEM_NOT_FOUND));
        if(!sessionItem.isUsable()) {
            throw new GameSessionInventoryException(GameSessionInventoryException.Type.ACTION_NOT_ALLOWED, "Action consume not allowed for this item");
        }
        final InventoryConfig config = cache.inventory(context.sessionId());
        GameConfigInventoryItem configItem = config.byId(sessionItem.configId()).orElseThrow(() -> new GameSessionInventoryException(GameSessionInventoryException.Type.ITEM_NOT_FOUND));
        executeConsequence(context, configItem.useRules());
    }

    public void consume(GameSessionContext context, GameSessionInventoryItem.Id sessionItemId) throws GameSessionInventoryException {
        GameSessionInventoryItem sessionItem = details(context, sessionItemId).orElseThrow(() -> new GameSessionInventoryException(GameSessionInventoryException.Type.ITEM_NOT_FOUND));
        if(!sessionItem.isConsumable()) {
            throw new GameSessionInventoryException(GameSessionInventoryException.Type.ACTION_NOT_ALLOWED, "Action consume not allowed for this item");
        }
        final InventoryConfig config = cache.inventory(context.sessionId());
        GameConfigInventoryItem configItem = config.byId(sessionItem.configId()).orElseThrow(() -> new GameSessionInventoryException(GameSessionInventoryException.Type.ITEM_NOT_FOUND));
        executeConsequence(context, configItem.consumeRules());

        deleteOne(sessionItem);
        pushPort.push(new PushEvent.Inventory(context));
    }
    
    public void equip(GameSessionContext context, GameSessionInventoryItem.Id sessionItemId) throws GameSessionInventoryException {
        GameSessionInventoryItem sessionItem = details(context, sessionItemId).orElseThrow(() -> new GameSessionInventoryException(GameSessionInventoryException.Type.ITEM_NOT_FOUND));
        if(!sessionItem.isEquippable()) {
            throw new GameSessionInventoryException(GameSessionInventoryException.Type.ACTION_NOT_ALLOWED, "Action equip not allowed for this item");
        }
        if (!sessionItem.isFree()) {
            throw new GameSessionInventoryException(GameSessionInventoryException.Type.ACTION_NOT_ALLOWED, "Item is not free, cannot equip");
        }

        // Déséquiper l'item actuellement équipé s'il y en a un
        port.findEquipped(context.playerId())
                .forEach(equipped -> {
                    try {
                        unequip(context, equipped.sessionId());
                    } catch (GameSessionInventoryException e) {
                        throw new RuntimeException(e);
                    }
                });

        // Équiper le nouvel item
        port.equip(sessionItemId);
    }

    public void unequip(GameSessionContext context, GameSessionInventoryItem.Id sessionItemId) throws GameSessionInventoryException {
        GameSessionInventoryItem sessionItem = details(context, sessionItemId).orElseThrow(() -> new GameSessionInventoryException(GameSessionInventoryException.Type.ITEM_NOT_FOUND));
        if(!sessionItem.isEquipped()) {
            throw new GameSessionInventoryException(GameSessionInventoryException.Type.ACTION_NOT_ALLOWED, "Item is not equipped, cannot unequip");
        }
        port.unequip(sessionItemId);
    }

    public void useEquip(GameSessionContext context, GameSessionInventoryItem.Id sessionItemId) throws GameSessionInventoryException {
        GameSessionInventoryItem sessionItem = details(context, sessionItemId).orElseThrow(() -> new GameSessionInventoryException(GameSessionInventoryException.Type.ITEM_NOT_FOUND));
        if(!sessionItem.isEquipped()) {
            throw new GameSessionInventoryException(GameSessionInventoryException.Type.ACTION_NOT_ALLOWED, "Item is equipped, cannot use");
        }
        final InventoryConfig config = cache.inventory(context.sessionId());
        GameConfigInventoryItem configItem = config.byId(sessionItem.configId()).orElseThrow(() -> new GameSessionInventoryException(GameSessionInventoryException.Type.ITEM_NOT_FOUND));
        executeConsequence(context, configItem.useRules());
    }

    
    private void executeConsequence(GameSessionContext context, Stream<InventoryItemActionRule> consumeRules) {
        consumeRules.forEach(consumeRule -> {
            switch (consumeRule.consequence()) {
                case InventoryItemActionRule.Consequence.Event event -> eventOrchestrator.fire(context, event.value());
                case InventoryItemActionRule.Consequence.Direct direct ->
                        direct.consequences().forEach(c -> consequenceUseCase.action(context, c));
            }
        });
    }

    private void deleteOne(GameSessionInventoryItem sessionItem) {
        switch (sessionItem.state()){
            case GameSessionInventoryItem.State.Collection collection -> {
                if(collection.count() > 1){
                    port.updateCount(sessionItem.sessionId(), collection.count()-1);
                } else{
                    port.delete(sessionItem.sessionId());
                }
            }
            case GameSessionInventoryItem.State.Unique ignored -> port.delete(sessionItem.sessionId());
        }
    }
    
}
