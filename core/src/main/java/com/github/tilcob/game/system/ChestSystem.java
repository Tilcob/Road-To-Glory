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
import com.github.tilcob.game.quest.QuestManager;

public class ChestSystem extends IteratingSystem implements Disposable {
    private final InventoryService inventoryService;
    private final GameEventBus eventBus;
    private final QuestManager questManager;
    private Entity openChestEntity;
    private Entity openPlayer;

    public ChestSystem(InventoryService inventoryService, GameEventBus eventBus, QuestManager questManager) {
        super(Family.all(OpenChestRequest.class).get());
        this.inventoryService = inventoryService;
        this.eventBus = eventBus;
        this.questManager = questManager;

        eventBus.subscribe(CloseChestEvent.class, this::close);
        eventBus.subscribe(DragAndDropChestEvent.class, this::onMoveChestItem);
        eventBus.subscribe(TransferChestToPlayerEvent.class, this::transferChestToPlayer);
        eventBus.subscribe(TransferPlayerToChestEvent.class, this::transferPlayerToChest);
        eventBus.subscribe(TransferChestToPlayerAutoEvent.class, this::transferChestToPlayerAuto);
        eventBus.subscribe(CommandEvent.class, this::onCommand);
    }

    @Override
    protected void processEntity(Entity player, float deltaTime) {
        Entity chestEntity = OpenChestRequest.MAPPER.get(player).getChest();
        inventoryService.setPlayer(player);
        if (chestEntity == null) return;

        Chest chest = Chest.MAPPER.get(chestEntity);
        if (chest == null) return;

        if (chest.isOpen()) {
            if (openPlayer == player && openChestEntity == chestEntity) return;
            Inventory inventory = Inventory.MAPPER.get(player);
            if (inventory == null) {
                chest.close();
                return;
            }
            openChestEntity = chestEntity;
            openPlayer = player;
            eventBus.fire(new OpenChestEvent(player, chestEntity));
        }
    }

    private void close(CloseChestEvent event) {
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
        transferChestToPlayer(event.fromIndex(), event.toIndex());
    }

    private void transferChestToPlayerAuto(TransferChestToPlayerAutoEvent event) {
        if (openChestEntity == null || openPlayer == null) return;
        Inventory inventory = Inventory.MAPPER.get(openPlayer);
        if (inventory == null) return;
        inventoryService.setPlayer(openPlayer);
        int emptySlot = inventoryService.emptySlotIndex(inventory);
        if (emptySlot == -1) return;
        transferChestToPlayer(event.fromIndex(), emptySlot);
    }

    private void onMoveChestItem(DragAndDropChestEvent event) {
        if (openChestEntity == null || openPlayer == null) return;
        Chest chest = Chest.MAPPER.get(openChestEntity);
        if (!inventoryService.moveChestContents(chest, event.fromIdx(), event.toIdx())) return;
        eventBus.fire(new UpdateChestInventoryEvent(openPlayer, openChestEntity));
    }

    private void transferChestToPlayer(int fromIndex, int toIndex) {
        if (openChestEntity == null || openPlayer == null) return;
        Chest chest = Chest.MAPPER.get(openChestEntity);
        Inventory inventory = Inventory.MAPPER.get(openPlayer);
        if (chest == null || inventory == null) return;

        Array<String> contents = chest.getContents();
        if (fromIndex < 0 || fromIndex >= contents.size) return;

        String itemId = ItemDefinitionRegistry.resolveId(contents.get(fromIndex));
        if (toIndex < 0 || toIndex >= Constants.INVENTORY_CAPACITY) return;
        inventoryService.setPlayer(openPlayer);
        Entity targetEntity = inventoryService.findItemAtSlot(inventory, toIndex);
        if (targetEntity != null) {
            Item targetItem = Item.MAPPER.get(targetEntity);
            if (targetItem == null) return;
            if (targetItem.getItemId().equals(itemId)) {
                ItemDefinition definition = ItemDefinitionRegistry.get(itemId);
                if (definition.isStackable() && targetItem.getCount() < definition.maxStack()) {
                    targetItem.add(1);
                    contents.removeIndex(fromIndex);
                    chest.setContents(contents);

                    questManager.signal(openPlayer, "collect", itemId, 1);
                    eventBus.fire(new UpdateInventoryEvent(openPlayer));
                    eventBus.fire(new UpdateChestInventoryEvent(openPlayer, openChestEntity));
                }
            }
            return;
        }
        Entity newItem = inventoryService.spawnItem(itemId, toIndex, inventory.nextId());
        inventory.add(newItem);
        contents.removeIndex(fromIndex);
        chest.setContents(contents);

        questManager.signal(openPlayer, "collect", itemId, 1);
        eventBus.fire(new UpdateInventoryEvent(openPlayer));
        eventBus.fire(new UpdateChestInventoryEvent(openPlayer, openChestEntity));
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(CloseChestEvent.class, this::close);
        eventBus.unsubscribe(DragAndDropChestEvent.class, this::onMoveChestItem);
        eventBus.unsubscribe(TransferChestToPlayerEvent.class, this::transferChestToPlayer);
        eventBus.unsubscribe(TransferChestToPlayerAutoEvent.class, this::transferChestToPlayerAuto);
        eventBus.unsubscribe(TransferPlayerToChestEvent.class, this::transferPlayerToChest);
        eventBus.unsubscribe(CommandEvent.class, this::onCommand);
    }
}
