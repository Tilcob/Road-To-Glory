package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.tilcob.game.component.Chest;
import com.github.tilcob.game.component.Inventory;
import com.github.tilcob.game.component.Item;
import com.github.tilcob.game.component.OpenChestRequest;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.CloseChestEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.OpenChestEvent;
import com.github.tilcob.game.inventory.InventoryService;
import com.github.tilcob.game.item.ItemDefinition;
import com.github.tilcob.game.item.ItemDefinitionRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChestSystem extends IteratingSystem {
    private final InventoryService inventoryService;
    private final GameEventBus eventBus;

    public ChestSystem(InventoryService inventoryService, GameEventBus eventBus) {
        super(Family.all(OpenChestRequest.class).get());
        this.inventoryService = inventoryService;
        this.eventBus = eventBus;

        eventBus.subscribe(CloseChestEvent.class, this::close);
    }

    @Override
    protected void processEntity(Entity player, float deltaTime) {
        Entity chestEntity = OpenChestRequest.MAPPER.get(player).getChest();
        inventoryService.setPlayer(player);
        if (chestEntity == null) {
            player.remove(OpenChestRequest.class);
            return;
        }
        Chest chest = Chest.MAPPER.get(chestEntity);
        if (chest == null) {
            player.remove(OpenChestRequest.class);
            return;
        }

        if (chest.isOpen()) {
            Inventory inventory = Inventory.MAPPER.get(player);
            if (inventory == null) {
                chest.close();
                return;
            }
            eventBus.fire(new OpenChestEvent(player, chestEntity));
            player.remove(OpenChestRequest.class);
        }
    }

    private void transferContents(Chest chest, Inventory inventory) {
        Map<String, Integer> stackSpace = new HashMap<>();
        int usedSlots = inventory.getItems().size;
        int emptySlots = Constants.INVENTORY_CAPACITY - usedSlots;

        for (var entity : inventory.getItems()) {
            Item item = Item.MAPPER.get(entity);
            ItemDefinition definition = ItemDefinitionRegistry.get(item.getItemId());
            if (!definition.isStackable()) continue;
            int space = definition.maxStack() - item.getCount();
            if (space > 0) {
                stackSpace.merge(item.getItemId(), space, Integer::sum);
            }
        }

        List<String> remaining = new ArrayList<>();
        List<String> transferred = new ArrayList<>();

        for (String itemId : chest.getContents()) {
            ItemDefinition definition = ItemDefinitionRegistry.get(itemId);
            if (definition.isStackable() && stackSpace.getOrDefault(itemId, 0) > 0) {
                stackSpace.put(itemId, stackSpace.get(itemId) - 1);
                transferred.add(itemId);
                continue;
            }

            if (emptySlots > 0) {
                emptySlots--;
                transferred.add(itemId);
                continue;
            }

            remaining.add(itemId);
        }

        if (!transferred.isEmpty()) {
            for (String itemId : transferred) {
                inventory.getItemsToAdd().add(itemId);
            }
        }
        chest.setContents(remaining);
    }

    private void close(CloseChestEvent event) {
        event.player().remove(OpenChestRequest.class);
        event.chest().close();
    }
}
