package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.tilcob.game.component.Chest;
import com.github.tilcob.game.component.Inventory;
import com.github.tilcob.game.component.Item;
import com.github.tilcob.game.component.OpenChestRequest;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.item.ItemType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChestSystem extends IteratingSystem {

    public ChestSystem() {
        super(Family.all(OpenChestRequest.class).get());
    }

    @Override
    protected void processEntity(Entity player, float deltaTime) {
        Entity chestEntity = OpenChestRequest.MAPPER.get(player).getChest();
        Chest chest = Chest.MAPPER.get(chestEntity);
        if (chest == null) return;

        if (chest.isOpen()) {
            Inventory inventory = Inventory.MAPPER.get(player);
            if (inventory == null) {
                close(player, chest);
                return;
            }

            transferContents(chest, inventory);
            close(player, chest);
        }
    }

    private void transferContents(Chest chest, Inventory inventory) {
        Map<ItemType, Integer> stackSpace = new HashMap<>();
        int usedSlots = inventory.getItems().size;
        int emptySlots = Constants.INVENTORY_CAPACITY - usedSlots;

        for (var entity : inventory.getItems()) {
            Item item = Item.MAPPER.get(entity);
            if (!item.getItemType().isStackable()) continue;
            int space = item.getItemType().getMaxStack() - item.getCount();
            if (space > 0) {
                stackSpace.merge(item.getItemType(), space, Integer::sum);
            }
        }

        List<ItemType> remaining = new ArrayList<>();
        List<ItemType> transferred = new ArrayList<>();

        for (ItemType item : chest.getContents()) {
            if (item.isStackable() && stackSpace.getOrDefault(item, 0) > 0) {
                stackSpace.put(item, stackSpace.get(item) - 1);
                transferred.add(item);
                continue;
            }

            if (emptySlots > 0) {
                emptySlots--;
                transferred.add(item);
                continue;
            }

            remaining.add(item);
        }

        if (!transferred.isEmpty()) {
            for (ItemType item : transferred) {
                inventory.getItemsToAdd().add(item);
            }
        }
        chest.setContents(remaining);
    }

    private void close(Entity player, Chest chest) {
        player.remove(OpenChestRequest.class);
        chest.close();
    }
}
