package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.component.Chest;
import com.github.tilcob.game.component.Inventory;
import com.github.tilcob.game.component.Item;
import com.github.tilcob.game.component.OpenChestRequest;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.*;
import com.github.tilcob.game.inventory.InventoryService;
import com.github.tilcob.game.item.ItemDefinition;
import com.github.tilcob.game.item.ItemDefinitionRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChestSystem extends IteratingSystem implements Disposable {
    private final InventoryService inventoryService;
    private final GameEventBus eventBus;
    private Entity openChestEntity;
    private Entity openPlayer;

    public ChestSystem(InventoryService inventoryService, GameEventBus eventBus) {
        super(Family.all(OpenChestRequest.class).get());
        this.inventoryService = inventoryService;
        this.eventBus = eventBus;

        eventBus.subscribe(CloseChestEvent.class, this::close);
        eventBus.subscribe(TransferChestToPlayerEvent.class, this::transferChestToPlayer);
        eventBus.subscribe(TransferPlayerToChestEvent.class, this::transferPlayerToChest);
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
            openChestEntity = chestEntity;
            openPlayer = player;
            eventBus.fire(new OpenChestEvent(player, chestEntity));
            player.remove(OpenChestRequest.class);
        }
    }

    @Deprecated
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
        if (event.chest() != null) {
            event.chest().close();
        } else {
            Chest chest = Chest.MAPPER.get(openChestEntity);
            if (chest != null) chest.close();
        }
        openChestEntity = null;
        openPlayer = null;
    }

    private void transferPlayerToChest(TransferPlayerToChestEvent event) {
        if (openChestEntity == null || openPlayer == null) return;
        Chest chest = Chest.MAPPER.get(openChestEntity);
        Inventory inventory = Inventory.MAPPER.get(openPlayer);
        if (chest == null || inventory == null) return;

        inventoryService.setPlayer(openPlayer);
        Entity itemEntity = inventoryService.findItemAtSlot(inventory, event.fromIndex());
        if (itemEntity == null) return;

        Item item = Item.MAPPER.get(itemEntity);
        if (item == null) return;

        Array<String> contents = chest.getContents();
        int available = Constants.INVENTORY_CAPACITY - contents.size;
        if (available <= 0) return;

        int moveCount = Math.min(available, item.getCount());
        for (int i = 0; i < moveCount; i++) {
            contents.add(item.getItemId());
        }

        if (moveCount >= item.getCount()) {
            inventory.remove(itemEntity);
            inventoryService.removeItem(itemEntity);
        } else {
            item.remove(moveCount);
        }

        chest.setContents(contents);
        eventBus.fire(new UpdateInventoryEvent(openPlayer));
        eventBus.fire(new UpdateChestInventoryEvent(openPlayer, openChestEntity));
    }

    private void transferChestToPlayer(TransferChestToPlayerEvent event) {
        if (openChestEntity == null || openPlayer == null) return;
        Chest chest = Chest.MAPPER.get(openChestEntity);
        Inventory inventory = Inventory.MAPPER.get(openPlayer);
        if (chest == null || inventory == null) return;

        Array<String> contents = chest.getContents();
        if (event.fromIndex() < 0 || event.fromIndex() >= contents.size) return;

        String itemId = ItemDefinitionRegistry.resolveId(contents.get(event.fromIndex()));
        if (event.toIndex() < 0 || event.toIndex() >= Constants.INVENTORY_CAPACITY) return;
        inventoryService.setPlayer(openPlayer);
        Entity targetEntity = inventoryService.findItemAtSlot(inventory, event.toIndex());
        if (targetEntity != null) {
            Item targetItem = Item.MAPPER.get(targetEntity);
            if (targetItem == null) return;
            if (targetItem.getItemId().equals(itemId)) {
                ItemDefinition definition = ItemDefinitionRegistry.get(itemId);
                if (definition.isStackable() && targetItem.getCount() < definition.maxStack()) {
                    targetItem.add(1);
                    contents.removeIndex(event.fromIndex());
                    chest.setContents(contents);
                    eventBus.fire(new UpdateInventoryEvent(openPlayer));
                    eventBus.fire(new UpdateChestInventoryEvent(openPlayer, openChestEntity));
                }
            }
            return;
        }
        Entity newItem = inventoryService.spawnItem(itemId, event.toIndex(), inventory.nextId());
        inventory.add(newItem);
        contents.removeIndex(event.fromIndex());
        chest.setContents(contents);
        eventBus.fire(new UpdateInventoryEvent(openPlayer));
        eventBus.fire(new UpdateChestInventoryEvent(openPlayer, openChestEntity));
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(CloseChestEvent.class, this::close);
        eventBus.unsubscribe(TransferChestToPlayerEvent.class, this::transferChestToPlayer);
        eventBus.unsubscribe(TransferPlayerToChestEvent.class, this::transferPlayerToChest);
    }
}
