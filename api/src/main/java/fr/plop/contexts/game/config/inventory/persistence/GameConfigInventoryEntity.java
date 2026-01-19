package fr.plop.contexts.game.config.inventory.persistence;


import fr.plop.contexts.game.config.inventory.domain.model.InventoryConfig;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "TEST2_CONFIG_INVENTORY")
public class GameConfigInventoryEntity {

    @Id
    private String id;

    @OneToMany(mappedBy = "config")
    private Set<GameConfigInventoryItemEntity> items = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<GameConfigInventoryItemEntity> getItems() {
        return items;
    }

    public void setItems(Set<GameConfigInventoryItemEntity> items) {
        this.items = items;
    }

    public InventoryConfig toModel() {
        return new InventoryConfig(new InventoryConfig.Id(id), items.stream()
                .map(GameConfigInventoryItemEntity::toModel)
                .toList(), List.of()); //TODO
    }
}
