package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.EntityAddItemEvent;
import com.github.tilcob.game.item.ItemType;

public class InventorySystem extends IteratingSystem {
    private final Stage stage;

    public InventorySystem(Stage stage) {
        super(Family.all(Inventory.class).get());
        this.stage = stage;
    }

    @Override
    protected void processEntity(Entity player, float deltaTime) {
        Inventory inventory = Inventory.MAPPER.get(player);
        if (inventory.getItemsToAdd().isEmpty()) return;

        for (ItemType itemType : inventory.getItemsToAdd()) {
            int slotIndex = emptySlotIndex(inventory);
            if (slotIndex == -1) return; // Inventory is full

            inventory.add(spawnItem(itemType, slotIndex));

        }
        stage.getRoot().fire(new EntityAddItemEvent(player));

        inventory.getItemsToAdd().clear();
    }

    private Entity spawnItem(ItemType itemType, int slotIndex) {
        Entity entity = getEngine().createEntity();
        entity.add(new Item(itemType, slotIndex));
        getEngine().addEntity(entity);
        return entity;
    }

    private int emptySlotIndex(Inventory inventory) {
        outer:
        for (int i = 0; i < Constants.INVENTORY_CAPACITY; i++) {
            for (var item : inventory.getItems()) {
                if (Item.MAPPER.get(item).getSlotIndex() == i) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
