package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.*;
import com.github.tilcob.game.item.ItemRegistry;
import com.github.tilcob.game.item.ItemType;

public class InventorySystem extends IteratingSystem implements Disposable {
    private final GameEventBus eventBus;
    private final ItemRegistry registry;
    private Entity player;
    private int id = 0;

    public InventorySystem(GameEventBus eventBus, ItemRegistry registry) {
        super(Family.all(Inventory.class).get());
        this.eventBus = eventBus;
        this.registry = registry;

        eventBus.subscribe(DragAndDropEvent.class, this::onMoveEntity);
    }

    @Override
    protected void processEntity(Entity player, float deltaTime) {
        Inventory inventory = Inventory.MAPPER.get(player);
        this.player = player;
        if (inventory.getItemsToAdd().isEmpty()) return;

        for (ItemType itemType : inventory.getItemsToAdd()) {
            int slotIndex = emptySlotIndex(inventory);
            if (slotIndex == -1) return; // Inventory is full

            inventory.add(spawnItem(itemType, slotIndex, ++id));

        }
        eventBus.fire(new EntityAddItemEvent(player));

        inventory.getItemsToAdd().clear();
    }

    private Entity spawnItem(ItemType itemType, int slotIndex, int id) {
        Entity entity = getEngine().createEntity();
        entity.add(new Item(itemType, slotIndex, 1));
        entity.add(new Id(id));
        getEngine().addEntity(entity);

        eventBus.fire(new ItemCreatedEvent(id, entity));
        return entity;
    }

    // remove items: gameEventBus.fire(new ItemRemovedEvent(id));
    //engine.removeEntity(entity);

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

    private void onMoveEntity(DragAndDropEvent event) {
        Inventory inventory = Inventory.MAPPER.get(player);

        Entity fromEntity = findItemAtSlot(inventory, event.fromIdx());
        if (fromEntity == null) return;

        Entity toEntity = findItemAtSlot(inventory, event.toIdx());

        Item from = Item.MAPPER.get(fromEntity);

        if (tryStack(fromEntity, from, toEntity, inventory)) {
            eventBus.fire(new UpdateInventoryEvent(player));
            return;
        }

        swapOrMove(from, toEntity, event.fromIdx(), event.toIdx());
        eventBus.fire(new UpdateInventoryEvent(player));
    }

    private void swapOrMove(Item from, Entity toEntity, int fromSlot, int toSlot) {
        if (toEntity != null) {
            Item to = Item.MAPPER.get(toEntity);
            to.setSlotIndex(fromSlot);
        }

        from.setSlotIndex(toSlot);
    }

    private boolean tryStack(Entity fromEntity, Item from, Entity toEntity, Inventory inventory) {
        if (toEntity == null) return false;

        Item to = Item.MAPPER.get(toEntity);

        if (from.getItemType() != to.getItemType() || !from.getItemType().isStackable()) return false;

        int space = from.getItemType().getMaxStack() - to.getCount();
        if (space <= 0) return false;

        int transfer = Math.min(space, from.getCount());
        to.add(transfer);
        from.remove(transfer);

        if (from.getCount() == 0) {
            inventory.remove(fromEntity);
            getEngine().removeEntity(fromEntity);
        }

        return true;
    }

    private Entity findItemAtSlot(Inventory inventory, int slot) {
        for (Entity e : inventory.getItems()) {
            if (Item.MAPPER.get(e).getSlotIndex() == slot) {
                return e;
            }
        }
        return null;
    }


    @Override
    public void dispose() {
        eventBus.unsubscribe(DragAndDropEvent.class, this::onMoveEntity);
    }
}
