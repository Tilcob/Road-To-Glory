package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.github.tilcob.game.component.Inventory;
import com.github.tilcob.game.event.*;
import com.github.tilcob.game.inventory.InventoryService;
import com.github.tilcob.game.item.ItemDefinitionRegistry;
import com.github.tilcob.game.quest.QuestManager;

public class InventorySystem extends IteratingSystem implements Disposable {
    private final GameEventBus eventBus;
    private final QuestManager questManager;
    private final InventoryService inventoryService;

    public InventorySystem(GameEventBus eventBus, QuestManager questManager, InventoryService inventoryService) {
        super(Family.all(Inventory.class).get());
        this.eventBus = eventBus;
        this.questManager = questManager;
        this.inventoryService = inventoryService;

        eventBus.subscribe(DragAndDropPlayerEvent.class, this::onMoveEntity);
        eventBus.subscribe(UnequipItemEvent.class, this::onUnequipItem);
        eventBus.subscribe(InventoryDropEvent.class, this::onDropItem);
        eventBus.subscribe(SplitStackEvent.class, this::onSplitStack);
    }

    @Override
    protected void processEntity(Entity player, float deltaTime) {
        Inventory inventory = Inventory.MAPPER.get(player);
        inventoryService.setPlayer(player);
        if (inventory.getItemsToAdd().isEmpty()) return;

        boolean inventoryFull = false;
        boolean addedAny = false;
        var remaining = new Array<String>();
        ObjectIntMap<String> addedCounts = new ObjectIntMap<>();

        for (String itemId : inventory.getItemsToAdd()) {
            String resolvedId = ItemDefinitionRegistry.resolveId(itemId);
            if (inventoryFull) {
                remaining.add(resolvedId);
                continue;
            }

            int slotIndex = inventoryService.emptySlotIndex(inventory);
            if (slotIndex == -1) {
                inventoryFull = true;
                remaining.add(resolvedId);
                continue;
            }
            inventoryService.addItem(inventory, resolvedId, slotIndex);
            addedAny = true;
            addedCounts.getAndIncrement(resolvedId, 0, 1);
        }
        inventory.getItemsToAdd().clear();
        inventory.getItemsToAdd().addAll(remaining);
        if (inventoryFull) eventBus.fire(new InventoryFullEvent());
        if (addedAny) {
            eventBus.fire(new EntityAddItemEvent(player));
            for (var entry : addedCounts) {
                questManager.signal(player, "collect", entry.key, entry.value);
            }
        }
    }

    private void onDropItem(InventoryDropEvent event) {
        inventoryService.dropItem(event.slotIndex());
    }

    private void onMoveEntity(DragAndDropPlayerEvent event) {
        inventoryService.moveEntity(event.toIdx(), event.fromIdx());
    }

    private void onUnequipItem(UnequipItemEvent event) {
        inventoryService.unequipItem(event.category(), event.toIndex());
    }

    private void onSplitStack(SplitStackEvent event) {
        inventoryService.splitStack(event.slotIndex());
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(DragAndDropPlayerEvent.class, this::onMoveEntity);
        eventBus.unsubscribe(UnequipItemEvent.class, this::onUnequipItem);
        eventBus.unsubscribe(InventoryDropEvent.class, this::onDropItem);
        eventBus.unsubscribe(SplitStackEvent.class, this::onSplitStack);
    }
}
