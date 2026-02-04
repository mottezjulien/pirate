package fr.plop.contexts.game.instance.inventory.persistence;


import fr.plop.contexts.game.config.inventory.domain.model.GameConfigInventoryItem;
import fr.plop.contexts.game.config.inventory.persistence.GameConfigInventoryItemEntity;
import fr.plop.contexts.game.instance.inventory.domain.GameInstanceInventoryItem;
import fr.plop.contexts.game.instance.inventory.domain.GameInstanceInventoryUseCase;
import jakarta.persistence.*;

@Entity
@Table(name = "LO_SESSION_INVENTORY_ITEM")
public class GameInstanceInventoryItemEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "inventory_id")
    private GameInstanceInventoryEntity inventory;

    @ManyToOne
    @JoinColumn(name = "config_item_id")
    private GameConfigInventoryItemEntity config;

    @Column(name = "pourcent_usury")
    private int pourcentUsury;

    private int weight;

    @Column(name = "collection_count")
    private int collectionCount;

    @Enumerated(EnumType.STRING)
    private GameInstanceInventoryItem.Availability availability;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GameInstanceInventoryEntity getInventory() {
        return inventory;
    }

    public void setInventory(GameInstanceInventoryEntity inventory) {
        this.inventory = inventory;
    }

    public GameConfigInventoryItemEntity getConfig() {
        return config;
    }

    public void setConfig(GameConfigInventoryItemEntity config) {
        this.config = config;
    }


    public int getCollectionCount() {
        return collectionCount;
    }

    public void setCollectionCount(int collectionCount) {
        this.collectionCount = collectionCount;
    }

    public void setAvailability(GameInstanceInventoryItem.Availability availability) {
        this.availability = availability;
    }

    public GameInstanceInventoryUseCase.SessionItemRaw toRawModel() {
        return new GameInstanceInventoryUseCase.SessionItemRaw(new GameInstanceInventoryItem.Id(id),
                new GameConfigInventoryItem.Id(config.getId()), pourcentUsury, availability, collectionCount);
    }
}
