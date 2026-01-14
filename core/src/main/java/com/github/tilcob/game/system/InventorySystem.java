package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.component.Id;
import com.github.tilcob.game.component.Inventory;
import com.github.tilcob.game.component.Item;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.*;
import com.github.tilcob.game.event.quest.CollectItemEvent;
import com.github.tilcob.game.item.ItemType;
import com.badlogic.gdx.utils.ObjectIntMap;

public class InventorySystem extends IteratingSystem implements Disposable {
    private final GameEventBus eventBus;
    private Entity player;

    public InventorySystem(GameEventBus eventBus) {
        super(Family.all(Inventory.class).get());
        this.eventBus = eventBus;

        eventBus.subscribe(DragAndDropEvent.class, this::onMoveEntity);
        eventBus.subscribe(SplitStackEvent.class, this::onSplitStack);
    }

    @Override
    protected void processEntity(Entity player, float deltaTime) {
        Inventory inventory = Inventory.MAPPER.get(player);
        this.player = player;
        if (inventory.getItemsToAdd().isEmpty()) return;

        boolean inventoryFull = false;
        boolean addedAny = false;
        var remaining = new com.badlogic.gdx.utils.Array<ItemType>();
        ObjectIntMap<ItemType> addedCounts = new ObjectIntMap<>();

        for (ItemType itemType : inventory.getItemsToAdd()) {
            if (inventoryFull) {
                remaining.add(itemType);
                continue;
            }

            int slotIndex = emptySlotIndex(inventory);
            if (slotIndex == -1) {
                inventoryFull = true;
                remaining.add(itemType);
                continue;
            }
            addItem(inventory, itemType, slotIndex);
            addedAny = true;
            addedCounts.getAndIncrement(itemType, 0, 1);
        }
        inventory.getItemsToAdd().clear();
        inventory.getItemsToAdd().addAll(remaining);
        if (inventoryFull) eventBus.fire(new InventoryFullEvent());
        if (addedAny) {
            eventBus.fire(new EntityAddItemEvent(player));
            for (ObjectIntMap.Entry<ItemType> entry : addedCounts) {
                eventBus.fire(new CollectItemEvent(entry.key, entry.value));
            }
        }
    }

    private void addItem(Inventory inventory, ItemType itemType, int slotIndex) {
        Entity stackEntity = findStackableItem(inventory, itemType);
        if (stackEntity != null) {
            Item stack = Item.MAPPER.get(stackEntity);
            stack.add(1);
            return;
        }
        inventory.add(spawnItem(itemType, slotIndex, inventory.nextId()));
    }

    private Entity spawnItem(ItemType itemType, int slotIndex, int id) {
        Entity entity = getEngine().createEntity();
        entity.add(new Item(itemType, slotIndex, 1));
        entity.add(new Id(id));
        getEngine().addEntity(entity);

        eventBus.fire(new ItemCreatedEvent(id, entity));
        return entity;
    }

    private void removeItem(Entity entity) {
        int id = Id.MAPPER.get(entity).getId();
        eventBus.fire(new ItemRemovedEvent(id));
        getEngine().removeEntity(entity);
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

    private Entity findStackableItem(Inventory inventory, ItemType type) {
        for (Entity e : inventory.getItems()) {
            Item item = Item.MAPPER.get(e);
            if (item.getItemType() == type && type.isStackable() && item.getCount() < type.getMaxStack()) {
                return e;
            }
        }
        return null;
    }

    private void onSplitStack(SplitStackEvent event) {
        Inventory inventory = Inventory.MAPPER.get(player);
        Entity itemEntity = findItemAtSlot(inventory, event.slotIndex());
        if (itemEntity == null) return;

        Item item = Item.MAPPER.get(itemEntity);
        if (item.getCount() < 2) return;

        int half = item.getCount() / 2;
        item.remove(half);

        int emptySlot = emptySlotIndex(inventory);
        if (emptySlot == -1) return;

        Entity newItemEntity = spawnItem(item.getItemType(), emptySlot, inventory.nextId());
        Item newItem = Item.MAPPER.get(newItemEntity);
        newItem.add(half - 1);
        newItem.setSlotIndex(emptySlot);

        inventory.add(newItemEntity);

        eventBus.fire(new UpdateInventoryEvent(player));
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(DragAndDropEvent.class, this::onMoveEntity);
        eventBus.unsubscribe(SplitStackEvent.class, this::onSplitStack);
    }
}
