package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.component.Equipment;
import com.github.tilcob.game.component.Inventory;
import com.github.tilcob.game.component.Item;
import com.github.tilcob.game.event.EquipItemEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.UpdateEquipmentEvent;
import com.github.tilcob.game.event.UpdateInventoryEvent;
import com.github.tilcob.game.inventory.InventoryService;
import com.github.tilcob.game.item.ItemCategory;

public class EquipmentSystem extends IteratingSystem implements Disposable {
    private final GameEventBus eventBus;
    private final InventoryService service;
    private Entity player;

    public EquipmentSystem(GameEventBus eventBus, InventoryService service) {
        super(Family.all(Inventory.class, Equipment.class).get());
        this.eventBus = eventBus;
        this.service = service;

        eventBus.subscribe(EquipItemEvent.class, this::onEquipItem);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        this.player = entity;
        service.setPlayer(entity);
    }

    private void onEquipItem(EquipItemEvent event) {
        if (player == null) return;

        if (event.fromIndex() < 0) {
            Inventory inventory = Inventory.MAPPER.get(player);
            Equipment equipment = Equipment.MAPPER.get(player);
            if (equipment == null || inventory == null) return;
            handleUnequip(event.category(), inventory, equipment);
            return;
        }
        service.equipItem(event.category(), event.fromIndex());
    }

    private void handleUnequip(ItemCategory category, Inventory inventory, Equipment equipment) {
        Entity equippedItem = equipment.getEquipped(category);
        if (equippedItem == null) return;
        int emptySlot = service.emptySlotIndex(inventory);
        if (emptySlot == -1) return;

        equipment.unequip(category);
        Item item = Item.MAPPER.get(equippedItem);
        if (item != null) {
            item.setSlotIndex(emptySlot);
        }
        eventBus.fire(new UpdateInventoryEvent(player));
        eventBus.fire(new UpdateEquipmentEvent(player));
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(EquipItemEvent.class, this::onEquipItem);
    }
}
