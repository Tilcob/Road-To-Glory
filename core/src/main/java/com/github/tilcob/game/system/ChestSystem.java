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
import com.github.tilcob.game.entity.TransferPlayerToChestAutoEvent;
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
        eventBus.subscribe(TransferPlayerToChestAutoEvent.class, this::transferPlayerToChestAuto);
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
        inventoryService.transferPlayerToChest(event.fromIndex(), openChestEntity, openPlayer);
    }

    private void transferChestToPlayer(TransferChestToPlayerEvent event) {
        inventoryService.transferChestToPlayer(event.fromIndex(), event.toIndex(), openChestEntity, openPlayer, questManager);
    }

    private void transferPlayerToChestAuto(TransferPlayerToChestAutoEvent event) {
        inventoryService.transferPlayerToChest(event.fromIndex(), openChestEntity, openPlayer);
    }

    private void transferChestToPlayerAuto(TransferChestToPlayerAutoEvent event) {
        if (openChestEntity == null || openPlayer == null) return;
        Inventory inventory = Inventory.MAPPER.get(openPlayer);
        if (inventory == null) return;
        inventoryService.setPlayer(openPlayer);
        int emptySlot = inventoryService.emptySlotIndex(inventory);
        if (emptySlot == -1) return;
        inventoryService.transferChestToPlayer(event.fromIndex(), emptySlot, openChestEntity, openPlayer, questManager);
    }

    private void onMoveChestItem(DragAndDropChestEvent event) {
        if (openChestEntity == null || openPlayer == null) return;
        Chest chest = Chest.MAPPER.get(openChestEntity);
        if (!inventoryService.moveChestContents(chest, event.fromIdx(), event.toIdx())) return;
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
