package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.component.Equipment;
import com.github.tilcob.game.component.Inventory;
import com.github.tilcob.game.component.Item;
import com.github.tilcob.game.component.StatComponent;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.EquipItemEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.UpdateEquipmentEvent;
import com.github.tilcob.game.event.UpdateInventoryEvent;
import com.github.tilcob.game.inventory.InventoryService;
import com.github.tilcob.game.item.ItemCategory;
import com.github.tilcob.game.item.ItemDefinition;
import com.github.tilcob.game.item.ItemDefinitionRegistry;
import com.github.tilcob.game.stat.StatType;

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
    }

    private void onEquipItem(EquipItemEvent event) {
        if (player == null) return;
        Inventory inventory = Inventory.MAPPER.get(player);
        Equipment equipment = Equipment.MAPPER.get(player);
        if (equipment == null) return;

        if (event.fromIndex() < 0) {
            if (inventory == null) return;
            handleUnequip(event.category(), inventory, equipment);
            return;
        }
        if (inventory == null) return;

        Entity itemEntity = service.findItemAtSlot(inventory, event.fromIndex());
        if (itemEntity == null) return;
        Item item = Item.MAPPER.get(itemEntity);
        if (item == null) return;
        ItemDefinition definition = ItemDefinitionRegistry.get(item.getItemId());
        if (definition.category() != event.category()) return;
        if (!meetsRequirements(player, definition)) return;

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

    private boolean meetsRequirements(Entity entity, ItemDefinition definition) {
        if (definition.requirements().isEmpty()) return true;
        StatComponent stats = StatComponent.MAPPER.get(entity);
        if (stats == null) return false;
        for (var entry : definition.requirements().entrySet()) {
            StatType statType = entry.getKey();
            float required = entry.getValue();
            if (stats.getFinalStat(statType) < required) return false;
        }
        return true;
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
