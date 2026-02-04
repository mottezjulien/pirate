package fr.plop.contexts.game.instance.inventory.persistence;


import fr.plop.contexts.game.instance.core.persistence.GamePlayerEntity;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "LO_SESSION_INVENTORY")
public class GameInstanceInventoryEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private GamePlayerEntity player;

    @OneToMany(mappedBy = "inventory")
    Set<GameInstanceInventoryItemEntity> items = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GamePlayerEntity getPlayer() {
        return player;
    }

    public void setPlayer(GamePlayerEntity player) {
        this.player = player;
    }

    public Set<GameInstanceInventoryItemEntity> getItems() {
        return items;
    }

    public void setItems(Set<GameInstanceInventoryItemEntity> items) {
        this.items = items;
    }
}
