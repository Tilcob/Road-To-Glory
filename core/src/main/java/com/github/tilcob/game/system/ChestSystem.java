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
import com.github.tilcob.game.input.Command;
import com.github.tilcob.game.inventory.InventoryService;
import com.github.tilcob.game.item.ItemDefinition;
import com.github.tilcob.game.item.ItemDefinitionRegistry;

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
        eventBus.subscribe(CommandEvent.class, this::onCommand);
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
                player.remove(OpenChestRequest.class);
                return;
            }
            openChestEntity = chestEntity;
            openPlayer = player;
            eventBus.fire(new OpenChestEvent(player, chestEntity));
            player.remove(OpenChestRequest.class);
        }
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

    private void onCommand(CommandEvent event) {
        if (event.isHandled()) return;
        if (event.getCommand() != Command.INTERACT) return;
        Entity player = event.getPlayer();
        if (openPlayer != null && openPlayer == player && openChestEntity != null) {
            event.setHandled(true);
            eventBus.fire(new CloseChestEvent(player, Chest.MAPPER.get(openChestEntity)));
            return;
        }
        OpenChestRequest req = OpenChestRequest.MAPPER.get(player);
        if (req == null) return;
        event.setHandled(true);

        Entity chestEntity = req.getChest();
        Chest chest = Chest.MAPPER.get(chestEntity);
        if (chest != null) chest.open();
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
        eventBus.unsubscribe(CommandEvent.class, this::onCommand);
    }
}
