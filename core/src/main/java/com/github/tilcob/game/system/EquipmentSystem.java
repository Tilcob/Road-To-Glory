package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.component.Equipment;
import com.github.tilcob.game.component.Inventory;
import com.github.tilcob.game.component.Item;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.EquipItemEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.UpdateEquipmentEvent;
import com.github.tilcob.game.event.UpdateInventoryEvent;
import com.github.tilcob.game.item.ItemCategory;
import com.github.tilcob.game.item.ItemDefinition;
import com.github.tilcob.game.item.ItemDefinitionRegistry;

public class EquipmentSystem extends IteratingSystem implements Disposable {
    private final GameEventBus eventBus;
    private Entity player;

    public EquipmentSystem(GameEventBus eventBus) {
        super(Family.all(Inventory.class, Equipment.class).get());
        this.eventBus = eventBus;
        eventBus.subscribe(EquipItemEvent.class, this::onEquipItem);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        this.player = entity;
    }

    private void onEquipItem(EquipItemEvent event) {
        if (player == null) return;
        Inventory inventory = Inventory.MAPPER.get(player);
        Equipment equipment = Equipment.MAPPER.get(player);
        if (inventory == null || equipment == null) return;

        if (event.fromIndex() < 0) {
            handleUnequip(event.category(), inventory, equipment);
            return;
        }

        Entity itemEntity = findItemAtSlot(inventory, event.fromIndex());
        if (itemEntity == null) return;
        Item item = Item.MAPPER.get(itemEntity);
        if (item == null) return;
        ItemDefinition definition = ItemDefinitionRegistry.get(item.getItemId());
        if (definition.category() != event.category()) return;

        Entity previous = equipment.equip(event.category(), itemEntity);
        item.setSlotIndex(-1);
        if (previous != null) {
            Item previousItem = Item.MAPPER.get(previous);
            if (previousItem != null) {
                previousItem.setSlotIndex(event.fromIndex());
            }
        }
        eventBus.fire(new UpdateInventoryEvent(player));
        eventBus.fire(new UpdateEquipmentEvent(player));
    }

    private void handleUnequip(ItemCategory category, Inventory inventory, Equipment equipment) {
        Entity equippedItem = equipment.getEquipped(category);
        if (equippedItem == null) return;
        int emptySlot = emptySlotIndex(inventory);
        if (emptySlot == -1) return;

        equipment.unequip(category);
        Item item = Item.MAPPER.get(equippedItem);
        if (item != null) {
            item.setSlotIndex(emptySlot);
        }
        eventBus.fire(new UpdateInventoryEvent(player));
        eventBus.fire(new UpdateEquipmentEvent(player));
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

    private Entity findItemAtSlot(Inventory inventory, int slot) {
        for (Entity entity : inventory.getItems()) {
            if (Item.MAPPER.get(entity).getSlotIndex() == slot) {
                return entity;
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(EquipItemEvent.class, this::onEquipItem);
    }
}
