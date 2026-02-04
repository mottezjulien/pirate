package fr.plop.contexts.game.config.scenario.persistence.possibility.consequence.entity;

import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.inventory.domain.model.GameConfigInventoryItem;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("REMOVE_ITEM")
public final class ConsequenceRemoveItemEntity
        extends ConsequenceAbstractEntity {

    @Column(name = "inventory_item_id")
    private String itemId;


    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public Consequence toModel() {
        return new Consequence.InventoryRemoveItem(new Consequence.Id(id), new GameConfigInventoryItem.Id(itemId));
    }
}
