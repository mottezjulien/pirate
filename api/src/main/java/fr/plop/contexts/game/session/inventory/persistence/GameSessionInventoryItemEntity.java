package fr.plop.contexts.game.session.inventory.persistence;


import fr.plop.contexts.game.config.inventory.domain.model.GameConfigInventoryItem;
import fr.plop.contexts.game.config.inventory.persistence.GameConfigInventoryItemEntity;
import fr.plop.contexts.game.session.inventory.domain.GameSessionInventoryItem;
import fr.plop.contexts.game.session.inventory.domain.GameSessionInventoryUseCase;
import jakarta.persistence.*;

@Entity
@Table(name = "TEST2_SESSION_INVENTORY_ITEM")
public class GameSessionInventoryItemEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "inventory_id")
    private GameSessionInventoryEntity inventory;

    @ManyToOne
    @JoinColumn(name = "config_item_id")
    private GameConfigInventoryItemEntity config;

    @Column(name = "pourcent_usury")
    private int pourcentUsury;

    private int weight;

    @Column(name = "collection_count")
    private int collectionCount;

    @Enumerated(EnumType.STRING)
    private GameSessionInventoryItem.Availability availability;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GameSessionInventoryEntity getInventory() {
        return inventory;
    }

    public void setInventory(GameSessionInventoryEntity inventory) {
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

    public void setAvailability(GameSessionInventoryItem.Availability availability) {
        this.availability = availability;
    }

    public GameSessionInventoryUseCase.SessionItemRaw toRawModel() {
        return new GameSessionInventoryUseCase.SessionItemRaw(new GameSessionInventoryItem.Id(id),
                new GameConfigInventoryItem.Id(config.getId()), pourcentUsury, availability, collectionCount);
    }
}
